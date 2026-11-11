#!/usr/bin/env python3
"""Pixel Eco Shatsu Retro Alpha.2: tactile arcade-fantasy repair for 19 real Minecraft use events.

Non-destructive: only those 19 events in minecraft/sounds.json changed; 57 new
original Shatsu-texture-assisted variants. Keeps all previously approved OGGs
bit-for-bit, all existing Java code, and all other mod-resource definitions.

Usage: python retro_interaction_polish.py <assets-root-containing-assets/>
Requires numpy/scipy/soundfile. Also integrated into real Shatsu Gradle audio pipeline.
"""
import hashlib, json, math, sys
from collections import Counter
from pathlib import Path
import numpy as np
from scipy import signal
import soundfile as sf

RATE=44100
# (real vanilla event, semantic family, seconds, volume)
EVENTS=[
 ('item.hoe.till', 'till', .255, .65),
 ('item.shovel.flatten', 'flatten', .305, .61),
 ('item.axe.strip', 'strip', .245, .65),
 ('item.axe.scrape', 'scrape', .255, .55),
 ('item.axe.wax_off', 'wax_off', .255, .52),
 ('item.bone_meal.use', 'bone_meal', .330, .49),
 ('item.brush.brushing.generic','brush_generic', .300, .50),
 ('item.brush.brushing.sand','brush_sand', .300, .49),
 ('item.brush.brushing.gravel','brush_gravel', .320, .53),
 ('item.brush.brushing.sand.complete','brush_sand_reveal', .345, .54),
 ('item.brush.brushing.gravel.complete','brush_gravel_reveal', .360, .55),
 ('item.flintandsteel.use', 'flint', .235, .50),
 ('item.shears.snip', 'shears', .205, .52),
 ('block.composter.fill', 'compost_fill', .255, .48),
 ('block.composter.fill_success', 'compost_success', .325, .52),
 ('block.composter.ready', 'compost_ready', .340, .47),
 ('block.composter.empty', 'compost_empty', .285, .50),
 ('item.book.page_turn', 'book_turn', .285, .49),
 ('item.book.put','book_put', .210, .48)
]
assert len(EVENTS)==len({x[0] for x in EVENTS})==19

def seed(text):
    return int.from_bytes(hashlib.blake2b(text.encode('utf-8'),digest_size=8).digest(),'little')

def filtered(x,rate,lo=None,hi=None):
    x=np.asarray(x,dtype='float64')
    if lo and lo < rate*.45: x=signal.sosfilt(signal.butter(2,lo,btype='highpass',fs=rate,output='sos'),x)
    if hi and hi < rate*.45: x=signal.sosfilt(signal.butter(3,hi,btype='lowpass',fs=rate,output='sos'),x)
    return np.asarray(x,dtype=np.float64)

def tone(t,frequency,decay,attack=.004,sweep=0):
    ph=2*np.pi*(frequency*t+sweep*t*t)
    return np.sin(ph)*(1-np.exp(-t/max(attack,.0002)))*np.exp(-decay*t)

def transient(t,offset,width,amp):
    dt=t-offset
    return amp*np.where(dt>0, np.exp(-np.maximum(dt,0)/width), 0.)

def make_sound(kind,variation,recorded,orig_rate,duration):
    s=seed(f'PixelEco:{kind}:{variation}:2026')
    rng=np.random.default_rng(s)
    n=int(RATE*duration)
    t=np.arange(n,dtype=np.float64)/RATE
    # Three grain textures: softened earth, crisp fibrous, bright paper/metal;
    # each is separately generated and filtered rather than reusing a single beep.
    white=rng.standard_normal(n)
    earth=filtered(white,RATE,90,1150)
    fibrous=filtered(white,RATE,450,4800)
    bright=filtered(white,RATE,1600,7400)
    # Normalize source recording to modest level; keep a recognizable Shatsu
    # texture without allowing loud sound peaks from inherited samples.
    src=recorded.mean(axis=1) if recorded.ndim==2 else recorded
    if orig_rate!=RATE:
        g=math.gcd(int(orig_rate),RATE)
        src=signal.resample_poly(src,RATE//g,int(orig_rate)//g)
    if len(src)<100: raise RuntimeError('Too short Shatsu reference for '+kind)
    src=src.astype(np.float64)
    src=filtered(src,RATE,70,7000)
    shift=min(max(0,len(src)-1),int((.008*variation)*RATE))
    indexes=np.minimum(shift+np.arange(n),len(src)-1)
    texture=src[indexes]
    # Silence inherited tails: make event-specific envelopes so tools don't
    # sound like unrelated block hits from the source event.
    ebase=np.exp(-t*(12+variation*1.25))*(1-np.exp(-t*240))
    record_rms=np.sqrt(np.mean(texture**2))+1e-6
    texture=texture*(.060/record_rms)*ebase
    x=np.zeros(n,dtype=np.float64)
    v=variation-1
    if kind=='till':
        # Two short soil cuts with a crumbly turn on the second impulse.
        hits=[(.005,.85),(.063+.011*v,.58),(.135-.004*v,.27)]
        for st,amp in hits:
            x += amp*transient(t,st,.026+v*.003,.066)*earth
        x+=.085*tone(t,146+11*v,30,.005,-20)
        x+=.18*texture
    elif kind=='flatten':
        # Longer, low scraping compression rather than the hoe's two cuts.
        flutter=1+.13*np.sin(2*np.pi*(24+v*5)*t)
        x+=.095*fibrous*np.exp(-t*(11+v))*flutter*(1-np.exp(-t*140))
        x+=.075*earth*np.exp(-t*14)+.060*tone(t,116+9*v,22,.015,-40)
        x+=.26*texture
    elif kind=='strip':
        x+=.105*fibrous*np.exp(-t*(25+2*v))*(1-np.exp(-t*850))
        x+=.040*earth*np.exp(-t*13)
        for st in (.019,.061+.007*v):x+=transient(t,st,.009,.075)*fibrous
        x+=.17*texture
    elif kind in ('scrape','wax_off'):
        factor=.88 if kind=='wax_off' else 1.0
        x+=factor*.085*fibrous*np.exp(-t*(13+v))*(1-np.exp(-t*170))
        x+=factor*.055*bright*np.exp(-t*(18+v))*(1-np.exp(-t*360))
        x+=factor*.052*tone(t,(620 if kind=='scrape' else 390)+39*v,28,.006,-240)
        x+=factor*.14*texture
    elif kind=='bone_meal':
        x+=.043*fibrous*np.exp(-t*11)*(1-np.exp(-t*80))
        x+=.065*earth*np.exp(-t*22)
        for j,st in enumerate((.05,.095,.155,.218)):
            shift=st+((s>>(j*6))%11)/1100
            dx=np.maximum(0,t-shift)
            x+=(t>=shift)*.019*np.exp(-dx*(34+j*8))*np.sin(2*np.pi*(840+122*j+65*v)*dx)
        x+=.09*texture
    elif kind.startswith('brush_'):
        hard='gravel' in kind
        reveal='reveal' in kind
        x+=( .075 if hard else .056 )*fibrous*np.exp(-t*(13+v))*(1-np.exp(-t*150))
        x+=( .047 if hard else .034 )*earth*np.exp(-t*13)
        for st in (.014,.092+.004*v,.165-.004*v):
            dx=t-st
            x+=(dx>=0)*(.016 if hard else .012)*np.exp(-np.maximum(dx,0)*45)*bright
        if reveal:
            x+=.048*tone(t,(790 if hard else 990)+42*v,22,.009,70)
        x+=.12*texture
    elif kind=='flint':
        x+=.17*bright*np.exp(-t*(54+v*4))*(1-np.exp(-t*2200))
        for j,st in enumerate((.010,.028,.055)):
            dx=np.maximum(t-st,0)
            x+=(t>=st)*(.026+.004*j)*np.exp(-dx*55)*np.sin(2*np.pi*(1000+270*j+45*v)*dx)
        x+=.11*texture
    elif kind=='shears':
        x+=.09*bright*np.exp(-t*43)*(1-np.exp(-t*1800))
        for j,st in enumerate((.009,.044+.006*v)):
            dx=np.maximum(t-st,0)
            x+=(t>=st)*.072*np.exp(-dx*(80+j*15))*np.sin(2*np.pi*(750+180*j+36*v)*dx)
        x+=.10*texture
    elif kind.startswith('compost_'):
        if kind=='compost_fill':
            x+=.093*earth*np.exp(-t*23)+.036*texture
            x+=.065*tone(t,128+11*v,35,.006,10)
        elif kind=='compost_success':
            x+=.095*earth*np.exp(-t*18)+.034*fibrous*np.exp(-t*19)
            x+=.080*tone(t,260+17*v,25,.012,80)
            x+=.050*tone(t,390+29*v,24,.070,110)
        elif kind=='compost_ready':
            x+=.044*earth*np.exp(-t*17)+.058*tone(t,360+14*v,24,.017,85)
            x+=.036*tone(t,525+20*v,23,.065,75)
        elif kind=='compost_empty':
            x+=.104*earth*np.exp(-t*13)*(1-np.exp(-t*125))
            x+=.042*fibrous*np.exp(-t*24)
            x+=.063*tone(t,135+12*v,28,.008,-34)
    elif kind=='book_turn':
        x+=.068*fibrous*np.exp(-t*14)*(1-np.exp(-t*135))
        for st in (.014,.065+.008*v,.132):
            dx=t-st
            x+=(dx>=0)*.050*np.exp(-np.maximum(dx,0)*31)*earth
        x+=.13*texture
    elif kind=='book_put':
        x+=.080*earth*np.exp(-t*31)+.036*tone(t,190+22*v,31,.004,-22)
        x+=.12*texture
    else:
        raise RuntimeError('Unknown interaction family '+kind)
    # Coarse material harmonic color is subtle, not a piercing game beep.
    x=filtered(x,RATE,85,5300 if kind=='flint' else (6200 if kind in ('shears','scrape') else 5150))
    x-=np.mean(x)
    nattack=min(int(.010*RATE),n//14)
    if nattack:x[:nattack]*=np.linspace(0,1,nattack)
    fade=min(int(.048*RATE),n//6)
    if fade:x[-fade:]*=np.linspace(1,0,fade)
    # Dynamic range + anti-annoyance limits. Scale per event to similar soft RMS.
    target=.035 if kind in ('brush_generic','brush_sand','book_turn') else (.046 if kind in ('flint','shears','scrape') else .043)
    rms=np.sqrt(np.mean(x*x))+1e-9
    x*=min(3.0,target/rms)
    peak=np.max(np.abs(x))
    if peak>.48:x*=.48/peak
    return x.astype('float32')

def apply(root):
    root=Path(root)
    manifest=root/'assets/minecraft/sounds.json'
    old=json.loads(manifest.read_text('utf-8'))
    before=json.dumps(old,sort_keys=True)
    modified={};metrics=[]
    for event,kind,duration,volume in EVENTS:
        assert event in old, f'Missing real vanilla sound event: {event}'
        options=old[event].get('sounds',[])
        assert len(options)>=3,(event,len(options))
        references=[]
        for variant in range(1,4):
            entry=options[(variant-1)%len(options)]
            name=entry if isinstance(entry,str) else entry['name']
            ns,rel=name.split(':',1)
            source=root/f'assets/{ns}/sounds/{rel}.ogg'
            assert source.is_file(),source
            a,rate=sf.read(str(source),dtype='float32',always_2d=True)
            sample=make_sound(kind,variant,a,rate,duration)
            relnew=f'physical/{kind}/{variant}'
            dest=root/f'assets/pixelecoretro/sounds/{relnew}.ogg'
            dest.parent.mkdir(parents=True,exist_ok=True)
            sf.write(str(dest),sample,RATE,format='OGG',subtype='VORBIS')
            test,received=sf.read(str(dest),dtype='float32')
            assert received==RATE and np.all(np.isfinite(test)),dest
            peak=float(np.max(np.abs(test))); rms=float(np.sqrt(np.mean(test*test)))
            assert .001<rms<.12 and peak<.58,(event,variant,rms,peak)
            references.append({'name':'pixelecoretro:'+relnew,'type':'file','volume':volume,'pitch':1.0})
            metrics.append({'event':event,'variation':variant,'kind':kind,
                            'duration':round(len(test)/RATE,3),'rms':round(rms,5),'peak':round(peak,4),
                            'source_sample':name})
        old[event]={**({'subtitle':old[event]['subtitle']} if 'subtitle' in old[event] else {}),
                    'replace':True,'sounds':references}
        modified[event]=kind
    assert json.dumps(old,sort_keys=True)!=before
    manifest.write_text(json.dumps(old,ensure_ascii=False,indent=2)+'\n','utf-8')
    report={'version':'retro-alpha.2','modified_event_count':len(modified),
            'new_audio_variants':len(metrics),'events':modified,
            'metrics':metrics,
            'invariants':['Shatsu event IDs unchanged','all preexisting audio preserved',
              'Clangor/Sounds/Custom Block Sounds untouched','verified decoding and clipping',
              'actual Minecraft runtime not tested']}
    (root/'pixeleco_interactions_qa.json').write_text(json.dumps(report,indent=2)+'\n','utf-8')
    print('PIXEL ECO SHATSU RETRO INTERACTIONS:',len(modified),'real sound events,',len(metrics),'original retro tactile variants')
    return report

if __name__=='__main__':
    if len(sys.argv)!=2:raise SystemExit(__doc__)
    apply(sys.argv[1])
