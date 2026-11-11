# Pixel Eco v0.1.5 — Runtime reliability hotfix

Minecraft Java 26.3, Fabric, experimental alpha. The downloadable user JAR contains the cumulative v0.1.4 original audio bank.

## Implementation

- Cache built-in sound-pack enabled state for the mixer and refresh once per client tick.
- When master OFF, suppress Pixel Eco-only custom audio from inventory, charge sounds, shield, bed, ordinary items, throwables and Cave Spider routing.
- Retain resource-pack-based vanilla restoration and other pack selections.
- Package eight recompiled Java classes and a translated K keybinding without altering any audio.
- All 6,437 audio files are byte-for-byte identical to v0.1.4, including 83 water-related audio files.

## Test evidence

- [Minecraft 26.3 Fabric Java compilation passed](https://github.com/Katsuraz/ShatsuSounds-Fabric/actions/runs/43006868911).
- Initial headless client found an empty source-only development sound pack: registration returned false.
- [Corrected headless Minecraft 26.3 client bootstrap passed](https://github.com/Katsuraz/ShatsuSounds-Fabric/actions/runs/43008179879): builtin pack registered true, no Mixin/class bootstrap errors.
- GitHub virtual display could not create an OpenGL/Vulkan backend, so no world opened, no K press was tested, and no audio was played.
- The released full JAR already contains 1,301 actual builtin-pack entries and has 24,634 resolved audio references with zero missing.

## Build and scope limitations

Github source branch compiles the Java code but does not host the cumulative 54 MB sound bank. The reproducible hotfix packaging script and offline QA report are provided with the ChatGPT download. Actual vanilla restoration and optional sound-mod behavior still need gameplay verification. No stable release claim.

## Installation

Remove prior Pixel Eco or regular Shatsu JARs; install only v0.1.5 under Minecraft Java 26.3 Fabric with Fabric API. Press K to try the global sound switch.
