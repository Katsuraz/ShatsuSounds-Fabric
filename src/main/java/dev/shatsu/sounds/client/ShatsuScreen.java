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
    private int tab = 0, filter = 0, page = 0, mixerPage = 0;
    private SoundInstance activePreview;
    private long lastPreviewNanos;
    private static final String[] TABS = {"Home", "Library", "Mixer", "Presets", "Tools", "FX Lab"};
    private static final String[] FILTERS = {"all", "block_hit", "block_break", "block_place",
        "footsteps", "block_other", "combat", "inventory", "movement", "environment",
        "interaction", "food", "magic", "other", "favorites"};

    public ShatsuScreen(Screen previous) {
        super(Component.literal("Shatsu Sounds"));
        this.previous = previous;
    }
    private int panelWidth() { return Math.min(500, Math.max(280, width - 20)); }
    private int panelX() { return Math.max(7, (width - panelWidth()) / 2); }
    private Button action(int x, int y, int w, int h, String text, Runnable callback) {
        Button button = new ShatsuButton(x, y, Math.max(20, w), h, text, callback);
        return addRenderableWidget(button);
    }
    private void refresh() { clearWidgets(); init(); }
    private void tab(int selected) { tab = selected; page = 0; refresh(); }
    @Override protected void init() {
        int x = panelX(), w = panelWidth();
        // The header is painted as typography instead of an inert giant button.
        int navW = (w - 8) / TABS.length;
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
            case 5 -> soundLab(x, 65, w);
            default -> dashboard(x, 65, w);
        }
        action(x, height - 27, Math.max(90, w / 2 - 3), 20, "Stop Preview", this::stopPreview);
        action(x + w / 2 + 3, height - 27, w / 2 - 3, 20,
            "Save & Close", this::onClose);
    }
    private void dashboard(int x, int y, int w) {
        var cfg = ShatsuConfig.current();
        action(x, y, w, 23, "Browse " + ShatsuCatalog.size() + " available sound events", () -> tab(1));
        action(x, y + 29, w, 23, "Active preset: " + cfg.preset, () -> tab(3));
        action(x, y + 58, w, 23, "Sound mixer  /  " + Math.round(cfg.master * 100) + "% master", () -> tab(2));
        action(x, y + 87, w, 23, "Food sounds  /  apples, bread, meat, soup", () -> {
            filter = 11; query = ""; tab(1);
        });
        if (height > y + 151) {
            action(x, y + 116, w, 22, "Audio diagnostics and dependencies", () -> tab(4));
        }
        if (height > y + 180) {
            action(x, y + 145, w, 22, "Open FX Lab  /  favorite sounds & quick auditions", () -> tab(5));
        }
        if (height > y + 209) {
            action(x, y + 174, w, 22, "Restore default mixer levels", () -> {
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
            .filter(e -> (filter == 0 || ("favorites".equals(FILTERS[filter])
                 ? ShatsuConfig.current().isFavorite(e.id())
                 : FILTERS[filter].equals(e.category())))
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
            int labelW = Math.max(85, w - 201);
            String label = id.length() > 32 ? id.substring(0, 29) + "..." : id;
            action(x, yy, labelW, 20, label, () -> preview(id));
            action(x + labelW + 4, yy, 45, 20, "Play", () -> preview(id));
            boolean enabled = !Boolean.FALSE.equals(ShatsuConfig.current().enabled.get(id));
            boolean favorite = ShatsuConfig.current().isFavorite(id);
            action(x + labelW + 52, yy, 30, 20, favorite ? "★" : "☆", () -> {
                ShatsuConfig.current().toggleFavorite(id); refresh();
            });
            action(x + labelW + 85, yy, 47, 20, enabled ? "ON" : "OFF", () -> {
                ShatsuConfig.current().setEnabled(id, !enabled); refresh();
            });
            action(x + labelW + 136, yy, 62, 20, "Reset", () -> {
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

    /** Live FX Lab: every control changes real playback or saved settings. */
    private void soundLab(int x, int y, int w) {
        ShatsuConfig cfg = ShatsuConfig.current();
        int col = (w - 6) / 2;
        action(x, y, col, 22, "Bow · crisp draw", () -> preview("shatsufresh:preview.premium.bow.draw"));
        action(x + col + 6, y, col, 22, "Bow · sharp release", () -> preview("shatsufresh:preview.premium.bow.shoot"));
        action(x, y + 25, col, 22, "Crossbow · loaded", () -> preview("shatsufresh:preview.premium.crossbow.end"));
        action(x + col + 6, y + 25, col, 22, "Trident · throw", () -> preview("shatsufresh:preview.premium.trident.throw"));
        action(x, y + 50, col, 22, "Spear · jab", () -> preview("minecraft:item.spear.attack"));
        action(x + col + 6, y + 50, col, 22, "Spear · wood", () -> preview("minecraft:item.spear_wood.attack"));
        action(x, y + 75, col, 22, "Fantasy XP · pickup", () -> preview("minecraft:entity.experience_orb.pickup"));
        action(x + col + 6, y + 75, col, 22, "Magic · level up", () -> preview("minecraft:entity.player.levelup"));
        action(x, y + 100, col, 22, "Food · crunchy apple", () -> preview("shatsu_food_audio:food.apple"));
        action(x + col + 6, y + 100, col, 22, "Food · wet soup", () -> preview("shatsu_food_audio:food.soup"));
        action(x, y + 133, col, 23,
            "Arrow whoosh: " + (cfg.arrowWhooshEnabled ? "ON" : "OFF"), () -> {
                cfg.arrowWhooshEnabled = !cfg.arrowWhooshEnabled;
                ShatsuConfig.save(); refresh();
            });
        action(x + col + 6, y + 133, col, 23,
            "Food profiles: " + (cfg.foodAudioEnabled ? "ON" : "OFF"), () -> {
                cfg.foodAudioEnabled = !cfg.foodAudioEnabled;
                ShatsuConfig.save(); refresh();
            });
        if (height > y + 230) {
            int i = 159;
            addRenderableWidget(new ShatsuLevelSlider(x, y + i, w, "COMBAT IMPACT",
                cfg.categoryVolumes.getOrDefault("combat", 1f),
                v -> cfg.setCategory("combat", (float)v)));
            addRenderableWidget(new ShatsuLevelSlider(x, y + i + 26, w, "MAGICAL SPARKLE",
                cfg.categoryVolumes.getOrDefault("magic", 1f),
                v -> cfg.setCategory("magic", (float)v)));
        }
        int diagY = height - 54;
        if (diagY > y + 192) {
            String foodInfo = "Food swaps local " + ShatsuFoodAudio.localMatches() +
                " / network " + ShatsuFoodAudio.remoteMatches() +
                " / missing " + ShatsuFoodAudio.missingEvents();
            action(x, diagY, w, 19, foodInfo, () -> {
                status = ShatsuFoodAudio.lastProfile();
            });
        }
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

    /** Soft dark-wood palette with muted antique-gold highlights. */
    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        int px = panelX(), w = panelWidth();
        g.fillGradient(0, 0, width, height, 0xF016221F, 0xF00D1517);
        g.fill(px - 10, 6, px + w + 10, height - 6, 0xEE23302D);
        g.outline(px - 10, 6, w + 20, height - 12, 0xFF687567);
        g.fill(px - 10, 6, px + w + 10, 9, 0xFFD2B57C);
        g.fill(px - 10, 10, px + w + 10, 11, 0xFF4C5D51);
        g.fill(px, 62, px + w, 63, 0xFF475C52);
        g.fill(px, height - 32, px + w, height - 31, 0xFF475C52);
        if (tab == 0) {
            for (int yy = 65; yy < Math.min(height - 38, 210); yy += 29)
                g.fill(px - 2, yy - 2, px + w + 2, yy + 25, 0x603E4B45);
        } else if (tab == 2) {
            g.fill(px - 2, 63, px + w + 2, Math.min(height - 37, 278), 0x403E4B45);
        }
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        super.extractRenderState(g, mx, my, partialTick);
        int x = panelX();
        g.text(font, "SHATSU", x + 8, 18, 0xFFF5E9CE);
        g.text(font, "SOUND STUDIO  •  FABRIC 26.3", x + 101, 18, 0xFFA9BDAF);
        if (status != null && !status.isBlank())
            g.text(font, status.length() > 58 ? status.substring(0, 55) + "..." : status,
                    x + 8, height - 42, 0xFFD2B57C);
    }

    private void stopPreview() {
        if (activePreview != null) {
            Minecraft.getInstance().getSoundManager().stop(activePreview);
            activePreview = null;
        }
        status = "Preview stopped";
    }
    private void preview(String id) {
        // Ignore repeated presses. An audio backend can still be finishing the
        // previous play request when Minecraft invokes a second click callback.
        long now = System.nanoTime();
        if (now - lastPreviewNanos < 120_000_000L) return;
        lastPreviewNanos = now;
        try {
            var manager = Minecraft.getInstance().getSoundManager();
            var key = Identifier.parse(id);
            var soundSet = manager.getSoundEvent(key);
            // getSoundEvent() can return an EMPTY event even when the entry
            // exists in sounds.json: missing OGGs are discarded during reload.
            if (soundSet == null || soundSet.getWeight() <= 0) {
                status = "No playable audio: " + id;
                System.err.println("[Shatsu Sounds] Skipped unavailable preview " + id
                        + " (no loaded samples). Check namespaced OGG paths.");
                return;
            }
            if (activePreview != null) manager.stop(activePreview);
            activePreview = SimpleSoundInstance.forUI(
                    SoundEvent.createVariableRangeEvent(key), ShatsuConfig.current().pitch(id), 1.0f);
            manager.play(activePreview);
            status = "Playing " + id;
        } catch (RuntimeException ex) {
            activePreview = null;
            status = "Preview failed: see latest.log";
            System.err.println("[Shatsu Sounds] Preview " + id + ": " + ex);
            ex.printStackTrace(System.err);
        }
    }
    @Override public void onClose() {
        stopPreview();
        ShatsuConfig.save();
        Minecraft.getInstance().gui.setScreen(previous);
    }
}
