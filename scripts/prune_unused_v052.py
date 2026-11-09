#!/usr/bin/env python3
"""Prune unused generated OGGs, keeping a hash-manifested archive outside JAR.
CustomBlockSounds intentionally supports duplicate block keys: never rewrite it.
"""
import csv
import hashlib
import io
import json
import zipfile
from pathlib import Path

def clean(staged_root, output_archive):
    root=Path(staged_root)
    events={}
    for p in (root/'assets').glob('*/sounds.json'):
        ns=p.parent.name
        for key,val in json.loads(p.read_text('utf8')).items():
            events[(ns,key)]=val
    referenced=set()
    def visit(ns,key,seen):
        if (ns,key) in seen:return
        seen.add((ns,key))
        val=events.get((ns,key))
        if not isinstance(val,dict):return
        for entry in val.get('sounds',[]):
            if isinstance(entry,str):s,kind=entry,'file'
            elif isinstance(entry,dict):s,kind=entry.get('name',''),entry.get('type','file')
            else:continue
            n,p=s.split(':',1) if ':' in s else ('minecraft',s)
            if kind=='event':visit(n,p,seen)
            else:referenced.add('assets/'+n+'/sounds/'+p+'.ogg')
    for ns,key in events:visit(ns,key,set())
    orphan=[]
    for file in (root/'assets').glob('*/sounds/**/*.ogg'):
        rel=file.relative_to(root).as_posix()
        if rel not in referenced and not any(x in rel for x in ('firework','inventory/open','ui/open')):
            orphan.append((rel,file))
    dest=Path(output_archive);dest.parent.mkdir(parents=True,exist_ok=True)
    rows=[('original_path','bytes','sha256')]
    with zipfile.ZipFile(dest,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as archive:
        for rel,p in orphan:
            blob=p.read_bytes()
            rows.append((rel,len(blob),hashlib.sha256(blob).hexdigest()))
            archive.writestr(rel,blob)
        report=io.StringIO();csv.writer(report).writerows(rows)
        archive.writestr('ARCHIVED_AUDIO_MANIFEST.csv',report.getvalue())
        archive.writestr('README.txt','Original unused Shatsu recordings. Not an installable mod.\n')
    for _,p in orphan:p.unlink()
    print(f'Shatsu 0.5.2 archived {len(orphan)} unreferenced audio files outside the mod JAR.')
    return len(orphan)

if __name__=='__main__':
    import sys
    clean(sys.argv[1],sys.argv[2])
