# Pixel Eco v0.1.4 — Material Identity and Newer-Mob Polish

**Minecraft Java 26.3 · Fabric · experimental audio alpha**

This release is assembled as one JAR from the user's existing Pixel Eco v0.1.3 sound build. The downloadable JAR and reproducible Python generation/QA scripts are provided in the same ChatGPT conversation. **The GitHub source checkout alone does not contain the cumulative 6,437 OGG library and cannot reproduce that audio JAR without v0.1.3.**

## Actual changes
- 1,176 previously generic block-action event definitions redirected to short, material-specific arcade-fantasy variations.
- 19 inventory material event definitions, each with 4 new variants.
- 190 previously unpolished mob events given identity-preserving, subtle retro coloration; median correlation to the original vanilla-derived voice is 0.991.
- 966 original/generated new OGGs (320 block, 76 inventory, 570 mob).
- 2,112 provably unreferenced legacy alternates excluded from final JAR, preserving old active audio and all previous Java classes. JAR size is smaller than v0.1.3.
- All 83 approved flowing-water-related audio files remain byte-for-byte identical. All 133 water-related sound-event definitions remain unchanged.
- The original bubbly game-start-inspired style retained and used as a low-level sound-design layer.

## Verification
- 6,437 OGG Vorbis files fully decoded, no silent new clips, no new clipping.
- 24,634 resolved in-pack audio references, 0 missing.
- 5,235 Custom Block Sounds rules and 1,286 Sounds item rules statically validated.
- Sound Studio 7,655 catalog event-variation counts validated.
- Minecraft 26.3 embedded resource pack declares correct format 97.1.
- 32 Minecraft 26.3 Java classes preserved byte-for-byte from the prior build.
- 12-hit repeated mining simulation stayed below the clipping threshold.
- 190 newer mob voices preserve original duration and spectral/voice identity; median correlation 0.991, minimum 0.950.

**Not tested:** Actual graphics-capable Minecraft launch, the K master switch in-game, companion mods in a real modpack, long-session perceptual comfort, source-specific explosion triggers. The 54 vanilla music events remain unmodified. Original vanilla-derived mob voice licensing needs review before a public stable release.

## Installation
Remove old Pixel Eco/Shatsu JARs and install only v0.1.4 in Minecraft 26.3 Fabric `mods` with Fabric API. The retained K key and Sound Studio are inherited from v0.1.3 and need in-game verification. Do not install regular Shatsu alongside this build (both use `shatsusounds`).

## Source
The downloadable source ZIP contains `pixeleco_v014_upgrade.py`, full QA scripts, approved synthesized timbre-stem ZIP, event change CSV, and audio measurements; it requires the cumulative v0.1.3 JAR as input.
