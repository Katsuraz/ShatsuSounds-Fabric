#!/usr/bin/env python3
"""Source-contract tests for inventory hook wiring and focused anti-overlap safeguards.

These are not a substitute for Minecraft runtime tests or asset-level JAR validation.
"""
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/dev/shatsu/sounds"
ROUTER = (JAVA / "client/ShatsuInventoryAudio.java").read_text("utf-8")
PROFILES = (JAVA / "client/ShatsuInventoryProfiles.java").read_text("utf-8")
NORMAL = (JAVA / "mixin/ShatsuInventoryScreenMixin.java").read_text("utf-8")
CREATIVE = (JAVA / "mixin/ShatsuCreativeInventoryScreenMixin.java").read_text("utf-8")
CONFIG = (JAVA / "ShatsuConfig.java").read_text("utf-8")
MIXINS = json.loads((ROOT / "src/main/resources/shatsu-sounds.mixins.json").read_text("utf-8"))


class InventoryRoutingTests(unittest.TestCase):
    def test_hook_routes_normal_and_creative_inventory(self):
        self.assertIn("ShatsuInventoryScreenMixin", MIXINS["client"])
        self.assertIn("ShatsuCreativeInventoryScreenMixin", MIXINS["client"])
        self.assertIn('method = "slotClicked(', NORMAL)
        self.assertIn('method = "slotClicked(', CREATIVE)
        self.assertIn("ShatsuInventoryAudio.clicked(", NORMAL)
        self.assertIn("ShatsuInventoryAudio.clicked(", CREATIVE)

    def test_global_cooldown_replaced_with_signature_guard(self):
        self.assertIn("signature.equals(lastSignature)", ROUTER)
        self.assertIn("18_000_000L", ROUTER)
        self.assertIn("System.identityHashCode(slot)", ROUTER)
        self.assertNotIn("if (now - lastSoundNanos <", ROUTER)

    def test_listenable_diagnostics_and_nonempty_events(self):
        self.assertIn("public static int hookHits()", ROUTER)
        self.assertIn("public static int interactions()", ROUTER)
        self.assertIn("public static int missingEvents()", ROUTER)
        self.assertIn("definition.getWeight() <= 0", ROUTER)
        self.assertIn("manager.play(SimpleSoundInstance.forUI(", ROUTER)

    def test_preserve_approved_apple_mapping_and_targeted_materials(self):
        for source in ('name.equals("enchanted_golden_apple")', 'name.equals("trident")',
                       'name.equals("bone_meal")', 'name.equals("nether_wart")',
                       'name.equals("ghast_tear")', 'name.equals("stonecutter")',
                       'name.endsWith("_concrete_powder")', 'name.contains("_coral")'):
            self.assertIn(source, PROFILES)

    def test_config_switch_and_reference_lookup(self):
        self.assertIn("public boolean inventoryAudioEnabled = true;", CONFIG)
        self.assertIn("if (!ShatsuConfig.current().inventoryAudioEnabled", ROUTER)
        self.assertIn('"/shatsu_inventory_profiles.json"', PROFILES)


if __name__ == "__main__":
    unittest.main()
