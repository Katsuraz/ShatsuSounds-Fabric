package dev.shatsu.sounds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

/** Selects the bundled Shatsu food profiles by consumed item.
 * This never replaces drinking, mob voices, or events whose OGGs are missing.
 */
public final class ShatsuFoodAudio {
    private ShatsuFoodAudio() {}

    public static boolean hasProfile(ItemStack stack) {
        return stack != null && !stack.isEmpty()
            && profile(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()) != null;
    }

    public static SoundEvent select(ItemStack stack, SoundEvent original) {
        if (stack == null || stack.isEmpty()) return original;
        String item = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        String profile = profile(item);
        if (profile == null) return original;
        Identifier event = Identifier.fromNamespaceAndPath("shatsu_food_audio", "food." + profile);
        // Keep vanilla sound instead of playing a missing or empty event.
        var soundSet = Minecraft.getInstance().getSoundManager().getSoundEvent(event);
        if (soundSet == null || soundSet.getWeight() <= 0) return original;
        return SoundEvent.createVariableRangeEvent(event);
    }

    private static String profile(String item) {
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
