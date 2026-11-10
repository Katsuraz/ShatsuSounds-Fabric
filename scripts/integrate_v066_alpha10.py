#!/usr/bin/env python3
"""Integrate Shatsu Alpha.10 final interaction foley onto cumulative Alpha.9.

Usage:
  python3 scripts/integrate_v066_alpha10.py <alpha9.jar> <alpha10-resourcepack.zip> <alpha10.jar>

This *only* patches 33 Minecraft vanilla use-event sound definitions and appends
the resource-pack's 99 new OGGs; all prior classes, Clangor mappings, accepted
audio, icon, saved configuration classes and other original assets stay untouched.

The independent sound designer/generator is bundled in the full Alpha.10 QA ZIP.
"""
import copy
import json
import sys
from zipfile import ZipFile, ZIP_DEFLATED

SOUNDS = "assets/minecraft/sounds.json"
META = "fabric.mod.json"
EXPECTED_EVENTS = 33
EXPECTED_CLIPS = 99

def integrate(base_path, pack_path, out_path):
    with ZipFile(base_path) as base, ZipFile(pack_path) as patch:
        assert base.testzip() is None and patch.testzip() is None
        old_names = set(base.namelist())
        part = json.loads(patch.read(SOUNDS))
        assert len(part) == EXPECTED_EVENTS, len(part)
        sounds = json.loads(base.read(SOUNDS))
        for event, definition in part.items():
            assert event in sounds, "Not an existing vanilla event: " + event
            assert event.startswith(("item.", "block.")), event
            assert definition.get("replace") is True, event
            assert len(definition.get("sounds", [])) == 3, event
            sounds[event] = definition
        audio = {n: patch.read(n) for n in patch.namelist()
                 if n.startswith("assets/shatsufresh/sounds/v066_final_use/")
                 and n.endswith(".ogg")}
        assert len(audio) == EXPECTED_CLIPS, len(audio)
        assert not set(audio).intersection(old_names)
        refs = set()
        for evt in part.values():
            for sound in evt["sounds"]:
                assert sound.get("type") == "file"
                ns, path = sound["name"].split(":", 1)
                refs.add("assets/" + ns + "/sounds/" + path + ".ogg")
        assert refs == set(audio), (len(refs), len(audio))
        assert all(blob.startswith(b"OggS") for blob in audio.values())
        metadata = json.loads(base.read(META))
        assert metadata["version"] == "0.6.6-alpha.9", metadata["version"]
        metadata["version"] = "0.6.6-alpha.10"
        metadata["name"] = "Shatsu Sounds v0.6.6 Alpha.10 — Final Physical Interaction Polish"
        metadata["description"] = ("Cumulative Shatsu audio plus refined hoe, shovel, "
                                   "axe, brush, composter and other physical use interactions")
        with ZipFile(out_path, "w") as target:
            for info in base.infolist():
                if info.filename == META:
                    data = (json.dumps(metadata, indent=2) + "\n").encode()
                elif info.filename == SOUNDS:
                    data = (json.dumps(sounds, indent=2) + "\n").encode()
                else:
                    data = base.read(info.filename)
                target.writestr(copy.copy(info), data)
            for filename, data in sorted(audio.items()):
                target.writestr(filename, data, compress_type=ZIP_DEFLATED)
    with ZipFile(base_path) as base, ZipFile(out_path) as new:
        assert new.testzip() is None
        assert set(new.namelist()) - set(base.namelist()) == set(audio)
        for info in base.infolist():
            if info.filename not in (SOUNDS, META):
                assert base.read(info.filename) == new.read(info.filename), info.filename
        old_sounds = json.loads(base.read(SOUNDS))
        new_sounds = json.loads(new.read(SOUNDS))
        assert {k for k in old_sounds if old_sounds[k] != new_sounds[k]} == set(part)
        assert base.read("assets/clangor/sounds.json") == new.read("assets/clangor/sounds.json")
    print("Shatsu Alpha.10 verified:", EXPECTED_EVENTS, "events,",
          EXPECTED_CLIPS, "new samples; all old classes and audio preserved")

if __name__ == "__main__":
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    integrate(*sys.argv[1:])
