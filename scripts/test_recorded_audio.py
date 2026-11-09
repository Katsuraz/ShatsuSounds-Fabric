#!/usr/bin/env python3
"""Integration checks for directly uploaded original CC0 Shatsu sound recordings."""
import json, pathlib, subprocess, unittest
ROOT=pathlib.Path(__file__).resolve().parents[1]/'build/generated/shatsu-assets/assets'
class RecordedAudioTests(unittest.TestCase):
 @classmethod
 def setUpClass(cls):
  cls.mc=json.loads((ROOT/'minecraft/sounds.json').read_text('utf8'))
  cls.clangor=json.loads((ROOT/'clangor/sounds.json').read_text('utf8'))
  cls.food=json.loads((ROOT/'shatsu_food_audio/sounds.json').read_text('utf8'))
  cls.preview=json.loads((ROOT/'shatsufresh/sounds.json').read_text('utf8'))
 def test_recordings_and_mono(self):
  files=list((ROOT/'shatsufresh/sounds').rglob('*.ogg'))
  self.assertEqual(len(files),166)
  for p in files:
   self.assertGreater(p.stat().st_size,300,str(p))
  for p in (files[0],files[-1],next(x for x in files if 'mace/' in str(x)),
            next(x for x in files if 'food/drink/' in str(x))):
   ff=subprocess.check_output(['ffprobe','-v','error','-select_streams','a:0',
     '-show_entries','stream=channels','-of','default=noprint_wrappers=1:nokey=1',str(p)],text=True).strip()
   self.assertEqual(ff,'1',str(p))
 def test_actual_references_exist(self):
  total=0
  for data in (self.mc,self.clangor,self.food,self.preview):
   for event,config in data.items():
    for item in config.get('sounds',[]):
     name=item.get('name','') if isinstance(item,dict) else str(item)
     if not name.startswith('shatsufresh:'):continue
     rel=name.split(':',1)[1]
     self.assertTrue((ROOT/'shatsufresh/sounds'/f'{rel}.ogg').is_file(),(event,name))
     total+=1
  self.assertGreater(total,150)
 def test_weapon_chain(self):
  for id in ('entity.arrow.shoot','entity.arrow.hit','entity.arrow.hit_player',
             'item.crossbow.shoot','item.crossbow.loading_start',
             'item.crossbow.loading_middle','item.crossbow.loading_end',
             'item.trident.throw','item.trident.hit','item.trident.hit_ground',
             'item.mace.smash_air','item.mace.smash_ground','item.mace.smash_ground_heavy',
             'item.shield.block','item.shield.break'):
   self.assertTrue(self.mc[id]['sounds'][0]['name'].startswith('shatsufresh:'),id)
  for k in ('hit.mace.strong','hit.mace.crit','swing.mace','select.mace',
            'swing.sword.iron','swing.sword.wooden','swing.axe.netherite',
            'select.axe.gold','use.bow.charge'):
   self.assertTrue(self.clangor[k]['sounds'][0]['name'].startswith('shatsufresh:'),k)
 def test_drinks_food_and_armor(self):
  for key in ('entity.generic.drink','item.honey_bottle.drink'):
   self.assertTrue(self.mc[key]['sounds'][0]['name'].startswith('shatsufresh:food/'))
  for kind in ('soup','honey','milk','potion','apple','bread','cookie','cooked_meat'):
   self.assertTrue(self.food['food.'+kind]['sounds'][0]['name'].startswith('shatsufresh:food/'))
  for material in ('leather','chain','iron','gold','diamond','netherite','copper'):
   self.assertTrue(self.mc['item.armor.equip_'+material]['sounds'][0]['name'].startswith('shatsufresh:armor/'))
 def test_magic_and_inventory(self):
  self.assertEqual(self.mc['entity.experience_orb.pickup']['sounds'][0]['name'],'shatsufresh:magic/xp/1')
  self.assertIn('preview.inventory.totem',self.preview)
if __name__=='__main__':unittest.main()
