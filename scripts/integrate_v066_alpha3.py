#!/usr/bin/env python3
"""Overlay Alpha.3 Audio Health and menu classes on the user's Alpha.2 sound JAR.

Usage:
  python scripts/integrate_v066_alpha3.py <alpha2.jar> <compiled-classes.zip> <alpha3.jar>

This intentionally leaves ALL recorded audio, gameplay listeners, the newer
ShatsuConfig binary and the newer sound catalog unchanged.
"""
import json
import sys
import zipfile
from copy import copy
from pathlib import Path

SCREEN="dev/shatsu/sounds/client/ShatsuScreen.class"
EXTRA={
    "dev/shatsu/sounds/client/ShatsuAudioHealth.class",
    "dev/shatsu/sounds/client/ShatsuAudioHealth$Snapshot.class",
}

def build(base, compiled, output):
    with zipfile.ZipFile(base) as original, zipfile.ZipFile(compiled) as classes:
        assert original.testzip() is None and classes.testzip() is None
        old_names=set(original.namelist())
        assert SCREEN in old_names and EXTRA.isdisjoint(old_names)
        for name in {SCREEN} | EXTRA:
            data=classes.read(name)
            assert data[:4]==b"\xca\xfe\xba\xbe"
            assert int.from_bytes(data[6:8],"big")==69, "Expected Java 25"
        metadata=json.loads(original.read("fabric.mod.json"))
        assert metadata["version"]=="0.6.6-alpha.2"
        metadata["version"]="0.6.6-alpha.3"
        metadata["name"]="Shatsu Sounds 0.6.6 Alpha.3 — Audio Health"
        with zipfile.ZipFile(output,"w") as result:
            for info in original.infolist():
                if info.filename==SCREEN: blob=classes.read(SCREEN)
                elif info.filename=="fabric.mod.json":
                    blob=json.dumps(metadata,indent=2).encode("utf-8")
                else: blob=original.read(info.filename)
                result.writestr(copy(info),blob)
            for name in sorted(EXTRA):
                result.writestr(name,classes.read(name),compress_type=zipfile.ZIP_DEFLATED)
    with zipfile.ZipFile(base) as original, zipfile.ZipFile(output) as result:
        assert result.testzip() is None
        assert set(result.namelist())-set(original.namelist())==EXTRA
        for info in original.infolist():
            if info.filename in {SCREEN,"fabric.mod.json"}:continue
            assert original.read(info.filename)==result.read(info.filename),info.filename
    print("Built validated Shatsu Alpha.3:",output)

if __name__=="__main__":
    if len(sys.argv)!=4: raise SystemExit(__doc__)
    build(*(Path(x) for x in sys.argv[1:]))
