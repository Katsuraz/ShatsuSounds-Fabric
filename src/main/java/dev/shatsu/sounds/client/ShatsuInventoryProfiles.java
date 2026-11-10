package dev.shatsu.sounds.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Inventory item-to-material routing. Reads the explicit profile table when the
 * v0.5.8 resource bundle is present. Uses conservative vanilla-family rules
 * otherwise so missing data doesn't crash an older source build.
 */
public final class ShatsuInventoryProfiles {
    private static final Map<String, String> EXPLICIT = load();

    private ShatsuInventoryProfiles() {}

    private static Map<String, String> load() {
        try (var stream = ShatsuInventoryProfiles.class.getResourceAsStream("/shatsu_inventory_profiles.json")) {
            if (stream == null) return Map.of();
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                Map<String, String> result = new Gson().fromJson(reader,
                    new TypeToken<HashMap<String, String>>() {}.getType());
                return result == null ? Map.of() : Collections.unmodifiableMap(result);
            }
        } catch (Exception e) {
            System.err.println("[Shatsu Sounds] Inventory profiles unavailable: " + e);
            return Map.of();
        }
    }

    /** Apply specific material corrections even if an older profile table is bundled. */
    public static String forId(String id) {
        if (id == null || id.isBlank()) return "utility";
        String key = id.contains(":") ? id.toLowerCase(Locale.ROOT) : "minecraft:" + id.toLowerCase(Locale.ROOT);
        String name = key.substring(key.indexOf(':') + 1);
        if (name.equals("enchanted_golden_apple")) return "enchanted_apple";
        if (name.equals("trident")) return "trident_item";
        if (name.equals("stonecutter")) return "stonecutter_handling";
        if (name.equals("bone_meal")) return "soil";
        if (name.equals("bone")) return "bone";
        if (name.equals("nether_wart")) return "plant_nether";
        if (name.equals("ghast_tear")) return "crystal";
        if (name.equals("sponge")) return "cloth";
        if (name.equals("wet_sponge")) return "slime";
        if (name.endsWith("_concrete_powder")) return "soil";
        if (name.endsWith("_concrete")) return "mineral";
        if (name.contains("dead_") && name.contains("_coral")) return "mineral";
        if (name.contains("_coral")) return "crystal";
        if (name.equals("kelp") || name.equals("seagrass") || name.equals("sea_pickle")) return "plant_kelp";
        if (name.endsWith("_sapling")) return "plant_sapling";
        if (name.endsWith("_leaves")) return "plant_leaves";
        if (name.equals("bamboo")) return "plant_bamboo";
        if (name.equals("moss_block") || name.equals("moss_carpet")) return "plant_moss";
        if (name.equals("bamboo_mosaic") || name.equals("bamboo_block")) return "wooden";
        String explicit = EXPLICIT.get(key);
        if (explicit != null && !explicit.isBlank()) return explicit;
        if (name.startsWith("music_disc_")) return "music_disc";
        if (name.endsWith("_spawn_egg")) return "spawn_egg";
        if (name.endsWith("_bucket")) return "fish_bucket";
        if (name.endsWith("_sword") || name.endsWith("_axe") || name.endsWith("_pickaxe")) return "weapon";
        if (name.endsWith("_helmet") || name.endsWith("_chestplate")
                || name.endsWith("_leggings") || name.endsWith("_boots")) return "armor_metal";
        if (name.endsWith("_planks") || name.endsWith("_log") || name.endsWith("_wood")) return "wooden";
        if (name.endsWith("_ore") || name.endsWith("_stone")) return "mineral";
        return "utility";
    }

    public static String of(ItemStack item) {
        if (item == null || item.isEmpty()) return "utility";
        return forId(BuiltInRegistries.ITEM.getKey(item.getItem()).toString());
    }
}
