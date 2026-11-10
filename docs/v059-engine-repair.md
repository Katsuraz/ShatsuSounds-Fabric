# Shatsu v0.5.9 — Sound Engine & Inventory Routing Repair (Alpha)

This milestone repairs the inventory playback source and the material profiles on the user's existing v0.5.8 alpha JAR. It is separate from the older v0.3.0 source build in this repository.

## Source changes

- Restore normal and Creative inventory click Mixins, matching Minecraft 26.3 method descriptors from the previously compiled v0.5.8 hooks.
- Restore inventory event routing and explicit material-profile JSON lookup.
- Replace the global 16ms inventory cooldown with a duplicate guard based on the physical click (screen, slot, input type, button). Preserve fast distinct actions while detecting a duplicate creative/super callback.
- Add source contract tests for hooks, config, material cases and diagnostics.

## v0.5.8 binary audio integration

The user-provided v0.5.8 JAR is ahead of the repository's 0.3.0 asset base. The standalone v0.5.9 test JAR therefore combines:

1. Original v0.5.8 alpha JAR
2. 168 corrected item profiles and new material-specific inventory event definitions using existing audio clips
3. Exclusion of 1,658 zero-volume optional Sounds-mod item JSON rules
4. Six inventory-engine Java class files compiled in a successful GitHub Actions source build

The test JAR preserves every original OGG byte, all other original compiled classes, and all Enchanted Golden Apple sound definitions. The old v0.3 GitHub JAR is not used as the audio baseline.

## Verified vs unverified

Verified: Java 25 source compilation, source regression tests, JAR archive CRC, original audio byte preservation, all 1,658 item profiles with 21,707 sound references, isolated Java class replacements.

Unverified: Minecraft gameplay audio, hook activity, third-party resource pack effects, subjective sound quality, modpack compatibility.

The optional Sounds-mod item rules were already volume-zero in v0.5.8. Their presence is a possible redundant layer, not proof of the reported overlap. Other mods, legacy resource packs, or undesirable randomized takes remain possible.

This remains experimental and should not become an official public release without a runtime test and audio licensing review.
