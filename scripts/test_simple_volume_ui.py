#!/usr/bin/env python3
"""Source-level checks for the simplified Shatsu volume page.

These checks validate navigation/configuration wiring, not Minecraft playback.
"""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCREEN = (ROOT / "src/main/java/dev/shatsu/sounds/client/ShatsuScreen.java").read_text("utf-8")
CONFIG = (ROOT / "src/main/java/dev/shatsu/sounds/ShatsuConfig.java").read_text("utf-8")


def java_string_array(text, expression):
    match = re.search(expression + r"\s*=\s*\{(.*?)\};", text, re.S)
    if not match:
        raise AssertionError(f"Missing Java array: {expression}")
    return match.group(1)


class SimpleVolumePageTests(unittest.TestCase):
    def test_every_original_category_is_exposed_once(self):
        original = re.findall(r'"([^"]+)"',
            java_string_array(CONFIG, r"String\[\] CATEGORIES"))
        groups = java_string_array(SCREEN, r"String\[\]\[\] VOLUME_GROUP_KEYS")
        grouped = []
        for group in re.findall(r"\{([^{}]+)\}", groups):
            grouped.extend(re.findall(r'"([^"]+)"', group))
        self.assertEqual(set(original), set(grouped))
        self.assertEqual(len(grouped), len(set(grouped)))

    def test_friendly_labels_and_master(self):
        names = re.findall(r'"([^"]+)"',
            java_string_array(SCREEN, r"String\[\] VOLUME_GROUPS"))
        self.assertEqual(len(names), 9)
        self.assertTrue(all("_" not in name for name in names))
        self.assertIn('"Master volume"', SCREEN)
        self.assertIn("volumeGroupMixed(cfg, keys)", SCREEN)

    def test_edits_can_be_saved_and_reset(self):
        self.assertIn("cfg.categoryVolumes.put(key, newValue)", SCREEN)
        self.assertIn("cfg.categoryVolumes.remove(key)", SCREEN)
        self.assertIn('cfg.preset = "Custom";', SCREEN)
        self.assertIn("if ((tab == 2 || tab == 3) && selected != tab) ShatsuConfig.save();", SCREEN)
        self.assertIn('action(x, footer, 86, 19, "Previous"', SCREEN)
        self.assertNotIn('"Category page "', SCREEN)


if __name__ == "__main__":
    unittest.main()
