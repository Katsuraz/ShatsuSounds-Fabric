# Shatsu Sounds — Minecraft Fabric 26.3

**v0.2.3 alpha: Optional third-party sound integrations**

This client-side mod bundles the approved Shatsu v7.3 sound library into one compiled Fabric JAR. The original resource-pack ZIP is stored in the repository and never changed; the Java 25/Gradle build creates an audio-enhanced copy inside the JAR.

## Requirements

Minecraft Java **26.3**, **Java 25**, **Fabric Loader** and **Fabric API**. The following three mods are **optional**; they enable additional functionality when installed:

- [Clangor](https://modrinth.com/mod/clangor) — weapon, selection, swing and impact sounds
- [Sounds](https://modrinth.com/mod/sound) — item/inventory/UI sound support
- [Custom Block Sounds](https://modrinth.com/mod/custom-block-sounds) — block-specific hit, break, place, step and fall

If you choose to install those optional mods, also install whatever dependencies Fabric reports for them. **Mod Menu** is optional, but installing it is recommended to open Shatsu's configuration GUI. **Sound Physics Remastered** remains optional.

These are client mods; Shatsu does not require installation on dedicated servers.

## What's new in v0.2.2 alpha

- **Block placement:** Replaces previous and inconsistent placement audio with **66 newly synthesized mono effects** across 33 material families, wired to both vanilla and all 1,047 Custom Block Sounds mappings. Independently adjustable place mixer. No old placement variants in the generated sound mappings.
- **Wood breaking:** Three new short, muted wood-snap recordings replace the previous heavy breaking samples for wood-based blocks; placement, mining hits and footsteps remain separate.
- **Magical ASMR:** Three new gentle rising XP pickup sounds and three original magical Totem of Undying effects. No old coin-like XP samples.
- **Bow and trident:** Sharper release, draw, throw and impact samples without the previous playful tone. Specialized mace effects and your approved weapon audio remain.
- **Shield raise:** A new client-side trigger plays a restrained shield-ready sound when blocking begins, independently from the vanilla shield-block impact. Short cooldown prevents spam.
- **Minimal Sound Studio:** Compact five-tab interface with searchable sound library, 13 category filters, per-sound volume, pitch, enable/disable and reset controls, paged audio mixer, audio presets and diagnostic test buttons.
- **Dependencies:** Clangor, Sounds and Custom Block Sounds now declared optional in `fabric.mod.json`. Mod Menu and Sound Physics remain optional.
- **Preservation:** Vanilla mob voices, approved inventory-opening chime, drinking, fireworks, Ender Pearl teleport, custom mace effects, and other approved sounds are not intentionally modified.

## Install and test

1. Open [GitHub Actions](https://github.com/Katsuraz/ShatsuSounds-Fabric/actions) and download the latest **successful** v0.2.2 artifact.
2. Extract the artifact and put the JAR in `.minecraft/mods` with Fabric API and all the required mods above.
3. **Disable older Shatsu full resource packs and overlays** to avoid competing sound definitions.
4. For best results, open **Mod Menu → Shatsu Sounds → Home/Library/Mixer/Presets/Tools**.
5. Compare placing stone, wood, gravel and glass; breaking wood; picking up XP; triggering a Totem; drawing a bow, throwing a trident, and raising a shield.

## Development and QA

- `scripts/prepare_audio.py`: stages v7.3 ZIP, repairs quiet mining hits and ordinary-item hit fallback, and generates the searchable catalog.
- `scripts/polish_audio.py`: original synthesized XP, totem, bow, trident, wood and shield samples and events.
- `scripts/placement_polish.py`: original 33-family placement samples replacing legacy variants.
- `scripts/test_sound_repair.py` / `scripts/test_sound_polish.py`: resource, sound-map and dependency regression tests.
- `.github/workflows/build.yml`: builds with Java 25, FFmpeg, tests, and validates real bundled audio in the final JAR.

**Important:** A passing GitHub Actions build validates compilation, files and mappings, not actual Minecraft sound playback or mod interaction. Please report any unexpected double sound with a video and `latest.log`; another enabled resource pack or Sounds-mod setting may play an additional effect even if Shatsu mappings are fixed. This mod does **not** change third-party configuration files or Sound Physics Remastered settings automatically. Redistribution rights for source recordings still need review before a public stable release.
