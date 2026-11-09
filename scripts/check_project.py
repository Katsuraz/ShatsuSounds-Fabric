#!/usr/bin/env python3
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
resources=ROOT/'src/main/resources'
metadata=json.loads((resources/'fabric.mod.json').read_text())
assert metadata['id']=='shatsusounds'
assert metadata['depends']['minecraft']=='~26.3'
for cls in metadata['entrypoints']['client']+metadata['entrypoints']['modmenu']:
    path = ROOT/'src/main/java'/Path(*cls.split('.')).with_suffix('.java')
    assert path.exists(),f'Missing source: {path}'
assert (resources/'shatsu_catalog.json').exists()
catalog=json.loads((resources/'shatsu_catalog.json').read_text())
assert isinstance(catalog,list)
assets = resources/'assets'
print(f'Source structure OK; catalog records: {len(catalog)}')
if not assets.exists():
    print('WARNING: Shatsu v7.3 audio assets not imported into this repository yet.')
