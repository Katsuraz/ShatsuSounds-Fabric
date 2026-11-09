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
            case PICKUP -> carried.isEmpty() ? "pickup" :
                    (target.isEmpty() ? "place" : "swap");
            case QUICK_MOVE, QUICK_CRAFT -> "transfer";
            case SWAP -> "swap";
            case PICKUP_ALL -> "transfer";
            case CLONE -> "pickup";
            case THROW -> "place";
            default -> null;
        };
        if (action == null) return;
        long now = System.nanoTime();
        if (now - lastSoundNanos < 38_000_000L) return;
        lastSoundNanos = now;
        ItemStack item = carried.isEmpty() ? target : carried;
        String material = materialOf(item);
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
            default -> 1.0f;
        };
        float gain = switch (action) {
            case "pickup" -> .83f;
            case "place" -> .87f;
            case "swap" -> .91f;
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

    private static String materialOf(ItemStack stack) {
        String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (name.contains("golden_apple") || name.contains("carrot")
                || name.contains("bread") || name.contains("apple") || name.contains("cookie")
                || name.contains("stew") || name.contains("soup") || name.contains("potato")
                || name.contains("melon") || name.contains("beef") || name.contains("pork")
                || name.contains("mutton") || name.contains("chicken") || name.contains("fish")
                || name.contains("salmon")) return "food";
        if (name.contains("sword") || name.contains("axe") || name.contains("bow")
                || name.contains("trident") || name.contains("spear") || name.contains("mace")) return "weapon";
        if (name.contains("potion") || name.contains("bottle") || name.contains("glass")) return "glass";
        if (name.contains("book") || name.contains("paper") || name.contains("map")) return "paper";
        if (name.contains("diamond") || name.contains("emerald") || name.contains("amethyst")) return "crystal";
        if (name.contains("iron") || name.contains("copper") || name.contains("gold")
                || name.contains("netherite") || name.contains("bucket")) return "metal";
        if (name.contains("wood") || name.contains("log") || name.contains("plank")
                || name.contains("stick") || name.contains("chest")) return "wooden";
        if (name.contains("leather") || name.contains("saddle")) return "leather";
        if (name.contains("wool") || name.contains("banner") || name.contains("carpet")) return "cloth";
        if (name.contains("bone") || name.contains("skull")) return "bone";
        if (name.contains("slime") || name.contains("honey")) return "slime";
        if (name.contains("dirt") || name.contains("mud") || name.contains("sand")) return "soil";
        if (name.contains("leaf") || name.contains("seed") || name.contains("sapling")
                || name.contains("flower")) return "plant";
        if (name.contains("ender") || name.contains("nether") || name.contains("blaze")) return "nether";
        if (name.contains("apple") || name.contains("bread") || name.contains("carrot")
                || name.contains("potato") || name.contains("meat") || name.contains("stew")
                || name.contains("beef") || name.contains("chicken") || name.contains("pork")) return "food";
        if (name.contains("stone") || name.contains("ore") || name.contains("cobble")) return "mineral";
        if (name.contains("snow") || name.contains("ice")) return "snow";
        if (name.contains("water")) return "water";
        return "utility";
    }
}
