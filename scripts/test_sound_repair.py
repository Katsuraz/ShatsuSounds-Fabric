#!/usr/bin/env python3
"""Integration checks for Shatsu's generated audio assets (not gameplay tests)."""
import json
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'build/generated/shatsu-assets/assets'


class ShatsuAudioRepairTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.minecraft = json.loads((ASSETS / 'minecraft/sounds.json').read_text('utf-8'))
        cls.shatsu4 = json.loads((ASSETS / 'shatsu4/sounds.json').read_text('utf-8'))
        cls.clangor = json.loads((ASSETS / 'clangor/sounds.json').read_text('utf-8'))
        cls.blockmap = (ASSETS / 'customblocksounds/config/blockmap.json').read_text('utf-8')
        cls.catalog = json.loads((ROOT / 'build/generated/shatsu-assets/shatsu_catalog.json').read_text('utf-8'))

    def test_mining_hit_samples_exist(self):
        for event in ('block.stone.hit', 'block.deepslate.hit', 'block.tuff.hit',
                      'block.wood.hit', 'block.gravel.hit', 'block.glass.hit',
                      'block.sand.hit'):
            with self.subTest(event=event):
                for item in self.minecraft[event]['sounds']:
                    namespace, filename = item['name'].split(':', 1)
                    path = ASSETS / namespace / 'sounds' / (filename + '.ogg')
                    self.assertTrue(path.is_file(), str(path))
                    self.assertGreater(path.stat().st_size, 200)

    def test_obsidian_uses_custom_block_hit(self):
        self.assertIn('block.obsidian.hit', self.shatsu4)
        sample = self.shatsu4['block.obsidian.hit']['sounds'][0]['name']
        self.assertIn('shatsuv7:block/obsidian/hit_', sample)

    def test_duplicate_block_keys_are_not_lost(self):
        # Duplicate keys are deliberately supported by Custom Block Sounds.
        self.assertEqual(self.blockmap.count('type=hit'), 1047)
        for name in ('stone', 'stone_bricks', 'andesite', 'deepslate', 'glass'):
            matches = re.findall(rf'"minecraft:{name}":\s*"([^"]+)"', self.blockmap)
            types = {re.search(r'type=(\w+)', item).group(1) for item in matches}
            self.assertEqual(types, {'hit', 'break', 'place', 'step', 'fall'})

    def test_stone_block_hit_gain_not_muffled(self):
        for name in ('stone', 'stone_bricks', 'deepslate', 'andesite'):
            match = re.search(rf'"minecraft:{name}":\s*"([^"]*type=hit[^"]*)"', self.blockmap)
            self.assertIsNotNone(match)
            self.assertIn('volume=0.93', match.group(1))

    def test_non_special_hits_match_empty_hand(self):
        for kind in ('weak', 'strong', 'crit', 'knockback', 'sweep'):
            for category in ('hit.weapon_n_tool.general.', 'hit.rods.'):
                self.assertEqual(self.clangor[category + kind],
                                 self.clangor['hit.empty_hand.' + kind])

    def test_custom_weapon_hits_survive(self):
        for kind in ('mace', 'spear', 'weapon_n_tool.sword',
                     'weapon_n_tool.axe', 'weapon_n_tool.pickaxe',
                     'weapon_n_tool.hoe', 'weapon_n_tool.shovel'):
            self.assertIn('hit.' + kind + '.strong', self.clangor)


if __name__ == '__main__':
    unittest.main()
