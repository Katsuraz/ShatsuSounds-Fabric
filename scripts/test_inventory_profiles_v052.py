#!/usr/bin/env python3
"""Minecraft 26.3 Shatsu inventory-profile regression checks."""
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROFILE = ROOT / 'src/main/resources/shatsu_inventory_profiles.json'

class InventoryProfiles052Tests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.profiles = json.loads(PROFILE.read_text('utf8'))

    def test_vanilla_registry_complete(self):
        self.assertEqual(len(self.profiles), 1658)
        self.assertTrue(all(key.startswith('minecraft:') for key in self.profiles))

    def test_no_misclassified_spawn_eggs(self):
        eggs=[(item,category) for item,category in self.profiles.items() if item.endswith('_spawn_egg')]
        self.assertGreater(len(eggs), 80)
        self.assertTrue(all(category == 'spawn_egg' for item,category in eggs),eggs)

    def test_special_items_have_dedicated_profiles(self):
        wanted={
            'enchanted_golden_apple':'enchanted_apple',
            'golden_apple':'golden_apple', 'golden_carrot':'golden_carrot',
            'chorus_fruit':'chorus', 'sculk_sensor':'sculk_sensor',
            'sculk_shrieker':'sculk_shrieker','sculk_catalyst':'sculk_catalyst',
            'snowball':'snowball', 'ender_pearl':'ender_pearl',
            'lava_bucket':'lava_bucket', 'water_bucket':'water_bucket',
            'enchanted_book':'enchanted_book', 'totem_of_undying':'totem',
            'elytra':'elytra', 'milk_bucket':'milk_bucket',
            'music_disc_lava_chicken':'music_disc', 'music_disc_precipice':'music_disc',
            'chicken_spawn_egg':'spawn_egg', 'silverfish_spawn_egg':'spawn_egg',
            'pufferfish_bucket':'fish_bucket','salmon_bucket':'fish_bucket',
            'leather_chestplate':'armor_leather','chainmail_chestplate':'armor_metal',
            'diamond_chestplate':'armor_diamond', 'waxed_copper_block':'metal',
            'waxed_copper_chest':'metal',
        }
        for name,expected in wanted.items():
            self.assertEqual(self.profiles['minecraft:'+name],expected,name)

if __name__ == '__main__':
    unittest.main()
