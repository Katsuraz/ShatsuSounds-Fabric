# Clangor 2.1.4 — actual binary audit and Alpha.7 sound-folder fix

## User report

The user said Shatsu fixed Clangor sounds early in development, but later pickaxe, axe, shovel, hoe and perhaps all Clangor sounds appeared silent. They supplied **the real `clangor-2.1.4-fabric-26.3.jar`** for review. This report is based on that binary and comparisons of early Shatsu v0.5.4 with the current v0.6.6 Alpha.6.

## Root issue confirmed in actual Clangor classes

Bytecode inspection of `com.bd0518.clangor.ClangorUtils` and `com.bd0518.clangor.mixin.ClangorSwingMixin` establishes:

1. For a vanilla material tool, Clangor builds a sound path like `swing.pickaxe.iron`.
2. Before playing it, Clangor calls `isFolderNotEmpty`. That checks whether the Minecraft ResourceManager has an **actual .ogg file under `sounds/swing/pickaxe/iron/`**, and caches empty folders. This is *not* a `sounds.json` event-registry lookup.
3. Earlier and later Shatsu JARs provided the `clangor:swing.pickaxe.iron` sound event but stored the associated OGG in **different physical directories**, such as `assets/shatsufresh/sounds/tool/pickaxe/swing/iron/` or `assets/shatsufresh/sounds/v066_tool_swing/pickaxe/iron/`. There was no `assets/clangor/sounds/swing/pickaxe/iron/*.ogg` in Alpha.6.
4. Accordingly, correct `sounds.json` event definitions and valid OGGs were **insufficient** to satisfy Clangor's raw-resource prefix check. Built-in/external Clangor packs could potentially mask the issue.
5. Clangor's `ClangorSwingMixin` intentionally **returns without a swing sound when the crosshair hitResult type is BLOCK**, and also suppresses some Better Combat weapon swings when Better Combat attributes are detected. Test on empty air to distinguish this behavior from missing resource routing.

Comparison: Early v0.5.4 had **the same 142 Clangor event IDs** as Alpha.6; 50 pools had changed their recordings, but the event IDs themselves had not disappeared. Therefore the earlier "fixed mappings" work was real, but isn't sufficient proof of correct folder gating in every configuration. The user's claim that earlier behavior worked is not disputed.

## Alpha.7 patch — minimal behavior change

Built from `Shatsu-v0.6.6-ALPHA.6-Retro-Bow-Crossbow-Louder-Drinking-Fabric26.3.jar`, a *cumulative Shatsu* experimental build.

For every one of the 142 Clangor sound events, the patch copies each currently referenced OGG **without changing its audio bytes** into:

`assets/clangor/sounds/<event.key.replace('.', '/')>/shatsu_fix_NN.ogg`

and changes only its sound-event sample reference to the matching `clangor:` path, preserving original event IDs, per-take volume, pitch and other sound metadata. Thus Clangor's event prefix folder is no longer empty. It does not touch Clangor itself.

- 45 swing, 50 hit, 45 select, 2 use events.
- 319 newly placed OGG copies representing 256 unique source samples.
- **6,842 original OGG files preserved byte-for-byte** from Alpha.6 (not 6,805 as estimated in an earlier message).
- Original compiled Java classes, config, and all unrelated JAR files byte-identical.
- Existing-member diff: only `fabric.mod.json` and `assets/clangor/sounds.json`.
- JAR ZIP CRC and resource-pack CRC passed; all 319 new event references exist beneath their matching Clangor folder prefixes.

## Deliverables (in conversation)

- `Shatsu-v0.6.6-ALPHA.7-Clangor-Resource-Routing-Fix-Fabric26.3.jar` cumulative Alpha.7
- `Shatsu-v0.6.6-Clangor-Folder-Routing-Fix-ResourcePack-26.3.zip` optional pack to test with the existing Alpha.6 JAR
- `Shatsu-v0.6.6-Alpha7-Clangor-True-Routing-Fix-Test-Package.zip` (JAR, optional pack, repair script, tests, instructions)

The optional pack may be installed above all other Clangor packs, **without** replacing the current Shatsu JAR. Do not install it simultaneously with Alpha.7 unless troubleshooting pack precedence. Reload F3+T and test a tool *in air*. A separate command `/playsound clangor:swing.pickaxe.iron master @s ~ ~ ~ 1 1` verifies the event registration, but not the hook.

## Caveats

- **Not run inside Minecraft 26.3:** remaining trigger/config/pack/Better Combat issues may exist.
- No alteration or redistribution of Clangor proprietary binary.
- The GitHub repository still has old v0.3 audio baseline; the actual Alpha.7 is a separate derivative of Alpha.6. Full source audio migration remains pending.
- Stable release is blocked on live gameplay QA and audio license verification.
