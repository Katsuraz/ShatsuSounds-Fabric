# Shatsu Sounds — Fabric 26.3

Dedicated **Minecraft Java 26.3 / Fabric** client-side sound mod. This repository is separate from the unrelated Shatsu music-sync app.

## Status: v0.2.1 alpha — mining and ordinary-item sound repair

The uploaded Shatsu v7.3 ZIP is stored at the repository root. GitHub Actions stages its `assets/**` and `pack.png` **inside the mod JAR** during compilation; users do not need to install the resource pack separately when testing the JAR.

The stage script `scripts/prepare_audio.py` makes narrowly scoped changes:

- Audits block hit audio; normalizes unusually quiet hit samples (not block break, place, step, or fall audio).
- Raises **hit-only** Custom Block Sounds gains for quiet material families, including stone, deepslate, glass, obsidian and sand.
- Preserves the five individual Custom Block Sounds rules per block; its repeated JSON keys are intentional and supported by the mod.
- Aligns Clangor's generic tool and rod **hit** effects with the exact empty-hand hit recordings; mace, sword, pickaxe, axe, spear, shovel, hoe and other dedicated weapon recordings stay unchanged.
- Supplies Shatsu's soft empty-hand **swing** feedback for non-weapon items (including ordinary blocks such as planks) when beginning an attack at air/an entity; does not trigger while mining a block.
- Generates `shatsu_catalog.json` from all the actual packaged sound definitions so the Mod Menu library and mixer can recognize the events.

The source is compiled with Java 25 and Fabric Loom via [GitHub Actions](https://github.com/Katsuraz/ShatsuSounds-Fabric/actions). **Automated compilation is not an in-game test.** Do not consider this a stable release until it has been launched and listened to in Minecraft 26.3 with the intended optional mods.

## Installation and test

Download the successful `ShatsuSounds-v0.2.1-ALPHA-MINING-AND-ITEM-FIX-UNTESTED` artifact from [GitHub Actions](https://github.com/Katsuraz/ShatsuSounds-Fabric/actions). Extract the JAR from the artifact ZIP into `.minecraft/mods`, with compatible Fabric Loader and Fabric API.

Disable separate older Shatsu full resource packs and the experimental retro-mob overlay while testing the JAR to avoid duplicated/overriding sounds. Run Minecraft 26.3 and use **Mod Menu → Shatsu Sounds**.

Test mining stone, cobblestone, stone bricks, and deepslate in survival mode long enough to hear repeated mining hits. Compare glass, obsidian, gravel and wood. Hit a mob with empty hands, then planks or food; the regular attack impact should use the same soft punch profile. Check that mace, swords and tools still retain specialized effects. Try an ordinary-item attack at air to check the new soft swing.

**Custom Block Sounds** is optional for one-to-one overrides of 1,047 individual block IDs. **Clangor** is optional for the item-specific weapon sound framework. Other mods may introduce their own sounds; avoid disabling them automatically or modifying their configs. Sound Physics Remastered is not overridden by this mod.

## Source, tests and caveats

- `scripts/prepare_audio.py`: reproducible audio normalization, mappings, catalog.
- `scripts/test_sound_repair.py`: six block-hit and generic item sound mapping tests, plus catalog test.
- `src/main/java/dev/shatsu/sounds/client/ShatsuGenericAttackFeedback.java`: safe click-edge ordinary-item swing feedback.
- `src/main/java/dev/shatsu/sounds/`: Mod Menu browser, JSON profile/mixer, sound-engine gain hook.
- `.github/workflows/build.yml`: compiles and checks packaged audio, including a minimum OGG count.

Sound-file references and resource inclusion are validated automatically; the mod's effects still require in-game listening tests. Some attacks or quick block breaks may not trigger a mining-hit event depending on Minecraft mechanics. Source-archive licensing and attribution must be reviewed before any public stable release.

Keep the approved inventory-opening chime, drinking, fireworks, teleport, weapon sound changes and original vanilla mob voices.
