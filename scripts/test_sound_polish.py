#!/usr/bin/env python3
"""Generated audio/regression tests (run after Gradle prepareShatsuAssets)."""
import json
from pathlib import Path
import re
import unittest

BASE = Path(__file__).resolve().parents[1]
ROOT = BASE / "build/generated/shatsu-assets"
ASSETS = ROOT / "assets"

class SoundPolishTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.minecraft = json.loads((ASSETS / "minecraft/sounds.json").read_text("utf8"))
        cls.custom = json.loads((ASSETS / "shatsu4/sounds.json").read_text("utf8"))
        cls.clangor = json.loads((ASSETS / "clangor/sounds.json").read_text("utf8"))
        cls.shield = json.loads((ASSETS / "shatsupolish/sounds.json").read_text("utf8"))
        cls.blockmap = (ASSETS / "customblocksounds/config/blockmap.json").read_text("utf8")

    def test_all_custom_audio_is_present(self):
        checked = 0
        for namespace, eventset in [
            ("minecraft", self.minecraft),
            ("shatsu4", self.custom),
            ("clangor", self.clangor),
            ("shatsupolish", self.shield),
        ]:
            for name, definition in eventset.items():
                for sample in definition.get("sounds", []):
                    if isinstance(sample, dict):
                        name_ref = sample.get("name", "")
                    else:
                        name_ref = sample
                    if not name_ref.startswith("shatsupolish:"):
                        continue
                    path = ASSETS / "shatsupolish/sounds" / (name_ref.split(":", 1)[1] + ".ogg")
                    self.assertTrue(path.is_file(), f"{namespace}:{name} missing {path}")
                    self.assertGreater(path.stat().st_size, 300)
                    checked += 1
        self.assertGreater(checked, 100)

    def test_magical_events_and_shield(self):
        for key in ("entity.experience_orb.pickup", "item.totem.use"):
            sounds = self.minecraft[key]["sounds"]
            self.assertEqual(len(sounds), 3)
            self.assertTrue(all(x["name"].startswith("shatsupolish:magic/") for x in sounds))
        self.assertIn("shield.raise", self.shield)
        self.assertEqual(len(self.shield["shield.raise"]["sounds"]), 2)

    def test_weapon_refresh_without_mace_damage(self):
        self.assertTrue(all(x["name"].startswith("shatsupolish:weapons/")
                            for x in self.minecraft["entity.arrow.shoot"]["sounds"]))
        self.assertTrue(all(x["name"].startswith("shatsupolish:weapons/")
                            for x in self.minecraft["item.trident.throw"]["sounds"]))
        self.assertTrue(all(x["name"].startswith("shatsupolish:weapons/")
                            for x in self.clangor["use.bow.charge"]["sounds"]))
        self.assertTrue(any(x.startswith("hit.mace.") for x in self.clangor))

    def test_wood_break_family_not_old_bang(self):
        self.assertTrue(all(x["name"].startswith("shatsupolish:block/wood_break/")
                            for x in self.minecraft["block.wood.break"]["sounds"]))
        self.assertTrue(all(x["name"].startswith("shatsupolish:block/wood_break/")
                            for x in self.custom["block.oak_planks.break"]["sounds"]))

    def test_place_rules_preserve_all_five_actions(self):
        self.assertEqual(self.blockmap.count("type=place"), 1047)
        self.assertEqual(self.blockmap.count("type=hit"), 1047)
        for block in ("stone", "stone_bricks", "oak_planks", "spruce_planks", "glass"):
            matches = re.findall(rf'"minecraft:{block}":\s*"([^"]+)"', self.blockmap)
            types = {re.search(r"type=(\w+)", entry).group(1) for entry in matches}
            self.assertEqual(types, {"hit", "break", "place", "step", "fall"})
            place = next(v for v in matches if "type=place" in v)
            self.assertIn("volume=0.77", place)
        for name, event in self.minecraft.items():
            if name.startswith("block.") and name.endswith(".place"):
                self.assertTrue(event.get("replace", False), name)
                self.assertLessEqual(len(event.get("sounds", [])), 2, name)

    def test_required_dependencies(self):
        data = json.loads((BASE / "src/main/resources/fabric.mod.json").read_text("utf8"))
        for mod in ("clangor", "sound", "customblocksounds", "fabric-api"):
            self.assertIn(mod, data["depends"])
        self.assertNotIn("modmenu", data["depends"])
        self.assertNotIn("sound_physics_remastered", data["depends"])

if __name__ == "__main__":
    unittest.main()
