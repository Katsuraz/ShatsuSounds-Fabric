# Shatsu v0.6.6 — Immersive UI + Audio Alpha.2

This branch contains real, Minecraft 26.3/Java 25 source changes to the Mod Menu and its controls. A separately integrated experimental JAR, audio resource pack, source scripts, preview audio, and testing datapack are supplied in the conversation's test package.

## Visual interface

- New `ShatsuSoftButton` and `ShatsuSoftSlider` classes render custom charcoal / lavender controls through the Minecraft 26.3 GUI extractor, without Minecraft button sprites.
- Retains actual click/drag functionality, accessible narration labels, favorites, search, per-sound volume/pitch and category/preset controls.
- Extends the existing ShatsuSoundStudio screen with custom-drawn panels and title treatments.
- Java 25 Fabric compile-only **passed in GitHub Actions run 42810442487**.

This is a **first functional custom-rendering pass, not final pixel-free typography**. Remaining design improvements include custom typography, motion/accessibility review and real GUI-scale tests.

## Audio resource pass (private Alpha JAR derived from v0.6.5)

- Shulker mob and all Shulker Boxes now have **identical opening and closing OGG paths**, with 3 randomized takes of each shell sound.
- Ender Chest opens and closes using its own magical foley, plus two distinct inventory material profiles.
- Beacon idle uses a quiet subdued synthesized harmonic bed, not a piercing magic cue.
- Existing horse, zombie horse, skeleton horse and other passive animal voice overrides in the custom Minecraft sound definitions are removed so vanilla audio is restored.
- Player Ender Pearl and Enderman teleport events both reference the same pair of original vanilla Minecraft teleport recordings.
- Water ambient volumes reduced without changing bucket, splashes or swimming audio.
- Egg throwing uses a lightweight non-metallic whoosh; other non-tool throwables receive independent sound pools. Potion and Bottle o' Enchanting share Minecraft's actual bottle-break event, which now has a crisp glass/magic replacement.
- Spider sound pools have darker recorded chittering, with a distinct Cave Spider pool compatible with the existing experimental client router.
- Bow and Crossbow sound pools have updated retro-fantasy samples; older client charging hooks remain intact.
- **141 new OGG files, 51 new/revised sound definitions, 14 deleted passive-mob event overrides**.
- All **6,582 original audio OGG files remain byte-identical** to v0.6.5. Existing gameplay Java classes, config and catalog class files remain unchanged.

## Audition/approval still required

The user previously rejected the current drinking sounds and asked for gentle realistic ASMR; no replacement has been silently installed. Likewise the full set of 18 Enchanted Golden Apple inventory recordings remains unchanged until user approval. This is intentional.

The complete source libraries (including user-provided magical, spider and mechanical recordings) are **not committed to GitHub**. Some have uncertain redistribution terms. Before a public stable release, the sound sources and licenses need review and their approved source files should be integrated into the repository's reproducible Gradle build.

## Static verification (separate Alpha JAR)

- New GUI source compiled under Java 25 and Minecraft/Fabric 26.3.
- All JAR archives have passed CRC validation.
- 6,582 previous OGG recordings preserved byte-for-byte; 141 newly encoded OGGs decoded and peaked safely.
- 6,405 referenced bundled sound files resolved in the audited namespaces.
- All 1,658 item profiles retained.
- Shulker/Box same open/close paths, Enderman/Player teleport matching, animal override removal, and quieter flowing water validated by reading actual JSON.
- Source compiled GUI class file major version is 69 (Java 25) and original ShatsuConfig/ShatsuCatalog binaries are unchanged.
- No running Minecraft 26.3 client was available for runtime testing, so Mixins, exact sound timing, third-party sound-mod conflicts and UI interactions **remain unverified in-game**.

## Rebuild and test

The conversation QA ZIP contains:
- `build_v066_sound_pass.py` — audio and sound-event resource changes from the v0.6.5 JAR and the user-supplied recording libraries;
- `integrate_v066_ui.py` — overlays only the GitHub-compiled UI classes and metadata;
- `validate_v066.py` — independently validates the resulting Alpha JAR;
- `Shatsu-v0.6.6-Sound-Lab-26.3-Test-Datapack.zip` — extends the earlier Ultimate Sound Lab.

The base JAR is `Shatsu-v0.6.5-ALPHA-Integrated-UI-QA-Fabric26.3.jar`, not the obsolete v0.3 Gradle asset baseline.

**Do not merge as stable or publish an official release until subjective listening approvals, licenses, and live Minecraft tests pass.**
