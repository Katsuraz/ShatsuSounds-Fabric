#!/usr/bin/env python3
"""v0.3.3 Shatsu sound redesign: actual 26.3 spear events, crisp ranged
weapons, distinct food textures, and original fantasy XP pickups.

Inputs are bundled prior-generation sound files; untouched drink, UI,
firework, and mob-voice recordings remain byte-for-byte identical.
"""
from __future__ import annotations
import json
import math
import shutil
import subprocess
import tempfile
import wave
from pathlib import Path

def run(*args):
    subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)

def effect(src, dst, kind):
    dst.parent.mkdir(parents=True,exist_ok=True)
    # Tight, clear transients with safety headroom; no exaggerated sharpness.
    bands={
        'bow': 'highpass=f=165,equalizer=f=2350:t=q:w=1.1:g=4.0,acompressor=threshold=-19dB:ratio=2.0:attack=5:release=80,alimiter=limit=0.91:level=false',
        'crossbow':'highpass=f=180,equalizer=f=2850:t=q:w=1.0:g=4.0,acompressor=threshold=-20dB:ratio=2.1:attack=3:release=75,alimiter=limit=0.91:level=false',
        'trident':'highpass=f=110,equalizer=f=1900:t=q:w=1.1:g=3.5,acompressor=threshold=-20dB:ratio=1.8:attack=5:release=85,alimiter=limit=0.91:level=false',
        'spear':'highpass=f=125,equalizer=f=2100:t=q:w=1.0:g=4.2,acompressor=threshold=-20dB:ratio=1.9:attack=4:release=65,alimiter=limit=0.91:level=false',
        'softfood':'highpass=f=80,lowpass=f=2650,acompressor=threshold=-21dB:ratio=1.5:attack=7:release=95,alimiter=limit=0.87:level=false',
        'crispfood':'highpass=f=210,equalizer=f=3000:t=q:w=1.1:g=3.2,acompressor=threshold=-22dB:ratio=1.5:attack=3:release=75,alimiter=limit=0.88:level=false',
        'wetfood':'highpass=f=100,lowpass=f=3400,acompressor=threshold=-19dB:ratio=1.5:attack=9:release=100,alimiter=limit=0.88:level=false'
    }
    run('ffmpeg','-nostdin','-hide_banner','-loglevel','error','-y','-i',str(src),
        '-af',bands[kind],'-ac','1','-c:a','libvorbis','-q:a','5',str(dst))

def synth_xp(path, note, length=0.42):
    """Deterministic original soft fantasy bell / glass pluck, 44.1 kHz mono."""
    rate=44100
    with tempfile.TemporaryDirectory() as tmp:
        wav=Path(tmp)/'bell.wav'
        with wave.open(str(wav),'wb') as out:
            out.setnchannels(1);out.setsampwidth(2);out.setframerate(rate)
            chunks=bytearray()
            for k in range(int(rate*length)):
                t=k/rate
                # Fast pluck attack, twinkling partials, rising spectral shimmer,
                # slight reverb-like delay and muted air tail.
                attack=min(1.0,t/0.009)
                tail=math.exp(-12.5*t)
                base=math.sin(2*math.pi*note*t)
                chime=(.55*base+.24*math.sin(2*math.pi*note*2.01*t)
                     +.10*math.sin(2*math.pi*note*3.97*t)
                     +.11*math.sin(2*math.pi*(note*1.501*t+1.5*t*t)))
                echo=0.23*math.sin(2*math.pi*note*1.5*max(0,t-0.095))*math.exp(-16*max(0,t-0.095)) if t>0.095 else 0
                value=(chime*tail*attack+echo)*0.68
                sample=max(-32767,min(32767,int(value*32767)))
                chunks.extend(sample.to_bytes(2,'little',signed=True))
            out.writeframes(bytes(chunks))
        path.parent.mkdir(parents=True,exist_ok=True)
        run('ffmpeg','-nostdin','-hide_banner','-loglevel','error','-y','-i',str(wav),
            '-af','lowpass=f=8800,alimiter=limit=0.85:level=false','-ac','1',
            '-c:a','libvorbis','-q:a','5',str(path))

def variants(group, count=2, volume=0.92):
    return {'replace': True,'sounds': [
        {'name':f'shatsufresh:{group}/{i}', 'type':'file','volume':volume,'pitch':1.0}
        for i in range(1,count+1)]}

def update(root):
    root=Path(root)
    base=root/'assets/shatsufresh/sounds'
    mcp=root/'assets/minecraft/sounds.json'
    clangp=root/'assets/clangor/sounds.json'
    foodp=root/'assets/shatsu_food_audio/sounds.json'
    previewp=root/'assets/shatsufresh/sounds.json'
    mc=json.loads(mcp.read_text('utf8'))
    clang=json.loads(clangp.read_text('utf8'))
    food=json.loads(foodp.read_text('utf8'))
    previews=json.loads(previewp.read_text('utf8'))
    generated=0

    # Only use recorded OGG material in the project. No new external library.
    families={
        'bow': ('draw','nock','shoot'),
        'crossbow': ('start','middle','end','shoot','impact','bolt_fly'),
        'trident': ('throw','hit','ground','return','flight'),
    }
    for family,actions in families.items():
        for action in actions:
            for i in (1,2):
                src=base/family/action/f'{i}.ogg'
                dst=base/'premium'/family/action/f'{i}.ogg'
                if not src.is_file():raise FileNotFoundError(src)
                effect(src,dst, family)
                generated+=1
            previews[f'preview.premium.{family}.{action}']=variants(f'premium/{family}/{action}',volume=.8)

    # Actual 26.3 vanilla spear events with two differentiated wood/metal takes.
    originals={
        'metal_attack':'spear_metal_attack',
        'metal_hit':'spear_metal_hit',
        'metal_use':'spear_use',
        'wood_attack':'spear_wood_attack',
        'wood_hit':'spear_wood_hit',
        'wood_use':'spear_wood_use',
        'lunge_1':'spear_lunge_1',
        'lunge_2':'spear_lunge_2',
        'lunge_3':'spear_lunge_3',
    }
    for action,folder in originals.items():
        for i in (1,2):
            src=root/'assets/shatsu7/sounds'/folder/f'{i}.ogg'
            if not src.is_file():raise FileNotFoundError(src)
            effect(src,base/'premium/spear'/action/f'{i}.ogg','spear')
            generated+=1
        previews[f'preview.premium.spear.{action}']=variants('premium/spear/'+action,volume=.83)

    # Real game sound registry IDs for spears. No synthetic/mod-only ID needed.
    spear_events={
        'item.spear.attack':'metal_attack',
        'item.spear.hit':'metal_hit',
        'item.spear.use':'metal_use',
        'item.spear_wood.attack':'wood_attack',
        'item.spear_wood.hit':'wood_hit',
        'item.spear_wood.use':'wood_use',
        'item.spear.lunge_1':'lunge_1',
        'item.spear.lunge_2':'lunge_2',
        'item.spear.lunge_3':'lunge_3',
    }
    for event,action in spear_events.items():
        if event not in mc:raise RuntimeError('Missing vanilla spear event '+event)
        mc[event]=variants('premium/spear/'+action,volume=.91)

    # Clangor category sounds are independent of vanilla jab/use events.
    for k,action in {
        'select.spear':'metal_use',
        'swing.spear':'metal_attack',
        'hit.spear.weak':'metal_hit',
        'hit.spear.strong':'metal_hit',
        'hit.spear.crit':'metal_hit',
        'hit.spear.knockback':'metal_hit',
        'hit.spear.sweep':'metal_attack'
    }.items():
        if k not in clang:raise RuntimeError('Missing Clangor spear event '+k)
        clang[k]=variants('premium/spear/'+action,volume=.88 if '.weak' in k else .97)

    mc_events={
        'entity.arrow.shoot':('bow','shoot'),
        'item.crossbow.loading_start':('crossbow','start'),
        'item.crossbow.loading_middle':('crossbow','middle'),
        'item.crossbow.loading_end':('crossbow','end'),
        'item.crossbow.quick_charge_1':('crossbow','start'),
        'item.crossbow.quick_charge_2':('crossbow','middle'),
        'item.crossbow.quick_charge_3':('crossbow','end'),
        'item.crossbow.shoot':('crossbow','shoot'),
        'item.crossbow.hit':('crossbow','impact'),
        'item.trident.throw':('trident','throw'),
        'item.trident.hit':('trident','hit'),
        'item.trident.hit_ground':('trident','ground'),
        'item.trident.return':('trident','return'),
    }
    for event,(family,action) in mc_events.items():
        if event in mc:mc[event]=variants(f'premium/{family}/{action}',volume=.94)
    for event,(family,action) in {
        'select.bow':('bow','nock'),
        'use.bow.charge':('bow','draw'),
        'select.crossbow':('crossbow','start'),
        'select.trident':('trident','throw'),
        'swing.trident':('trident','throw'),
        'use.trident.charge':('trident','throw')
    }.items():
        if event in clang:clang[event]=variants(f'premium/{family}/{action}',volume=.94)

    # 16 distinctive real food categories, rather than six generic profiles.
    # Leave all approved drinking sources (milk, honey, potion) untouched.
    for kind in (
        'apple','carrot','beetroot','melon','berries','bread','cookie',
        'pie','potato','raw_meat','cooked_meat','raw_fish',
        'cooked_fish','kelp','chorus','soup'
    ):
        if kind in ('bread','pie','potato','cooked_fish','cooked_meat'): style='softfood'
        elif kind in ('soup','raw_meat','raw_fish'): style='wetfood'
        else:style='crispfood'
        for i in (1,2,3):
            src=root/'assets/shatsu_food_audio/sounds/food'/f'{kind}_{i}.ogg'
            if not src.is_file():raise FileNotFoundError(src)
            effect(src,base/'premium/food'/kind/f'{i}.ogg',style)
            generated+=1
        current=food['food.'+kind]
        food['food.'+kind]=variants('premium/food/'+kind,count=3,volume=.92)
        if 'subtitle' in current:food['food.'+kind]['subtitle']=current['subtitle']
        previews['preview.premium.food.'+kind]=variants('premium/food/'+kind,count=3,volume=.83)

    # Original fantasy chimes. Each variant is gently different; keep pickup
    # short to avoid overlapping long tails on XP farms.
    for i,note in enumerate((659.25,783.99,880.0,987.77),1):
        synth_xp(base/'premium/magic/experience'/f'{i}.ogg',note)
        generated+=1
    for i,note in enumerate((523.25,659.25,783.99),1):
        synth_xp(base/'premium/magic/levelup'/f'{i}.ogg',note,0.64)
        generated+=1
    mc['entity.experience_orb.pickup']=variants('premium/magic/experience',count=4,volume=.67)
    if 'entity.player.levelup' in mc:
        mc['entity.player.levelup']=variants('premium/magic/levelup',count=3,volume=.72)
    previews['preview.premium.magic.experience']=variants('premium/magic/experience',count=4,volume=.7)
    previews['preview.premium.magic.levelup']=variants('premium/magic/levelup',count=3,volume=.72)

    for path,data in ((mcp,mc),(clangp,clang),(foodp,food),(previewp,previews)):
        path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n','utf8')
    print(f'Shatsu 0.3.3: {generated} refined/created premium OGG files; '
          f'{len(spear_events)} vanilla spear events; 16 unique food families; original fantasy XP')
    return generated

if __name__=='__main__':
    import sys
    update(sys.argv[1])
