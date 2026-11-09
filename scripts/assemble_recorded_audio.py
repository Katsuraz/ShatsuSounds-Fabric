#!/usr/bin/env python3
"""Assemble CC0 sound recordings stored as small GitHub text blobs."""
from pathlib import Path
import sys, base64, hashlib, zipfile
SOURCE=Path(__file__).resolve().parents[1] / 'audio/shatsu_v03'
SHA256='22eb499b48a9d6c25a503fe66a3adead80b67c8008b350a943d3bcf3fc1e3a4f'

def assemble(target, parts=SOURCE):
    target=Path(target)
    pieces=sorted(Path(parts).glob('part_*.b64'))
    if len(pieces)!=9: raise RuntimeError(f'Expected nine audio segments, got {len(pieces)}')
    target.parent.mkdir(parents=True,exist_ok=True)
    with target.open('wb') as out:
        for piece in pieces:
            out.write(base64.b64decode(piece.read_text('ascii')))
    digest=hashlib.sha256(target.read_bytes()).hexdigest()
    if digest!=SHA256: raise RuntimeError(f'Shatsu audio checksum mismatch: {digest}')
    with zipfile.ZipFile(target) as z:
        assert z.testzip() is None
        clips=[x for x in z.namelist() if x.endswith('.ogg')]
        if len(clips)<150: raise RuntimeError('Missing curated audio recordings')
    print(f'Audio bundle verified: {len(clips)} OGG files, {target.stat().st_size} bytes')
if __name__=='__main__': assemble(sys.argv[1])
