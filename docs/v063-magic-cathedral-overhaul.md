# Shatsu v0.6.3 — Magical & Fantasy Sounds + Cathedral Bells (Alpha)

**Category milestone:** completed implementation and resource-level validation for the user's “next” in the one-category-at-a-time workflow.

## Build and provenance
The installable experimental **Shatsu-v0.6.3-ALPHA-Magic-and-Cathedral-Bells-Fabric26.3.jar** is built from the user's **v0.6.2 Integrated Blocks & Environment** JAR, NOT from this repository's older v0.3 Gradle audio baseline. The JAR plus Python rebuild recipe, licensed-source manifest, complete change report, validator and A/B preview are delivered in the conversation's v0.6.3 test package. **Do not substitute the repository's older CI JAR for it.**

This pass is **resource-only**: it does not change any Java class, Mixin, entrypoint, user configuration, earlier sound effect recording or approved audio.

## Changes
- **30 Minecraft gameplay event mappings:** Beacon activate/deactivate/power-select/ambient; Conduit activate/deactivate/ambient/ambient-short/attack; enchanting-table use; Totem of Undying; XP pickup and player levelup; Bottle o' Enchanting throw; Dragon's Breath bottle filling; End Portal activation/frame fill; End Gateway spawn; Nether portal trigger/travel/ambient; Respawn Anchor charge/deplete/set-spawn/ambient; Eye of Ender launch/death; End Crystal placement.
- **84 inventory events:** six actions each (pickup/place/swap/split/transfer/drop) across 14 distinct magical or rare-material sound families: Beacon, Conduit, End Crystal, Nether Star, Totem, Heart of the Sea, XP Bottle, Eye of Ender, Ghast Tear, Dragon's Breath, Firework Star, Enchanting Table, Enchanted Book, and bell handling.
- **Two real cathedral bell events:** `block.bell.use` and `block.bell.resonate`. Three genuinely different CC0 cathedral recordings serve as randomized takes: Cologne Cathedral St. Peter's Bell, Cologne Cathedral Pretiosa, and Audeption Church Bell. All are short edited single strikes/ringing tails, not 60-second ambience.
- **339 new OGG files**, all from CC0 retro synth/glass foley/real church bell recordings plus original synthesized components; variation is not achieved by pitch-shifting a single recording.

## Protected
- Every one of the 6,177 OGG files in v0.6.2 remains byte-for-byte unchanged.
- All 31 previously compiled Java classes unchanged.
- Enchanted Golden Apple sounds, fireworks explosions, music discs/music, Ender Dragon voices, previous combat and block audio stay untouched.

## Validation performed
- JAR archive CRC: PASS.
- All original file members present; no original OGG removed or modified.
- All 339 new 32 kHz mono Vorbis clips decoded and contained no NaNs or clipping (maximum sample peak 0.8837).
- 114 revised/new sound definitions reference existing assets; all are catalogued, and randomized takes are different.
- Every sound definition outside the 30 world, 84 inventory event scope unchanged.
- Two audio previews delivered: 30 old/new comparison pairs plus bell-only triple audition.

## Not verified or implemented
- **No Minecraft runtime or third-party sound-mod playtest yet.** A sound-event mapping alone does not prove an action invokes it.
- End Crystal explosion, Dragon healing, and Bottle o' Enchanting's dedicated impact timing have not been independently hooked; altering the global explosion event was intentionally avoided.
- The game's UI redesign is still a separate source-branch feature and is not yet in the installable v0.6.3 audio JAR.
- Previous Shatsu bundled sounds still need a complete rights review before public official release.

## Sources (CC0)
- rubberduck, *50 CC0 retro / synth SFX* — https://opengameart.org/content/50-cc0-retro-synth-sfx
- Vehicle, *Fantasy Accessory SFX Library* — https://opengameart.org/content/fantasy-accessory-sfx-library
- BIGBEN12345, Cologne Cathedral St. Peter's Bell — https://commons.wikimedia.org/wiki/File:200437_bigben12345_petersglocke.oga
- BIGBEN12345, Cologne Cathedral Pretiosa — https://commons.wikimedia.org/wiki/File:200357_bigben12345_pretiosa.ogg
- Audeption, Church Bell — https://freesound.org/people/Audeption/sounds/425172/

## Next category
Stop now; await the user's **“next”** before starting selective mob sounds, including the user-requested Dragon growl samples, Zombie Villager, undead horses, and spiders.
