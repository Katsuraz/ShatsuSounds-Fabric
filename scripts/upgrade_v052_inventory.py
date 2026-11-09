#!/usr/bin/env python3
"""Reproducible baseline inventory profiles for Shatsu v0.5.2 source builds.

Full ASMR replacement recordings are packaged in the separate complete-audio
release. This baseline generator makes *every* registered item-profile event
work when building the source without optional user-uploaded audio libraries.
"""
import json
import shutil
from pathlib import Path

ACTIONS = ("pickup", "place", "swap", "transfer", "split", "drop")
BASE = {
    "golden_apple":"food","enchanted_apple":"food","golden_carrot":"food",
    "chorus":"nether","sculk":"nether","sculk_sensor":"nether",
    "sculk_shrieker":"nether","sculk_catalyst":"nether","creaking_heart":"wooden",
    "totem":"crystal","enchanted_book":"paper","ender_pearl":"crystal",
    "water_bucket":"water","lava_bucket":"metal","milk_bucket":"water",
    "honey_bottle":"water","potion":"glass","xp_bottle":"glass",
    "music_disc":"utility","spawn_egg":"utility","elytra":"cloth",
    "shulker_box":"wooden","ender_chest":"wooden","shield":"weapon",
    "snowball":"snow","egg":"utility","wind_charge":"utility",
    "fire_charge":"nether","dragon_egg":"mineral","dragon_breath":"nether",
    "trial_mechanism":"metal","armor_metal":"metal","armor_leather":"leather",
    "armor_diamond":"crystal","bundle":"cloth","fish_bucket":"water",
    "heart_sea":"crystal","horn":"bone","redstone":"metal"
}

def update(root):
    root=Path(root)
    index=json.loads((Path(__file__).resolve().parents[1] /
        "src/main/resources/shatsu_inventory_profiles.json").read_text("utf8"))
    categories=sorted(set(index.values()))
    dest=root/"assets/shatsufresh"
    sounds=dest/"sounds"
    definition_path=dest/"sounds.json"
    if not definition_path.is_file():
        raise RuntimeError("No Shatsu sound registry found in staged assets")
    defs=json.loads(definition_path.read_text("utf8"))
    count=0
    for category in categories:
        fallback=BASE.get(category,category)
        for action in ACTIONS:
            refs=[]
            for variation in (1,2):
                # Try an existing themed recording, then an approved old material
                # recording as a source-only fallback (never invent file names).
                sources=[
                    root/f"assets/shatsuv7/sounds/inventory/{fallback}_{variation}.ogg",
                    root/f"assets/shatsuv7/sounds/inventory/{fallback}_1.ogg",
                    root/f"assets/shatsuv7/sounds/inventory/utility_{variation}.ogg",
                ]
                preferred={
                    "sculk_sensor":f"dark/sculk_sensor/click/{variation}",
                    "sculk_shrieker":f"dark/sculk/step/{variation}",
                    "sculk":"dark/sculk/step/{variation}",
                    "creaking_heart":f"dark/creaking_heart/idle/{variation}",
                    "water_bucket":f"asmr/bucket/water/fill/{variation}",
                    "lava_bucket":f"asmr/bucket/lava/fill/{variation}",
                    "golden_apple":f"premium/food/apple/{variation}",
                    "enchanted_apple":f"premium/food/apple/{variation}",
                    "golden_carrot":f"premium/food/carrot/{variation}"
                }.get(category)
                if preferred:
                    sources.insert(0,sounds/(preferred+".ogg"))
                source=next((p for p in sources if p.is_file()),None)
                if source is None:
                    raise RuntimeError(f"No sound source for {category}/{action}/{variation}")
                stem=f"repair/inventory/{category}/{action}/{variation}"
                out=sounds/(stem+".ogg")
                out.parent.mkdir(parents=True,exist_ok=True)
                shutil.copyfile(source,out)
                refs.append({"name":"shatsufresh:"+stem,"type":"file",
                    "volume":0.9,"pitch":1.0 if action in ("pickup","transfer") else .98})
                count+=1
            defs[f"inventory.move.{action}.{category}"]={"replace":True,"sounds":refs}
    definition_path.write_text(json.dumps(defs,indent=2,ensure_ascii=False)+"\n","utf8")
    print(f"Shatsu v0.5.2 source build: {count} available inventory clips ({len(categories)} profiles)")
    return count

if __name__=="__main__":
    import sys
    update(sys.argv[1])
