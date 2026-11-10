# Shatsu v0.6.4 Alpha — Selective Mob Sound Overhaul

The user directs one complete sound category per "next". This milestone covers **Ender Dragon, Zombie Villager, Skeleton Horse, Zombie Horse, Spider and Cave Spider**.

## Which JAR to install

The **installable** experimental JAR supplied in this conversation is:

`Shatsu-v0.6.4-ALPHA-Mob-Overhaul-Integrated-Fabric26.3.jar`

It is built from the **v0.6.3 Magical & Cathedral Bells audio JAR**, NOT from the old v0.3 asset baseline still referenced in this repository's regular Gradle builds. Do not substitute a generic GitHub Actions v0.3 JAR for the experimental v0.6.4 audio JAR.

## Audio work

- **Ender Dragon:** Five events remapped: growl, ambient, hurt, death and shoot. Uses the user's two `Fantasy Sound Library` Dragon Growl recordings mixed with existing Shatsu Dragon audio. Wing-flap unchanged.
- **Zombie Villager:** Ambient, hurt and death event definitions are exact clones of Shatsu's ordinary Zombie pools; curing/converted events untouched.
- **Skeleton Horse:** Ambient, underwater ambient, hurt, death, angry; hollow equine-style tones with existing Skeleton clatters.
- **Zombie Horse:** Ambient, hurt, death, angry; synthetic undead whinny-like audio with original Zombie sounds plus fantasy raspy textures. **Not authentic field-recorded horse sounds**.
- **Spider:** Ambient, hurt, death and step are now concise, creepy chitter/hiss/clack pools.
- **Cave Spider:** Minecraft Java 26.3 uses the *same* vanilla Spider sound events. Four custom distinct audio pools plus a new experimental **client packet substitution Mixin** aim to route sounds based on the actual speaker rather than layering multiple effects.

There are **21 changed Minecraft sound event definitions**, **4 added custom Cave Spider events**, and **66 new OGG files in 22 three-take pools**. All **6,516 original OGG files from v0.6.3 remain byte-for-byte unchanged**; other mob voices and previously approved sound families are preserved. No invented vanilla Spider attack sound is claimed.

## Source changes

- `src/main/java/dev/shatsu/sounds/client/ShatsuCaveSpiderRouting.java`
- `src/main/java/dev/shatsu/sounds/mixin/ShatsuCaveSpiderPacketMixin.java`
- `src/main/resources/shatsu-sounds.mixins.json`

The routing handles both positional and entity-bound sound packets, only remaps `minecraft:entity.spider.(ambient|hurt|death|step)` to a cave-specific asset when the audio source can be attributed to a Cave Spider, and otherwise falls back to normal spider sounds. Mixin injection is optional and scoped to the client. Both new classes were compiled using Fabric 26.3 / Java 25 in GitHub Actions run **42786043478**, then overlaid into the actual v0.6.4 resource JAR without replacing existing classes.

## Checks

- JAR archive CRC: PASS.
- 6,516 protected original OGG recordings: all identical.
- 66 new mono 32 kHz Vorbis clips: decode correctly, 3 distinct byte-hash takes per pool.
- Sound-resource references: 22,983 inspected; zero missing non-Minecraft references.
- Zombie Villager identity to ordinary Zombie sound definition: PASS.
- Fabric 26.3 Java 25 compile-only GitHub Actions: PASS.
- New cave packet Mixin in final JAR manifest: PASS.
- Unrelated original classes and sound recordings: unchanged.
- **NOT TESTED in a running Minecraft 26.3 client:** Cave Spider packet matching, subjective audio, game crash safety, mob timing, compatibility with the user's modpack.

## Listening preview

A 35-clip timestamped MP3 preview is in the conversation test package. It uses authentic v0.6.3 -> v0.6.4 comparisons for the five Dragon events and standalone previews for other mobs whose vanilla original audio isn't included inside the Shatsu base JAR.

## License / attribution

*Fantasy Sound Effects Library* by **Little Robot Sound Factory**, **CC BY 3.0**, source https://opengameart.org/content/fantasy-sound-effects-library and creator site www.littlerobotsoundfactory.com — mixed/modified Dragon Growl and Goblin samples.

*100 CC0 SFX* by **rubberduck**, CC0, https://opengameart.org/content/100-cc0-sfx — additional short click/noise/foley samples.

Other vocal, hoof-like and insect sounds were synthesized or came from existing Shatsu sounds. Public release still requires the inherited audio licensing audit.

### Next

Stop here. Await user instruction before any new category or full release/QA pass.
