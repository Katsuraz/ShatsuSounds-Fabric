# Shatsu v0.6.6 Alpha.8 — Clangor hold-matched tool audio overhaul

## Direction from the user

The user approves the sounds heard when selecting/holding tools, but dislikes the tool swings. They want **new swings reminiscent of the selection sound, not the exact same recording**, with every Clangor-supported item covered, **except Bow, Crossbow and Trident**. Do not lose the previous Clangor folder-gating fix.

## Source & delivery

This experimental build derives **from cumulative Alpha.7**, which contains the confirmed Clangor folder-routing repair. It does not redistribute Clangor's own binary.

The full generator and reproducibility script are in the user-facing ZIP:
`Shatsu-v0.6.6-Alpha8-Clangor-Hold-Matched-Complete-Test-Package.zip`

Outputs:
- `Shatsu-v0.6.6-ALPHA.8-Hold-Matched-Clangor-Overhaul-Fabric26.3.jar` — cumulative mod
- `Shatsu-Clangor-Hold-Matched-Swings-and-Hits-26.3.zip` — optional full audio resource pack for the Alpha.7 baseline
- `Shatsu-Clangor-Hold-Matched-Swings-ONLY-26.3.zip` — optional isolated swings-only comparison
- `Shatsu-Clangor-Hold-Matched-Audition-Preview.mp3` — 16 numbered triplets (original selection → new swing → new strong hit)
- `build_shatsu_clangor_hold_matched.py`, exact JSON validation manifest, testing README

## Implemented

- **58 new swing event pools** and **95 new hit event pools**: 153 redesigned events, each with **two genuinely different OGG takes** (306 newly rendered clips).
- Audio design: extract a quiet, short timbral accent from the user's already-liked tool-selection sound, blend with distinct air-swipe motion and material-appropriate CC0 foley. For hits, use slightly stronger material detail and a familiar impact transient. Not just repitching or replaying the holding sound.
- Extend the previously absent Clangor preset categories by **14 new select event pools** and **28 unmodified select OGG copies** from the already-made Retro-Fantasy Clangor pack, so additional modded weapons have coherent selection/swing/hit relationships.
- Existing **45 selection events unchanged**, including their original filenames, settings and samples. Existing `select.bow`, `select.crossbow`, `select.trident`, `swing.trident`, `use.bow.charge` and `use.trident.charge` unchanged. No new `hit.trident.*` was added.
- Covers 18 active swing item types: sword, axe, pickaxe, shovel, hoe, mace, spear, rods, empty hand, dagger, club, scythe, sickle, staff, whip, katana, glaive and gauntlet. Bow, Crossbow and Trident are the 3 explicitly excluded Clangor preset categories.
- Material-specific swings (general/wooden/stone/copper/iron/gold/diamond/netherite) remain distinct.
- All **7,161 original OGG files** and all old compiled Java classes are preserved **byte for byte**. Existing archive entries except `fabric.mod.json` and `assets/clangor/sounds.json` unchanged.

## Static QA

- Alpha.7 version advances to `0.6.6-alpha.8`.
- Final event catalog: **215 Clangor events** (previous Alpha.7: 142).
- JAR and both test resource packs pass archive CRC integrity.
- All 334 new/copied OGGs decode as 44.1 kHz mono Vorbis without invalid samples or digital clipping. 306 new swing/hit clips have mean RMS ~−22 dBFS (range ~−23.1 to −19.33).
- All new sound references point under their exact event directory, for Clangor's `isFolderNotEmpty` check.
- The generator asserts all existing selection events and all excluded event definitions remain identical, and all old assets remain identical.
- Optional swing-only pack has 58 swing + 14 new select event pools and 144 referenced sound files. It doesn't modify hit pools.
- Optional full pack has 58 swing + 95 hit + 14 new select event pools and 334 referenced sound files.

## Testing

1. Back up the Minecraft config and install **only Alpha.8 JAR**, removing previous Shatsu JARs; or keep Alpha.7 and enable **one** optional resource pack above other Clangor packs.
2. Keep Clangor 2.1.4 Fabric 26.3 installed; enable relevant events in its F12 options, disable its built-in resource pack for external pack tests, reload resources with F3+T.
3. Hold an iron sword, axe, pickaxe, wooden shovel and hoe. Confirm **the selection sounds did not change**. Look into empty air and swing; Clangor suppresses swings when the crosshair is pointing at a block.
4. Test entity hits to distinguish weak/strong/crit/sweep/knockback; test added modded categories only when Clangor actually recognizes the tool class (assignment UI default V).
5. Explicitly confirm Bow, Crossbow and Trident actions still use the previous audio.
6. If swing is absent but `/playsound clangor:swing.pickaxe.iron master @s ~ ~ ~ 1 1` works, this indicates category or event triggering, not file routing. Capture `latest.log` and Clangor config.

**Runtime status: Unverified.** We have not launched Minecraft or observed these sound events in-game. This is a complete *audio content* implementation and static validation, not a gameplay-certified release.

## Rights

Newly rendered sounds incorporate original Shatsu selected audio and user-supplied rubberduck `100 CC0 SFX` foley; the 28 copied new-category select sounds come from the prior Retro-Fantasy pack made with user-supplied CC0 collections. Inherited Shatsu recordings require their own redistribution review before public release. Do not merge the GitHub draft or publish stable solely on these checks.

## Source repository limitation

The normal GitHub Gradle audio pipeline still assembles the outdated v0.3 baseline. Alpha.8 is generated from the cumulative Alpha.7 JAR by the script in the QA ZIP, and not yet reproducible with `gradle build` alone. A complete audio source migration remains a separate engineering task.