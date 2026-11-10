# Shatsu v0.6.5 Alpha.1 — Final UI Integration and QA

## Status
**Integrated alpha JAR created and statically validated. Not an official/stable release.**

Installable file supplied in the ChatGPT conversation:
`Shatsu-v0.6.5-ALPHA-Integrated-UI-QA-Fabric26.3.jar`

It was built by overlaying just three Java 25-compiled menu class files from GitHub CI onto the **existing v0.6.4 Mob Overhaul integrated JAR**, then updating the Fabric mod metadata. The original configuration and catalog classes were *not replaced*, because the newer v0.6.4 configuration contains additional playback toggles that the older GitHub source does not.

## Final integration scope
- **Mod Menu:** Home, Presets, Volume, Library in dark-purple/lavender Shatsu music-app style.
- **Presets:** Balanced, Retro, Soft ASMR, Crisp & Tactile, Immersive, Combat Focus, Minimal. Retro now applies its selected levels in the UI even when hosted by the newer v0.6.4 configuration code.
- **Category Volume & Library:** Group controls and library filters now include **Mobs** and **Water & Lava**, which exist in the v0.6.4 catalog.
- **Library:** Search, direct category picker, favorites, audition, pitch/volume, mute and restore defaults. Uses the newer v0.6.4 API methods for favorites without changing saved favorites data format.
- **Audio and gameplay:** untouched. All 6,582 previous OGG files and all original gameplay hooks remain byte-for-byte identical.

## Verification
- Java 25/Fabric 26.3 compile-only GitHub Actions run **42787675387**: PASS.
- New UI public config/catalog references verified using `javap`: **17 method/field symbol references resolved** against the preserved v0.6.4 classes.
- ZIP CRC: PASS.
- JAR member diff: replaced `ShatsuScreen.class`, `ShatsuScreen$ShatsuLevelSlider.class`, changed only `fabric.mod.json`; added new `ShatsuScreen$ShatsuPitchSlider.class`. No other old members changed.
- All 6,582 original OGG files byte-identical.
- 7,804 catalog events preserved; 22,983 sound references resolve; 0 missing non-Minecraft audio files.
- Key original classes (ShatsuConfig, ShatsuCatalog, ShatsuClient, inventory, combat, world, food and cave-spider routing) byte-identical to v0.6.4.
- Version metadata now `0.6.5-alpha.1`; Minecraft dependency remains `~26.3`.

## Reproducing
Use the **v0.6.4 integrated audio JAR** together with the Java-25 GitHub compile-only artifact, not the repository's old v0.3 source-audio bundle.

```bash
python3 scripts/integrate_v065_ui.py Shatsu-v0.6.4-ALPHA-Mob-Overhaul-Integrated-Fabric26.3.jar Shatsu-v0.6.5-Final-UI-Compiled-Classes.zip Shatsu-v0.6.5-ALPHA-Integrated-UI-QA-Fabric26.3.jar
```

The conversation's complete test package contains a stricter independent validator, the compiled class archive, JSON reports, and a test checklist.

## Remaining acceptance blockers

**No Minecraft GUI/world has been run in this environment**, so menu widget interaction, saved-setting round trips, and audio events in an actual modpack remain to be playtested. Cave Spider packet routing, snowball/egg impact hooks, charging, and creative-inventory behavior have only compiled/static verification. Some bespoke event hooks remain unimplemented (different lava formation results, End Crystal destruction, Dragon healing and Bottle o' Enchanting impact). Sound-library licensing for earlier versions still needs review.

**Do not merge as stable, claim all sounds fixed in-game, or publish an official release without gameplay and licensing verification.** The user can install this alpha in a backed-up test instance and report issues.

### Important source discrepancy
The normal GitHub Gradle build continues to assemble an older v0.3 audio baseline, while the shipped integrated alpha derives from v0.6.4. The reproducible integration script closes this specific UI/binary gap, **not** the entire source-and-asset history gap. The complete source migration remains future technical work.
