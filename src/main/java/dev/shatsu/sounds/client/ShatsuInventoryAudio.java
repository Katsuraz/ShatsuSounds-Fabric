package dev.shatsu.sounds.client;

import dev.shatsu.sounds.ShatsuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Material-aware, action-aware feedback for actual inventory slot interactions.
 * This intentionally does not hook screen opening (user-approved sound preserved).
 * Stops when disabled, when clicking empty space, or when the sound resource is missing.
 */
public final class ShatsuInventoryAudio {
    private static long lastSoundNanos;
    private static int interactions;
    private static int hookHits;
    private static int missingEvents;
    private static String lastEvent = "No inventory item clicks yet";
    private ShatsuInventoryAudio() {}

    public static int interactions() { return interactions; }
    public static int hookHits() { return hookHits; }
    public static int missingEvents() { return missingEvents; }
    public static String lastEvent() { return lastEvent; }

    public static void clicked(Slot slot, ItemStack carried, ContainerInput type, int mouseButton) {
        ShatsuConfig cfg = ShatsuConfig.current();
        hookHits++;
        if (!cfg.inventoryAudioEnabled || type == null) return;
        ItemStack target = slot == null ? ItemStack.EMPTY : slot.getItem();
        if (carried == null) carried = ItemStack.EMPTY;
        if (target.isEmpty() && carried.isEmpty()) return;
        // QUICK_CRAFT is repeatedly called for each slot while dragging;
        // one gentle final interaction is preferable to a sound flood.
        if (type == ContainerInput.QUICK_CRAFT &&
                AbstractContainerMenu.getQuickcraftHeader(mouseButton) != 2) return;
        String action = switch (type) {
            case PICKUP -> carried.isEmpty() ?
                    (mouseButton == 1 ? "split" : "pickup") :
                    (target.isEmpty() ? "place" : "swap");
            case QUICK_MOVE, QUICK_CRAFT, PICKUP_ALL -> "transfer";
            case SWAP -> "swap";
            case CLONE -> "pickup";
            case THROW -> "drop";
            default -> null;
        };
        if (action == null) return;
        long now = System.nanoTime();
        if (now - lastSoundNanos < 38_000_000L) return;
        lastSoundNanos = now;
        ItemStack item = carried.isEmpty() ? target : carried;
        String material = ShatsuInventoryProfiles.of(item);
        String eventName = "inventory.move." + action + "." + material;
        var manager = Minecraft.getInstance().getSoundManager();
        Identifier key = Identifier.fromNamespaceAndPath("shatsufresh", eventName);
        var loaded = manager.getSoundEvent(key);
        if (loaded == null || loaded.getWeight() <= 0) {
            missingEvents++;
            lastEvent = "Missing sound: " + key;
            if (missingEvents <= 3)
                System.err.println("[Shatsu Sounds] Inventory audio missing: " + key +
                        " (check resource packs / sound registration)");
            return;
        }
        float pitch = switch (action) {
            case "pickup" -> 1.07f;
            case "place" -> .94f;
            case "swap" -> 1.02f;
            case "split" -> 1.10f;
            case "drop" -> .91f;
            default -> 1.0f;
        };
        float gain = switch (action) {
            case "pickup" -> .83f;
            case "place" -> .87f;
            case "swap" -> .91f;
            case "split" -> .80f;
            case "drop" -> .84f;
            default -> .78f;
        };
        manager.play(SimpleSoundInstance.forUI(
            SoundEvent.createVariableRangeEvent(key), pitch, gain));
        interactions++;
        lastEvent = "Played " + eventName + " (item " +
                BuiltInRegistries.ITEM.getKey(item.getItem()).getPath() + ")";
        if (interactions <= 3)
            System.out.println("[Shatsu Sounds] Inventory " + lastEvent);
    }

}
