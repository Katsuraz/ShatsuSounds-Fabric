#!/usr/bin/env python3
"""Regression coverage for the legacy missing-bow-OGG problem."""
import json
import unittest
from pathlib import Path

ASSETS = Path(__file__).resolve().parents[1] / 'build/generated/shatsu-assets/assets'


class NamespacedAudioTests(unittest.TestCase):
    def test_bow_audio_references_resolve_to_shatsu(self):
        checks = {
            'shatsu9': ('bow_release', 'shatsu9:bow_shoot/1'),
            'shatsu10': ('crossbow_release', 'shatsu10:crossbow_shoot/1'),
            'shatsu11': ('bow_draw', 'shatsu11:bow_draw/1'),
            'shatsu12': ('loyalty_quick', 'shatsu12:loyalty_quick/1'),
        }
        for namespace, (event, wanted) in checks.items():
            data = json.loads((ASSETS / namespace / 'sounds.json').read_text('utf-8'))
            first = data[event]['sounds'][0]
            actual = first['name'] if isinstance(first, dict) else first
            self.assertEqual(actual, wanted, (namespace, event))
            self.assertTrue((ASSETS / namespace / 'sounds' / (actual.split(':', 1)[1] + '.ogg')).is_file())

    def test_existing_local_audio_is_never_left_unqualified(self):
        for sounds_json in sorted(ASSETS.glob('*/sounds.json')):
            ns = sounds_json.parent.name
            entries = json.loads(sounds_json.read_text('utf-8'))
            for event, value in entries.items():
                for item in value.get('sounds', []):
                    if isinstance(item, dict) and item.get('type', 'file') != 'file':
                        continue
                    name = item.get('name', '') if isinstance(item, dict) else item
                    if not isinstance(name, str) or ':' in name:
                        continue
                    local = ASSETS / ns / 'sounds' / (name + '.ogg')
                    self.assertFalse(local.is_file(), (ns, event, name))


if __name__ == '__main__':
    unittest.main()
