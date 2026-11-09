package dev.shatsu.sounds.client;

import dev.shatsu.sounds.ShatsuCatalog;
import dev.shatsu.sounds.ShatsuConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
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
    private int tab = 0, filter = 0, page = 0, mixerPage = 0;
    private SoundInstance activePreview;
    private static final String[] TABS = {"Home", "Library", "Mixer", "Presets", "Tools"};
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
        action(x, 10, w, 22, "SHATSU  /  SOUND STUDIO     26.3", () -> tab(0));
        int navW = (w - 8) / 5;
        for (int i = 0; i < TABS.length; i++) {
            final int selected = i;
            action(x + i * navW, 37, navW - 2, 20,
                (tab == i ? "• " : "") + TABS[i], () -> tab(selected));
        }
        switch (tab) {
            case 0 -> dashboard(x, 65, w);
            case 1 -> library(x, 65, w);
            case 2 -> mixer(x, 65, w);
            case 3 -> presets(x, 65, w);
            case 4 -> diagnostics(x, 65, w);
            default -> dashboard(x, 65, w);
        }
        action(x, height - 27, Math.max(90, w / 2 - 3), 20, "Stop sound", this::stopPreview);
        action(x + w / 2 + 3, height - 27, w / 2 - 3, 20,
            "Save & Close", this::onClose);
    }
    private void dashboard(int x, int y, int w) {
        var cfg = ShatsuConfig.current();
        action(x, y, w, 23, "Browse " + ShatsuCatalog.size() + " available sound events", () -> tab(1));
        action(x, y + 29, w, 23, "Active preset: " + cfg.preset, () -> tab(3));
        action(x, y + 58, w, 23, "Sound mixer  /  " + Math.round(cfg.master * 100) + "% master", () -> tab(2));
        action(x, y + 87, w, 23, "Test audio & check installed dependencies", () -> tab(4));
        if (height > y + 151) {
            action(x, y + 116, w, 22, "Restore default mixer levels", () -> {
                cfg.resetMixer(); status = "Mixer reset"; refresh();
            });
        }
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
        int half = Math.max(50, (w - 96) / 2);
        action(x, y, half, 22, "MASTER", () -> {cfg.master = 1f; ShatsuConfig.save(); refresh();});
        action(x + half + 3, y, 28, 22, "-", () -> {
            cfg.master = ShatsuConfig.clamp(cfg.master - .1f); ShatsuConfig.save(); refresh();
        });
        action(x + half + 34, y, w - half - 72, 22,
            Math.round(cfg.master * 100) + "%", () -> {
                cfg.master = 1f; ShatsuConfig.save(); refresh();
            });
        action(x + w - 35, y, 35, 22, "+", () -> {
            cfg.master = ShatsuConfig.clamp(cfg.master + .1f); ShatsuConfig.save(); refresh();
        });
        String[] cats = ShatsuConfig.CATEGORIES;
        int visible = Math.max(1, Math.min(6, (height - y - 90) / 28));
        int endPage = Math.max(0, (cats.length - 1) / visible);
        mixerPage = Math.min(mixerPage, endPage);
        for (int j = 0; j < visible; j++) {
            int index = mixerPage * visible + j;
            if (index >= cats.length) break;
            String category = cats[index];
            int row = y + 29 + j * 28;
            float level = cfg.categoryVolumes.getOrDefault(category, 1f);
            action(x, row, w - 143, 23, category.replace('_', ' ').toUpperCase(Locale.ROOT),
                () -> {cfg.setCategory(category, 1f); refresh();});
            action(x + w - 139, row, 32, 23, "-", () -> {
                cfg.setCategory(category, level - .1f); refresh();
            });
            action(x + w - 104, row, 66, 23, Math.round(level * 100) + "%",
                () -> {cfg.setCategory(category, 1f); refresh();});
            action(x + w - 35, row, 35, 23, "+", () -> {
                cfg.setCategory(category, level + .1f); refresh();
            });
        }
        int footer = Math.min(height - 53, y + 34 + visible * 28);
        action(x, footer, 93, 19, "Previous", () -> {
            mixerPage = Math.max(0, mixerPage - 1); refresh();
        });
        action(x + 97, footer, w - 194, 19, "Mixer " + (mixerPage + 1) + "/" + (endPage + 1),
            () -> {cfg.resetMixer(); refresh();});
        action(x + w - 93, footer, 93, 19, "Next", () -> {
            mixerPage = Math.min(endPage, mixerPage + 1); refresh();
        });
    }
    private void presets(int x, int y, int w) {
        String[] names = {"Balanced", "Soft ASMR", "Crisp & Tactile", "Immersive", "Combat Focus", "Minimal"};
        int step = Math.max(23, Math.min(29, (height - y - 40) / names.length));
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            action(x, y + i * step, w, step - 3,
                (name.equals(ShatsuConfig.current().preset) ? "● " : "") + name, () -> {
                    ShatsuConfig.current().applyPreset(name); status = "Preset applied"; refresh();
                });
        }
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
