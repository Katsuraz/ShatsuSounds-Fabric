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
  self.assertEqual(len(files),425) # plus 56 ASMR liquid, wood, ranged and magic variants
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
 def test_tool_family_audio(self):
  for tool in ('pickaxe','shovel','hoe'):
   for mat in ('wooden','stone','copper','iron','gold','diamond','netherite','general'):
    for action in ('swing','select'):
     event=f'{action}.{tool}.{mat}'
     self.assertIn(event,self.clangor)
     sounds=self.clangor[event]['sounds']
     self.assertEqual(len(sounds),2,event)
     self.assertGreaterEqual(sounds[0]['volume'],0.90,event)
     for i in (1,2):
      self.assertTrue((ROOT/'shatsufresh/sounds'/f'tool/{tool}/{action}/{mat}/{i}.ogg').exists())
   for kind in ('weak','strong','crit','knockback','sweep'):
    event=f'hit.weapon_n_tool.{tool}.{kind}'
    self.assertTrue(self.clangor[event]['sounds'][0]['name'].startswith(f'shatsufresh:tool/{tool}/impact/'))
 def test_quieter_approved_voices_are_not_replaced(self):
  # Preserving the approved milk/honey/potion sources and vanilla mob voices
  for event in ('food.honey','food.milk','food.potion'):
   self.assertTrue(self.food[event]['sounds'][0]['name'].startswith('shatsufresh:food/'))
  self.assertFalse(any(k.startswith('entity.zombie.') for k in self.preview))
 def test_food_and_flight_events_are_discoverable(self):
  catalog=json.loads((ROOT.parent/'shatsu_catalog.json').read_text('utf8'))
  self.assertIn({'id':'shatsu_food_audio:food.apple','category':'food',
                 'variations':3,'namespace':'shatsu_food_audio'},catalog)
  self.assertIn('preview.arrow.fly',self.preview)
  self.assertTrue((ROOT/'shatsufresh/sounds/arrow/fly/1.ogg').is_file())
 def test_real_spear_events_and_crisp_bow(self):
  for key in ('item.spear.attack','item.spear.hit','item.spear.use',
              'item.spear_wood.attack','item.spear_wood.hit','item.spear_wood.use',
              'item.spear.lunge_1','item.spear.lunge_2','item.spear.lunge_3'):
   self.assertTrue(self.mc[key]['sounds'][0]['name'].startswith('shatsufresh:premium/spear/'),key)
  for key in ('select.spear','swing.spear','hit.spear.weak','hit.spear.strong','hit.spear.crit'):
   self.assertTrue(self.clangor[key]['sounds'][0]['name'].startswith('shatsufresh:premium/spear/'),key)
  for key in ('entity.arrow.shoot','item.crossbow.shoot','item.crossbow.loading_start','item.trident.throw'):
   self.assertTrue(self.mc[key]['sounds'][0]['name'].startswith('shatsufresh:premium/'),key)
 def test_real_distinct_food_profiles(self):
  import hashlib
  names=[]
  for kind in ('apple','bread','soup','cooked_meat','cookie','raw_fish'):
   name=self.food['food.'+kind]['sounds'][0]['name']
   self.assertEqual(name,'shatsufresh:premium/food/'+kind+'/1')
   names.append(hashlib.sha256((ROOT/'shatsufresh/sounds/premium/food'/kind/'1.ogg').read_bytes()).hexdigest())
  self.assertEqual(len(set(names)),len(names))
  for kind in ('honey','milk','potion'):
   self.assertNotIn('premium/',self.food['food.'+kind]['sounds'][0]['name'])
 def test_magical_xp_variations(self):
  xp=self.mc['entity.experience_orb.pickup']['sounds']
  self.assertEqual(len(xp),4)
  for sound in xp:
   self.assertTrue(sound['name'].startswith('shatsufresh:premium/magic/experience/'))
  self.assertEqual(len(self.mc['entity.player.levelup']['sounds']),3)
 def test_asmr_buckets_drinks_and_magic(self):
  for material in ('water','lava'):
   for action in ('fill','empty'):
    event='item.bucket.'+action+('_lava' if material=='lava' else '')
    value=self.mc[event]['sounds']
    self.assertEqual(len(value),4,event)
    self.assertTrue(value[0]['name'].startswith('shatsufresh:asmr/bucket/'+material+'/'+action+'/'))
  liquid=json.loads((ROOT/'shatsu_liquids/sounds.json').read_text('utf8'))
  for item in ('honey','milk','potion','water'):
   self.assertEqual(len(liquid['drink.'+item]['sounds']),4)
   for i in (1,2,3,4):
    self.assertTrue((ROOT/'shatsufresh/sounds/asmr/drink'/item/f'{i}.ogg').is_file())
  for item in ('golden_apple','enchanted_apple','golden_carrot','chorus_fruit'):
   self.assertIn('preview.asmr.magic.'+item,self.preview)
  self.assertEqual(len(self.mc['block.wood.break']['sounds']),4)
 def test_asmr_sound_catalog_categories(self):
  cat=json.loads((ROOT.parent/'shatsu_catalog.json').read_text('utf8'))
  self.assertIn({'id':'shatsu_liquids:drink.honey','category':'liquids',
                 'variations':4,'namespace':'shatsu_liquids'},cat)
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
   self.assertTrue(self.food['food.'+kind]['sounds'][0]['name'].startswith('shatsufresh:'),kind)
  for material in ('leather','chain','iron','gold','diamond','netherite','copper'):
   self.assertTrue(self.mc['item.armor.equip_'+material]['sounds'][0]['name'].startswith('shatsufresh:armor/'))
 def test_magic_and_inventory(self):
  self.assertEqual(self.mc['entity.experience_orb.pickup']['sounds'][0]['name'],'shatsufresh:premium/magic/experience/1')
  self.assertIn('preview.inventory.totem',self.preview)
  rule=json.loads((ROOT/'minecraft/sounds/items/shatsu_v7_totem_of_undying.json').read_text('utf8'))
  self.assertEqual(rule['soundEvent'],'shatsufresh:preview.inventory.totem')
  self.assertLessEqual(rule['volume'],.5)
if __name__=='__main__':unittest.main()
