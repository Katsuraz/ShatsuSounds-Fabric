package dev.shatsu.sounds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public final class ShatsuConfig {
    public static final String[] CATEGORIES = {
        "block_hit", "block_break", "block_place", "footsteps", "block_other",
        "combat", "inventory", "movement", "environment", "interaction", "food", "magic", "animals", "liquids", "other"
    };
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("shatsu-sounds/profile.json");
    private static volatile ShatsuConfig ACTIVE = new ShatsuConfig();
    public float master = 1.0f;
    public boolean inventoryAudioEnabled = true;
    public String preset = "Balanced";
    public Map<String, Float> categoryVolumes = new HashMap<>();
    public Map<String, Float> eventVolumes = new HashMap<>();
    public Map<String, Float> eventPitches = new HashMap<>();
    public Map<String, Boolean> enabled = new HashMap<>();
    public Map<String, Boolean> favoriteSounds = new HashMap<>();
    private ShatsuConfig() {}
    public static ShatsuConfig current() { return ACTIVE; }
    public static synchronized void load() {
        try {
            if (Files.isRegularFile(PATH)) {
                ShatsuConfig result = GSON.fromJson(Files.readString(PATH, StandardCharsets.UTF_8), ShatsuConfig.class);
                if (result != null) {
                    if (result.categoryVolumes == null) result.categoryVolumes = new HashMap<>();
                    if (result.eventVolumes == null) result.eventVolumes = new HashMap<>();
                    if (result.eventPitches == null) result.eventPitches = new HashMap<>();
                    if (result.enabled == null) result.enabled = new HashMap<>();
                    if (result.favoriteSounds == null) result.favoriteSounds = new HashMap<>();
                    ACTIVE = result;
                }
            } else save();
        } catch (Exception e) { System.err.println("[Shatsu Sounds] Config error: " + e); }
    }
    public static synchronized void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Path tmp = PATH.resolveSibling("profile.json.tmp");
            Files.writeString(tmp, GSON.toJson(ACTIVE), StandardCharsets.UTF_8);
            try { Files.move(tmp, PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (Exception e) { Files.move(tmp, PATH, StandardCopyOption.REPLACE_EXISTING); }
        } catch (Exception e) { System.err.println("[Shatsu Sounds] Save error: " + e); }
    }
    public static float clamp(float value) { return Math.max(0f, Math.min(2f, Float.isFinite(value) ? value : 1f)); }
    public static String category(String event) {
        String id = event.contains(":") ? event.substring(event.indexOf(':')+1) : event;
        if (id.startsWith("block.") || id.startsWith("block/")) {
            if (id.endsWith(".step") || id.endsWith(".fall") || id.endsWith(".land")) return "footsteps";
            if (id.endsWith(".hit")) return "block_hit";
            if (id.endsWith(".break")) return "block_break";
            if (id.endsWith(".place")) return "block_place";
            return "block_other";
        }
        if (id.contains("experience_orb") || id.contains("totem")) return "magic";
        if (id.startsWith("animal.") || id.startsWith("entity.zombie.")
                || id.startsWith("entity.ender_dragon.") || id.startsWith("entity.spider.")
                || id.startsWith("entity.cave_spider.") || id.startsWith("entity.skeleton.")
                || id.startsWith("entity.skeleton_horse.") || id.startsWith("entity.zombie_horse."))
            return "animals";
        if (id.startsWith("drink.") || id.contains("bucket.") || id.startsWith("liquid."))
            return "liquids";
        if (id.contains("shield")) return "combat";
        if (id.contains("inventory") || id.contains("typing") || id.contains("item_click") || id.contains("item_drag")) return "inventory";
        if (id.contains("step") || id.contains("landing") || id.contains("swim")) return "movement";
        if (id.contains("attack") || id.contains("hit") || id.contains("mace") || id.contains("weapon") || id.contains("trident") || id.contains("bow") || id.contains("spear")) return "combat";
        if (id.contains("eat") || id.contains("drink") || id.contains("honey")) return "food";
        if (id.contains("rain") || id.contains("thunder") || id.contains("explode") || id.contains("wind_burst")) return "environment";
        if (id.contains("open") || id.contains("close") || id.contains("click") || id.contains("door")) return "interaction";
        return "other";
    }
    public float multiplier(String id) {
        // Do not modify arbitrary other mods or vanilla mob-voice sounds.
        if (!ShatsuCatalog.contains(id)) return 1f;
        if (Boolean.FALSE.equals(enabled.get(id))) return 0f;
        return clamp(master) * clamp(categoryVolumes.getOrDefault(category(id),1f)) *
                clamp(eventVolumes.getOrDefault(id,1f));
    }
    public float pitch(String id) { return Math.max(.5f, Math.min(2f,eventPitches.getOrDefault(id,1f))); }
    public void setCategory(String category,float volume) { categoryVolumes.put(category,clamp(volume));save(); }
    public void setVolume(String id,float volume) { eventVolumes.put(id,clamp(volume));save(); }
    public void setEnabled(String id,boolean value) { enabled.put(id,value);save(); }
    public boolean isFavorite(String id) { return Boolean.TRUE.equals(favoriteSounds.get(id)); }
    public void toggleFavorite(String id) {
        if (isFavorite(id)) favoriteSounds.remove(id); else favoriteSounds.put(id, true);
        save();
    }
    public void setPitch(String id,float value) { eventPitches.put(id,Math.max(.5f,Math.min(2f,value)));save(); }
    public void resetEvent(String id) { enabled.remove(id);eventVolumes.remove(id);eventPitches.remove(id);save(); }
    public void resetMixer() { master=1f;categoryVolumes.clear();eventVolumes.clear();eventPitches.clear();save(); }
    public void applyPreset(String name) {
        preset = name;
        master = 1f;
        categoryVolumes.clear();
        switch(name) {
            case "Retro" -> { master=1.05f; categoryVolumes.put("combat",1.15f); categoryVolumes.put("inventory",1.1f); categoryVolumes.put("interaction",1.1f); categoryVolumes.put("magic",1.05f); }
            case "Soft ASMR" -> { master=.7f; categoryVolumes.put("combat",.65f);categoryVolumes.put("inventory",.7f);categoryVolumes.put("magic",.65f); }
            case "Crisp & Tactile" -> { categoryVolumes.put("inventory",1.1f);categoryVolumes.put("block_hit",1.05f); }
            case "Immersive" -> { categoryVolumes.put("environment",1.1f);categoryVolumes.put("movement",1.1f); }
            case "Combat Focus" -> { categoryVolumes.put("combat",1.15f);categoryVolumes.put("inventory",.7f); }
            case "Minimal" -> { master=.75f;categoryVolumes.put("inventory",.55f); }
            default -> { }
        }
        save();
    }
}
