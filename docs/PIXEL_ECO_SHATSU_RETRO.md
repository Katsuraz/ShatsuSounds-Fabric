# Pixel Eco — Shatsu Retro Edition (Fabric 26.3)

**Branch:** `pixel-eco-retro-v066`  
**Mod:** `shatsusounds` (Shatsu drop-in replacement)  
**Version:** `0.6.6-retro-alpha.1`  
**Status:** Built and offline-audited, **not in-world gameplay tested**.

## Alpha.2 — Real Shatsu-based tactile interaction improvements

**Changes:** 19 existing Minecraft sound events that previously reused identical or inappropriate recordings now point to 57 original deterministic retro-tactile variations (three per action). Hoe tilling is distinct from shovel path flattening; axe strip, scrape, and wax removal are distinct from each other; brush, bone meal, flint and steel, shears, composter stages and book actions have material-specific sounds.

The update uses Shatsu's generated audio as a source texture, plus newly synthesized layers. It does **not** alter any Shatsu Java class, existing OGG clip, Clangor mapping, Sounds mapping, Custom Block Sounds mapping, or previous retro explosion family. The user's 2,473 earlier retro-edition OGG clips are all preserved byte-for-byte. The playable Alpha.2 JAR has 2,530 OGG clips.

Quality controls: 44.1-kHz mono OGG Vorbis, limited peak amplitude, restrained high-frequency energy, moderate duration/volume, and three different variations per event. Automatic checks found no missing custom asset references. Actual Minecraft launch, mod interactions and subjective long-session listening have **not** been tested.

The script `scripts/retro_interaction_polish.py` runs automatically after `retro_transform.py` in the original Shatsu audio preparation pipeline; the GitHub workflow checks all 19 event pools and 57 resulting OGGs.

**Install:** Replace the previous Pixel Eco Retro JAR in `mods` with Alpha.2; do not keep standard Shatsu installed simultaneously (same `shatsusounds` mod ID). This is still built on the Shatsu 0.6.6 feature branch using the older repository sound archive, not the uncommitted cumulative Shatsu Alpha.10 audio JAR.

---

## What this actually is

A retro audio transformation of the *real Shatsu mod*, built on the existing `v066/immersive-ui-audio` source branch. It preserves the original Shatsu UI, Mod Menu support, audio mixer, configuration, generic attack hooks, inventory/audio client hooks, and shipped integrations with Clangor, Sounds and Custom Block Sounds. This is not a recreation of Shatsu from scratch or a separately operating resource-pack manager.

Shatsu's own Gradle sound processing first stages the existing recordings and mappings, then `scripts/retro_transform.py` processes generated OGGs without modifying the original archived sound sources.

## Results from first completed CI build

- **2,431** Shatsu OGG assets inspected and decoded
- **2,409** retraced/retro-processed
- **22** intentionally preserved unchanged
- **42** original arcade-fantasy explosion clips added to 7 existing Minecraft sound-event pools
- **2,473** final OGGs (all decode)
- **6,732** sound-event definitions present in 13 sound-definition files
- **18,482** custom-namespace sound references checked; zero missing custom OGGs
- **31** Shatsu Java classes retained in finished JAR
- Real Fabric 26.3/Java 25 Gradle build and Shatsu source tests passed

[Successful GitHub build](https://github.com/Katsuraz/ShatsuSounds-Fabric/actions/runs/42984159095).

## Audio direction

Warm modern arcade-fantasy coloration: short bright transients, low-gain harmonic accents, material-aware crunchy textures and controlled peaks, never uniform 8-bit beeps. Shatsu's authentic Trident recording is used as an attack-envelope loudness reference, **not copied into the new original explosion samples**. No Honor of Kings commercial sound assets are included.

## Important limitation: cumulative Shatsu v0.6.6 Alpha.10 sounds

This repository's v0.6.6 branch includes the expanded source code but **does not contain the cumulative latest 7,618-OGG Alpha.10 audio JAR** described in its documentation. This build therefore starts from the Shatsu repository's published v0.3 audio generation archive (2,431 clips) while preserving the available v0.6.6 Java/UI code. It does **not** yet reproduce the newer Alpha.10-specific sounds. To transform that exact sound library, the latest Alpha.10 JAR must be provided as input.

## Install

1. **Remove old Shatsu and PixelEco JARs** from Minecraft 26.3 Fabric `mods`. Shatsu Retro has the same mod ID `shatsusounds`, so it cannot coexist with regular Shatsu.
2. Put the full `PixelEco-Shatsu-Retro-v0.6.6-Fabric26.3-ALPHA.jar` supplied in the ChatGPT conversation into `mods`, with Fabric API.
3. Use the existing Shatsu UI and optional Mod Menu as before. Optional Clangor, Sounds and Custom Block Sounds remain separate mods with their own dependencies.

## Remaining QA

No complete Minecraft launch/in-world audio verification, no long-session listening fatigue certification, no exact Alpha.10 asset parity, no official stable release. Use the provided audio preview to review the style before replacing a main modpack.
