# Shatsu v0.6.6 Alpha.5 — Clangor Tool Swing Compatibility & Loudness

## User-reported regression

**Sword and spear swing sounds work.** Axe, pickaxe and possibly shovel/hoe swing sounds are missing or inaudible.

This is a targeted fix, not a comprehensive combat redesign. Preserve working sword and spear audio.

## Findings from real v0.6.6 Alpha.4 JAR audit

- `assets/clangor/sounds.json` already maps **45 swing sound events**.
- Each axe, pickaxe, shovel and hoe contains eight material profiles: `general, wooden, stone, copper, iron, gold, diamond, netherite`, each with two OGG takes.
- All original references exist in the JAR; absent files were not the cause.
- **Several pickaxe takes are about -31 to -35 dBFS RMS**, compared with working swords at about -19 to -24 dBFS. Hoe and shovel examples have similar low RMS; axe files are already relatively loud.
- Original tool OGGs were 22,050 Hz mono, while Clangor's published guidelines specify 44,100 or 48,000 Hz mono OGG Vorbis, 128 or 192 kb/s.
- Therefore the repair addresses recording format and audibility, while an absent axe trigger still points to Clangor's settings, item mapping or resource pack selection. **Do not claim the missing trigger is fixed until gameplay confirms it.**

## Targeted fix in the cumulative Alpha.5 JAR

Built from `Shatsu-v0.6.6-ALPHA.4-Enchanted-GA-High-Fantasy-Fabric26.3.jar`:

- **64 new OGG clips** using original Shatsu tool swings (4 tools × 8 materials × 2 variations), resampled to 44.1 kHz mono and encoded to OGG Vorbis at nominal 128 kb/s.
- Per-clip audibility adjustment with soft limiting, avoiding peak clipping. New average RMS approximately -21 to -22.5 dBFS. The average RMS increase versus older tool files is +5.22 dB, higher for faint pickaxes/hoes.
- 32 existing Clangor event pools now refer to paths `shatsufresh:v066_tool_swing/<tool>/<material>/<take>` via `assets/clangor/sounds.json`.
- The original 6,741 OGG files remain byte-for-byte identical. All existing Java classes, all other resource packs and audio data are preserved.
- Explicitly preserve all **sword, spear, trident, mace** sound definitions, Enchanted Golden Apple eating and inventory, drinking, and all other previous audio.

## Optional test pack

`Shatsu-v0.6.6-Tool-Swings-Compatibility-ResourcePack-26.3.zip` contains **only 32 Clangor tool swing pools and 64 new OGGs**, leaving sword and spear sound definitions untouched. No Clangor mod code was modified or redistributed.

1. Keep Alpha.4 JAR installed for the A/B test.
2. Put the optional resource-pack ZIP in `resourcepacks`.
3. Enable it **above all other Clangor soundpacks**, disable Clangor's built-in pack if it overrides it, then F3+T.
4. Open Clangor config with default **F12**, verify Swing sounds and category toggles.
5. Swing an iron axe, iron pickaxe, stone shovel and iron hoe in empty air, and separately test block hits.
6. If the sound still fails, hold each item and use the default **V** key to check its Clangor tool/material mapping.
7. Compare results against the known-working sword/spear. Provide Clangor config + `latest.log` only if still silent.

Alternatively, use the cumulative Alpha.5 JAR without the optional resource pack, after removing older Shatsu JARs and backing up the profile.

## Static verification

- JAR and optional pack CRC checks PASS.
- 64 new clips decode, mono 44,100 Hz Vorbis, nominal 128 kb/s.
- Each new clip's decoded peak below -0.7 dBFS.
- Exactly 32 tool swing sound-event pools changed.
- 6,741 original audio recordings and all unrelated compiled Java classes preserved bit-identically.
- No sword/spear/trident/mace event changes.
- Eight before/after listening pairs plus per-clip validation JSON supplied in the conversation test package.

**Not verified:** Minecraft 26.3 runtime, whether the installed Clangor mod emits each tool swing event, the user's individual Clangor configuration, and compatibility with other resource pack orders. No claim of a stable release.

## Source and reproduction

The complete conversation ZIP includes:

- `build_tool_swing_fix.py`
- `make_audition.py`
- `tool_audio_validation.json`
- `Tool-Swings-8-Before-After-Pairs.mp3`
- `AUDIO-PREVIEW-INDEX.md`
- `README-TEST-FIRST.md`
- The new full JAR and optional tool-only resource pack

The standalone integrated Alpha is derived from the newer cumulative JAR; GitHub's older v0.3 Gradle asset baseline is **not** an equivalent output. Keep this PR draft until runtime and source licensing checks pass.

## External documentation

Clangor official configuration & audio format documentation:
https://modrinth.com/mod/clangor

Clangor 2.1.4 Fabric 26.3:
https://www.curseforge.com/minecraft/mc-mods/clangor/files/9026602
