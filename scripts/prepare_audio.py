#!/usr/bin/env python3
"""Stage the uploaded Shatsu audio; repair quiet block hits and ordinary-item fallback.
Changes only block HIT OGGs, Custom Block Sounds HIT gain, and Clangor generic
HIT definitions. The original ZIP and other approved sounds remain untouched.
"""
import copy
import json
from pathlib import Path, PurePosixPath
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile

TARGET_RMS_DB = -20.9
MIN_GAIN_DB = 2.0
MAX_GAIN_DB = 9.0
BOOST_FAMILIES = {
    'stone', 'glass', 'obsidian', 'end', 'slime', 'sponge', 'bamboo',
    'anvil', 'chest', 'redstone', 'sand', 'water', 'snow', 'deepslate',
    'generic', 'clay', 'bone', 'metal', 'ceramic', 'ice', 'sculk',
}
BOOST_BLOCKMAP = {
    'stone', 'glass', 'obsidian', 'end', 'slime', 'sponge', 'bamboo',
    'anvil', 'chest', 'redstone', 'sand', 'water', 'snow', 'deepslate',
}
KINDS = ('weak', 'strong', 'crit', 'knockback', 'sweep')


def run(*args):
    return subprocess.run(args, text=True, capture_output=True, check=True)


def normalize(path):
    result = run('ffmpeg', '-nostdin', '-hide_banner', '-i', str(path),
                 '-af', 'volumedetect', '-f', 'null', '-')
    value = re.search(r'mean_volume:\s*([-\d.]+) dB', result.stderr)
    if not value:
        raise RuntimeError(f'Cannot read hit sound level: {path}')
    gain = min(MAX_GAIN_DB, max(0, TARGET_RMS_DB - float(value.group(1))))
    if gain < MIN_GAIN_DB:
        return False
    with tempfile.TemporaryDirectory() as temp:
        replacement = Path(temp) / 'hit.ogg'
        run('ffmpeg', '-nostdin', '-loglevel', 'error', '-y', '-i', str(path),
            '-af', f'volume={gain:.2f}dB,alimiter=limit=0.90:level=false',
            '-c:a', 'libvorbis', '-q:a', '4', str(replacement))
        if replacement.stat().st_size < 100:
            raise RuntimeError(f'Audio conversion failed: {path}')
        shutil.copyfile(replacement, path)
    return True


def patch_custom_block_sounds(root):
    path = root / 'assets/customblocksounds/config/blockmap.json'
    data = json.loads((root / 'assets/shatsu4/sounds.json').read_text('utf-8'))
    # Custom Block Sounds explicitly supports duplicate block keys, one per
    # action. NEVER parse/re-dump this file through a JSON dictionary.
    lines, changed, hit_count = [], 0, 0
    for line in path.read_text('utf-8').splitlines(keepends=True):
        if 'type=hit' in line:
            hit_count += 1
            match = re.search(r'sound=shatsu4:(block\.[a-z0-9_]+\.hit)', line)
            if match:
                sounds = data.get(match.group(1), {}).get('sounds', [])
                if sounds:
                    sample = sounds[0]
                    name = sample.get('name', '') if isinstance(sample, dict) else sample
                    family = re.search(r'shatsuv7:block/([^/]+)/hit_', name)
                    if family and family.group(1) in BOOST_BLOCKMAP:
                        line, n = re.subn(r'volume=0\.71(?=,|")', 'volume=0.93', line, count=1)
                        changed += n
        lines.append(line)
    if hit_count != 1047 or changed < 100:
        raise RuntimeError(f'Unexpected block mapping count: hits={hit_count}, patched={changed}')
    path.write_text(''.join(lines), encoding='utf-8')
    return changed


def patch_generic_hits(root):
    path = root / 'assets/clangor/sounds.json'
    data = json.loads(path.read_text('utf-8'))
    for kind in KINDS:
        empty = data[f'hit.empty_hand.{kind}']
        for prefix in ('hit.weapon_n_tool.general.', 'hit.rods.'):
            data[prefix + kind] = copy.deepcopy(empty)
    # Special sword/axe/pickaxe/shovel/hoe/mace/spear sounds are unchanged.
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', 'utf-8')


def validate(root):
    for namespace in ('minecraft', 'shatsu4', 'clangor'):
        events = json.loads((root / f'assets/{namespace}/sounds.json').read_text('utf-8'))
        for event, sound in events.items():
            if not (event.endswith('.hit') or event.startswith('hit.')):
                continue
            for sample in sound.get('sounds', []):
                name = sample.get('name', '') if isinstance(sample, dict) else sample
                if ':' in name:
                    ns, rel = name.split(':', 1)
                    file = root / f'assets/{ns}/sounds/{rel}.ogg'
                    if not file.is_file():
                        raise RuntimeError(f'Missing {namespace}:{event} -> {file}')



def qualify_local_sound_references(root):
    """Minecraft resolves unqualified file names under minecraft:, not under
    the namespace of the sounds.json file. Fix only files we can prove exist.
    This corrects legacy bow/trident sounds without changing any OGG bytes or
    rerouting intentional vanilla/event references.
    """
    changes = {}
    for path in sorted((root / 'assets').glob('*/sounds.json')):
        namespace = path.parent.name
        events = json.loads(path.read_text('utf-8'))
        modified = 0
        for event, definition in events.items():
            if not isinstance(definition, dict):
                continue
            sounds = definition.get('sounds', [])
            if not isinstance(sounds, list):
                continue
            for index, original in enumerate(sounds):
                obj = original if isinstance(original, dict) else None
                if obj is not None and obj.get('type', 'file') != 'file':
                    continue
                name = obj.get('name', '') if obj is not None else original
                if not isinstance(name, str) or ':' in name or not name:
                    continue
                if not (root / 'assets' / namespace / 'sounds' / (name + '.ogg')).is_file():
                    continue
                fixed_name = namespace + ':' + name
                if obj is not None:
                    obj['name'] = fixed_name
                else:
                    sounds[index] = fixed_name
                modified += 1
        if modified:
            path.write_text(json.dumps(events, indent=2, ensure_ascii=False) + '\n', 'utf-8')
            changes[namespace] = modified
    print('Shatsu namespace repair:', changes)
    return changes



def generate_catalog(root):
    """Build the actual Mod Menu/mixer catalog from the sound events in the JAR."""
    rows = []
    for file in sorted((root / 'assets').glob('*/sounds.json')):
        namespace = file.parent.name
        definitions = json.loads(file.read_text('utf-8'))
        for event, value in definitions.items():
            if event.startswith('block.'):
                if event.endswith('.hit'): category = 'block_hit'
                elif event.endswith('.break'): category = 'block_break'
                elif event.endswith('.place'): category = 'block_place'
                elif event.endswith(('.step', '.fall')): category = 'footsteps'
                else: category = 'block_other'
            elif any(v in event for v in ('experience_orb', 'totem')):
                category = 'magic'
            elif any(v in event for v in ('inventory', 'typing', 'item_click', 'item_drag')):
                category = 'inventory'
            elif any(v in event for v in ('attack', 'hit', 'weapon', 'mace', 'spear', 'trident', 'bow', 'shield')):
                category = 'combat'
            elif event.startswith('food.') or any(v in event for v in ('eat', 'drink', 'honey')):
                category = 'food'
            elif any(v in event for v in ('weather', 'rain', 'thunder', 'explode')):
                category = 'environment'
            else:
                category = 'other'
            rows.append({'id': f'{namespace}:{event}', 'category': category,
                         'variations': len(value.get('sounds', [])), 'namespace': namespace})
    if len(rows) < 6500:
        raise RuntimeError(f'Incomplete Shatsu event catalog: {len(rows)} entries')
    (root / 'shatsu_catalog.json').write_text(json.dumps(rows, ensure_ascii=False), encoding='utf-8')
    return len(rows)

def main():
    if len(sys.argv) != 3:
        sys.exit('Usage: prepare_audio.py ORIGINAL_PACK.zip STAGING_DIRECTORY')
    original, root = Path(sys.argv[1]), Path(sys.argv[2])
    if not original.is_file():
        sys.exit(f'Missing uploaded Shatsu v7.3 pack: {original}')
    if not shutil.which('ffmpeg'):
        sys.exit('ffmpeg is required to normalize mining sounds')
    if root.exists():
        shutil.rmtree(root)
    root.mkdir(parents=True)
    with zipfile.ZipFile(original) as z:
        bad = z.testzip()
        if bad is not None:
            raise RuntimeError(f'Corrupt archive entry: {bad}')
        for entry in z.infolist():
            name = PurePosixPath(entry.filename)
            if name.is_absolute() or '..' in name.parts:
                raise RuntimeError(f'Unsafe archive path: {entry.filename}')
            if entry.is_dir() or not (entry.filename.startswith('assets/') or entry.filename == 'pack.png'):
                continue
            out = root.joinpath(*name.parts)
            out.parent.mkdir(parents=True, exist_ok=True)
            with z.open(entry) as inp, out.open('wb') as dest:
                shutil.copyfileobj(inp, dest)
    block_sounds = root / 'assets/shatsuv7/sounds/block'
    processed = raised = 0
    for family in sorted(BOOST_FAMILIES):
        for path in sorted((block_sounds / family).glob('hit_*.ogg')):
            processed += 1
            raised += bool(normalize(path))
    if processed < 40 or raised < 10:
        raise RuntimeError(f'Suspicious audio count: {processed=} {raised=}')
    changed = patch_custom_block_sounds(root)
    patch_generic_hits(root)
    from polish_audio import polish
    polish(root)
    from apply_recorded_audio import update as apply_recorded
    apply_recorded(root, Path(__file__).resolve().parents[1] / 'build/generated/shatsu-v03-audio.zip')
    from upgrade_action_audio import update as upgrade_action_audio
    upgrade_action_audio(root)
    from upgrade_fantasy_audio import update as upgrade_fantasy_audio
    upgrade_fantasy_audio(root)
    qualify_local_sound_references(root)
    count = generate_catalog(root)
    validate(root)
    print(f'Shatsu repair: {raised}/{processed} quiet hit OGGs normalized; '
          f'{changed}/1047 block hit volumes improved; 10 generic/rods hit events repaired; {count} catalog entries.')


if __name__ == '__main__':
    main()
