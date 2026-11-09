#!/usr/bin/env python3
"""Fresh per-material placement recordings, synthesized at build time.

The old v7 place samples are removed from BOTH vanilla and Custom Block Sounds
event definitions, avoiding intermittent old variations. No dependency config
or third-party resource file is overwritten.
"""
import json
import math
import random
import re
import struct
import subprocess
import tempfile
import wave
from pathlib import Path

RATE = 44100
TAU = 2 * math.pi
FAMILY_RX = re.compile(r"shatsuv7:block/([^/]+)/place_")
CHARACTERS = {
    "wood": (260, 0.09, 0.19), "bamboo": (390, 0.06, 0.15),
    "stone": (275, 0.14, 0.17), "deepslate": (210, 0.15, 0.22),
    "obsidian": (190, 0.05, 0.23), "metal": (550, 0.15, 0.28),
    "copper": (480, 0.12, 0.22), "anvil": (350, 0.11, 0.26),
    "glass": (970, 0.15, 0.24), "ice": (820, 0.11, 0.20),
    "crystal": (1160, 0.12, 0.24), "ceramic": (680, 0.10, 0.19),
    "dirt": (130, 0.20, 0.16), "grass": (160, 0.21, 0.16),
    "gravel": (145, 0.23, 0.15), "sand": (170, 0.22, 0.15),
    "snow": (140, 0.14, 0.16), "wool": (180, 0.045, 0.11),
    "sponge": (150, 0.045, 0.14), "slime": (175, 0.09, 0.18),
    "leaves": (240, 0.21, 0.12), "plant": (300, 0.16, 0.12),
    "paper": (450, 0.17, 0.13), "bone": (710, 0.13, 0.17),
    "sculk": (205, 0.10, 0.24), "end": (720, 0.09, 0.22),
    "nether": (270, 0.18, 0.21), "mud": (110, 0.18, 0.17),
    "clay": (230, 0.10, 0.18), "redstone": (430, 0.11, 0.19),
    "water": (210, 0.17, 0.18), "chest": (325, 0.10, 0.17),
    "generic": (260, 0.13, 0.16),
}

def synth(root, family, variant):
    base, grit, tone = CHARACTERS[family]
    rng = random.Random(f"{family}-{variant}-Shatsu-2026")
    length = 0.24 if family not in ("metal", "copper", "anvil", "glass", "crystal") else 0.29
    buf = []
    lp = 0.0
    hp = 0.0
    for i in range(int(RATE * length)):
        t = i / RATE
        noise = rng.uniform(-1.0, 1.0)
        lp = .91 * lp + .09 * noise
        hp = .65 * hp + .35 * noise
        texture = hp - lp
        attack = min(1.0, t * 220)
        fall = math.exp(-t * (20 + (variant % 2) * 4))
        body = math.sin(TAU * base * t) * .74 + math.sin(TAU * base * 2.43 * t) * .26
        s = attack * fall * (tone * body + grit * texture)
        # Extremely quiet material-specific terminal texture; no boomy tail.
        if family in ("gravel", "sand", "dirt", "mud", "leaves", "plant"):
            dt = t - .060
            if dt > 0:
                s += .07 * math.exp(-dt * 29) * texture
        if family in ("metal", "copper", "anvil", "glass", "crystal"):
            s += .055 * math.exp(-t * 16) * math.sin(TAU * base * 3.09 * t)
        if family in ("wool", "snow", "sponge"):
            s *= .57
        s *= min(1.0, t / .003)
        buf.append(max(-.62, min(.62, s)))
    rel = f"block/place/{family}/{variant}"
    ogg = root / f"assets/shatsupolish/sounds/{rel}.ogg"
    ogg.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as temp:
        wavfile = Path(temp) / "source.wav"
        with wave.open(str(wavfile), "wb") as wav:
            wav.setnchannels(1)
            wav.setsampwidth(2)
            wav.setframerate(RATE)
            wav.writeframes(b"".join(struct.pack("<h", int(s * 32767)) for s in buf))
        subprocess.run(["ffmpeg", "-nostdin", "-loglevel", "error", "-y",
                        "-i", str(wavfile), "-c:a", "libvorbis", "-q:a", "4",
                        str(ogg)], check=True)

def family_of(definition):
    for item in definition.get("sounds", []):
        name = item.get("name", "") if isinstance(item, dict) else item
        found = FAMILY_RX.search(name)
        if found:
            return found.group(1)
    return None

def replace_placement(root):
    root = Path(root)
    staged = {}
    for namespace in ("minecraft", "shatsu4"):
        path = root / f"assets/{namespace}/sounds.json"
        data = json.loads(path.read_text(encoding="utf8"))
        changed = 0
        for event, obj in data.items():
            if not (event.startswith("block.") and event.endswith(".place")):
                continue
            fam = family_of(obj)
            if fam is None:
                # Prior polish script truncated to first two original samples,
                # but every mapped material still identifies itself in the name.
                raise RuntimeError(f"No material family for {namespace}:{event}")
            if fam not in CHARACTERS:
                raise RuntimeError(f"Unknown material family {fam} for {event}")
            old = obj.get("sounds", [])
            pitch = old[0].get("pitch", 1.0) if old and isinstance(old[0], dict) else 1.0
            obj["replace"] = True
            obj["sounds"] = [
                {"name": f"shatsupolish:block/place/{fam}/{i}",
                 "type": "file", "volume": .82, "pitch": pitch}
                for i in (1, 2)
            ]
            staged[fam] = True
            changed += 1
        path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf8")
        if changed < (40 if namespace == "minecraft" else 900):
            raise RuntimeError(f"Too few placement events in {namespace}: {changed}")
        print(f"Shatsu fresh placement: {namespace} {changed} mapped events")
    for fam in sorted(staged):
        for i in (1, 2):
            synth(root, fam, i)
    print(f"Shatsu fresh placement: {len(staged)} material families, {2 * len(staged)} mono samples")

if __name__ == "__main__":
    import sys
    replace_placement(sys.argv[1])
