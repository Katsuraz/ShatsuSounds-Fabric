# Shatsu v0.6.0 Alpha — Full Inventory Audio Category

## Scope

This category starts from the user's **v0.5.9-ALPHA-Engine-and-Legacy-Repair.jar**, NOT the older 0.3 audio build in this repository. The inventory audio JAR and complete reproducibility/test package were supplied as conversation artifacts.

- 1,658 listed Minecraft 26.3 inventory items retain valid profiles.
- 408 item-to-material profile assignments corrected in the experimental JAR.
- 57 sound-family pools rebuilt for pickup, place, swap, split, transfer, and drop (342 event definitions; 1,026 short OGG clips).
- New randomization pools contain three distinct source takes per event, selected from supplied CC0 libraries (Accessory SFX, Weapons/Apparel SFX, Kenney Interface Sounds, 100 CC0 SFX, 50 CC0 Retro Synth SFX).
- Inventory catalog expanded to include all old and new inventory events.
- Updated source ShatsuInventoryProfiles class now consults the explicit profile table **before** fallback classification; compiled with GitHub Actions Java 25/Fabric 26.3 and included in the experimental JAR.

## Families covered

Wood planks/logs/furniture/wooden tools; Bone vs Bone Meal; solid concrete vs concrete powder; terracotta/ceramic; sand and gravel; dry and wet sponge; live, dead and fan corals; sea pickles and shells; slimeball, slime and honey; all dyed beds, cushions and bundles; Trident; Nether Wart and Ghast Tear; Beacon/Conduit/End Crystal/Heart of the Sea; paper and books; candles and wax; glass; Bottle o' Enchanting inventory; Ender Pearl; fireworks; Armor Stand; and other listed families.

**Preserved:** all 4,326 original OGG bytes, including approved newer Enchanted Golden Apple; wool and wool carpets keep the same cloth profile; the prior Stonecutter handling correction remains intact. Gameplay sounds, including drinking, bows and trident charging, bells, mobs and water/lava, were not modified.

## Verification

- Java 25 source build and tests: PASS in GitHub Actions run 42772755419.
- JAR CRC: PASS.
- 1,658 items x 6 interactions = 9,948 item-action routes: PASS.
- 30,450 inventory audio-file references resolve: PASS.
- 1,026 newly encoded OGG clips decode, have safe peaks, and are short: PASS.
- Random takes: three different hashed source clips per modified event: PASS.
- Approved Enchanted Golden Apple event definitions identical to v0.5.9: PASS.
- Two compiled profile-routing classes applied, with other old code preserved: PASS.
- Actual Minecraft run, sound-mod compatibility and subjective listening in user's modpack: **NOT TESTED**.

## Source and release limitation

The GitHub repository's Gradle build still assembles older 0.3 audio. **Do not replace the user's v0.6.0 inventory-alpha JAR with that CI JAR.** To reproduce this alpha, use the accompanying scripted inventory resource pass on the v0.5.9 base and overlay the two compiled profile classes. All scripts and per-take attribution/provenance records are in the complete conversation test package, not committed as 1,026 binary GitHub assets. Do not mark v0.6.0 officially stable without gameplay and licensing review.

## Subsequent category

The next category, only after the user says "next", is combat/weapons (bow/crossbow/trident charging, shield, mace, projectiles).
