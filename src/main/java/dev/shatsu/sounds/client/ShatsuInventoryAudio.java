package dev.shatsu.sounds.client;

import dev.shatsu.sounds.ShatsuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Inventory audio is routed through one source. A signature-specific debounce
 * prevents a duplicate from a creative-screen super call, without dropping
 * distinct fast inventory actions as the old global 38/16ms guard did.
 *
 * This class does not suppress vanilla/third-party sounds. Optional Sounds-mod
 * item definitions should be excluded in the bundled audio to avoid overlap.
 */
public final class ShatsuInventoryAudio {
    private static long lastSoundNanos = 0L;
    private static String lastSignature = "";
    private static int hookHits = 0, interactions = 0, missingEvents = 0;
    private static String lastEvent = "No inventory clicks yet";

    private ShatsuInventoryAudio() {}

    public static int hookHits() { return hookHits; }
    public static int interactions() { return interactions; }
    public static int missingEvents() { return missingEvents; }
    public static String lastEvent() { return lastEvent; }

    public static void clicked(Slot slot, ItemStack carried, ContainerInput type, int mouseButton) {
        hookHits++;
        if (!ShatsuConfig.current().inventoryAudioEnabled || type == null) return;
        ItemStack cursor = carried == null ? ItemStack.EMPTY : carried;
        ItemStack inSlot = slot == null ? ItemStack.EMPTY : slot.getItem();
        if (cursor.isEmpty() && inSlot.isEmpty()) return;

        String action = switch (type) {
            case PICKUP -> cursor.isEmpty()
                ? (mouseButton == 1 ? "split" : "pickup")
                : (inSlot.isEmpty() ? "place" : "swap");
            case QUICK_MOVE, QUICK_CRAFT, PICKUP_ALL -> "transfer";
            case SWAP -> "swap";
            case CLONE -> "pickup";
            case THROW -> "drop";
            default -> null;
        };
        if (action == null) return;
        if (type == ContainerInput.QUICK_CRAFT
            && AbstractContainerMenu.getQuickcraftHeader(mouseButton) != 2) return;

        ItemStack target = cursor.isEmpty() ? inSlot : cursor;
        String profile = ShatsuInventoryProfiles.of(target);
        String eventPath = "inventory.move." + action + "." + profile;
        Identifier id = Identifier.fromNamespaceAndPath("shatsufresh", eventPath);
        Minecraft client = Minecraft.getInstance();
        var manager = client.getSoundManager();
        var definition = manager.getSoundEvent(id);
        if (definition == null || definition.getWeight() <= 0) {
            missingEvents++;
            lastEvent = "Missing " + id;
            if (missingEvents <= 5)
                System.err.println("[Shatsu Sounds] Missing inventory sound " + id
                    + " for " + BuiltInRegistries.ITEM.getKey(target.getItem()));
            return;
        }

        // Suppress only the same hook invoked twice for exactly the same click.
        // Do not globally suppress different items, slots or actions.
        // A creative override may call the base handler after changing the cursor.
        // Identity of the physical click stays the same, even if its selected audio changes.
        String signature = System.identityHashCode(client.gui.screen()) + "/"
            + System.identityHashCode(slot) + "/" + type + "/" + mouseButton;
        long now = System.nanoTime();
        if (signature.equals(lastSignature) && now - lastSoundNanos < 18_000_000L)
            return;
        lastSignature = signature;
        lastSoundNanos = now;

        float level = switch (action) {
            case "pickup" -> .83f;
            case "place" -> .87f;
            case "swap" -> .91f;
            case "split" -> .80f;
            case "drop" -> .84f;
            default -> .78f;
        };
        manager.play(SimpleSoundInstance.forUI(
            SoundEvent.createVariableRangeEvent(id), 1.0f, level));
        interactions++;
        lastEvent = id + " (" + BuiltInRegistries.ITEM.getKey(target.getItem()).getPath() + ")";
        if (interactions <= 5)
            System.out.println("[Shatsu Sounds] Inventory: " + lastEvent);
    }
}
