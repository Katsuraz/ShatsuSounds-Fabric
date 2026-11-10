# Shatsu v0.6.1 Alpha — Combat & Weapons Complete Category

This stage continues the user-directed one-category-per-'next' workflow.

## Built output (delivered with conversation files)

**Shatsu-v0.6.1-ALPHA-Combat-Overhaul-Fabric26.3.jar** is a standalone experimental derivative of the user's v0.6.0 inventory-alpha JAR. The GitHub Gradle build still uses the repository's earlier 0.3.0 audio base. **Do not install the old CI JAR in place of the v0.6.1 combat-alpha.**

## Changes

- 45 separate combat audio families, 135 new 32 kHz OGG takes; original 5,352 OGG recordings byte-identical.
- 38 mapped vanilla Minecraft weapon events, 10 Clangor events, 2 existing Shatsu events; 6 new combat sound events.
- Bow nock, audible draw, firing; +4.8 dB RMS on the new first bow-draw take against the original.
- Crossbow loading stages, Quick Charge 1/2/3, shooting and impacts.
- Trident separate charge-start and charge-ready event, throw, hits, Loyalty, Riptide levels, Channeling.
- Shield raise/block/break, mace selection/swing/smashes, wooden/metal spear attack/hit/use/lunges.
- Arrow shot, flyby, block/entity impact; snowball, egg, Ender Pearl, XP bottle throw; two experimental throwable impact events.
- Existing Clangor bow/trident charging definitions are set near-silent, avoiding a redundant sound layer while the new Shatsu client hook handles charge starts.
- Added a separate Fabric client entrypoint (ShatsuCombatClient), which preserves all older v0.6.0 runtime classes and registered audio handlers.
- New experimental snowball/egg mixins listen for clientbound entity event 3 to play unique impact recordings.

## Source work

- src/main/java/dev/shatsu/sounds/client/ShatsuCombatClient.java
- src/main/java/dev/shatsu/sounds/client/ShatsuCombatChargeFeedback.java
- src/main/java/dev/shatsu/sounds/client/ShatsuThrowableImpactAudio.java
- src/main/java/dev/shatsu/sounds/mixin/ShatsuSnowballImpactMixin.java
- src/main/java/dev/shatsu/sounds/mixin/ShatsuEggImpactMixin.java
- src/main/resources/shatsu-sounds.mixins.json (two new mixins)
- scripts/test_combat_source.py (source-contract tests)

## Validation

Java 25/Fabric 26.3 compilation passed in GitHub Actions CI run 42776053191. A subsequent test-augmented run verifies the new source contracts. The experimental v0.6.1 JAR was separately statically tested:

- JAR archive CRC: pass
- All 22,425 sound-file references resolve: pass, zero missing
- New clips: 135 decode, 45 families with 3 distinct hashes each, controlled peak (max 0.9782)
- All 5,352 pre-existing OGG files preserved byte-for-byte
- No unrelated vanilla sound definitions changed
- All 5 new compiled class files included, and all previous compiled classes preserved

**Not tested:** real Minecraft client gameplay, Mixin firing for projectile impact packets, duplicate sounds with third-party audio mods, subjective listening quality, audio redistribution licensing. This alpha must not be called a final release.

## Reproduction

The complete conversation test package includes the older audio baseline *reference by filename*, source Python scripts, compiled CI class archive, itemized source/texture change report, A/B preview MP3 and test checklist. Put the v0.6.0 inventory alpha and user-supplied CC0 ZIP files beside the scripts, run shatsu_combat_pass060.py, integrate_shatsu_combat_061.py, then validate_shatsu_v061.py.

## Next category

Stop after this category. Await the user's 'next' before starting water/lava, blocks/environment, magical/beacon, or mobs.
