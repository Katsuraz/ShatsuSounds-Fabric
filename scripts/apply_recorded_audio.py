#!/usr/bin/env python3
"""Apply Shatsu 0.3 curated CC0 combat, food and armor sounds to generated assets.
Run after prepare_audio.py's original normalization/polish passes.
Never edits original resource archive; only generated assets in the build directory.
"""
import json,zipfile,sys,shutil
from pathlib import Path

ROOT=Path(__file__).resolve().parent
AUDIO_BUNDLE=ROOT/'Shatsu_v0.3_CC0_Audio_Bundle.zip'

def update(root, bundle=AUDIO_BUNDLE):
 root=Path(root);bundle=Path(bundle)
 if not bundle.is_file():raise FileNotFoundError(f'Missing CC0 audio bundle: {bundle}')
 with zipfile.ZipFile(bundle) as z:
  bad=z.testzip()
  if bad:raise RuntimeError('Damaged audio bundle: '+bad)
  for name in z.namelist():
   if not name.startswith('assets/shatsufresh/sounds/') or not name.endswith('.ogg'):continue
   if '..' in Path(name).parts:raise RuntimeError('Unexpected path in audio bundle: '+name)
   dest=root/name;dest.parent.mkdir(parents=True,exist_ok=True)
   with z.open(name) as src,dest.open('wb') as dst:shutil.copyfileobj(src,dst)

 def get(ns):
  p=root/'assets'/ns/'sounds.json';return json.loads(p.read_text('utf-8')),p
 def save(data,path):path.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n','utf-8')
 def ev(group, n=2, volume=.72, pitch=1., replace=True):
  return {'replace':replace,'sounds':[{'name':f'shatsufresh:{group}/{i}','type':'file','volume':volume,'pitch':pitch} for i in range(1,n+1)]}
 mc,mp=get('minecraft'); clang,cp=get('clangor');food,fp=get('shatsu_food_audio')
 changes={}
 vanilla={
  'entity.arrow.shoot':('bow/shoot',.88),
  'entity.arrow.hit':('arrow/impact_block',.75),
  'entity.arrow.hit_player':('arrow/impact_entity',.74),
  'item.crossbow.loading_start':('crossbow/start',.75),
  'item.crossbow.loading_middle':('crossbow/middle',.75),
  'item.crossbow.loading_end':('crossbow/end',.72),
  'item.crossbow.shoot':('crossbow/shoot',.88),
  'item.crossbow.hit':('crossbow/impact',.79),
  'item.crossbow.quick_charge_1':('crossbow/start',.68),
  'item.crossbow.quick_charge_2':('crossbow/middle',.68),
  'item.crossbow.quick_charge_3':('crossbow/end',.68),
  'item.trident.throw':('trident/throw',.84),
  'item.trident.hit':('trident/hit',.80),
  'item.trident.hit_ground':('trident/ground',.80),
  'item.trident.return':('trident/return',.70),
  'item.shield.block':('shield/block',.78),
  'item.shield.break':('shield/break',.85),
  'item.mace.smash_air':('mace/smash_air',.80),
  'item.mace.smash_ground':('mace/smash_ground',.87),
  'item.mace.smash_ground_heavy':('mace/smash_heavy',.91),
  'entity.generic.eat':('food/crisp',.63),
  'entity.generic.drink':('food/drink',.64),
  'item.honey_bottle.drink':('food/honey',.67),
  'entity.experience_orb.pickup':('magic/xp',.57),
 }
 for event,(src,vol) in vanilla.items():
  # Leave genuinely absent events to vanilla; do not invent a v26.3 override.
  if event in mc:mc[event]=ev(src,volume=vol);changes[event]=src
 armor={'leather':'leather','chain':'chain','iron':'iron','gold':'gold','diamond':'diamond','netherite':'netherite','copper':'copper','turtle':'turtle','elytra':'elytra','wolf':'wolf','nautilus':'nautilus','generic':'generic'}
 for suffix,material in armor.items():
  key='item.armor.equip_'+suffix
  if key in mc: mc[key]=ev('armor/'+material,volume=.73);changes[key]='armor/'+material
  unequip='item.armor.unequip_'+suffix
  if unequip in mc:mc[unequip]=ev('armor/'+material,volume=.62);changes[unequip]='armor/'+material
 if 'item.horse_armor.unequip' in mc:mc['item.horse_armor.unequip']=ev('armor/iron',volume=.66)
 save(mc,mp)

 for mat in ('wooden','stone','copper','iron','gold','diamond','netherite','general'):
  for tool in ('sword','axe'):
   for action in ('swing','select'):
    k=f'{action}.{tool}.{mat}'
    if k in clang:clang[k]=ev(f'{tool}/'+('swing' if action=='swing' else 'equip')+f'/{mat}',volume=.73 if action=='swing' else .54)
 for k,src,vol in [
  ('select.bow','bow/nock',.56),('select.crossbow','crossbow/start',.52),
  ('use.bow.charge','bow/draw',.60),('swing.trident','trident/throw',.52),
  ('select.trident','trident/throw',.40),('use.trident.charge','trident/throw',.36),
  ('swing.mace','mace/swing',.70),('select.mace','mace/select',.62),
 ]:
  if k in clang:clang[k]=ev(src,volume=vol)
 for k in ('weak','strong','crit','knockback','sweep'):
  ck=f'hit.mace.{k}'
  if ck in clang:clang[ck]=ev('mace/critical' if k=='crit' else 'mace/hit',volume=.72 if k=='weak' else .83)
  for tool in ('sword','axe'):
   ck=f'hit.weapon_n_tool.{tool}.{k}'
   if ck in clang:clang[ck]=ev('shield/block' if tool=='sword' else 'mace/hit',volume=.52 if k=='weak' else .66)
 save(clang,cp)

 food_profiles={
  'apple':'crisp','carrot':'carrot','beetroot':'crisp','melon':'crisp','berries':'crisp',
  'bread':'soft','cookie':'dry','pie':'soft','potato':'soft','raw_meat':'meat',
  'cooked_meat':'meat','raw_fish':'meat','cooked_fish':'soft','kelp':'dry',
  'chorus':'crisp','soup':'soup','honey':'honey','milk':'milk','potion':'potion'
 }
 for kind,profile in food_profiles.items():
  k='food.'+kind
  if k in food:
   value=ev('food/'+profile,volume=.62)
   value['subtitle']=food[k].get('subtitle','')
   food[k]=value
 save(food,fp)

 # Shield raise is already emitted by Shatsu's one-shot client hook.
 shield_file=root/'assets/shatsupolish/sounds.json'
 shield_data=json.loads(shield_file.read_text('utf-8'))
 shield_data['shield.raise']=ev('shield/raise',volume=.64)
 save(shield_data,shield_file)

 # Discoverable standalone preview events. Flight noises need an actual client hook
 # to follow arrows/tridents: do not claim their automatic gameplay playback here.
 preview={}
 for key in sorted({f'{x.parent.relative_to(root/"assets/shatsufresh/sounds")}' for x in (root/'assets/shatsufresh/sounds').rglob('*.ogg')}):
  first=root/'assets/shatsufresh/sounds'/key/'1.ogg'
  if first.exists():preview['preview.'+key.replace('/','.')]=ev(key,volume=.6)
 newpath=root/'assets/shatsufresh/sounds.json';newpath.parent.mkdir(parents=True,exist_ok=True)
 save(preview,newpath)
 print('Shatsu 0.3: mapped',len(changes),'vanilla events;',len(preview),'new preview events',len(food_profiles),'food profiles; Clangor mappings updated')
 return {'vanilla':len(changes),'preview':len(preview)}
if __name__=='__main__':
 root=Path(sys.argv[1]);bundle=Path(sys.argv[2]) if len(sys.argv)>2 else AUDIO_BUNDLE
 update(root,bundle)