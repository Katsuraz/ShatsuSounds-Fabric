#!/usr/bin/env python3
"""Integrate the simple Mod Menu from a Java-25 compile-only class archive.

Usage:
  python3 scripts/integrate_v065_ui.py <v0.6.4-audio.jar> <compiled-classes.zip> <output.jar>

This script intentionally DOES NOT replace ShatsuConfig, gameplay hooks, sound
assets or the sound catalog. All of these belong to the newer v0.6.4 baseline.
"""
import json
import sys
import zipfile
from pathlib import Path

UI = (
    "dev/shatsu/sounds/client/ShatsuScreen.class",
    "dev/shatsu/sounds/client/ShatsuScreen$ShatsuLevelSlider.class",
    "dev/shatsu/sounds/client/ShatsuScreen$ShatsuPitchSlider.class",
)

def integrate(original, compiled, output):
    original, compiled, output = Path(original), Path(compiled), Path(output)
    with zipfile.ZipFile(original) as older, zipfile.ZipFile(compiled) as classes:
        assert older.testzip() is None and classes.testzip() is None
        present = set(older.namelist())
        assert all(name in classes.namelist() for name in UI)
        for name in UI:
            data = classes.read(name)
            assert data[:4] == bytes.fromhex("cafebabe")
            assert int.from_bytes(data[6:8], "big") == 69, "Java 25 required"
        oldconfig = older.read("dev/shatsu/sounds/ShatsuConfig.class")
        oldcatalog = older.read("shatsu_catalog.json")
        metadata = json.loads(older.read("fabric.mod.json"))
        assert metadata["id"] == "shatsusounds"
        assert metadata["depends"]["minecraft"] == "~26.3"
        metadata["version"] = "0.6.5-alpha.1"
        metadata["name"] = "Shatsu Sounds v0.6.5 (UI Integration & QA Alpha)"
        metadata["description"] = "Cumulative Shatsu v0.6.4 sounds plus simplified in-game menu"
        with zipfile.ZipFile(output, "w") as out:
            for item in older.infolist():
                if item.filename in UI:
                    out.writestr(item, classes.read(item.filename))
                elif item.filename == "fabric.mod.json":
                    out.writestr(item, (json.dumps(metadata, indent=2) + "\n").encode())
                else:
                    out.writestr(item, older.read(item.filename))
            if UI[-1] not in present:
                out.writestr(UI[-1], classes.read(UI[-1]),
                             compress_type=zipfile.ZIP_DEFLATED)
    with zipfile.ZipFile(output) as done, zipfile.ZipFile(original) as older:
        assert done.testzip() is None
        assert done.read("dev/shatsu/sounds/ShatsuConfig.class") == oldconfig
        assert done.read("shatsu_catalog.json") == oldcatalog
        for name in older.namelist():
            if name not in UI and name != "fabric.mod.json":
                assert older.getinfo(name).CRC == done.getinfo(name).CRC, name
        print("Integrated UI verified:", output)

if __name__ == "__main__":
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    integrate(*sys.argv[1:])
