#!/usr/bin/env python3
"""Shatsu 0.4: reproducible ASMR sound baseline.

Generates *new* safe, short audio from the CC0 sources already bundled in Shatsu.
The final player package can replace these with additional separately supplied
CC0 field recordings without changing the event IDs or Java hooks.
Never alters original source samples, approved inventory/drinks/fireworks, or
Actions & Beyond's ans: namespace. Drinking is intentionally changed in this version.
"""
import json
import subprocess
from pathlib import Path

def ffmpeg(src, dst, filter_chain):
    src, dst = Path(src), Path(dst)
    if not src.is_file(): raise FileNotFoundError(src)
    dst.parent.mkdir(parents=True, exist_ok=True)
    cmd = ['ffmpeg','-nostdin','-hide_banner','-loglevel','error','-y',
           '-i',str(src),'-af',filter_chain,'-ac','1','-ar','44100',
           '-c:a','libvorbis','-q:a','5',str(dst)]
    subprocess.run(cmd,check=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE)

def variants(group, count=4, volume=.84):
    return {'replace':True,'sounds':[
        {'name':f'shatsufresh:asmr/{group}/{i}','type':'file','volume':volume,'pitch':1.0}
        for i in range(1,count+1)]}

def update(root):
    root=Path(root); base=root/'assets/shatsufresh/sounds'
    def load(ns):
        path=root/'assets'/ns/'sounds.json'
        return json.loads(path.read_text('utf8')),path
    def save(data,path):path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n','utf8')
    mc,mp=load('minecraft')
    sh4,sh4p=load('shatsu4')
    shv,shvp=load('shatsuv7')
    preview,pp=load('shatsufresh')
    liquids={}
    made=0
    def bake(group, source, count=4, eq='highpass=f=110,lowpass=f=6500',duration=.94):
        nonlocal made
        for i in range(1,count+1):
            inpath=source[(i-1)%len(source)]
            pitch=1.0+([-0.035,0.024,-0.015,0.042][(i-1)%4])
            # A small amount of natural variation while retaining each recording
            # and keeping output bounded for rapid gameplay repetition.
            chain=f'{eq},asetrate=44100*{pitch:.4f},aresample=44100,afade=t=out:st={max(.10,duration-.13):.3f}:d=0.13,alimiter=limit=0.91:level=false'
            ffmpeg(inpath,base/'asmr'/group/f'{i}.ogg',f'atrim=duration={duration:.3f},asetpts=PTS-STARTPTS,{chain}')
            made+=1
    D=lambda rel:base/rel
    for key,group,inputs,eq,duration in [
        ('water_fill','bucket/water/fill',[
            root/'assets/shatsuv7/sounds/block/water/place_1.ogg',
            root/'assets/shatsuv7/sounds/block/water/fall_1.ogg'],
            'highpass=f=110,lowpass=f=4200',.68),
        ('water_empty','bucket/water/empty',[
            root/'assets/shatsuv7/sounds/block/water/fall_1.ogg',
            root/'assets/shatsuv7/sounds/block/water/place_1.ogg'],
            'highpass=f=150,lowpass=f=5900',.88),
        ('lava_fill','bucket/lava/fill',[
            root/'assets/shatsu6/sounds/lava/1.ogg',
            root/'assets/shatsu6/sounds/lava/2.ogg'],
            'highpass=f=95,lowpass=f=2900',.79),
        ('lava_empty','bucket/lava/empty',[
            root/'assets/shatsu6/sounds/lava/2.ogg',
            root/'assets/shatsu6/sounds/lava/1.ogg'],
            'highpass=f=90,lowpass=f=3450',1.02),
        ('wood_break','wood/break',[
            root/'assets/shatsupolish/sounds/block/wood_break/1.ogg',
            root/'assets/shatsupolish/sounds/block/wood_break/2.ogg'],
            'highpass=f=170,equalizer=f=2200:t=q:w=1.1:g=2.6,lowpass=f=7600',.72),
    ]:
        bake(group,inputs,4,eq,duration)
        preview['preview.asmr.'+group.replace('/','.')]=variants(group,4,.78)
    for material in ('water','lava'):
        for action in ('fill','empty'):
            key=f'item.bucket.{action}'+('_lava' if material=='lava' else '')
            if key not in mc: raise RuntimeError('Missing bucket event '+key)
            mc[key]=variants('bucket/'+material+'/'+action,4,.88 if material=='water' else .78)
    # Override original wood break sound events wherever our original pack already
    # did. No blanket entity or unrelated material replacement.
    wood_sources=('shatsupolish:block/wood_break/','shatsuv7:block/wood/break_')
    adjusted=0
    for data in (mc,sh4,shv):
        for event,defn in data.items():
            if not (event.startswith('block.') and event.endswith('.break')):continue
            if any(any((s.get('name','') if isinstance(s,dict) else s).startswith(x)
                       for x in wood_sources) for s in defn.get('sounds',[])):
                data[event]=variants('wood/break',4,.78)
                adjusted+=1
    for drink,source,duration in (
        ('honey','honey',.91),('milk','milk',.80),
        ('potion','potion',1.02),('water','drink',.82)):
        bake('drink/'+drink,[D('food/'+source+'/1.ogg'),D('food/'+source+'/2.ogg')],
             4,'highpass=f=95,lowpass=f=3700,acompressor=threshold=-25dB:ratio=1.35:attack=9:release=95',duration)
        liquids['drink.'+drink]=variants('drink/'+drink,4,.87)
        preview['preview.asmr.drink.'+drink]=variants('drink/'+drink,4,.77)
    # Reproducible light magic accents. Overlay with user's newly uploaded
    # original magical_*.ogg recordings for the player-facing build.
    accents={
        'golden_apple':('experience',.48),
        'enchanted_apple':('levelup',.62),
        'golden_carrot':('experience',.31),
        'chorus_fruit':('levelup',.49),
    }
    for kind,(source,duration) in accents.items():
        bake('magic/'+kind,[D('premium/magic/'+source+'/1.ogg'),
                           D('premium/magic/'+source+'/2.ogg')],
            2,'highpass=f=260,lowpass=f=6400',duration)
        preview['preview.asmr.magic.'+kind]=variants('magic/'+kind,2,.36)
    for group,inputs,eq,duration in [
        ('bow/release',[D('premium/bow/shoot/1.ogg'),D('premium/bow/shoot/2.ogg')],
            'highpass=f=150,equalizer=f=2900:t=q:w=1.1:g=4,lowpass=f=8200',.72),
        ('crossbow/release',[D('premium/crossbow/shoot/1.ogg'),D('premium/crossbow/shoot/2.ogg')],
            'highpass=f=145,equalizer=f=3300:t=q:w=1:g=4.5,lowpass=f=8600',.71),
        ('trident/throw',[D('premium/trident/throw/1.ogg'),D('premium/trident/throw/2.ogg')],
            'highpass=f=150,equalizer=f=2400:t=q:w=1.1:g=2.4,lowpass=f=6600',.78),
    ]:
        bake(group,inputs,4,eq,duration)
        preview['preview.asmr.'+group.replace('/','.')]=variants(group,4,.80)
    mc['entity.arrow.shoot']=variants('bow/release',4,.93)
    mc['item.crossbow.shoot']=variants('crossbow/release',4,.95)
    mc['item.trident.throw']=variants('trident/throw',4,.86)
    save(mc,mp);save(sh4,sh4p);save(shv,shvp);save(preview,pp)
    liquids_path=root/'assets/shatsu_liquids/sounds.json'
    liquids_path.parent.mkdir(parents=True,exist_ok=True)
    save(liquids,liquids_path)
    print('Shatsu ASMR: generated',made,'baseline OGG variants; wood sound events',adjusted,
          'bucket events 4; liquid event families 4; subtle food accents 4')
    return made

if __name__=='__main__':
    import sys
    update(sys.argv[1])
