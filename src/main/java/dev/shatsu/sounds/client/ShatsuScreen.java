package dev.shatsu.sounds.client;

import dev.shatsu.sounds.ShatsuCatalog;
import dev.shatsu.sounds.ShatsuConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.function.DoubleConsumer;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import java.util.List;
import java.util.Locale;

/** Compact Shatsu Sound Studio; every editing control writes settings immediately. */
public final class ShatsuScreen extends Screen {
    private final Screen previous;
    private String query = "";
    private String status = "";
    private int tab = 0, filter = 0, page = 0, mixerPage = 0, presetPage = 0;
    private String selectedSoundId = null;
    private boolean favoritesOnly = false;
    private SoundInstance activePreview;
    private static final String[] TABS = {"Home", "Presets", "Volume", "Library"};
    private static final String[] PRESET_NAMES = {
        "Balanced", "Retro", "Soft ASMR", "Crisp & Tactile",
        "Immersive", "Combat Focus", "Minimal"
    };
    private static final String[] PRESET_DETAILS = {
        "The original Shatsu mix",
        "Punchy clicks, weapons and magic",
        "Gentle and cozy listening",
        "Clean, detailed everyday sounds",
        "Richer movement and atmosphere",
        "More weight behind combat",
        "Only the essentials, quieter"
    };
    // Friendly groups over the original low-level category keys.
    // Grouping is presentation-only, so existing profile.json values remain compatible.
    private static final String[] VOLUME_GROUPS = {
        "Inventory", "Combat", "Blocks", "Movement", "Food & Drinks",
        "Magic", "Nature & World", "Interactions & UI", "Other"
    };
    private static final String[][] VOLUME_GROUP_KEYS = {
        {"inventory"},
        {"combat"},
        {"block_hit", "block_break", "block_place", "block_other"},
        {"footsteps", "movement"},
        {"food"},
        {"magic"},
        {"environment"},
        {"interaction"},
        {"other"}
    };
    private static final String[] LIBRARY_FILTERS = {
        "All sounds", "Inventory", "Combat", "Blocks", "Movement", "Food & Drinks",
        "Magic", "Nature & World", "Interactions & UI", "Other"
    };

    public ShatsuScreen(Screen previous) {
        super(Component.literal("Shatsu Sounds"));
        this.previous = previous;
    }
    private int panelWidth() { return Math.min(500, Math.max(280, width - 20)); }
    private int panelX() { return Math.max(7, (width - panelWidth()) / 2); }
    private Button action(int x, int y, int w, int h, String text, Runnable callback) {
        Button button = Button.builder(Component.literal(text), b -> callback.run())
            .pos(x, y).size(Math.max(20, w), h).build();
        return addRenderableWidget(button);
    }
    private void refresh() { clearWidgets(); init(); }
    private void tab(int selected) {
        // Save volume and per-sound edits when switching screens.
        if ((tab == 2 || tab == 3) && selected != tab) ShatsuConfig.save();
        if (tab == 3 && selected != tab) {
            selectedSoundId = null;
            if (activePreview != null) stopPreview();
        }
        tab = selected;
        page = 0;
        refresh();
    }
    @Override protected void init() {
        int x = panelX(), w = panelWidth();
        // The header is painted as typography instead of an inert giant button.
        int navW = (w - 6) / TABS.length;
        for (int i = 0; i < TABS.length; i++) {
            final int selected = i;
            action(x + i * navW, 37, navW - 2, 20,
                (tab == i ? "• " : "") + TABS[i], () -> tab(selected));
        }
        switch (tab) {
            case 0 -> dashboard(x, 65, w);
            case 1 -> presets(x, 65, w);
            case 2 -> mixer(x, 65, w);
            case 3 -> library(x, 65, w);
            default -> dashboard(x, 65, w);
        }
        action(x, height - 27, Math.max(90, w / 2 - 3), 20,
            tab == 3 && selectedSoundId == null ? "Stop preview" : (tab == 0 ? "Close" : "Home"),
            () -> { if (tab == 3 && selectedSoundId == null) stopPreview(); else if (tab == 0) onClose(); else tab(0); });
        action(x + w / 2 + 3, height - 27, w / 2 - 3, 20,
            "Save & Close", this::onClose);
    }
    private void dashboard(int x, int y, int w) {
        var cfg = ShatsuConfig.current();
        action(x, y + 24, w, 30, "Presets   /   " + cfg.preset, () -> tab(1));
        action(x, y + 64, w, 30, "Volume   /   " + Math.round(cfg.master * 100) + "%", () -> tab(2));
        action(x, y + 104, w, 30, "Sound library   /   Browse, preview & edit", () -> tab(3));
    }

    /** Friendly names keep raw technical IDs out of the everyday library UI. */
    private static String friendlySoundName(String id) {
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        String[] tokens = path.replace('/', '.').replace('-', '_').split("\\.");
        StringBuilder name = new StringBuilder();
        for (String token : tokens) {
            if (token.isBlank()) continue;
            if (name.length() > 0) name.append(" ");
            String word = token.replace('_', ' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.length() == 0 ? id : name.toString();
    }

    private static String shortened(String value, int max) {
        return value.length() > max ? value.substring(0, Math.max(1, max - 3)) + "..." : value;
    }

    private boolean matchesLibraryCategory(ShatsuCatalog.Entry entry) {
        if (filter == 0) return true;
        String[] keys = VOLUME_GROUP_KEYS[filter - 1];
        for (String key : keys) if (key.equals(entry.category())) return true;
        return false;
    }

    private List<ShatsuCatalog.Entry> matchingSounds() {
        String search = query.trim().toLowerCase(Locale.ROOT);
        ShatsuConfig cfg = ShatsuConfig.current();
        return ShatsuCatalog.all().stream()
            .filter(e -> matchesLibraryCategory(e)
                && (!favoritesOnly || cfg.isFavorite(e.id()))
                && (search.isBlank() || e.id().toLowerCase(Locale.ROOT).contains(search)
                    || friendlySoundName(e.id()).toLowerCase(Locale.ROOT).contains(search)))
            .toList();
    }

    private void library(int x, int y, int w) {
        if (selectedSoundId != null) {
            librarySoundDetail(x, y, w, selectedSoundId);
            return;
        }

        int searchW = Math.max(90, w - 133);
        EditBox field = new EditBox(font, x, y, searchW, 20, Component.literal("Search sounds"));
        field.setValue(query);
        field.setHint(Component.literal("Find an item or sound..."));
        field.setResponder(value -> { query = value; page = 0; });
        addRenderableWidget(field);
        action(x + searchW + 4, y, 61, 20, "Search", this::refresh);
        action(x + searchW + 69, y, w - searchW - 69, 20, "Clear", () -> {
            query = "";
            page = 0;
            refresh();
        });

        int categoryW = w - 107;
        action(x, y + 24, categoryW, 20, LIBRARY_FILTERS[filter], () -> {
            filter = (filter + 1) % LIBRARY_FILTERS.length;
            page = 0;
            refresh();
        });
        action(x + categoryW + 4, y + 24, w - categoryW - 4, 20,
            favoritesOnly ? "★ Favorites" : "☆ Favorites", () -> {
                favoritesOnly = !favoritesOnly;
                page = 0;
                refresh();
            });

        List<ShatsuCatalog.Entry> hits = matchingSounds();
        int listY = y + 58;
        int footer = height - 61;
        int perPage = Math.max(1, Math.min(8, (footer - listY - 4) / 27));
        int maxPage = Math.max(0, (hits.size() - 1) / perPage);
        page = Math.max(0, Math.min(page, maxPage));

        for (int i = 0; i < perPage; i++) {
            int index = page * perPage + i;
            if (index >= hits.size()) break;
            ShatsuCatalog.Entry entry = hits.get(index);
            String id = entry.id();
            int yy = listY + i * 27;
            int labelW = w - 113;
            String label = shortened(friendlySoundName(id), Math.max(16, labelW / 6));
            action(x, yy, labelW, 22, label, () -> {
                selectedSoundId = id;
                ShatsuConfig.save();
                refresh();
            });
            action(x + labelW + 4, yy, 47, 22, "Play", () -> preview(id));
            boolean favorite = ShatsuConfig.current().isFavorite(id);
            action(x + labelW + 55, yy, w - labelW - 55, 22,
                favorite ? "★" : "☆", () -> {
                    ShatsuConfig.current().toggleFavorite(id);
                    refresh();
                });
        }

        action(x, footer, 84, 20, "Previous", () -> {
            page = Math.max(0, page - 1);
            refresh();
        });
        action(x + w - 84, footer, 84, 20, "Next", () -> {
            page = Math.min(maxPage, page + 1);
            refresh();
        });
        // Result count and page number are displayed as non-clickable text.
    }

    private void librarySoundDetail(int x, int y, int w, String id) {
        ShatsuConfig cfg = ShatsuConfig.current();
        int step = Math.max(21, Math.min(32, (height - y - 80) / 5));
        int half = (w - 6) / 2;
        action(x, y, w, 20, shortened("< Back  |  " + friendlySoundName(id), Math.max(32, w / 6)), () -> {
            ShatsuConfig.save();
            selectedSoundId = null;
            if (activePreview != null) stopPreview();
            refresh();
        });

        action(x, y + step, half, 20, "Play sound", () -> preview(id));
        action(x + half + 6, y + step, w - half - 6, 20, "Stop", this::stopPreview);
        boolean enabled = !Boolean.FALSE.equals(cfg.enabled.get(id));
        action(x, y + step * 2, half, 20, enabled ? "Enabled" : "Muted", () -> {
            if (enabled && activePreview != null) stopPreview();
            cfg.setEnabled(id, !enabled);
            cfg.preset = "Custom";
            ShatsuConfig.save();
            refresh();
        });
        action(x + half + 6, y + step * 2, w - half - 6, 20,
            cfg.isFavorite(id) ? "★ Favorited" : "☆ Favorite", () -> {
                cfg.toggleFavorite(id);
                refresh();
            });

        addRenderableWidget(new ShatsuLevelSlider(x, y + step * 3, w,
            "Sound volume", cfg.eventVolumes.getOrDefault(id, 1f), v -> {
                cfg.eventVolumes.put(id, (float) v);
                cfg.preset = "Custom";
            }));
        addRenderableWidget(new ShatsuPitchSlider(x, y + step * 4, w,
            cfg.eventPitches.getOrDefault(id, 1f), v -> {
                cfg.eventPitches.put(id, (float) v);
                cfg.preset = "Custom";
            }));
        action(x, y + step * 5, w, 20, "Restore this sound's defaults", () -> {
            if (activePreview != null) stopPreview();
            cfg.resetEvent(id);
            cfg.preset = "Custom";
            ShatsuConfig.save();
            status = "Sound settings restored";
            refresh();
        });
    }

    /** Average for display only. Adjusting a mixed group intentionally unifies its levels. */
    private float volumeGroupLevel(ShatsuConfig cfg, String[] keys) {
        float sum = 0f;
        for (String key : keys) sum += cfg.categoryVolumes.getOrDefault(key, 1f);
        return sum / keys.length;
    }

    private boolean volumeGroupMixed(ShatsuConfig cfg, String[] keys) {
        float first = cfg.categoryVolumes.getOrDefault(keys[0], 1f);
        for (String key : keys) {
            if (Math.abs(first - cfg.categoryVolumes.getOrDefault(key, 1f)) > .015f)
                return true;
        }
        return false;
    }

    /** Familiar categories and one master slider. No raw event IDs or diagnostic controls. */
    private void mixer(int x, int y, int w) {
        ShatsuConfig cfg = ShatsuConfig.current();
        addRenderableWidget(new ShatsuLevelSlider(x, y, w - 78,
            "Master volume", cfg.master, v -> {
                cfg.master = (float) v;
                cfg.preset = "Custom";
            }));
        action(x + w - 74, y, 74, 20, "Reset", () -> {
            cfg.master = 1f;
            cfg.preset = "Custom";
            ShatsuConfig.save();
            status = "Master volume reset";
            refresh();
        });

        int top = y + 42;
        int footer = height - 61;
        int visible = Math.max(1, Math.min(5, (footer - top - 5) / 31));
        int maxPage = (VOLUME_GROUPS.length - 1) / visible;
        mixerPage = Math.max(0, Math.min(mixerPage, maxPage));

        for (int i = 0; i < visible; i++) {
            int idx = mixerPage * visible + i;
            if (idx >= VOLUME_GROUPS.length) break;
            String[] keys = VOLUME_GROUP_KEYS[idx];
            String label = VOLUME_GROUPS[idx];
            float value = volumeGroupLevel(cfg, keys);
            boolean mixed = volumeGroupMixed(cfg, keys);
            int yy = top + i * 31;
            addRenderableWidget(new ShatsuLevelSlider(x, yy, w - 78,
                label + (mixed ? " (mixed)" : ""), value, v -> {
                    float newValue = (float) v;
                    for (String key : keys) cfg.categoryVolumes.put(key, newValue);
                    cfg.preset = "Custom";
                }));
            action(x + w - 74, yy, 74, 20, "Reset", () -> {
                for (String key : keys) cfg.categoryVolumes.remove(key);
                cfg.preset = "Custom";
                ShatsuConfig.save();
                status = label + " volume reset";
                refresh();
            });
        }

        action(x, footer, 86, 19, "Previous", () -> {
            ShatsuConfig.save();
            mixerPage = Math.max(0, mixerPage - 1);
            refresh();
        });
        action(x + w - 86, footer, 86, 19, "Next", () -> {
            ShatsuConfig.save();
            mixerPage = Math.min(maxPage, mixerPage + 1);
            refresh();
        });
    }

    /** One-click cards for everyday listening: no technical event IDs here. */
    private void presets(int x, int y, int w) {
        ShatsuConfig cfg = ShatsuConfig.current();
        action(x, y, w, 19, "Active preset: " + cfg.preset, () -> {
            status = "Current sound mix: " + cfg.preset; refresh();
        });
        int top = y + 24;
        int visible = Math.max(1, Math.min(4, (height - top - 65) / 36));
        int maxPage = (PRESET_NAMES.length - 1) / visible;
        presetPage = Math.max(0, Math.min(maxPage, presetPage));
        for (int i = 0; i < visible; i++) {
            int index = presetPage * visible + i;
            if (index >= PRESET_NAMES.length) break;
            String name = PRESET_NAMES[index];
            boolean selected = name.equals(cfg.preset);
            int yy = top + i * 36;
            action(x + 4, yy, w - 8, 23,
                (selected ? "✓  " : "") + name + (selected ? "  - Active" : ""), () -> {
                    cfg.applyPreset(name);
                    status = name + " preset applied";
                    refresh();
                });
        }
        int footer = height - 61;
        action(x, footer, 85, 19, "Previous", () -> {
            presetPage = Math.max(0, presetPage - 1); refresh();
        });
        action(x + w - 85, footer, 85, 19, "Next", () -> {
            presetPage = Math.min(maxPage, presetPage + 1); refresh();
        });
    }
    private void diagnostics(int x, int y, int w) {
        action(x, y, w, 20, "Catalog: " + ShatsuCatalog.size() + " loaded sound events",
            () -> tab(1));
        String[] mods = {"clangor", "sound", "customblocksounds", "fabric-api",
                          "modmenu", "sound_physics_remastered"};
        for (int i = 0; i < mods.length && y + 25 + i * 22 < height - 100; i++) {
            String id = mods[i];
            boolean loaded = FabricLoader.getInstance().isModLoaded(id);
            action(x, y + 25 + i * 22, w, 20,
                (loaded ? "✓ " : "✕ ") + id + " — " + (loaded ? "installed" : "missing"),
                () -> {status = loaded ? id + " is installed" : "Install " + id; refresh();});
        }
        int bottom = height - 81;
        action(x, bottom, w / 3 - 2, 20, "Test XP",
            () -> preview("minecraft:entity.experience_orb.pickup"));
        action(x + w / 3 + 2, bottom, w / 3 - 2, 20, "Test Shield",
            () -> preview("shatsupolish:shield.raise"));
        action(x + 2 * w / 3 + 2, bottom, w - 2 * w / 3 - 2, 20, "Test Totem",
            () -> preview("minecraft:item.totem.use"));
    }
    /** Real drag sliders; volume changes affect playback immediately and save on close. */
    private static final class ShatsuLevelSlider extends AbstractSliderButton {
        private final String label;
        private final DoubleConsumer update;
        ShatsuLevelSlider(int x, int y, int w, String label, float initial, DoubleConsumer update) {
            super(x, y, w, 20, Component.literal(label),
                    Math.max(0, Math.min(1, initial / 2.0)));
            this.label = label;
            this.update = update;
            updateMessage();
        }
        @Override protected void updateMessage() {
            setMessage(Component.literal(label + "     " + Math.round(value * 200) + "%"));
        }
        @Override protected void applyValue() {
            update.accept(Math.max(0, Math.min(2, value * 2)));
        }
    }

    /** Per-sound pitch ranges from 0.50x to 2.00x. */
    private static final class ShatsuPitchSlider extends AbstractSliderButton {
        private final DoubleConsumer update;
        ShatsuPitchSlider(int x, int y, int w, float initial, DoubleConsumer update) {
            super(x, y, w, 20, Component.literal("Pitch"),
                Math.max(0, Math.min(1, (initial - .5f) / 1.5)));
            this.update = update;
            updateMessage();
        }
        @Override protected void updateMessage() {
            setMessage(Component.literal(String.format(Locale.ROOT, "Pitch   %.2fx", .5 + value * 1.5)));
        }
        @Override protected void applyValue() {
            update.accept(.5 + value * 1.5);
        }
    }

    /** Shatsu music-app colors, adapted to Minecraft's native UI renderer. */
    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        int px = panelX(), w = panelWidth();
        g.fillGradient(0, 0, width, height, 0xF0181425, 0xF0101019);
        g.fill(px - 10, 6, px + w + 10, height - 6, 0xEE242032);
        g.outline(px - 10, 6, w + 20, height - 12, 0xFF625178);
        g.fill(px - 10, 6, px + w + 10, 9, 0xFFBD9BFF);
        g.fill(px, 62, px + w, 63, 0xFF4C3E60);
        g.fill(px, height - 32, px + w, height - 31, 0xFF4C3E60);
        if (tab == 0) {
            for (int yy = 89; yy <= 169 && yy + 30 < height - 33; yy += 40)
                g.fill(px - 3, yy - 3, px + w + 3, yy + 33, 0x663F3551);
        } else if (tab == 1) {
            int top = 89;
            int visible = Math.max(1, Math.min(4, (height - top - 65) / 36));
            for (int i = 0; i < visible; i++) {
                int idx = presetPage * visible + i;
                if (idx >= PRESET_NAMES.length) break;
                int yy = top + i * 36;
                int bg = PRESET_NAMES[idx].equals(ShatsuConfig.current().preset)
                    ? 0xAA584378 : 0x66352C49;
                g.fill(px, yy - 2, px + w, yy + 34, bg);
            }
        }
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        super.extractRenderState(g, mx, my, partialTick);
        int x = panelX();
        g.text(font, "SHATSU", x + 8, 18, 0xFFF8F3FF);
        g.text(font, "SOUNDS  •  FABRIC 26.3", x + 101, 18, 0xFFC3A9E8);
        if (tab == 0)
            g.text(font, "YOUR SOUND, YOUR WAY", x + 8, 70, 0xFFCFB6FA);
        if (tab == 3 && selectedSoundId == null) {
            List<ShatsuCatalog.Entry> hits = matchingSounds();
            int listY = 65 + 58;
            int footer = height - 61;
            int perPage = Math.max(1, Math.min(8, (footer - listY - 4) / 27));
            int maxPage = Math.max(0, (hits.size() - 1) / perPage);
            g.text(font, hits.size() + " sounds", x + 8, 113, 0xFFC3A9E8);
            g.text(font, (page + 1) + " / " + (maxPage + 1),
                x + Math.max(90, panelWidth() / 2 - 16), height - 57, 0xFFC3A9E8);
        }
        if (tab == 2) {
            g.text(font, "SOUND CATEGORIES", x + 8, 92, 0xFFCFB6FA);
            int top = 65 + 42;
            int footer = height - 61;
            int visible = Math.max(1, Math.min(5, (footer - top - 5) / 31));
            int maxPage = (VOLUME_GROUPS.length - 1) / visible;
            g.text(font, (mixerPage + 1) + " / " + (maxPage + 1),
                x + Math.max(90, panelWidth() / 2 - 16), height - 57, 0xFFC3A9E8);
        }
        if (tab == 1) {
            int top = 89;
            int visible = Math.max(1, Math.min(4, (height - top - 65) / 36));
            for (int i = 0; i < visible; i++) {
                int idx = presetPage * visible + i;
                if (idx >= PRESET_DETAILS.length) break;
                g.text(font, PRESET_DETAILS[idx], x + 12, top + i * 36 + 25, 0xFFBDB0CD);
            }
            int maxPage = (PRESET_NAMES.length - 1) / visible;
            g.text(font, (presetPage + 1) + " / " + (maxPage + 1),
                    x + Math.max(87, (panelWidth() / 2) - 16), height - 57, 0xFFC3A9E8);
        }
        if (status != null && !status.isBlank())
            g.text(font, status.length() > 58 ? status.substring(0, 55) + "..." : status,
                    x + 8, height - 42, 0xFFD8C1FF);
    }

    private void stopPreview() {
        if (activePreview != null) {
            Minecraft.getInstance().getSoundManager().stop(activePreview);
            activePreview = null;
        }
        status = "Preview stopped";
    }
    private void preview(String id) {
        try {
            var manager = Minecraft.getInstance().getSoundManager();
            var key = Identifier.parse(id);
            if (manager.getSoundEvent(key) == null) {
                status = "Not registered: " + id;
                System.err.println("[Shatsu Sounds] Missing preview event " + id);
                return;
            }
            if (activePreview != null) manager.stop(activePreview);
            activePreview = SimpleSoundInstance.forUI(
                SoundEvent.createVariableRangeEvent(key), ShatsuConfig.current().pitch(id), 1.0f);
            manager.play(activePreview);
            status = "Playing " + id;
        } catch (Exception ex) {
            status = "Preview failed: see latest.log";
            System.err.println("[Shatsu Sounds] Preview " + id + ": " + ex);
        }
    }
    @Override public void onClose() {
        stopPreview();
        ShatsuConfig.save();
        Minecraft.getInstance().gui.setScreen(previous);
    }
}
