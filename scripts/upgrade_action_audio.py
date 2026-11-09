#!/usr/bin/env python3
"""Tool-specific Shatsu audio and careful mastering of *new* recorded effects.
Only generated assets are modified. Approved inventory, mob, drink, and
fireworks recordings are not processed or replaced.
"""
from __future__ import annotations
import json
import re
import shutil
import subprocess
import tempfile
from pathlib import Path

MATERIALS = ('wooden','stone','copper','iron','gold','diamond','netherite','general')
TOOLS = ('pickaxe','shovel','hoe')
KINDS = ('weak','strong','crit','knockback','sweep')
BOOST_GROUPS = {'bow','crossbow','arrow','sword','axe','mace','trident','tool'}
BOOST_FOOD = {'crisp','soft','dry','carrot','meat','soup'}


def execute(*args):
    return subprocess.run(args, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)


def process(infile: Path, outfile: Path, chain: str):
    outfile.parent.mkdir(parents=True, exist_ok=True)
    execute('ffmpeg','-hide_banner','-loglevel','error','-nostdin','-y',
            '-i',str(infile),'-af',chain,'-ac','1',
            '-c:a','libvorbis','-q:a','5',str(outfile))


def create_tools(root: Path):
    base = root/'assets/shatsufresh/sounds'
    settings = {
        'pickaxe': {
            'swing': ('axe/swing', 'highpass=f=260,lowpass=f=6500'),
            'select': ('sword/equip', 'highpass=f=290,lowpass=f=7000'),
            'impact': ('mace/hit', 'highpass=f=270,lowpass=f=6500'),
        },
        'shovel': {
            'swing': ('axe/swing', 'highpass=f=130,lowpass=f=2300'),
            'select': ('axe/equip', 'highpass=f=130,lowpass=f=2800'),
            'impact': ('axe/swing', 'highpass=f=140,lowpass=f=2500'),
        },
        'hoe': {
            'swing': ('sword/swing', 'highpass=f=500,lowpass=f=5200'),
            'select': ('sword/equip', 'highpass=f=450,lowpass=f=5000'),
            'impact': ('sword/swing', 'highpass=f=500,lowpass=f=5600'),
        },
    }
    generated = 0
    for tool in TOOLS:
        for action in ('swing','select'):
            group, effects = settings[tool][action]
            for material in MATERIALS:
                for i in (1,2):
                    src = base/group/material/f'{i}.ogg'
                    dst = base/'tool'/tool/action/material/f'{i}.ogg'
                    if not src.is_file():
                        raise FileNotFoundError(f'Missing tool input {src}')
                    process(src,dst,effects)
                    generated += 1
        group, effects = settings[tool]['impact']
        for i in (1,2):
            if tool == 'pickaxe':
                src = base/group/f'{i}.ogg'
            elif tool == 'shovel':
                src = base/group/'general'/f'{i}.ogg'
            else:
                src = base/group/'stone'/f'{i}.ogg'
            process(src,base/'tool'/tool/'impact'/f'{i}.ogg',effects)
            generated += 1
    return generated


def master_new_recordings(root: Path):
    base = root/'assets/shatsufresh/sounds'
    processed = 0
    for path in sorted(base.rglob('*.ogg')):
        rel = path.relative_to(base)
        if rel.parts[0] not in BOOST_GROUPS and not (
            len(rel.parts) > 1 and rel.parts[0] == 'food' and rel.parts[1] in BOOST_FOOD
        ):
            continue
        # Peak-normalize without clipping. Preserve each recording's original
        # transient envelope, pitch, ambience and stereo-free mono format.
        probe = execute('ffmpeg','-hide_banner','-nostdin','-i',str(path),
                        '-af','volumedetect','-f','null','-')
        match = re.search(rb'max_volume:\s*(-?\d+(?:\.\d+)?)\s*dB',probe.stderr)
        if not match:
            raise RuntimeError(f'Cannot measure peak: {path}')
        peak_db = float(match.group(1))
        gain = max(0.0, min(12.0, -3.5 - peak_db))
        if gain <= 0.25:
            continue
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp)/'mastered.ogg'
            process(path,out,f'volume={gain:.3f}dB,alimiter=limit=0.95:level=false')
            shutil.copyfile(out,path)
        processed += 1
    return processed


def upgrade_events(root: Path):
    def read(ns):
        path=root/'assets'/ns/'sounds.json'
        return json.loads(path.read_text('utf-8')),path
    def put(data,path):
        path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n','utf-8')
    def variants(group, volume=1.0):
        return {'replace':True,'sounds':[
            {'name':f'shatsufresh:{group}/{i}','type':'file','volume':volume,'pitch':1.0}
            for i in (1,2)
        ]}
    clang,cp=read('clangor')
    changed=0
    for tool in TOOLS:
        for material in MATERIALS:
            for action in ('select','swing'):
                key=f'{action}.{tool}.{material}'
                if key in clang:
                    clang[key] = variants(f'tool/{tool}/{action}/{material}', 0.98 if action=='swing' else 0.90)
                    changed += 1
        for kind in KINDS:
            key=f'hit.weapon_n_tool.{tool}.{kind}'
            if key in clang:
                clang[key]=variants(f'tool/{tool}/impact',0.96 if kind=='weak' else 1.0)
                changed += 1
    # Increase event levels on the quiet recordings in addition to mastering
    # samples. Do not alter the already-approved drink effects.
    for key,value in clang.items():
        if key.startswith(('select.sword.','swing.sword.','select.axe.','swing.axe.')) or key in (
            'select.bow','use.bow.charge','select.crossbow','swing.mace','select.mace',
            'select.trident','swing.trident','use.trident.charge'
        ) or key.startswith('hit.mace.') or key.startswith(('hit.weapon_n_tool.sword.',
                                                           'hit.weapon_n_tool.axe.')):
            for entry in value.get('sounds',[]):
                if isinstance(entry,dict):
                    entry['volume'] = max(float(entry.get('volume',1)), 0.93 if 'select.' in key else 0.98)
    put(clang,cp)
    mc,mp=read('minecraft')
    for event,value in mc.items():
        if event.startswith(('entity.arrow.','item.crossbow.','item.trident.','item.mace.')):
            for entry in value.get('sounds',[]):
                if isinstance(entry,dict) and str(entry.get('name','')).startswith('shatsufresh:'):
                    entry['volume']=max(float(entry.get('volume',1)),0.97)
    put(mc,mp)
    food,fp=read('shatsu_food_audio')
    for event,value in food.items():
        if event in ('food.honey','food.milk','food.potion'):
            continue
        for entry in value.get('sounds',[]):
            if isinstance(entry,dict):
                entry['volume']=0.92
    put(food,fp)
    return changed


def update(root):
    root=Path(root)
    created=create_tools(root)
    boosted=master_new_recordings(root)
    linked=upgrade_events(root)
    print(f'Shatsu tool upgrade: {created} dedicated OGG variants; '
          f'{boosted} mastered weapon/food recordings; {linked} tool event overrides')
    return created, boosted, linked


if __name__ == '__main__':
    import sys
    update(Path(sys.argv[1]))
