# Shatsu v0.6.6 Alpha.3 — Audio Health & Clangor swing verification

## What changed in source

- New `ShatsuAudioHealth.java` performs a **read-only** scan of whether the Clangor mod is installed and whether all 45 expected `clangor:swing.*` sound events have been registered by the resource manager.
- It also reads inventory interactions/missing-event counters and the newer integrated JAR's food/drinking match counters (when available). The newer food API is accessed via guarded reflection because the public GitHub source still predates that compiled module.
- `ShatsuScreen.java` now has an unobtrusive `?` advanced health screen. It contains a rescan button, separate sword-swing and potion-drink audio previews and a concise read-only status.
- **An event appearing in `sounds.json` is not proof it triggered during gameplay.** The UI explicitly communicates this.
- Compiled with Java 25 / Fabric 26.3 in GitHub Actions run **42813723282** (PASS).

## Actual integrated JAR

The experimental `Shatsu-v0.6.6-ALPHA.3-Audio-Health-UI-Fabric26.3.jar` was assembled from the Alpha.2 integrated JAR, replacing only `ShatsuScreen.class`, updating version metadata, and adding:

- `ShatsuAudioHealth.class`
- `ShatsuAudioHealth$Snapshot.class`

All **6,723 existing OGGs**, all **35 unrelated Java classes**, the actual newer configuration binary, sound catalog and all gameplay mixins were preserved byte-for-byte.

The GitHub's Gradle build still uses the older source audio baseline; do not install the plain CI JAR over the integrated Alpha.3.

## Optional Clangor resource pack

Standalone `Shatsu-v0.6.6-Clangor-Swings-Optional-ResourcePack-26.3.zip` was generated directly from Alpha.2. It contains **45 swing event definitions** and **95 referenced OGG files** plus only the files required to resolve those event definitions.

This pack targets Minecraft Java 26.3 resource pack format 97.1. It is **not a mod JAR** and must be placed in `.minecraft/resourcepacks`. Enable it **above other Clangor packs**, then press F3 + T.

It cannot make Clangor trigger sounds if Clangor itself is missing, has Swing disabled, or doesn't recognize the item's classification. Clangor's own settings are accessible by F12; item categorization is configurable with V by default. Follow the mod's documentation and confirm these keys in the user's keybind settings.

Clangor 2.1.4 Fabric 26.3 is supported; consult the official Modrinth page: https://modrinth.com/mod/clangor

## How to diagnose (in a test instance)

1. Back up `config/shatsu-sounds/profile.json`, remove old Shatsu JARs, and install only Alpha.3.
2. In Minecraft, open Mod Menu > Shatsu > `?` (Audio Health).
3. Check `Clangor: installed` and `Swing events: 45 / 45 registered`.
4. **Real gameplay:** swing swords, axes, pickaxes, shovels and hoes at empty air and in combat, then listen. A registered event is only a static prerequisite.
5. If sound is absent, open Clangor configuration (default F12), confirm global Swing enabled/volume and the item's category. If using competing sound packs, enable the optional Shatsu resource pack at higher priority and reload with F3+T.
6. Drink a potion; click `Rescan`. Inspect `Drinking: enabled`, `Local`, and `Missing`. Local > 0 indicates a routing attempt, *not* audible playback; zero can indicate missing hook or no supported event.
7. Record results and provide `latest.log` if something crashes or stays silent.

## Preserved and pending work

- **Not altered:** drinking clips, approved other audio, all 18 Enchanted Golden Apple inventory recordings (await user approval), gameplay listener classes, and all previous sound assets.
- **Unverified:** real Minecraft 26.3 gameplay, full GUI-scale/keyboard behavior, Clangor swing hooks, drinking sound timing, compatibility with other mods and audio source redistribution rights.
- **UI remaining:** custom modern typography and responsive GUI-scale polish; native Minecraft font is still used.
- No stable/public release is justified yet.

## Validation

- Java 25 source compilation: PASS
- Integrated ZIP/JAR CRC: PASS
- Existing audio byte preservation: PASS (6,723 OGG)
- Old unrelated code preservation: PASS (35 classes)
- Optional resource-pack 45 event/95 sample mapping: PASS
- In-game acceptance: NOT PERFORMED
