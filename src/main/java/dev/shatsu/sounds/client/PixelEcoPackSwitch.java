package dev.shatsu.sounds.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackRepository;

/**
 * Pixel Eco 0.1.5: global vanilla-restoring audio switch with cached selection.
 *
 * All replacements are in one optional built-in pack, while audio bytes stay
 * in the mod's resource assets. Turning this pack off removes only Pixel Eco's
 * definitions, not vanilla effects or other resource packs.
 *
 * This cache is polled ONCE per client tick, not for every sound mixer event.
 * It also sees changes made through Minecraft's Resource Packs screen.
 */
public final class PixelEcoPackSwitch {
    private static final String NAME = "pixeleco_original";
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("shatsusounds", "pixeleco"));
    private static boolean installed;
    private static volatile boolean cachedEnabled;
    private static volatile boolean hasSnapshot;
    private static boolean loggedPack;

    private PixelEcoPackSwitch() {}

    public static void register() {
        var container = FabricLoader.getInstance().getModContainer("shatsusounds");
        if (container.isEmpty()) {
            System.err.println("[Pixel Eco] Missing source mod container");
            return;
        }
        System.out.println("[Pixel Eco] Bundled SFX pack source: " +
            container.get().findPath("resourcepacks/" + NAME + "/pack.mcmeta"));
        System.out.println("[Pixel Eco] Mod container roots: " + container.get().getRootPaths());
        installed = ResourceLoader.registerBuiltinPack(
            Identifier.fromNamespaceAndPath("shatsusounds", NAME),
            container.get(), PackActivationType.DEFAULT_ENABLED);
        System.out.println("[Pixel Eco] Built-in retro SFX pack registered: " + installed);
        KeyMapping key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.pixeleco.toggle", InputConstants.Type.KEYBOARD,
            InputConstants.KEY_K, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            poll(client); // Once per tick, even while a GUI is open.
            if (client.gui.screen() != null) return;
            while (key.consumeClick()) {
                boolean desired = !cachedEnabled;
                boolean ok = setEnabled(client, desired);
                System.out.println("[Pixel Eco] K toggle " + (ok ?
                    (desired ? "ON" : "OFF — vanilla sound events restored") :
                    "FAILED — check resourcepacks/pixeleco_original/pack.mcmeta"));
            }
        });
    }

    private static String find(PackRepository repository) {
        if (repository == null) return null;
        for (String id : repository.getAvailableIds()) {
            String text = id.toLowerCase(Locale.ROOT);
            if (text.contains("shatsusounds") && text.contains(NAME)) return id;
        }
        return null;
    }

    private static void poll(Minecraft client) {
        if (!installed || client == null) {
            cachedEnabled = false;
            hasSnapshot = true;
            return;
        }
        PackRepository repo = client.getResourcePackRepository();
        if (repo == null) return;
        String id = find(repo);
        cachedEnabled = id != null && repo.getSelectedIds().contains(id);
        hasSnapshot = true;
        if (!loggedPack) {
            System.out.println("[Pixel Eco] Resolved built-in SFX pack id: " +
                (id == null ? "MISSING" : id) +
                "; selected=" + cachedEnabled +
                "; selected packs=" + repo.getSelectedIds());
            loggedPack = true;
        }
    }

    /** Safe to call from the audio thread: no resource repository iteration. */
    public static boolean isEnabled(Minecraft client) {
        if (!installed || client == null) return false;
        if (!hasSnapshot) poll(client);
        return cachedEnabled;
    }

    public static boolean setEnabled(Minecraft client, boolean enabled) {
        if (!installed || client == null) return false;
        PackRepository repo = client.getResourcePackRepository();
        String id = find(repo);
        if (id == null) {
            System.err.println("[Pixel Eco] Cannot toggle: built-in SFX pack not available");
            return false;
        }

        List<String> before = new ArrayList<>(repo.getSelectedIds());
        List<String> after = new ArrayList<>(before);
        after.removeIf(name -> name.equals(id));
        if (enabled) after.add(id);
        if (before.equals(after)) {
            cachedEnabled = enabled;
            hasSnapshot = true;
            return true;
        }
        try {
            repo.setSelected(after);
            client.options.updateResourcePacks(repo);
            cachedEnabled = enabled;
            hasSnapshot = true;
            System.out.println("[Pixel Eco] SFX selection changed; resource reload requested.");
            return true;
        } catch (RuntimeException ex) {
            System.err.println("[Pixel Eco] Pack toggle failed: " + ex);
            repo.setSelected(before);
            poll(client);
            return false;
        }
    }
}
