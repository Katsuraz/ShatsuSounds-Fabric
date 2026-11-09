# Shatsu Sounds — Fabric 26.3

**Source project under active development.** This is the dedicated Minecraft sound-manager repository and is **not** the unrelated Shatsu music-sync app.

## Current repository state

The Java source and Fabric/Mod Menu build configuration are committed in browsable source form. The GitHub Actions workflow attempts a Java 25 + Gradle/Fabric build.

**IMPORTANT:** The complete v7.3 audio bundle is **not committed**. A build artifact produced now is source/UI-only and does not contain the full Shatsu sound overhaul. **Do not announce a completed mod or create a stable release before in-game testing.**

The preserved Shatsu v7.3 resource-pack audio is in a separate local project package. It must be imported under `src/main/resources/assets/**`, with the matching generated `src/main/resources/shatsu_catalog.json` (replacing the currently empty catalog). There are 3,511 files in the local prepared source archive, most of them short OGG samples.

## Work included so far

- Fabric 26.3 + Java 25 Gradle build setup.
- A Mod Menu configuration screen: dashboard, searchable events, audio preview, mixer, presets, diagnostic mod detection.
- JSON configuration in `config/shatsu-sounds/profile.json`.
- JSON-driven sound catalog with missing-asset detection.
- A GitHub Actions compilation workflow that distinguishes UI-only builds from complete audio releases.

## Work still required

- Upload/import all v7.3 sound assets with their original licenses and attribution.
- Verify every event and sample in a running Minecraft 26.3 client.
- Add and test non-duplicating audio mixer hooks, contextual item triggers, inventory actions, keyboard typing and optional Clangor/Sounds/Custom Block Sounds integrations.
- Implement the advanced sound-pack manager, import/export, and runtime diagnostics.
- Publish a release only after compilation and testing.

## Preserved audio design goals

Keep vanilla mob voices, the approved inventory-opening ping and other approved sound changes. Ordinary-item attack hits should use soft hand-style impacts; retain distinctive effects for named weapons; avoid overpowering combat/explosion sounds. Respect other mods' settings, and never overwrite unrelated JARs.
