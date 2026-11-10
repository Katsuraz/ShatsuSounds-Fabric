# Shatsu v0.6.2 Alpha — Blocks, Water & Lava

This is the complete **Blocks & Environment** development pass in the user's one-category-per-'next' workflow.

## Build baseline

The separate, installable experimental v0.6.2 JAR is derived directly from the user's v0.6.1 Combat Overhaul JAR. GitHub's main Gradle pipeline still assembles an older v0.3 source-audio baseline; do **not** substitute that CI artifact for the standalone v0.6.2 JAR.

## Coverage implemented in the experimental JAR

- 775 block names mapped to 39 material profiles: soft/padded beds, wood/cherry/bamboo, live/dead coral, prismarine, wet/dry sponge, grass/plants/leaves/kelp/nether plants, slime/honey, stone/minerals, concrete vs concrete powder, terracotta/glass, snow, sculk, metal and mechanical blocks.
- Five block actions—break/place/hit/step/fall—where old Shatsu Custom Block Sounds mappings existed.
- 16 wool colors and all 16 carpets now share the same short breaking audio (trimmed from the original long break tail), while existing approved wool placement, hit, footsteps and fall events remain unchanged.
- Water and lava bucket fill/empty, aquatic bucket creatures, flow/ambient, bubble columns, player splashes/swimming, boating, fishing, dripstone/cauldrons, sponge absorption and drying, lava pops, and a prominent water-lava steam hiss.
- Wood, stone and metal pressure plates have distinct on/off sound pools.
- Stonecutter and grindstone use events have distinct material-appropriate recordings, not inventory handling cutting sounds.
- Separate source classes ShatsuBedFeedback.java and ShatsuWorldClient.java add one local-player cue on sleeping/waking state transitions, without replacing the older ShatsuClient and combat listeners.

## Static verification

- 690 newly rendered 32kHz Vorbis OGG clips; 230 3-take pools.
- 3,747 custom block event definitions changed, 449 native/environment events updated.
- 22,901 total audio file references resolved with zero missing assets.
- No old file removed; all 5,487 original OGG files from v0.6.1 preserved byte-for-byte.
- All new OGGs decoded and tested for length, RMS and clipping after encoding; highest decoded sample peak approximately 0.834.
- Wool vs carpet breaking definitions identical; original wool placement, hit, step, fall definitions unchanged.
- 28 before/after audio comparisons are provided in the complete conversation test package.
- Java compilation and artifact integration status must be checked separately against the latest GitHub Actions run.

## Technical boundary

Minecraft uses shared extinguish sound events for several fluid-block reactions. The generic water-lava contact has been redesigned; **distinct per-result audio for obsidian vs cobblestone vs basalt vs stone has not been wired** because it requires a more invasive verified block-update or fluid-interaction hook.

**Actual Minecraft gameplay, integration with other sound mods, bed event triggering, and subjective listening have not been tested.** Keep experimental until player feedback. Never claim a feature is complete solely because its sounds.json entry or compiled class exists.

## Licensing

New foley source libraries used: rubberduck's *100 CC0 SFX* and OwlishMedia's *Sound Effects Pack*, both CC0. Procedural steam textures were created locally. Earlier Shatsu audio libraries still require their own licensing audit before public distribution.

- https://opengameart.org/content/100-cc0-sfx
- https://opengameart.org/content/sound-effects-pack

## Reproduction

The full conversation test package includes Shatsu-world-pass build script, static validator, A/B preview script, integration script and test instructions. Start with the v0.6.1 Combat Alpha JAR, produce world062 sound assets, compile Java classes for Fabric 26.3, overlay only ShatsuBedFeedback and ShatsuWorldClient, and revalidate the final output.

Stop at this category until user says next. The next category is **Magic / Fantasy + Cathedral Bells**, followed later by mobs and final regression testing.
