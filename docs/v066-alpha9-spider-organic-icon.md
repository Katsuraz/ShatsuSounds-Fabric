# Shatsu v0.6.6 Alpha.9 — Organic Spider Audio + Mod Menu Icon

## User feedback
- **Do not make Spiders retro.** User specifically rejected retro sound character for Spider and Cave Spider.
- User asked whether more recordings are needed and requested a proper Shatsu mod icon.
- We used existing uploaded sources first; no extra sound libraries are required for this initial revision.

## Delivered experimental cumulative JAR

`Shatsu-v0.6.6-ALPHA.9-Organic-Spiders-Mod-Icon-Fabric26.3.jar`, assembled from the user-visible **Alpha.8 cumulative Clangor overhaul** (the existing functionality remains intact).

### Spider sound changes
- Changed **4 vanilla Minecraft** sound pools: `entity.spider.ambient`, `entity.spider.hurt`, `entity.spider.death`, `entity.spider.step`.
- Changed **4 custom Shatsu cave spider** pools: `shatsufresh:cave_spider.preview.{ambient,hurt,death,step}`.
- Added **24 OGG Vorbis clips** (3 takes per event, 2 mobs × 4 event types × 3). All 44.1 kHz mono, encoded from recorded creature and insect-like source audio, not from retro synthesis.
- Regular Spider character: darker, deeper layered chittering, shorter guttural hurt cries, dry articulated movement and final collapse.
- Cave Spider character: brighter, sharper, quicker chitter/hiss and short brittle skittering.
- These are **edited mixes** of the user-uploaded Spider Chattering and 80 CC0 creature SFX #2, not actual spider animal field recordings.
- **The previous experimental Cave Spider client-side event-routing Mixin was not modified.** Cave-specific playback in real Minecraft remains unverified.

### Mod icon
- Added `assets/shatsusounds/icon.png` (new 256×256 RGBA square minimalist lavender headset/waveform icon).
- Set `fabric.mod.json` `icon` to this asset path.
- No previous pack artwork was replaced.
- The icon is a new original vector-like raster illustration, generated procedurally; no outside art or font files are included.

### Protected
- **All 7,495 existing OGGs** from Alpha.8 preserved byte-for-byte.
- **All 38 existing compiled classes** and previous Clangor sound events retained.
- Unrelated JSON event pools, accepted Golden Apple and tool sounds, Bow/Crossbow/Trident, and existing drinking unchanged.

## Exact JAR diff
Only three pre-existing members changed:
1. `assets/minecraft/sounds.json` (4 spider events only).
2. `assets/shatsufresh/sounds.json` (4 cave spider events only).
3. `fabric.mod.json` (name/version/icon/description).

25 new members: 24 audio OGGs + the new icon.

## Static QA
- ZIP/JAR integrity check: PASS.
- Existing Java and audio-byte preservation: PASS.
- All new audio decodes as 44.1 kHz mono Vorbis; no non-finite samples, no clipping: PASS.
- All eight sound pools have three resolvable references: PASS.
- Icon exists, is a valid 256×256 PNG, and metadata path resolves: PASS.
- Real Minecraft 26.3 in-game gameplay/Mod Menu rendering: **NOT TESTED**.

## Download and rebuild
The conversation's complete Alpha.9 package contains:
- Cumulative JAR
- Original 256×256 Shatsu icon PNG
- 24 sample OGG files
- Numbered 30-second MP3 audition
- `build_shatsu_v066_alpha9_spiders_icon.py` full deterministic script (requires numpy, scipy, soundfile, Pillow, ffmpeg and the uploaded source files)
- `preview_timestamps.txt`, `audio_index.csv`, `validation.json`, `CREDITS.md`, `README.md`.

**Repository caveat:** The normal Gradle asset pipeline still uses an older audio base. The cumulative Alpha.9 Jar has not been reconstructed entirely into Gradle sources; do not substitute the default CI JAR for this experimental sound build.

## License / attribution
- **spookymodem**, `Spider Chattering`, **CC BY 3.0** — https://opengameart.org/content/spider-chattering. Audio edited/filtered/mixed.
- **rubberduck**, `80 CC0 creture SFX #2`, **CC0** — https://opengameart.org/content/80-cc0-creture-sfx-2. Audio edited/mixed.
- Newly generated Shatsu mod icon, original design, no third-party image asset.
- Prior Shatsu bundled sounds require independent redistribution review before a stable/public release.

## Acceptance
The user should listen to the numbered 24-sound audition, then test Spider and Cave Spider ambient, hurt, death and footsteps in a disposable Minecraft 26.3 world. Check the Shatsu Mod Menu listing for the new icon.

**Do not claim Cave Spider separation or icon rendering is confirmed until an actual Minecraft client test.**
