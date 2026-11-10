# Shatsu v0.6.6 Alpha.10 — Final Physical Interaction Foley Polish

The user's last requested sound-polish category targets using a Hoe on dirt, using a Shovel to create a dirt path, and previously untouched/generic interaction sounds.

**Important:** Hoe tilling (`minecraft:item.hoe.till`) and Shovel path creation (`minecraft:item.shovel.flatten`) are Minecraft item-use events, **not Clangor swing events**. Audio samples can be replaced using Minecraft `sounds.json` directly. The previous Clangor 215-event mapping and all approved swings remain unchanged.

## Root cause

The cumulative Alpha.9 JAR had both `item.hoe.till` and `item.shovel.flatten` pointing to **exactly the same** `shatsuv7:block/dirt/hit_1..3` recordings. Other old generic reuses included Axe strip/scrape/wax removal, the four composter states all using `rock_use`, bone meal using block plant placement, archaeology brush sounds using block footsteps and an unrelated coin-pick reveal, flint-and-steel using UI button clicks, shears using a generic metal click, books using block paper steps, and bundle actions all using soft_use.

## Targeted Alpha.10 work

Built from the **v0.6.6 Alpha.9 cumulative JAR**, changing exactly **33 existing Minecraft item/block interaction event pools**:

- Hoe till: cut and turn soil, three distinct variations
- Shovel flatten: broader, lower earthen scrape/compression, three variations
- Axe strip, scrape, remove wax: independent wood / metal / soft friction sounds
- Bone meal use: gentle granular growth texture
- Brush generic/sand/gravel and sand/gravel reveal: separate dry scrapes and subtle reveal cues
- Compost fill, fill success, ready and empty: four distinct organic action identities
- Flint-and-steel use, shears snip, book page turn and put, bundle insert/dump/remove/fail, dye use
- Leads tie/untie/break, Spyglass extend/retract, ink/glow ink use, bottle fill/empty

**99 newly authored mono OGG Vorbis 44.1 kHz recordings** (three per event) with genuine variation. Sound layering uses the user-supplied libraries: Fantasy Sound Effects Library dirt footsteps (Little Robot Sound Factory, CC BY 3.0), rubberduck 100 CC0 SFX wood/metal/paper takes (CC0), and Owlish Media scrape/page/cloth/water recordings as well as inherited Shatsu foley (latter sources need license review before public distribution). No new arcade/retro tones are used for soil.

## Preserved by byte-level regression checks

- **All 7,519 original OGG files** from Alpha.9, unchanged
- **All 38 existing compiled Java .class files**, unchanged
- Every sound event outside the 33 selected Minecraft events, including the full `clangor` sounds namespace and previously approved Golden Apple, Bow, Crossbow, Trident, Spiders, Mod Menu, drinking, and UI audio
- Previous icon and saved-config data unchanged

The only existing JAR members replaced are `assets/minecraft/sounds.json` (33 precise event changes) and `fabric.mod.json` (version metadata 0.6.6-alpha.10). Added 99 `assets/shatsufresh/sounds/v066_final_use/...` clips.

## Tests executed

- JAR CRC: PASS
- Optional resource-pack CRC: PASS
- 99 OGG Vorbis clips decoded at 44.1 kHz mono: PASS
- New encoded recordings have unique SHA256 hashes: PASS
- Safe audio peaks with soft normalization, no clipping: PASS
- 33/33 intended event pools changed; all other mapped events identical: PASS
- 99/99 newly referenced sound paths exist: PASS
- 7,519 original OGGs and 38 compiled classes byte-preserved: PASS

**NOT TESTED:** In-game playback and mix with the user's real modpack, GUI, sound-pack priority, subjective audio approval. Do not claim stable/official release.

## Download and use

The conversation includes:

- `Shatsu-v0.6.6-ALPHA.10-Final-Tool-Use-and-Foley-Polish-Fabric26.3.jar` (complete cumulative experimental mod)
- `Shatsu-v0.6.6-Final-Interactions-Polish-ResourcePack-26.3.zip` (only the new 33 Minecraft sound events; safest A/B test with Alpha.9)
- `Shatsu-Alpha10-Hoe-and-Shovel-6-New-Variations.mp3` (six targeted sounds)
- `Shatsu-v066-Alpha10-Final-Physical-Sounds-Before-After.mp3` (12 before/after comparisons)
- `Shatsu-v0.6.6-Alpha10-Final-Physical-Foley-Test-Package.zip` (JAR, pack, audio previews, validation, licensing, source build script)

To test without replacing the mod, install the resource pack above other sound resource packs while keeping Alpha.9 installed; reload F3+T. To test the full build, remove old Shatsu JARs and install **only Alpha.10**. Do not stack both modes unnecessarily.

## Source and licensing limitations

The ZIP includes `build_shatsu_alpha10_final_polish.py` to rebuild the cumulative JAR from Alpha.9 and the user-provided source audio archives. GitHub's normal Gradle source is still missing previous cumulative audio migrations and will not reproduce this final JAR alone. The imported Owlish Media and `sounds(1).zip` rights, plus prior bundled Shatsu recordings, still need review before a public stable release.

**Keep the parent v0.6.6 pull request as draft.**
