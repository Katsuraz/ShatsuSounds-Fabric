# Shatsu v7.3 Sound Bundle

The source and build workflow live in this repository. The large sound library is packaged separately to simplify importing it through GitHub's web interface.

Upload the single file `ShatsuSounds_v7.3_assets_only.zip` into this `audio/` directory. It contains the actual `assets/**` sound trees, the generated `shatsu_catalog.json`, and `pack.png`.

- Expected ZIP size: approximately 14.48 MiB
- Expected files: 3,476
- SHA-256: `5075b3d3d606409f95e97cb61da81246ac3c011f9de7bc35b59042b372debc36`

The Gradle `processResources` task automatically includes the ZIP's contents in the built mod JAR; do not extract and commit thousands of OGGs manually.

The build does not claim runtime or Sound Physics Remastered compatibility until in-game testing is completed. See the sound-source credit files in the prepared source archive, and confirm redistribution rights before a public release.
