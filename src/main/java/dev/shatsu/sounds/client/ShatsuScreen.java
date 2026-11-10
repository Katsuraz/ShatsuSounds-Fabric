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
    private static final String[] FILTERS = {"all", "block_hit", "block_break", "block_place",
        "footsteps", "block_other", "combat", "inventory", "movement", "environment",
        "interaction", "food", "magic", "other"};

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
    private void tab(int selected) { tab = selected; page = 0; refresh(); }
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
            tab == 3 ? "Stop preview" : (tab == 0 ? "Close" : "Home"),
            () -> { if (tab == 3) stopPreview(); else if (tab == 0) onClose(); else tab(0); });
        action(x + w / 2 + 3, height - 27, w / 2 - 3, 20,
            "Save & Close", this::onClose);
    }
    private void dashboard(int x, int y, int w) {
        var cfg = ShatsuConfig.current();
        action(x, y + 24, w, 30, "Presets   /   " + cfg.preset, () -> tab(1));
        action(x, y + 64, w, 30, "Volume   /   " + Math.round(cfg.master * 100) + "%", () -> tab(2));
        action(x, y + 104, w, 30, "Sound library   /   Browse & preview", () -> tab(3));
    }
    private void library(int x, int y, int w) {
        int searchW = Math.max(80, w - 167);
        EditBox field = new EditBox(font, x, y, searchW, 20, Component.literal("Find a sound"));
        field.setValue(query);
        field.setHint(Component.literal("Search by event ID..."));
        field.setResponder(v -> { query = v; page = 0; });
        addRenderableWidget(field);
        action(x + searchW + 4, y, 53, 20, "Find", this::refresh);
        action(x + searchW + 61, y, 106, 20, FILTERS[filter], () -> {
            filter = (filter + 1) % FILTERS.length; page = 0; refresh();
        });
        List<ShatsuCatalog.Entry> hits = ShatsuCatalog.all().stream()
            .filter(e -> (filter == 0 || FILTERS[filter].equals(e.category()))
                && (query.isBlank() || e.id().toLowerCase(Locale.ROOT)
                    .contains(query.toLowerCase(Locale.ROOT)))).toList();
        int perPage = Math.max(1, Math.min(5, (height - y - 111) / 45));
        int maxPage = Math.max(0, (hits.size() - 1) / perPage);
        page = Math.max(0, Math.min(page, maxPage));
        for (int i = 0; i < perPage; i++) {
            int index = page * perPage + i;
            if (index >= hits.size()) break;
            ShatsuCatalog.Entry item = hits.get(index);
            String id = item.id();
            int yy = y + 27 + i * 45;
            int labelW = Math.max(85, w - 167);
            String label = id.length() > 32 ? id.substring(0, 29) + "..." : id;
            action(x, yy, labelW, 20, label, () -> preview(id));
            action(x + labelW + 4, yy, 45, 20, "Play", () -> preview(id));
            boolean enabled = !Boolean.FALSE.equals(ShatsuConfig.current().enabled.get(id));
            action(x + labelW + 52, yy, 48, 20, enabled ? "ON" : "OFF", () -> {
                ShatsuConfig.current().setEnabled(id, !enabled); refresh();
            });
            action(x + labelW + 104, yy, 62, 20, "Reset", () -> {
                ShatsuConfig.current().resetEvent(id); refresh();
            });
            var cfg = ShatsuConfig.current();
            float volume = cfg.eventVolumes.getOrDefault(id, 1f);
            float pitch = cfg.eventPitches.getOrDefault(id, 1f);
            int q = w / 6;
            action(x, yy + 21, q - 2, 20, "Vol -", () -> {
                cfg.setVolume(id, volume - .1f); refresh();
            });
            action(x + q, yy + 21, q - 2, 20, Math.round(volume * 100) + "%", () -> preview(id));
            action(x + 2 * q, yy + 21, q - 2, 20, "Vol +", () -> {
                cfg.setVolume(id, volume + .1f); refresh();
            });
            action(x + 3 * q, yy + 21, q - 2, 20, "Pitch -", () -> {
                cfg.setPitch(id, pitch - .05f); refresh();
            });
            action(x + 4 * q, yy + 21, q - 2, 20, String.format(Locale.ROOT, "%.2fx", pitch),
                () -> {cfg.setPitch(id, 1f); refresh();});
            action(x + 5 * q, yy + 21, w - 5 * q, 20, "Pitch +", () -> {
                cfg.setPitch(id, pitch + .05f); refresh();
            });
        }
        int footer = Math.min(height - 53, y + 30 + perPage * 45);
        action(x, footer, 80, 19, "< Back", () -> { page = Math.max(0, page - 1); refresh(); });
        action(x + 84, footer, w - 170, 19,
            (page + 1) + "/" + (maxPage + 1) + "  /  " + hits.size() + " sounds",
            () -> {page = 0; refresh();});
        action(x + w - 82, footer, 82, 19, "Next >", () -> {
            page = Math.min(maxPage, page + 1); refresh();
        });
    }
    private void mixer(int x, int y, int w) {
        ShatsuConfig cfg = ShatsuConfig.current();
        addRenderableWidget(new ShatsuLevelSlider(x, y, w - 92, "MASTER VOLUME",
                cfg.master, v -> cfg.master = (float) v));
        action(x + w - 88, y, 88, 20, "Reset", () -> {
            cfg.master = 1f; ShatsuConfig.save(); refresh();
        });
        String[] cats = ShatsuConfig.CATEGORIES;
        int visible = Math.max(1, Math.min(6, (height - y - 89) / 28));
        int endPage = Math.max(0, (cats.length - 1) / visible);
        mixerPage = Math.min(mixerPage, endPage);
        for (int i = 0; i < visible; i++) {
            int index = mixerPage * visible + i;
            if (index >= cats.length) break;
            String key = cats[index];
            int yy = y + 28 + i * 28;
            float value = cfg.categoryVolumes.getOrDefault(key, 1f);
            addRenderableWidget(new ShatsuLevelSlider(x, yy, w - 92,
                    key.replace('_', ' ').toUpperCase(Locale.ROOT),
                    value, v -> cfg.categoryVolumes.put(key, (float) v)));
            action(x + w - 88, yy, 88, 20, "Default", () -> {
                cfg.categoryVolumes.remove(key); ShatsuConfig.save(); refresh();
            });
        }
        int footer = Math.min(height - 53, y + 33 + visible * 28);
        action(x, footer, 93, 19, "Previous", () -> {
            mixerPage = Math.max(0, mixerPage - 1); refresh();
        });
        action(x + 97, footer, w - 194, 19, "Category page " + (mixerPage + 1) + "/" + (endPage + 1),
            () -> {cfg.resetMixer(); refresh();});
        action(x + w - 93, footer, 93, 19, "Next", () -> {
            mixerPage = Math.min(endPage, mixerPage + 1); refresh();
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
                SoundEvent.createVariableRangeEvent(key), ShatsuConfig.current().pitch(id), .6f);
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
