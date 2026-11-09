#!/usr/bin/env python3
"""Validate Java source structure and the optional v7.3 audio bundle."""
import json
import hashlib
import zipfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
resources=ROOT/'src/main/resources'
metadata=json.loads((resources/'fabric.mod.json').read_text())
assert metadata['id']=='shatsusounds'
assert metadata['depends']['minecraft']=='~26.3'
for cls in metadata['entrypoints']['client']+metadata['entrypoints']['modmenu']:
    path=ROOT/'src/main/java'/Path(*cls.split('.')).with_suffix('.java')
    assert path.exists(),f'Missing source: {path}'
for mixin in metadata.get('mixins',[]):
    assert (resources/mixin).is_file(),f'Missing mixin descriptor: {mixin}'
bundle=ROOT/'audio/ShatsuSounds_v7.3_assets_only.zip'
if bundle.is_file():
    expected='5075b3d3d606409f95e97cb61da81246ac3c011f9de7bc35b59042b372debc36'
    assert hashlib.sha256(bundle.read_bytes()).hexdigest()==expected, 'Audio bundle checksum mismatch'
    with zipfile.ZipFile(bundle) as archive:
        assert archive.testzip() is None,'Damaged audio archive'
        names=archive.namelist()
        assert 'shatsu_catalog.json' in names,'Missing event catalog'
        assert any(n.endswith('.ogg') for n in names),'No audio files'
        catalog=json.loads(archive.read('shatsu_catalog.json'))
        assert isinstance(catalog,list) and len(catalog)>0
        print(f'Audio bundle validated: {len(names)} entries, {len(catalog)} event catalog rows')
else:
    print('WARNING: Audio bundle has not been uploaded. CI artifact is source/UI only.')
print('Fabric 26.3 source structure validated')
