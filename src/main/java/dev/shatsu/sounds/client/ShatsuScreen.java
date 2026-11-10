package dev.shatsu.sounds.client;

import dev.shatsu.sounds.ShatsuCatalog;
import dev.shatsu.sounds.ShatsuConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
// Shatsu buttons are custom-rendered widgets, not Minecraft Button sprites.
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
    private boolean choosingLibraryCategory = false;
    private int libraryCategoryPage = 0;
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
        "Magic", "Nature & World", "Interactions & UI", "Mobs",
        "Water & Lava", "Other"
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
        {"animals"},
        {"liquids"},
        {"other"}
    };
    private static final String[] LIBRARY_FILTERS = {
        "All sounds", "Inventory", "Combat", "Blocks", "Movement", "Food & Drinks",
        "Magic", "Nature & World", "Interactions & UI", "Mobs",
        "Water & Lava", "Other"
    };

    public ShatsuScreen(Screen previous) {
        super(Component.literal("Shatsu Sounds"));
        this.previous = previous;
    }
    private int panelWidth() { return Math.min(620, Math.max(280, width - 22)); }
    private int panelX() { return Math.max(7, (width - panelWidth()) / 2); }
    private ShatsuSoftButton action(int x, int y, int w, int h, String text, Runnable callback) {
        boolean accent = text.startsWith("• ") || text.contains("  - Active")
            || text.equals("Save & Close") || text.equals("Play sound");
        return addRenderableWidget(new ShatsuSoftButton(x, y, w, h, Component.literal(text), callback, accent));
    }
    private void refresh() { clearWidgets(); init(); }
    private void tab(int selected) {
        // Save volume and per-sound edits when switching screens.
        if ((tab == 2 || tab == 3) && selected != tab) ShatsuConfig.save();
        if (tab == 3 && selected != tab) {
            selectedSoundId = null;
            choosingLibraryCategory = false;
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
        if (choosingLibraryCategory) {
            libraryCategoryPicker(x, y, w);
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
        action(x, y + 24, categoryW, 20, "Category: " + LIBRARY_FILTERS[filter], () -> {
            choosingLibraryCategory = true;
            libraryCategoryPage = 0;
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

    /** Choose a category directly rather than cycling through a long list. */
    private void libraryCategoryPicker(int x, int y, int w) {
        action(x, y, w, 20, "< Back to sounds", () -> {
            choosingLibraryCategory = false;
            refresh();
        });
        int first = y + 28;
        int footer = height - 61;
        int perPage = Math.max(1, Math.min(6, (footer - first - 3) / 28));
        int maxPage = Math.max(0, (LIBRARY_FILTERS.length - 1) / perPage);
        libraryCategoryPage = Math.max(0, Math.min(libraryCategoryPage, maxPage));
        for (int i = 0; i < perPage; i++) {
            int selected = libraryCategoryPage * perPage + i;
            if (selected >= LIBRARY_FILTERS.length) break;
            action(x, first + i * 28, w, 24,
                (filter == selected ? "✓  " : "") + LIBRARY_FILTERS[selected], () -> {
                    filter = selected;
                    page = 0;
                    choosingLibraryCategory = false;
                    refresh();
                });
        }
        action(x, footer, 84, 20, "Previous", () -> {
            libraryCategoryPage = Math.max(0, libraryCategoryPage - 1);
            refresh();
        });
        action(x + w - 84, footer, 84, 20, "Next", () -> {
            libraryCategoryPage = Math.min(maxPage, libraryCategoryPage + 1);
            refresh();
        });
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

        addRenderableWidget(new ShatsuSoftSlider(x, y + step * 3, w,
            "Sound volume", cfg.eventVolumes.getOrDefault(id, 1f), 0f, 2f, false, v -> {
                cfg.eventVolumes.put(id, (float) v);
                cfg.preset = "Custom";
            }));
        addRenderableWidget(new ShatsuSoftSlider(x, y + step * 4, w,
            "Pitch", cfg.eventPitches.getOrDefault(id, 1f), .5f, 2f, true, v -> {
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
        addRenderableWidget(new ShatsuSoftSlider(x, y, w - 78,
            "Master volume", cfg.master, 0f, 2f, false, v -> {
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
            addRenderableWidget(new ShatsuSoftSlider(x, yy, w - 78,
                label + (mixed ? " (mixed)" : ""), value, 0f, 2f, false, v -> {
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
                    // The v0.6.4 sound engine predates the Retro preset, so
                    // apply its mix explicitly without replacing that engine.
                    if ("Retro".equals(name)) {
                        cfg.master = 1.05f;
                        cfg.categoryVolumes.put("combat", 1.15f);
                        cfg.categoryVolumes.put("inventory", 1.10f);
                        cfg.categoryVolumes.put("interaction", 1.10f);
                        cfg.categoryVolumes.put("magic", 1.05f);
                        ShatsuConfig.save();
                    }
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

    /** Modern app-inspired surface, built from simple GPU-efficient rects. */
    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        int px = panelX(), w = panelWidth();
        g.fillGradient(0, 0, width, height, 0xFF14101C, 0xFF0C0A11);
        // Soft shadow and cut-corner app shell, independent of Minecraft GUI sprites.
        g.fill(px - 10, 8, px + w + 10, height - 7, 0x7A000000);
        g.fill(px - 8, 10, px + w + 8, height - 9, 0xFF242032);
        g.fill(px - 8, 16, px + w + 8, height - 15, 0xFF242032);
        g.fill(px - 2, 12, px + w + 2, 14, 0xFF7B6897);
        g.fill(px, 60, px + w, 61, 0xFF43374F);
        g.fill(px, height - 31, px + w, height - 30, 0xFF43374F);
        if (tab == 0) {
            for (int yy = 87; yy <= 167 && yy + 31 < height - 34; yy += 40) {
                g.fill(px - 1, yy, px + w + 1, yy + 34, 0xFF2D263A);
            }
        } else if (tab == 1) {
            int top = 89;
            int visible = Math.max(1, Math.min(4, (height - top - 65) / 36));
            for (int i = 0; i < visible; i++) {
                int idx = presetPage * visible + i;
                if (idx >= PRESET_NAMES.length) break;
                if (PRESET_NAMES[idx].equals(ShatsuConfig.current().preset)) {
                    int yy = top + i * 36;
                    g.fill(px - 1, yy - 3, px + w + 1, yy + 33, 0xAA55456E);
                }
            }
        }
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        super.extractRenderState(g, mx, my, partialTick);
        int x = panelX();
        g.text(font, "S H A T S U", x + 8, 18, 0xFFF8F3FF);
        g.text(font, "SOUND STUDIO  /  26.3", x + 122, 18, 0xFFC3A9E8);
        if (tab == 0)
            g.text(font, "YOUR SOUND, YOUR WAY", x + 8, 70, 0xFFCFB6FA);
        if (tab == 3 && selectedSoundId == null && choosingLibraryCategory)
            g.text(font, "CHOOSE A SOUND CATEGORY", x + 8, 84, 0xFFCFB6FA);
        if (tab == 3 && selectedSoundId == null && !choosingLibraryCategory) {
            List<ShatsuCatalog.Entry> hits = matchingSounds();
            int listY = 65 + 58;
            int footer = height - 61;
            int perPage = Math.max(1, Math.min(8, (footer - listY - 4) / 27));
            int maxPage = Math.max(0, (hits.size() - 1) / perPage);
            g.text(font, hits.size() + " sounds", x + 8, 113, 0xFFC3A9E8);
            if (hits.isEmpty())
                g.text(font, "No matches. Clear search or choose All sounds.",
                    x + 8, Math.min(height - 85, 151), 0xFFBDB0CD);
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
            if (Boolean.FALSE.equals(ShatsuConfig.current().enabled.get(id))) {
                status = "This sound is muted. Enable it to preview.";
                return;
            }
            if (manager.getSoundEvent(key) == null) {
                status = "Preview unavailable for this sound";
                System.err.println("[Shatsu Sounds] Missing preview event " + id);
                return;
            }
            if (activePreview != null) manager.stop(activePreview);
            activePreview = SimpleSoundInstance.forUI(
                SoundEvent.createVariableRangeEvent(key), 1.0f, 1.0f);
            manager.play(activePreview);
            status = "Playing " + shortened(friendlySoundName(id), 43);
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
