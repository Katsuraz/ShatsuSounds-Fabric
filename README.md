# Shatsu Sounds — Fabric 26.3

**v0.4.0 alpha · ASMR wood and liquids, new enchanted-food accents, configurable sound comparisons, ANS integration**

A client-side Minecraft Java 26.3 / Fabric mod with a bundled Shatsu soundscape. **Do not install old Shatsu resource packs above this mod** during testing; overlapping sound packs may defeat its overrides.



## New in v0.4.0 alpha — tactile ASMR + compatibility

- **Wood breaking:** four natural-feeling short crack variants, remapped across 146 already-overridden wood sound events (including oak planks). Designed to be distinct from hit/place sounds.
- **Buckets:** distinct 4-variant sounds for water fill, water empty, lava fill, and lava empty. Lava is thicker/darker than water, and collection does not sound like pouring.
- **Drinks:** four variations each for honey, milk, potions, and a water-drink preview. Item-aware milk, honey and potion consumption uses new \`shatsu_liquids:drink.*\` events and graceful fallback for unavailable sounds. Some Minecraft water bottles share the \`potion\` item ID and may currently use the potion profile.
- **Special foods:** golden apple, enchanted golden apple, golden carrot and chorus fruit gain a quiet, cooldown-protected magic chime accent *underneath* the original eating sounds, not in place of them. Toggle from the Sound Studio.
- **Gear:** short refreshed bow release, crossbow release and trident-throw profiles, each with four variations. Existing separate spear events from 0.3.3 remain in place.
- **Studio:** an eight-tab interface with the three-page FX Lab, real old/new comparisons, a nonblocking coverage scanner in 512-event batches, Favorites, and ASMR Woodland/Fantasy Whisper presets. Arrow, food, drink and special-food magic controls persist in \`config/shatsu-sounds/profile.json\`.
- **Actions & Beyond 1.13:** optional compatibility, with detection of its mod ID \`ans\`. Shatsu never modifies \`ans:\` sound files or their mixer gain. Animal-voice changes are offered as a **separate optional resource pack**, not inserted into ANS or the core Shatsu JAR.
- **Approved sounds:** existing inventory opening, fireworks, and animal voices are deliberately preserved in the *core* mod. Liquid sounds are redesigned because the user explicitly requested that change.

### Asset stages and limitations

GitHub Actions builds a reproducible baseline from bundled Shatsu CC0 recordings. A **separately applied local mastering pass** can use recordings the user uploaded (35 wood-cracks, SpritePilot bow, water/lava audio, and seven magical samples). Those binary user uploads are not committed to this public repository. The user-facing compiled file may therefore contain more refined OGG assets than the generic GitHub Actions artifact, while using the **exact same event IDs and Java classes**.

**Minecraft 26.3/Fabric/Java 25 only.** Remove the older Shatsu JAR and the broken \`shatsu_food_audio-0.1.0.jar\`. Do not stack an older Shatsu sound pack. CI compilation and audio-reference tests do **not** constitute an actual in-game test, especially for third-party mod interception and multiplayer consumption.

## v0.3.2 alpha — arrow whoosh, tool audio and visible food profiles

- **Arrow whoosh:** client-local positional whoosh for nearby moving arrows and spectral arrows, not a new sound layered on the bow release or impact; crossbow-fired arrows are included.
- **New tools:** generated two distinct swing and select samples for each pickaxe, shovel and hoe material, plus tool-type-specific hit profiles. Dedicated audio is sourced from the existing user-approved recorded library and processed differently per tool; it does not redistribute the external Eating Sounds mod.
- **Weapon volume:** quiet bow, arrow, crossbow, sword, axe, mace and trident OGGs are mastered for stronger audible peaks, and the corresponding sound-event levels are increased.
- **Food visibility:** \`shatsu_food_audio:food.*\` now appears under **Food** in the library/mixer instead of **Other**. The Home screen includes a Food shortcut, and the integrated client replacement supports both locally emitted eating audio and server-transmitted player chewing sounds.
- **Preservation:** drinking, milk, honey, potion, inventory opening, fireworks and mob voices are not replaced by this mastering pass.

**Test carefully:** Minecraft 26.3/Fabric/Java 25 required. Remove the obsolete \`shatsu_food_audio-0.1.0.jar\` and older \`shatsu-sounds\` JAR before trying this build. Disable old Shatsu resource pack overrides while testing. Source compilation and CI sound checks do not establish real in-game compatibility with every installed mod.

## Install

1. Install Java 25, a compatible Fabric Loader, Fabric API and Minecraft **26.3**.
2. Download the latest **successful** v0.4.0 [GitHub Actions artifact](https://github.com/Katsuraz/ShatsuSounds-Fabric/actions). Extract its JAR into `.minecraft/mods`.
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
