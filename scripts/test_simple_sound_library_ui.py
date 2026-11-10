#!/usr/bin/env python3
"""Source-level checks for the simple sound library (not Minecraft runtime tests)."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCREEN = (ROOT / "src/main/java/dev/shatsu/sounds/client/ShatsuScreen.java").read_text("utf-8")
CONFIG = (ROOT / "src/main/java/dev/shatsu/sounds/ShatsuConfig.java").read_text("utf-8")
CATALOG = (ROOT / "src/main/java/dev/shatsu/sounds/ShatsuCatalog.java").read_text("utf-8")


class SoundLibraryUITests(unittest.TestCase):
    def test_catalog_driven_browsing(self):
        self.assertIn("ShatsuCatalog.all().stream()", SCREEN)
        self.assertIn("matchingSounds()", SCREEN)
        self.assertIn("matchesLibraryCategory(e)", SCREEN)
        self.assertIn("VOLUME_GROUP_KEYS[filter - 1]", SCREEN)
        self.assertIn("entry.category()", SCREEN)

    def test_search_and_pagination(self):
        self.assertIn("EditBox field", SCREEN)
        self.assertIn('"Search", this::refresh', SCREEN)
        self.assertIn("friendlySoundName(e.id())", SCREEN)
        self.assertIn('action(x, footer, 84, 20, "Previous"', SCREEN)
        self.assertIn('action(x + w - 84, footer, 84, 20, "Next"', SCREEN)

    def test_preview_editor_and_reset(self):
        self.assertIn('preview(id)', SCREEN)
        self.assertIn("selectedSoundId = id;", SCREEN)
        self.assertIn("cfg.eventVolumes.put(id, (float) v);", SCREEN)
        self.assertIn("cfg.eventPitches.put(id, (float) v);", SCREEN)
        self.assertIn("cfg.setEnabled(id, !enabled);", SCREEN)
        self.assertIn("cfg.resetEvent(id);", SCREEN)
        self.assertIn("ShatsuPitchSlider", SCREEN)
        self.assertIn('"Restore this sound\'s defaults"', SCREEN)
        self.assertIn("ShatsuConfig.save();", SCREEN)

    def test_favorites_persist(self):
        self.assertIn("cfg.isFavorite(e.id())", SCREEN)
        self.assertIn("toggleFavorite(id)", SCREEN)
        self.assertIn("public Map<String, Boolean> favoriteSounds = new HashMap<>();", CONFIG)
        self.assertIn("if (result.favoriteSounds == null)", CONFIG)
        self.assertIn("public void toggleFavorite(String id)", CONFIG)
        self.assertIn("save();", CONFIG)

    def test_all_sound_events_supported(self):
        self.assertIn("public static List<Entry> all()", CATALOG)
        self.assertIn('private static final String[] LIBRARY_FILTERS', SCREEN)
        self.assertIn('"All sounds"', SCREEN)
        self.assertIn('if (filter == 0) return true;', SCREEN)

    def test_no_complex_tools_tab(self):
        self.assertIn('{"Home", "Presets", "Volume", "Library"}', SCREEN)
        self.assertNotIn('"Tools"};', SCREEN)


if __name__ == "__main__":
    unittest.main()
