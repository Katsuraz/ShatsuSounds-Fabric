#!/usr/bin/env python3
"""Rebuild Shatsu Clangor 2.1.4 folder-gating fix from an existing audio JAR.

Usage:
    python scripts/repair_clangor_resource_paths.py <alpha6.jar> <alpha7.jar> <optional-pack.zip>

Clangor's ClangorUtils.isFolderNotEmpty checks raw OGG files in
sounds/{event.name.replace('.', '/')}/, NOT just registered sounds.json events.
Do not redistribute or modify the proprietary Clangor mod.
"""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import copy
import json
import sys

EVENTS = "assets/clangor/sounds.json"
META = "fabric.mod.json"


def repair(source_path, jar_path, pack_path):
    with ZipFile(source_path) as src:
        assert src.testzip() is None
        original_names = set(src.namelist())
        original = json.loads(src.read(EVENTS))
        assert len(original) == 142, len(original)
        rewritten = {}
        additions = {}
        for event, definition in original.items():
            assert all(p.replace("_", "").isalnum() for p in event.split("."))
            prefix = event.replace(".", "/")
            updated = copy.deepcopy(definition)
            for i, sound in enumerate(updated["sounds"], 1):
                name = sound["name"] if isinstance(sound, dict) else sound
                ns, path = name.split(":", 1)
                old = f"assets/{ns}/sounds/{path}.ogg"
                assert old in original_names, (event, old)
                leaf = f"{prefix}/shatsu_fix_{i:02}"
                new = f"assets/clangor/sounds/{leaf}.ogg"
                assert new not in original_names and new not in additions
                audio = src.read(old)
                assert audio.startswith(b"OggS")
                additions[new] = audio
                if isinstance(sound, dict):
                    sound["name"] = f"clangor:{leaf}"
                else:
                    updated["sounds"][i - 1] = f"clangor:{leaf}"
            rewritten[event] = updated
        assert len(additions) == 319, len(additions)
        data = (json.dumps(rewritten, indent=2, ensure_ascii=False) + "\n").encode()
        metadata = json.loads(src.read(META))
        assert metadata["version"] == "0.6.6-alpha.6"
        metadata["version"] = "0.6.6-alpha.7"
        metadata["name"] = "Shatsu Sounds v0.6.6 Alpha.7 — Clangor Path Compatibility"
        meta_bytes = (json.dumps(metadata, indent=2, ensure_ascii=False) + "\n").encode()

        with ZipFile(jar_path, "w") as target:
            for member in src.infolist():
                if member.filename == EVENTS:
                    blob = data
                elif member.filename == META:
                    blob = meta_bytes
                else:
                    blob = src.read(member.filename)
                target.writestr(copy.copy(member), blob)
            for path, audio in sorted(additions.items()):
                target.writestr(path, audio, compress_type=ZIP_DEFLATED)

        pack_info = {"pack": {
            "description": "Shatsu Clangor 2.1.4 folder routing fix — Minecraft 26.3",
            "min_format": [97, 1], "max_format": [97, 1]
        }}
        with ZipFile(pack_path, "w", compression=ZIP_DEFLATED) as pack:
            pack.writestr("pack.mcmeta", json.dumps(pack_info, indent=2))
            pack.writestr(EVENTS, data)
            for path, audio in sorted(additions.items()):
                pack.writestr(path, audio)

        with ZipFile(jar_path) as result, ZipFile(pack_path) as pack:
            assert result.testzip() is None and pack.testzip() is None
            assert set(result.namelist()) - original_names == set(additions)
            for name in original_names - {META, EVENTS}:
                assert src.read(name) == result.read(name), name
            for event, definition in rewritten.items():
                expected = f"assets/clangor/sounds/{event.replace('.', '/')}/"
                for sound in definition["sounds"]:
                    name = sound["name"] if isinstance(sound, dict) else sound
                    namespace, path = name.split(":", 1)
                    assert namespace == "clangor"
                    sample = f"assets/clangor/sounds/{path}.ogg"
                    assert sample.startswith(expected) and sample in additions
                    assert pack.read(sample) == additions[sample]
        print("Verified:", len(rewritten), "Clangor events;", len(additions), "OGG copies")


if __name__ == "__main__":
    if len(sys.argv) != 4:
        raise SystemExit(__doc__)
    repair(*(Path(arg) for arg in sys.argv[1:]))
