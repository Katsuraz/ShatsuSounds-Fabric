package dev.shatsu.sounds.client;

import dev.shatsu.sounds.ShatsuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

/**
 * Item-aware eating and drinking sound selection. The vanilla mouth sound is
 * replaced exactly once; special-food sparkle is a separate quiet accent.
 * Never affects mob voices or Actions & Beyond's ans: sound events.
 */
public final class ShatsuFoodAudio {
    private ShatsuFoodAudio() {}
    private static int localMatches, remoteMatches, missingEvents;
    private static String lastProfile = "None yet";
    private static long lastAccentNanos;
    private static String lastAccentItem = "";

    public static int localMatches() { return localMatches; }
    public static int remoteMatches() { return remoteMatches; }
    public static int missingEvents() { return missingEvents; }
    public static String lastProfile() { return lastProfile; }

    public static boolean hasProfile(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String item = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return foodProfile(item) != null || drinkProfile(item) != null;
    }

    public static SoundEvent select(ItemStack stack, SoundEvent original) {
        return select(stack, original, "local");
    }

    public static SoundEvent select(ItemStack stack, SoundEvent original, String source) {
        if (stack == null || stack.isEmpty()) return original;
        String item = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        String food = foodProfile(item), drink = drinkProfile(item);
        ShatsuConfig cfg = ShatsuConfig.current();
        if (food == null && drink == null) return original;
        if (food != null && !cfg.foodAudioEnabled) return original;
        if (drink != null && !cfg.drinkAudioEnabled) return original;

        Identifier id = food != null
            ? Identifier.fromNamespaceAndPath("shatsu_food_audio", "food." + food)
            : Identifier.fromNamespaceAndPath("shatsu_liquids", "drink." + drink);
        var manager = Minecraft.getInstance().getSoundManager();
        var set = manager.getSoundEvent(id);
        if (set == null || set.getWeight() <= 0) {
            missingEvents++;
            if (missingEvents <= 4)
                System.err.println("[Shatsu Sounds] Missing edible/drink preview: " + id);
            return original;
        }
        if ("remote".equals(source)) remoteMatches++; else localMatches++;
        lastProfile = item + " > " + id + " (" + source + ")";
        if (localMatches + remoteMatches <= 5)
            System.out.println("[Shatsu Sounds] Replaced food/drink sound " + lastProfile);
        // Accent only for local player. The ordinary crunch stays fully audible.
        if (food != null && "local".equals(source) && cfg.specialMagicEnabled)
            playMagicAccent(item, manager);
        return SoundEvent.createVariableRangeEvent(id);
    }

    private static void playMagicAccent(String item, net.minecraft.client.sounds.SoundManager manager) {
        String style = switch (item) {
            case "golden_apple" -> "golden_apple";
            case "enchanted_golden_apple" -> "enchanted_apple";
            case "golden_carrot" -> "golden_carrot";
            case "chorus_fruit" -> "chorus_fruit";
            default -> null;
        };
        if (style == null) return;
        long now = System.nanoTime();
        // Anti-spam: the eating loop plays several bites for a single snack.
        if (now - lastAccentNanos < 1_300_000_000L && item.equals(lastAccentItem)) return;
        Identifier id = Identifier.fromNamespaceAndPath("shatsufresh", "preview.asmr.magic." + style);
        var event = manager.getSoundEvent(id);
        if (event == null || event.getWeight() <= 0) return;
        lastAccentItem = item;
        lastAccentNanos = now;
        float volume = "enchanted_golden_apple".equals(item) ? .20f : .13f;
        manager.play(SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(id), 1.0f, volume));
    }

    private static String drinkProfile(String item) {
        return switch (item) {
            case "honey_bottle" -> "honey";
            case "milk_bucket" -> "milk";
            case "potion" -> "potion";
            default -> null;
        };
    }

    private static String foodProfile(String item) {
        return switch (item) {
            case "apple", "golden_apple", "enchanted_golden_apple" -> "apple";
            case "carrot", "golden_carrot" -> "carrot";
            case "beetroot" -> "beetroot";
            case "melon_slice" -> "melon";
            case "sweet_berries", "glow_berries" -> "berries";
            case "bread" -> "bread";
            case "cookie" -> "cookie";
            case "pumpkin_pie" -> "pie";
            case "potato", "baked_potato", "poisonous_potato" -> "potato";
            case "beef", "porkchop", "chicken", "mutton", "rabbit" -> "raw_meat";
            case "cooked_beef", "cooked_porkchop", "cooked_chicken", "cooked_mutton",
                 "cooked_rabbit" -> "cooked_meat";
            case "cod", "salmon", "tropical_fish", "pufferfish" -> "raw_fish";
            case "cooked_cod", "cooked_salmon" -> "cooked_fish";
            case "dried_kelp" -> "kelp";
            case "chorus_fruit" -> "chorus";
            case "mushroom_stew", "beetroot_soup", "rabbit_stew", "suspicious_stew" -> "soup";
            default -> null;
        };
    }
}
