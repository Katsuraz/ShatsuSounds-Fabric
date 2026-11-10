# Shatsu v0.6.6 Alpha.4 — Enchanted Golden Apple high-fantasy inventory

**Status:** experimental, statically validated; not a stable/public release or gameplay-certified fix.

## Why
The user rejected the old metallic/uncanny Enchanted Golden Apple inventory sound pool, and explicitly rejected a retro/8-bit direction. The user approved the direction of eight new audition samples built from the Fantasy Sound Effects Library (crystalline, soft, majestic and high-fantasy). This Alpha.4 implements 18 individual inventory action sounds using those approved-style foundations.

## What was built

- Based on the cumulative **v0.6.6 Alpha.3 Audio Health** JAR, preserving all new UI, audio health and gameplay classes.
- **Six** sound events changed in `assets/shatsufresh/sounds.json`: `inventory.move.{pickup,place,swap,transfer,split,drop}.enchanted_apple`
- **Three** distinct randomized OGG takes per action = **18 new clips** under `assets/shatsufresh/sounds/v066_ga_fantasy/`.
- New sources combine the eight user-liked fantasy audition mixes with different `Pickup_Gold_00..04` takes from Little Robot Sound Factory. Mild EQ/short ambience, balanced loudness, no new arcade synth tones. Each clip is a real distinct mix, not just pitch-shifting one sample.
- Old v0.5.7 (`overhaul057`) sound paths are no longer referenced by those six events. Previous files remain embedded for rollback/reference, not active in these pools.
- **Enchanted Golden Apple eating**, unrelated item sounds, `preview.asmr.magic.enchanted_apple`, all existing Java classes, config data and sound catalog are unchanged.

## Validation

- Base JAR version `0.6.6-alpha.3`; rebuilt metadata version `0.6.6-alpha.4`.
- ZIP archive CRC passes.
- **6,723 original OGG files byte-identical; 18 new Vorbis OGG files decode at 32 kHz**.
- 18 unique hashes, safe decoded peak below 0.85, short clips ~0.32–0.42 seconds.
- Exact JAR member-diff: only `fabric.mod.json`, `assets/shatsufresh/sounds.json` replaced and 18 new sound files added.
- JSON diff contains exactly six intended inventory event changes, with three references each. All new sound paths resolve.
- In-game runtime, third-party mod interactions and user subjective acceptance **not yet verified**.

## Deliverables

The conversation supplies:

- `Shatsu-v0.6.6-ALPHA.4-Enchanted-GA-High-Fantasy-Fabric26.3.jar`
- `Shatsu-Enchanted-GA-High-Fantasy-18-Preview.mp3` (all 18 twice, numbered)
- `Shatsu-v066-GA-High-Fantasy-18-Individual-Sounds.zip` (all 18 WAV and OGG, index)
- `Shatsu-v0.6.6-Alpha4-GA-High-Fantasy-Complete-Test-Package.zip` (JAR, source reconstruction script, audio and QA)

**Source-base caution:** GitHub Gradle still uses old v0.3 audio; the completed Alpha.4 is built atop the Alpha.3 cumulative JAR. Source generator in the delivered QA pack requires the user's supplied `Fantasy Sound Library.zip` and approved-style audition ZIP. Do not substitute the standard CI JAR.

## Licensing

Original Fantasy Sound Effects Library by **Little Robot Sound Factory**, **CC BY 3.0**, with adaptation: https://opengameart.org/content/fantasy-sound-effects-library . Attribution required. Inherited Shatsu files require their independent rights review before stable/public release.

## Acceptance / rollback

Back up the config, remove all older Shatsu JARs, install Alpha.4 only, test the six inventory interactions with an Enchanted Golden Apple in a temporary Minecraft 26.3 world, listen for all 18 random variations. If any sound is wrong, provide its numbered preview index; return to Alpha.3 for rollback.

**Continue pending user direction:** Gentle realistic drinking ASMR still needs explicitly approved samples; do not silently replace drinking. Remaining v0.6.6 QA includes real Minecraft UI, Clangor and Cave Spider tests.