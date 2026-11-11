#!/usr/bin/env python3
"""Short, labeled-by-index listening preview from the ACTUAL staged Pixel Eco audio."""
import json, pathlib, subprocess, sys
import numpy as np
import scipy.signal as sig
import soundfile as sf

root=pathlib.Path(sys.argv[1])
out=pathlib.Path(sys.argv[2])
out.parent.mkdir(parents=True,exist_ok=True)
events=[
    ("minecraft","entity.generic.explode","Retro explosion"),
    ("minecraft","block.stone.break","Stone breaking"),
    ("minecraft","block.wood.break","Wood breaking"),
    ("minecraft","item.trident.throw","Shatsu Trident character"),
    ("minecraft","entity.arrow.shoot","Bow release"),
    ("minecraft","item.crossbow.shoot","Crossbow"),
    ("minecraft","item.mace.smash_ground","Mace"),
    ("minecraft","entity.experience_orb.pickup","Magic XP"),
    ("clangor","swing.axe.iron","Iron axe swing"),
    ("clangor","swing.sword.diamond","Diamond sword swing"),
]
clips=[]
index=[]
rate=44100
for ns,key,label in events:
    src=root/"assets"/ns/"sounds.json"
    if not src.is_file():continue
    data=json.loads(src.read_text("utf-8"))
    event=data.get(key)
    if not event or not event.get("sounds"):continue
    item=event["sounds"][0]
    name=item if isinstance(item,str) else item.get("name","")
    if ":" not in name:continue
    namespace,rel=name.split(":",1)
    filename=root/"assets"/namespace/"sounds"/(rel+".ogg")
    if not filename.is_file():continue
    sample,original_rate=sf.read(str(filename),dtype="float32",always_2d=True)
    sample=sample.mean(axis=1)
    n=min(len(sample),int(1.35*original_rate))
    sample=sample[:n]
    if original_rate!=rate:
        import math
        g=math.gcd(int(original_rate),rate)
        sample=sig.resample_poly(sample,rate//g,original_rate//g)
    if len(sample)<20:continue
    m=np.max(np.abs(sample))
    if m>0.78:sample=sample*(.78/m)
    edge=min(350,len(sample)//12)
    if edge:sample[:edge]*=np.linspace(0,1,edge);sample[-edge:]*=np.linspace(1,0,edge)
    clips.append(sample.astype("float32"))
    clips.append(np.zeros(int(rate*.21),dtype=np.float32))
    index.append({"segment":len(index)+1,"event":f"{ns}:{key}","label":label,"file":str(filename.relative_to(root))})
if len(index)<6:raise RuntimeError(f"Only {len(index)} listening preview segments: {index}")
signal=np.concatenate(clips)
wav=out.with_suffix(".wav")
sf.write(wav,signal,rate)
subprocess.run(["ffmpeg","-y","-nostdin","-loglevel","error","-i",str(wav),
                "-codec:a","libmp3lame","-qscale:a","3",str(out)],check=True)
wav.unlink()
out.with_suffix(".json").write_text(json.dumps(index,indent=2))
print("Preview:",len(index),"segments,",round(len(signal)/rate,1),"seconds")
