# Shatsu Sounds — Minecraft Fabric 26.3

Dedicated repository for the **Shatsu Satisfying Sounds** Minecraft mod. This is separate from the Shatsu music-sync app.

## Current status

**Source import pending.** The complete v0.2 alpha source and bundled Shatsu v7.3 sound assets are prepared as `ShatsuSounds_Fabric_26.3_Source_Beta.zip` in the ChatGPT project conversation. They have **not** been uploaded or compiled here yet. No released or tested JAR exists.

The source package contains Fabric Loom/Gradle configuration, a Mod Menu sound browser, mixer and presets, bundled resource mappings, asset-validation scripts and a GitHub Actions build workflow.

## Next step

Upload the extracted source package to this repository (preserving the directory structure). The supplied `PUBLISH_TO_GITHUB.bat` script can publish it with Git for Windows. After the source is pushed, GitHub Actions will attempt to build the Java 25 / Minecraft 26.3 mod, with an artifact on successful compilation. Check the run logs before treating the artifact as working in Minecraft.

## Sound design requirements

Keep mob voices vanilla. Preserve user-approved inventory opening, drinking, fireworks and other approved sounds. Avoid duplicated effects with Clangor, Sounds and Custom Block Sounds. The mod must not silently modify third-party mod configurations. Audio sources and redistribution rights must be checked before public releases.
