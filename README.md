# Shatsu Sounds — Fabric 26.3

**v0.3.2 alpha · Louder tools + real arrow whoosh + food category fix**

A client-side Minecraft Java 26.3 / Fabric mod with a bundled Shatsu soundscape. **Do not install old Shatsu resource packs above this mod** during testing; overlapping sound packs may defeat its overrides.

## Install

1. Install Java 25, a compatible Fabric Loader, Fabric API and Minecraft **26.3**.
2. Download the latest **successful** v0.3.0 [GitHub Actions artifact](https://github.com/Katsuraz/ShatsuSounds-Fabric/actions). Extract its JAR into `.minecraft/mods`.
3. Install [Mod Menu](https://modrinth.com/mod/modmenu) to open Shatsu's sound studio (optional but recommended).
4. [Clangor](https://modrinth.com/mod/clangor), [Sounds](https://modrinth.com/mod/sound) and [Custom Block Sounds](https://modrinth.com/mod/custom-block-sounds) are **optional**. Each unlocks its own integration. Check their own dependency requirements if installing.
5. [Sound Physics Remastered](https://modrinth.com/mod/sound-physics-remastered) is optional and uses its own configuration.

## v0.3 changes

- **Weapons:** Recorded longbow shots and draws; crossbow loading/locking/firing and bolt impacts; arrows; trident throw/impact/ground/return; distinct sword and axe swing/equip styles for eight material families.
- **Shield:** Separate ready, block and break sounds. Shield raise is handled by Shatsu's client hook; vanilla block impacts remain separate.
- **Mace:** Weighted swing and equip; distinct contact, critical, air-smash, ground-smash and heavy-smash treatments. Wind Burst remains independent.
- **Armor:** New leather, chainmail, iron, gold, diamond, netherite, copper, turtle, elytra, wolf and nautilus materials.
- **Food and drinks:** New dry/cooked/soft/crispy food categories, short real swallows, potion-bottle handling, honey and milk sounds. Shatsu now has a built-in item-aware 26.3 food selector for apples, bread, soup, meat, fish, fruit and more. Existing drinking sounds remain unchanged.
- **XP & Totem:** Gentle, quieter XP pickups; cozy totem inventory pickup mapped in the Sounds item-rule definitions. The dramatic life-saving totem-use effect remains separate.
- **Cozy Studio:** forest-toned background, warm highlights, real draggable master/category sliders, sound-library search and previews, per-event volume/pitch overrides, reset buttons and paged categories.

## Quality checks and known limitations

GitHub Actions assembles all **166 curated CC0 mono OGG recordings** from nine audited text chunks at `audio/shatsu_v03/`. The SHA-256 is checked before packaging. The build pipeline then applies the resource mappings on top of the older Shatsu v7.3 pack, without modifying its original ZIP.

Tests validate sound file references, mono encoding, vanilla and Clangor mappings, material preservation, and the final JAR. **Successful compilation is not in-game verification**. Playtest bow/crossbow/trident and their projectile events, mace smashes, shield raising, armor equips, totem inventory pickup (with Sounds installed), new food sounds and the Mod Menu sliders. The current build does not yet add a dedicated client-side hook for live projectile flyby audio; those samples exist for preview and future positional playback. The client-only food mixin was compiled but should be verified in-game before a stable release. Remove the earlier broken shatsu_food_audio-0.1.0.jar from mods; this functionality is now integrated into Shatsu itself.

A separate full Shatsu pack or another mod's sound overrides can cause duplicate/old sound effects. Disable old packs first. Keep original mob voices unchanged.

## Audio sources and licensing

The new recorded samples are processed derivatives of CC0 sound effect libraries submitted to [OpenGameArt](https://opengameart.org): Medieval Weapon Textures and Weapon Impacts (Ben Jaszczak/Brian Nelson), Swishes and RPG Sound Pack (artisticdude), Fantasy Weapons and Apparel (Vehicle), Liquid Bottle Drink Set (qubodup), and the cucumber/toast crunches (cogitollc). We thank their creators. Project source code and legacy sounds may have separate licenses; review all distribution rights before publishing a stable release.

## Developer files

- `scripts/assemble_recorded_audio.py`: validates/reconstitutes CC0 OGG bundle
- `scripts/apply_recorded_audio.py`: applies new sound events and inventory totem override
- `scripts/prepare_audio.py`, `scripts/polish_audio.py`, `scripts/placement_polish.py`: existing non-destructive sound pipeline
- `scripts/test_recorded_audio.py`, `scripts/test_sound_polish.py`, `scripts/test_sound_repair.py`: CI tests
- `src/main/java/dev/shatsu/sounds/client/ShatsuScreen.java`: Cozy Sound Studio GUI
