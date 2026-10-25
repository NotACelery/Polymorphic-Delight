# Polymorphic Delight — Forge 1.20.1

Polymorphic Delight adds recipe selection for Farmer's Delight cooking systems when multiple recipes match the same ingredients. This is the **Forge 1.20.1**, **1.0.1** source project.

## Requirements

- Minecraft **1.20.1** with **Forge 47** or newer, compatible with Minecraft 1.20.1.
- **Farmer's Delight 1.3.0** or newer, compatible with Minecraft 1.20.1 (compiled against 1.3.4).
- **Polymorph 0.49.10+1.20.1**, or a compatible **Polymorph+** implementation supplying the Polymorph mod ID/API.
- **Java 17 JDK** to compile.

Install **Polymorph or Polymorph+**, not both together. Only one Polymorphic Delight JAR is required; there is no separate build for each recipe selector provider.

## Features

- **Cooking Pot:** standard Polymorph recipe-selector widget.
- **Cutting Board:** sneak + right-click with the proper cutting tool to cycle matching recipes, then right-click to cut.
- **Placed Skillet:** switch between conflicting `campfire_cooking` recipes.
- **Campfire and Soul Campfire:** switch recipes per occupied slot, or across matching slots when no individual item is targeted.
- **Farmer's Delight-style Stoves:** per-slot selection for compatible stoves using the Farmer's Delight cooking implementation.
- **Persistence:** selected recipe IDs are saved; supported cooking devices retain relative cooking progress when switching recipes.

Use **Shift + Right Click** with a Farmer's Delight-compatible knife to change recipes on active in-world cooking devices. For blocks with multiple cooking slots, targeting a specific food item changes its slot; otherwise the selection can apply to equivalent occupied slots.

## Forge 1.0.1 compatibility note

Version **1.0.1** contains the compatibility adjustment for an issue between certain JEI and Polymorph versions. If that upstream incompatibility is no longer present in your modpack, version **1.0.0** remains an alternative. This release preserves the tested 1.0.1 codebase.

## Technical documentation

- [Compatibility and requirements](docs/COMPATIBILITY.md)
- [Implementation notes](docs/IMPLEMENTATION.md)

## Building

Run `build.bat` on Windows. The build helper selects a JDK 17, downloading Eclipse Temurin 17 locally if needed, and uses Gradle 8.8 when no Gradle installation or wrapper is available.

The script runs a **single `clean build`**, verifies the release filename, and moves the completed, reobfuscated Forge JAR to `dist/`:

```text
polymorphic_delight-1.0.1-forge-1.20.1.jar
```

The Gradle build also works independently. Its direct artifact appears under `build/libs/` until the helper moves it to `dist/`.

Only the runtime JAR is intended for installation. Build outputs, caches, local JDK/Gradle downloads, and logs are excluded from the source package.

## Credits

- [Farmer's Delight](https://github.com/vectorwing/FarmersDelight) by vectorwing
- [Polymorph](https://github.com/illusivesoulworks/polymorph) by Illusive Soulworks

## License

MIT. See `LICENSE`.
