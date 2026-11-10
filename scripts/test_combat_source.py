#!/usr/bin/env python3
"""Source contract tests for combat charging and client throwable impact routing.

These tests do not replace an in-game Minecraft sound or Mixin playtest.
"""
import json
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"src/main/java/dev/shatsu/sounds"
COMBAT=(JAVA/"client/ShatsuCombatChargeFeedback.java").read_text("utf-8")
ENTRY=(JAVA/"client/ShatsuCombatClient.java").read_text("utf-8")
IMPACT=(JAVA/"client/ShatsuThrowableImpactAudio.java").read_text("utf-8")
SNOWBALL=(JAVA/"mixin/ShatsuSnowballImpactMixin.java").read_text("utf-8")
EGG=(JAVA/"mixin/ShatsuEggImpactMixin.java").read_text("utf-8")
MIXINS=json.loads((ROOT/"src/main/resources/shatsu-sounds.mixins.json").read_text("utf-8"))

class CombatSourceTests(unittest.TestCase):
    def test_bow_draw_stages(self):
        self.assertIn("combat060.bow.nock",COMBAT)
        self.assertIn("combat060.bow.draw",COMBAT)
        self.assertIn("bowTicks >= 6",COMBAT)
        self.assertIn("!bowTensionPlayed",COMBAT)
        self.assertIn("Items.BOW",COMBAT)

    def test_trident_charge_independent_of_optional_mods(self):
        self.assertIn("combat060.trident.charge_start",COMBAT)
        self.assertIn("combat060.trident.charge_ready",COMBAT)
        self.assertIn("tridentTicks >= 9",COMBAT)
        self.assertIn("!tridentReadyPlayed",COMBAT)
        self.assertIn("Items.TRIDENT",COMBAT)
        self.assertIn("ClientTickEvents.END_CLIENT_TICK.register",COMBAT)

    def test_separate_entrypoint_preserves_old_listeners(self):
        self.assertIn("implements ClientModInitializer",ENTRY)
        self.assertIn("ShatsuCombatChargeFeedback.register()",ENTRY)
        self.assertNotIn("ShatsuArrowFlyby.register()",ENTRY)

    def test_throwable_client_events_are_scoped(self):
        self.assertIn("ShatsuSnowballImpactMixin",MIXINS["client"])
        self.assertIn("ShatsuEggImpactMixin",MIXINS["client"])
        self.assertIn('handleEntityEvent(B)V',SNOWBALL)
        self.assertIn('handleEntityEvent(B)V',EGG)
        self.assertIn("if (event == 3)",SNOWBALL)
        self.assertIn("if (event == 3)",EGG)
        self.assertIn("shatsufresh:combat060.throw.snowball_impact",SNOWBALL)
        self.assertIn("shatsufresh:combat060.throw.egg_impact",EGG)
        self.assertIn("projectile.level() != client.level",IMPACT)

if __name__=="__main__":
    unittest.main()
