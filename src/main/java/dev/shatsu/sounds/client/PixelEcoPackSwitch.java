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
 * Pixel Eco master audio ON/OFF. The original Pixel Eco sound-event overrides
 * are stored in a built-in pack; removing it restores vanilla definitions.
 * Never uses master gain=0 to imitate OFF (which would silence vanilla).
 *
 * All other installed resource packs retain their selection and order.
 */
public final class PixelEcoPackSwitch {
    private static final String NAME = "pixeleco_original";
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("shatsusounds", "pixeleco"));
    private static boolean installed = false;
    private PixelEcoPackSwitch() {}

    public static void register() {
        var container = FabricLoader.getInstance().getModContainer("shatsusounds");
        if (container.isEmpty()) {
            System.err.println("[Pixel Eco] Missing source mod container");
            return;
        }
        installed = ResourceLoader.registerBuiltinPack(
            Identifier.fromNamespaceAndPath("shatsusounds", NAME),
            container.get(), PackActivationType.DEFAULT_ENABLED);
        System.out.println("[Pixel Eco] Default-enabled SFX pack registration: " + installed);

        KeyMapping key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.pixeleco.toggle", InputConstants.Type.KEYBOARD,
            InputConstants.KEY_K, CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.gui.screen() != null) return;
            while (key.consumeClick()) {
                boolean desired = !isEnabled(client);
                boolean ok = setEnabled(client, desired);
                System.out.println("[Pixel Eco] " +
                    (ok ? "SFX switched " + (desired ? "ON" : "OFF (vanilla)")
                        : "SFX pack could not be switched"));
            }
        });
    }

    private static String find(PackRepository packs) {
        for (String id : packs.getAvailableIds()) {
            String lower = id.toLowerCase(Locale.ROOT);
            if (lower.contains("shatsusounds") && lower.contains(NAME)) {
                return id;
            }
        }
        return null;
    }

    public static boolean isEnabled(Minecraft client) {
        if (!installed || client == null) return false;
        PackRepository packs = client.getResourcePackRepository();
        String id = find(packs);
        return id != null && packs.getSelectedIds().contains(id);
    }

    public static boolean setEnabled(Minecraft client, boolean enabled) {
        if (!installed || client == null) return false;
        PackRepository packs = client.getResourcePackRepository();
        String id = find(packs);
        if (id == null) return false;
        List<String> selected = new ArrayList<>(packs.getSelectedIds());
        if (enabled) {
            if (!selected.contains(id)) selected.add(id);
        } else {
            selected.removeIf(name -> name.equals(id));
        }
        if (new ArrayList<>(packs.getSelectedIds()).equals(selected)) return true;
        packs.setSelected(selected);
        client.options.updateResourcePacks(packs);
        return true;
    }
}
