#!/usr/bin/env python3
"""Pixel Eco RETRO EDITION: audio-only transformation of real Shatsu assets.

Called at the END of Shatsu's existing, complete audio preparation pipeline.
Does not replace Shatsu's Java code, Sound Studio, Clangor/Sounds/CBS mappings,
event inventory, or configuration format. Changes generated audio only.
"""
from __future__ import annotations
import hashlib, io, json, math, re
from collections import Counter
from pathlib import Path
import numpy as np
from scipy import signal
import soundfile as sf

OWNED=("shatsuv7","shatsu4","shatsufresh","shatsupolish","shatsu_food_audio","shatsupremium")
PRESERVE=("/trident/","/food/drink/","/food/honey/","/inventory/open","/firework/")
SEED=0x50E1
def stable(name):
    return int.from_bytes(hashlib.blake2b(name.encode(),digest_size=8).digest(),"big")
def decode(path):
    sample,rate=sf.read(str(path),dtype="float32",always_2d=True)
    return sample,rate
def encode(path,a,rate):
    sf.write(str(path),a.astype("float32"),rate,format="OGG",subtype="VORBIS")
def soft_filter(x,rate,cut,high=False):
    cutoff=min(cut,rate*.43)
    if cutoff<30:return x
    return signal.sosfilt(signal.butter(2,cutoff,btype="highpass" if high else "lowpass",
                                    fs=rate,output="sos"),x,axis=0).astype("float32")
def category(rel):
    s=rel.lower()
    if "trident" in s:return "trident"
    if any(t in s for t in ("explode","explosion","blast","firework","tnt","boom")):return "explosion"
    if any(t in s for t in ("axe","pickaxe","shovel","hoe","stone","wood","sword","spear","hit","break","dig","mine","swing","place")):return "tactile"
    if any(t in s for t in ("bow","crossbow","shield","mace","combat","weapon","clangor")):return "combat"
    if any(t in s for t in ("inventory","food","drink","swallow","eat")):return "inventory"
    if any(t in s for t in ("magic","totem","xp","experience","potion","enchant")):return "magic"
    return "other"

def transform(x,rate,rel):
    """Warm retro saturation, short transient harmonic spark, no piercing beeps."""
    if len(x)<100 or not np.isfinite(x).all():return x
    orig=x.copy()
    cat=category(rel)
    if any(p in rel.lower() for p in PRESERVE):
        return x # approved Shatsu character / Shatsu original remains intact
    s=stable(rel);n=len(x);c=x.shape[1]
    mix={"trident":.07,"explosion":.27,"tactile":.23,
         "combat":.20,"inventory":.10,"magic":.20,"other":.11}[cat]
    y=soft_filter(x,rate,75,True)
    y=soft_filter(y,rate,7300 if cat in ("tactile","combat","explosion") else 6600)
    # 16-bit-inspired coloration mixed back with the full original detail.
    hold=2+(s%3)
    idx=np.minimum((np.arange(n)//hold)*hold,n-1)
    stair=np.round(y[idx]*2048.0)/2048.0
    y=(1-mix)*y+mix*stair
    y=.90*y+.10*np.tanh(y*1.6)/1.6
    if cat in ("tactile","combat","magic","explosion"):
        duration=min(n,int(rate*(.10 if cat=="magic" else .07)))
        t=np.arange(duration,dtype=np.float32)/rate
        # Low-gain, short sonic accent; material-dependent, not generic 8-bit blips.
        woody=any(v in rel.lower() for v in ("/wood/","/dirt/","/grass/","/snow/","/wool/","/leaves/","/axe/"))
        frequency=(240+s%180) if woody else (580+s%620)
        phi=2*np.pi*(frequency*t+(70+s%100)*t*t)
        accent=np.sin(phi)*np.exp(-t*(37+s%14))*(1-np.exp(-700*t))
        gain=.010 if woody else (.014 if cat=="tactile" else .018)
        y[:duration]+=(gain*accent)[:,None]
    # gentle limiting; stay close to original perceptual RMS.
    peak=np.max(np.abs(y))
    if peak>0.90:y*=0.90/peak
    rms0=np.sqrt(np.mean(orig*orig))+1e-9
    rms1=np.sqrt(np.mean(y*y))+1e-9
    ratio=np.clip(rms0/rms1,.75,1.18)
    y*=ratio
    peak=np.max(np.abs(y))
    if peak>.88:y*=.88/peak
    # Prevent encoded OGG clicks only when the input was already naturally silent.
    fade=min(int(rate*.0025),n//10)
    if fade and np.max(np.abs(orig[:fade]))<.04:y[:fade]*=np.linspace(0,1,fade)[:,None]
    if fade and np.max(np.abs(orig[-fade:]))<.04:y[-fade:]*=np.linspace(1,0,fade)[:,None]
    return y.astype("float32")

def reference(root):
    clips=sorted((root/"assets/shatsufresh/sounds").rglob("trident/**/*.ogg"))
    if not clips:clips=sorted((root/"assets").rglob("*trident*/*.ogg"))
    if not clips:raise RuntimeError("Real Shatsu Trident recordings unavailable. Cannot calibrate.")
    x,rate=decode(clips[0])
    duration=len(x)/rate
    attack=x[:max(1,min(len(x),int(rate*.15)))]
    attack_energy=float(np.sqrt(np.mean(attack*attack)))
    if attack_energy<.001:raise RuntimeError("Trident reference has no audible attack.")
    return {"reference_path":str(clips[0].relative_to(root)),"duration_s":round(duration,3),
            "attack_rms":round(attack_energy,4),"sha256":hashlib.sha256(clips[0].read_bytes()).hexdigest()}

def explode_variant(rate,event,j,trident_rms):
    """Original arcade energy burst; not a copy of Shatsu/Honor of Kings audio."""
    seed=stable(f"{event}:{j}:{SEED}")
    rng=np.random.default_rng(seed)
    family="firework" if "firework" in event else ("magic" if any(p in event for p in ("crystal","wind_burst","dragon")) else "energy")
    duration=.39+.027*(j%3)+(.08 if "large" in event else 0)
    n=int(duration*rate)
    t=np.arange(n,dtype=np.float32)/rate
    # Punchy attack, short filtered fizz, cascaded sparkles, musical energy tail.
    raw=rng.normal(0,1,n).astype("float32")
    noise=soft_filter(raw[:,None],rate,260,True)[:,0]
    noise=soft_filter(noise[:,None],rate,6300)[:,0]
    a=np.exp(-t*(29+3*(j%3)))*(1-np.exp(-t*900))
    first=(.12+.02*(j%2))*a*noise
    f0=(590 if family=="firework" else 380)+(seed%175)
    chirp=np.sin(2*np.pi*(f0*t+((210+22*j)*t*t)))
    body=(.105+.02*(j%2))*np.exp(-t*(17+2*(j%2)))*chirp*(1-np.exp(-t*300))
    spark=np.zeros(n,dtype=np.float32)
    for k in range(3+(j%3)):
        start=(.035+.022*k)+((seed>>(k*4))%12)/1000
        tau=t-start
        valid=tau>=0
        f=1200+((seed>>(k*6))%1200)
        spark[valid]+=(.029 if family=="firework" else .018)*np.exp(-tau[valid]*(50+5*k))*np.sin(2*np.pi*(f*tau[valid]+400*tau[valid]**2))
    # Trident attack analysis influences transient prominence, but never copies samples.
    balance=np.clip(.09/max(.03,trident_rms),.7,1.3)
    x=first*balance+body+spark
    x=soft_filter(x[:,None],rate,180,True)[:,0]
    x=soft_filter(x[:,None],rate,7250)[:,0]
    edge=min(int(.007*rate),n//10);x[:edge]*=np.linspace(0,1,edge)
    tail=min(int(.08*rate),n//8);x[-tail:]*=np.linspace(1,0,tail)
    x-=np.mean(x)
    rms=np.sqrt(np.mean(x*x))+1e-9
    x*=min(2.0,.075/rms)
    if np.max(np.abs(x))>.70:x*=.70/np.max(np.abs(x))
    return x[:,None].astype("float32")

def actual_events(root):
    # Use only existing Shatsu vanilla events; never invent event identifiers.
    path=root/"assets/minecraft/sounds.json"
    data=json.loads(path.read_text())
    chosen=[]
    for event in sorted(data):
        if re.search(r"(?:^|\.)(?:explode|blast|blast_far|large_blast|large_blast_far|twinkle|twinkle_far)$",event):
            if ("firework" in event or "explode" in event or "explosion" in event):
                chosen.append(event)
    # One shared vanilla explosion event covers TNT/creepers/beds without contextual code.
    if "entity.generic.explode" not in chosen and "entity.generic.explode" in data:
        chosen.append("entity.generic.explode")
    return data,path,sorted(set(chosen))

def apply(root):
    root=Path(root)
    out=root/"pixeleco_retro_report.json"
    ref=reference(root)
    # Shatsu's actual sound bank spans many namespaces. Inspect ALL staged audio,
    # not just the six original namespaces. Avoid changing recognizable mob voices.
    clips=sorted(x for x in root.glob("assets/*/sounds/**/*.ogg")
        if "/sounds/entity/" not in str(x).lower()
        and "/sounds/mob/" not in str(x).lower())
    if len(clips)<900:raise RuntimeError(f"Shatsu audio assets incomplete: {len(clips)}")
    changes=Counter();before={}
    for clip in clips:
        relative=str(clip.relative_to(root))
        x,rate=decode(clip)
        if rate<8000 or rate>192000 or x.ndim!=2:raise RuntimeError(f"Unsupported audio: {relative}")
        sig=hashlib.sha256(clip.read_bytes()).hexdigest()
        y=transform(x,rate,relative)
        if not np.all(np.isfinite(y)):raise RuntimeError(f"nonfinite: {relative}")
        if np.max(np.abs(y))>.91:raise RuntimeError(f"peak exceeded: {relative}")
        if np.array_equal(y,x):
            changes["preserved"]+=1
        else:
            encode(clip,y,rate)
            test,actual_rate=decode(clip)
            if actual_rate!=rate or not np.isfinite(test).all():raise RuntimeError(f"corrupt OGG: {relative}")
            changes[category(relative)]+=1
    mappings,path,events=actual_events(root)
    n_new=0
    for event in events:
        group=re.sub(r"[^a-z0-9_.-]+","_",event)
        sounds=[]
        for j in range(6):
            rel=f"explosions/{group}/{j+1:02d}"
            destination=root/"assets/pixelecoretro/sounds"/(rel+".ogg")
            destination.parent.mkdir(parents=True,exist_ok=True)
            a=explode_variant(44100,event,j,ref["attack_rms"])
            encode(destination,a,44100)
            test,rate=decode(destination)
            if rate!=44100 or not np.isfinite(test).all() or np.max(np.abs(test))>.8:
                raise RuntimeError(f"New explosion OGG invalid: {destination}")
            sounds.append({"name":"pixelecoretro:"+rel,"type":"file","volume":.79,"pitch":1.0})
            n_new+=1
        current=mappings[event]
        current["replace"]=True
        current["sounds"]=sounds
        # Preserve original subtitle if present.
    path.write_text(json.dumps(mappings,indent=2,ensure_ascii=False)+"\n")
    report={"base":"ShatsuSounds-Fabric v066/immersive-ui-audio branch (Shatsu 0.6.6 code and features)",
        "trident_reference":ref,
        "base_shatsu_ogg_count":len(clips),
        "transformed_by_category":dict(changes),
        "real_vanilla_explosion_events":events,
        "new_arcade_explosion_variants":n_new,
        "compatibility":"Shatsu Java/config/Mod Menu, Clangor, Sounds, CBS files retained",
        "notes":"Automatic drop-in retro edition; no unrelated mod. Vanilla creature voices preserved. Not in-game tested."}
    out.write_text(json.dumps(report,indent=2)+"\n")
    print("PIXEL ECO SHATSU RETRO:",len(clips),"original OGGs inspected;",sum(v for k,v in changes.items() if k!="preserved"),
          "retro-processed;",n_new,"new original explosion clips;",len(events),"real sound events",flush=True)
    return report

if __name__=="__main__":
    import sys
    apply(sys.argv[1])
