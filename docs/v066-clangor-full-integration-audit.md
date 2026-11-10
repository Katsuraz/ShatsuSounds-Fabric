# Clangor 2.1.4 Fabric 26.3 — Shatsu v0.6.6 complete integration audit

## Reason for audit

User reports sword and spear swings working but axe, pickaxe and other tools silent, and later suspects broad Clangor dysfunction. This is not simply an instruction to make the existing files louder. **Do not mark tool swing fixes as working without gameplay.**

## Verified against the cumulative Shatsu Alpha.6 JAR

`Shatsu-v0.6.6-ALPHA.6-Retro-Bow-Crossbow-Louder-Drinking-Fabric26.3.jar`

- **142 Clangor event pools**: 45 `swing`, 50 `hit`, 45 `select`, 2 `use`.
- **319 audio references, 256 unique referenced OGG files**, all exist and decode.
- **132 of 256 source files use 22,050 or 32,000 Hz** (outside Clangor's published 44,100/48,000 Hz audio spec). The rest use 44.1 or 48 kHz. All 256 are mono.
- **21 referenced audio files average below −32 dBFS RMS**, many in item selection.
- The two Clangor `use` IDs are `use.bow.charge` and `use.trident.charge`. Crossbow is presently remapped through vanilla `item.crossbow.*` sound events; drinking is entirely unrelated to Clangor.
- These facts establish mapping existence, file presence and measurable source-format issues. **They do not establish that Clangor invokes all these event IDs or that the user's configuration allows playback.**

## Official mod documentation / configuration

- Official Fabric 26.3 release: `clangor-2.1.4-fabric-26.3` (October 1, 2026)
- Requires Fabric API and YetAnotherConfigLib. Clangor configuration default F12, mapping UI default V, reload resource packs F3+T.
- Categories include sword, axe, pickaxe, shovel, hoe, rods, trident, mace, spear, bow/crossbow and multiple modded weapon categories.
- The official *Clangor: Dungeons Soundpack* instructions explicitly say to **disable the built-in resource pack** when using an external soundpack. It also lists shovel and hoe for *hit/select*, but does not demonstrate these categories under *swing*. Therefore Shatsu's 16 `swing.shovel.*` and `swing.hoe.*` entries require real trigger verification, not guesses.
- The v2.1.4 release notes say missing-sound warnings are hidden unless **Debug Log** is enabled.
- Clangor is all-rights-reserved; official GitHub repository provides an issue tracker, **not public source code**. Do not modify, patch, or redistribute the Clangor JAR.

## Test packs delivered in conversation

1. **Shatsu-Clangor-Tool-Trigger-Probe-26.3.zip** — synthetic obvious per-tool pings (axe 2, pickaxe 3, shovel 4, hoe 5); 32 sound events and four 44.1 kHz mono Vorbis clips. Test *first*, then disable it.
2. **Shatsu-Clangor-142-Event-Compatibility-Pack-26.3.zip** — all 142 Clangor event pools and the 256 referenced OGGs. Resamples the 132 out-of-spec files to 44.1 kHz mono Vorbis; preserves the remaining 124 OGGs byte-for-byte. No modifications to the original Shatsu JAR.
3. **Shatsu-Clangor-Complete-Audit-And-Tester-26.3.zip** — both packs, full per-file audit JSON, event TSV, reproduction script and diagnostic README.

All generated packs have valid Minecraft 26.3 resource pack metadata (`min_format/max_format [97,1]`), no unresolved references, correct sample formats, and passing ZIP CRC checks.

### Correct triage sequence

1. Back up settings. Run one cumulative Shatsu JAR, Clangor 2.1.4 Fabric 26.3, Fabric API and YACL.
2. In Clangor F12 enable global/swing and per-category sound toggles; enable Debug Log. Disable **Built-in Resource Pack** when testing external packs.
3. Enable **only** the diagnostic pack above other Clangor soundpacks and press F3+T.
4. Equip an iron axe/pickaxe and left-click **empty air**. Also try shovel/hoe but note they may not have real swing triggers. Compare the known-working sword.
5. Test registration separately with `/playsound clangor:swing.pickaxe.iron master @s ~ ~ ~ 1 1`. If this is audible but tool air-swing silent, debug *item category, mapping or Clangor event emission* rather than resampling recordings again.
6. Press V while holding each tool, check assigned type and material; test with/without conflicting swing/audio mods such as Better Combat.
7. If diagnostic tool pings are triggered, disable probe and test full 142-event pack. If not, ask for the installed `clangor-2.1.4-fabric-26.3.jar`, `config/clangor_config.json`, and focused `latest.log` to investigate active settings and exact event dispatch. Do not invent a working result.

## Release and source gaps

The Clangor binary is not available here; internal method and category-switch logic cannot be verified from public source. This audit **does not** mean Clangor is broken, and does not make v0.6.6 Alpha.6 stable. Source-asset migration into the Shatsu Gradle build and Minecraft runtime testing are still outstanding.

The requested retro Bow/Crossbow and louder drinking Alpha.6 work is a separate Shatsu-audio change; it must not be confused with a fix for Clangor's missing tool-swing triggers.

## Sources

- https://modrinth.com/mod/clangor
- https://www.curseforge.com/minecraft/mc-mods/clangor/files/9026602
- https://modrinth.com/resourcepack/clangor-dungeons-soundpack
- https://minecraft.wiki/w/Pack_format
