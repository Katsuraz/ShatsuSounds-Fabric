#!/usr/bin/env python3
"""Make an optional high-priority Clangor swing resource pack from a Shatsu JAR.

Usage: python scripts/build_clangor_swing_pack.py <shatsu_v066_jar> <output.zip>
Does not modify the source mod JAR or any original OGG bytes.
"""
import json
import sys
import zipfile
from pathlib import Path

def main(jar_path, output_path):
    with zipfile.ZipFile(jar_path) as source:
        all_events = json.loads(source.read("assets/clangor/sounds.json"))
        swings = {key:value for key,value in all_events.items() if key.startswith("swing.")}
        if len(swings) != 45:
            raise ValueError(f"Expected 45 Clangor swing event families; got {len(swings)}")
        sample_paths = set()
        for key, definition in swings.items():
            assert definition.get("replace") is True and definition.get("sounds"), key
            for sample in definition["sounds"]:
                name = sample["name"] if isinstance(sample, dict) else sample
                ns, rest = name.split(":", 1)
                if ".." in rest or "\\" in rest:
                    raise ValueError(name)
                filename = f"assets/{ns}/sounds/{rest}.ogg"
                if filename not in source.namelist():
                    raise FileNotFoundError(filename)
                sample_paths.add(filename)
        metadata = {"pack": {
            "description": {"text": "Shatsu v0.6.6 — Clangor swing audio (Minecraft 26.3)"},
            "min_format": [97, 1], "max_format": [97, 1]
        }}
        with zipfile.ZipFile(output_path, "w", compression=zipfile.ZIP_DEFLATED) as out:
            out.writestr("pack.mcmeta", json.dumps(metadata, indent=2))
            out.writestr("assets/clangor/sounds.json", json.dumps(swings, indent=2))
            for asset in sorted(sample_paths):
                out.writestr(asset, source.read(asset))
    with zipfile.ZipFile(output_path) as final:
        assert final.testzip() is None
    print(f"Created 26.3 pack: {len(swings)} swing events, {len(sample_paths)} OGG files")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise SystemExit(__doc__)
    main(Path(sys.argv[1]), Path(sys.argv[2]))
