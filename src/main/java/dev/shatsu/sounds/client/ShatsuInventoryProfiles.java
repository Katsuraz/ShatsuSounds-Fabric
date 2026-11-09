package dev.shatsu.sounds.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Registry-driven item profiles: exact vanilla IDs, safe modded-item fallback.
 * No false substring matches such as waxed/axe or chestplate/chest.
 */
public final class ShatsuInventoryProfiles {
    private static final Map<String, String> VANILLA = readProfiles();
    private ShatsuInventoryProfiles() {}
    private static Map<String, String> readProfiles() {
        Map<String, String> result = new HashMap<>();
        try (InputStream input = ShatsuInventoryProfiles.class.getResourceAsStream("/shatsu_inventory_profiles.json")) {
            if (input == null) {
                System.err.println("[Shatsu Sounds] inventory profiles missing: using utility fallback");
                return result;
            }
            try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                JsonObject profiles = JsonParser.parseReader(reader).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : profiles.entrySet())
                    if (entry.getValue().isJsonPrimitive()) result.put(entry.getKey(), entry.getValue().getAsString());
            }
        } catch (Exception ex) {
            System.err.println("[Shatsu Sounds] Failed reading item sound profiles: " + ex);
        }
        return result;
    }
    public static String of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "utility";
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String known = VANILLA.get(id);
        if (known != null) return known;
        String path = id.substring(id.indexOf(':') + 1);
        if (path.endsWith("_spawn_egg")) return "spawn_egg";
        if (path.startsWith("music_disc_")) return "music_disc";
        if (path.endsWith("_bucket")) return "fish_bucket";
        if (path.endsWith("_sword") || path.endsWith("_axe") || path.endsWith("_pickaxe") ||
                path.endsWith("_spear") || path.endsWith("_bow") || path.endsWith("_trident")) return "weapon";
        if (path.endsWith("_chestplate") || path.endsWith("_leggings") ||
                path.endsWith("_helmet") || path.endsWith("_boots")) return "armor_metal";
        if (path.endsWith("_planks") || path.endsWith("_log") || path.endsWith("_wood")) return "wooden";
        if (path.endsWith("_ore") || path.endsWith("_stone")) return "mineral";
        return "utility";
    }
}
