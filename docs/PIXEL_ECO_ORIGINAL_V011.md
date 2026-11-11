# Pixel Eco v0.1.1 — Original Retro-Fantasy Audio and True Master Switch

**Target:** Minecraft Java 26.3 / Fabric. **Status:** Experimental alpha; no graphics-capable Minecraft gameplay test.

This is the source branch for Pixel Eco's **Shatsu-engine-based** audio replacement, **not** the earlier retro-filtered Shatsu sound library. The downloadable JAR (provided in the ChatGPT conversation) carries all audio; GitHub stores the Java changes only.

## New this phase

- One new `PixelEcoPackSwitch` class registers `resourcepacks/pixeleco_original` as an optional, default-enabled Fabric built-in resource pack.
- The entire Pixel Eco sound-event override catalogue, including the original Shatsu compatibility declarations for **Clangor, Sounds, and Custom Block Sounds**, moves into the built-in pack. The OGG library remains accessible as the mod's own sound assets.
- Press **K** in gameplay to toggle all Pixel Eco replacement mappings, or use the existing Shatsu Sound Studio Home control. Removing the pack is intended to restore underlying vanilla and other-mod sound behavior; there is **no zero-master-volume muting trick**.
- The existing mixer now passes through vanilla sound gain when the pack is unselected.
- **42** high-frequency gameplay event pools replaced with **168 new, original multi-layer arcade-fantasy OGG variants** (4 per event). Built using original synthesis and the user's CC0 wood/metal and RPG source textures; no Shatsu recordings copied.
  - 11 UI / inventory events
  - 21 real vanilla block/tool/combat/explosion events
  - 8 native Clangor weapon selection/swing events
  - 2 Shatsu weapon preview events
- Old **4,569 Pixel Eco OGGs preserved byte-for-byte**; total 4,737 OGGs.

## Validations performed

- **Minecraft 26.3 / Fabric Java compile** successful: [GitHub Actions run](https://github.com/Katsuraz/ShatsuSounds-Fabric/actions/runs/42991139214).
- Every new OGG decoded at 32 kHz, four distinct variants per event; no identical generated payloads, silence, missing references or clipping.
- 1,300 JSON mapping/item compatibility files relocated into one built-in pack.
- All 7,655 sound-event definitions present inside the pack; 23,211 referenced sound entries validated, no missing OGGs.
- New spectral checks: maximum peak < 0.60, maximum DC offset 0.00047, maximum energy above 6 kHz ~10.1%; objective QA **does not replace human listening tests**.
- All unrelated Shatsu engine functionality remains in the mod.

## Not yet verified

- **Master ON/OFF gameplay behavior**, default-enabled pack registration and real resource reload inside Minecraft.
- No real in-game test with Clangor/Sounds/Custom Block Sounds or actual GUI.
- No subjective long-session hearing fatigue testing and no gameplay performance benchmark.
- Category mixer sliders are volume controls, NOT separate vanilla-restoration pack toggles. These need follow-up.
- Original source audio and mob derivative distribution rights must be reviewed for public stable release.
- Music and music-disc events (54 events) remain vanilla.

## Installation

1. Remove the old Pixel Eco and Shatsu JARs from `mods`.
2. Install only the new Pixel Eco v0.1.1 JAR and matching Fabric API for Minecraft 26.3.
3. Press **K** in-game or open the existing Shatsu-based Sound Studio via Mod Menu.
4. If a saved old master volume is zero, reset to a nonzero volume when Pixel Eco is ON.

**No new audio ZIPs needed** for this phase.
