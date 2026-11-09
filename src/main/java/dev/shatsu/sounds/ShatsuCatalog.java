package dev.shatsu.sounds;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Searchable catalog generated from the actual bundled sound JSON definitions. */
public final class ShatsuCatalog {
    public record Entry(String id, String category, int variations, String namespace) {}
    private static final Gson GSON = new Gson();
    private static final List<Entry> ENTRIES = load();
    private static final Set<String> IDS = ENTRIES.stream().map(Entry::id).collect(Collectors.toUnmodifiableSet());
    private ShatsuCatalog() {}
    private static List<Entry> load() {
        try (var stream = ShatsuCatalog.class.getResourceAsStream("/shatsu_catalog.json")) {
            if (stream == null) return List.of();
            List<Entry> entries = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8),
                    new TypeToken<ArrayList<Entry>>(){}.getType());
            return entries == null ? List.of() : Collections.unmodifiableList(entries);
        } catch (Exception e) {
            System.err.println("[Shatsu Sounds] Catalog failed: " + e);
            return List.of();
        }
    }
    public static List<Entry> all() { return ENTRIES; }
    public static int size() { return ENTRIES.size(); }
    public static boolean contains(String name) { return IDS.contains(name); }
}
