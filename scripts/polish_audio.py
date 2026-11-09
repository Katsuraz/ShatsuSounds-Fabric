#!/usr/bin/env python3
"""Reversible Shatsu 0.2.2 sound polish applied to an extracted v7.3 pack.

Only generated resources are changed. The original uploaded ZIP stays unmodified.
All new sound samples are procedurally synthesized here, without external audio.
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

def render(root, rel, kind, seed, length):
    rng = random.Random(seed)
    count = int(RATE * length)
    samples = []
    noise_lp = 0.0
    noise_fast = 0.0
    for i in range(count):
        t = i / RATE
        raw = rng.uniform(-1.0, 1.0)
        noise_lp = 0.90 * noise_lp + 0.10 * raw
        noise_fast = 0.55 * noise_fast + 0.45 * raw
        scratch = noise_fast - noise_lp
        s = 0.0
        if kind == "wood":
            # Short fibrous snap with a quiet wooden body, not a metallic bang.
            for start, amp, decay in ((0.008, 0.26, 30), (0.058, 0.15, 37), (0.104, 0.075, 36)):
                dt = t - start
                if dt >= 0:
                    e = math.exp(-dt * decay)
                    s += amp * e * (0.69 * scratch + 0.31 * math.sin(TAU * (170 + seed % 45) * dt))
            s += 0.055 * math.exp(-t * 13) * math.sin(TAU * 240 * t)
        elif kind == "xp":
            for offset, freq, amp in ((0.0, 960, 0.24), (0.086, 1220, 0.20), (0.170, 1570, 0.17)):
                dt = t - offset
                if dt >= 0:
                    e = min(1, dt * 150) * math.exp(-dt * 13)
                    s += amp * e * (math.sin(TAU * freq * dt)
                                      + 0.19 * math.sin(TAU * freq * 2.01 * dt))
            s += 0.018 * math.exp(-t * 9) * scratch
        elif kind == "totem":
            swell = min(1, t * 9) * math.exp(-t * 1.85)
            shimmer = 1 + 0.06 * math.sin(TAU * 4.3 * t)
            s += 0.24 * swell * shimmer * (
                math.sin(TAU * 523.25 * t) * 0.36 +
                math.sin(TAU * 659.25 * t) * 0.29 +
                math.sin(TAU * 783.99 * t) * 0.24 +
                math.sin(TAU * 1046.50 * t) * 0.11)
            for offset, freq in ((0.12, 1318.51), (0.31, 1567.98), (0.52, 2093.0), (0.74, 2637.0)):
                dt = t - offset
                if dt >= 0:
                    s += 0.105 * math.exp(-dt * 8.3) * (math.sin(TAU * freq * dt) +
                                                         0.17 * math.sin(TAU * freq * 2.005 * dt))
            s += 0.012 * math.exp(-t * 2.6) * scratch
        elif kind == "bow":
            # Taut string release, with a sharp but short broadband transient.
            s += 0.31 * math.exp(-t * 27) * scratch
            s += 0.20 * math.exp(-t * 19) * math.sin(TAU * (380 - 140 * t) * t)
            s += 0.10 * math.exp(-t * 34) * math.sin(TAU * 870 * t)
        elif kind == "bow_draw":
            s += 0.18 * math.exp(-t * 5) * scratch
            s += 0.09 * (1 - math.exp(-t * 20)) * math.exp(-t * 5) * math.sin(TAU * (260 + 140 * t) * t)
        elif kind == "trident":
            envelope = min(1, t * 130) * math.exp(-t * 14)
            s += envelope * (0.30 * scratch + 0.14 * math.sin(TAU * (510 + 115 * t) * t))
            s += 0.09 * math.exp(-t * 23) * math.sin(TAU * 1190 * t)
        elif kind == "shield":
            s += 0.23 * math.exp(-t * 31) * scratch
            s += 0.15 * math.exp(-t * 22) * math.sin(TAU * 350 * t)
            s += 0.075 * math.exp(-t * 31) * math.sin(TAU * 1030 * t)
        else:
            raise ValueError(kind)
        # Gentle 4ms attack and headroom; smooth for repeated playback.
        s *= min(1, t / 0.004)
        samples.append(max(-0.75, min(0.75, s)))
    dest = root / "assets" / "shatsupolish" / "sounds" / (rel + ".ogg")
    dest.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as temp:
        wavfile = Path(temp) / "sound.wav"
        with wave.open(str(wavfile), "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(RATE)
            w.writeframes(b"".join(struct.pack("<h", int(v * 32767)) for v in samples))
        subprocess.run(["ffmpeg", "-nostdin", "-loglevel", "error", "-y", "-i",
                        str(wavfile), "-c:a", "libvorbis", "-q:a", "4", str(dest)], check=True)
    if dest.stat().st_size < 300:
        raise RuntimeError("Failed rendering " + str(dest))

def sounds(name, number, volume):
    return [{"name": "shatsupolish:" + name + "/" + str(i),
             "type": "file", "volume": volume, "pitch": 1.0}
            for i in range(1, number + 1)]

def polish(root):
    root = Path(root)
    for i in range(1, 4):
        for path, kind, length in [
            ("block/wood_break", "wood", 0.38),
            ("magic/xp", "xp", 0.52),
            ("magic/totem", "totem", 1.35),
            ("weapons/bow_release", "bow", 0.34),
            ("weapons/bow_draw", "bow_draw", 0.34),
            ("weapons/trident_throw", "trident", 0.42),
            ("weapons/trident_hit", "trident", 0.34),
        ]:
            render(root, f"{path}/{i}", kind, i * 173, length)
    for i in range(1, 3):
        render(root, f"weapons/shield_raise/{i}", "shield", i * 59, 0.25)

    mpath = root / "assets/minecraft/sounds.json"
    m = json.loads(mpath.read_text(encoding="utf8"))
    # Use only these newly generated effects. Do not touch mob voices.
    replacements = {
        "entity.experience_orb.pickup": ("magic/xp", 3, 0.63),
        "item.totem.use": ("magic/totem", 3, 0.94),
        "entity.arrow.shoot": ("weapons/bow_release", 3, 0.86),
        "item.trident.throw": ("weapons/trident_throw", 3, 0.83),
        "item.trident.hit": ("weapons/trident_hit", 3, 0.76),
        "block.wood.break": ("block/wood_break", 3, 0.64),
    }
    for event, (name, n, vol) in replacements.items():
        if event not in m:
            raise RuntimeError("Expected Minecraft event absent: " + event)
        m[event] = {"replace": True, "sounds": sounds(name, n, vol)}
    place_events = 0
    for name, definition in m.items():
        if name.startswith("block.") and name.endswith(".place"):
            definition["replace"] = True
            # Remove unpredictable legacy variants; preserve the material family.
            clips = definition.get("sounds", [])
            if clips:
                definition["sounds"] = clips[:min(2, len(clips))]
                for entry in definition["sounds"]:
                    if isinstance(entry, dict):
                        entry["volume"] = min(float(entry.get("volume", 1)), 0.81)
                place_events += 1
    mpath.write_text(json.dumps(m, ensure_ascii=False, indent=2) + "\n", encoding="utf8")

    shatsu = root / "assets/shatsu4/sounds.json"
    d = json.loads(shatsu.read_text(encoding="utf8"))
    changed_wood = 0
    for event, definition in d.items():
        if event.startswith("block.") and event.endswith(".break"):
            names = [v.get("name", "") if isinstance(v, dict) else v
                     for v in definition.get("sounds", [])]
            if any(str(n).startswith("shatsuv7:block/wood/break_") for n in names):
                definition["sounds"] = sounds("block/wood_break", 3, 0.69)
                changed_wood += 1
        if event.startswith("block.") and event.endswith(".place"):
            clips = definition.get("sounds", [])
            if clips:
                definition["sounds"] = clips[:min(2, len(clips))]
                for entry in definition["sounds"]:
                    if isinstance(entry, dict):
                        entry["volume"] = min(float(entry.get("volume", 1)), 0.80)
    if changed_wood < 30 or place_events < 40:
        raise RuntimeError(f"Suspicious wood and place coverage: wood={changed_wood}, place={place_events}")
    shatsu.write_text(json.dumps(d, ensure_ascii=False, indent=2) + "\n", encoding="utf8")

    blockmap_path = root / "assets/customblocksounds/config/blockmap.json"
    original = blockmap_path.read_text(encoding="utf8")
    # Keep duplicate keys: the dependency intentionally supports one key per action.
    lines = []
    count = 0
    for line in original.splitlines(keepends=True):
        if "type=place" in line:
            line, n = re.subn(r"volume=0\.91(?=,|\")", "volume=0.77", line, count=1)
            count += n
        lines.append(line)
    if count < 900:
        raise RuntimeError(f"Unexpected place rule count: {count}")
    blockmap_path.write_text("".join(lines), encoding="utf8")

    cpath = root / "assets/clangor/sounds.json"
    clangor = json.loads(cpath.read_text(encoding="utf8"))
    clangor["use.bow.charge"] = {"replace": True, "sounds": sounds("weapons/bow_draw", 3, 0.58)}
    if "swing.trident" in clangor:
        clangor["swing.trident"] = {"replace": True, "sounds": sounds("weapons/trident_throw", 3, 0.35)}
    cpath.write_text(json.dumps(clangor, ensure_ascii=False, indent=2) + "\n", encoding="utf8")

    ns = root / "assets/shatsupolish/sounds.json"
    ns.write_text(json.dumps({
        "shield.raise": {"sounds": sounds("weapons/shield_raise", 2, 0.63)}
    }, indent=2) + "\n", encoding="utf8")
    print(f"Shatsu polish: {changed_wood} wood break entries; {place_events} vanilla place events; {count} blockmap place levels; 23 new original OGG sounds.")

if __name__ == "__main__":
    import sys
    polish(sys.argv[1])
