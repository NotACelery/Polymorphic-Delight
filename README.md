# Polymorphic Delight — Fabric 1.21.1

**Polymorphic Delight** adds recipe selection to **Farmer's Delight Refabricated** when multiple recipes use the same ingredients. It works with either **Polymorph** or **Polymorph+**; install **only one** of them.

## Features

- **Cooking Pot:** choose between conflicting recipes through the Polymorph selector in its interface.
- **Cutting Board:** sneak and right-click with a compatible tool to cycle through matching recipes; right-click normally to process the item.
- **Skillet, Campfire and Stove:** sneak and right-click with a knife to cycle cooking recipes, with per-slot targeting for multiple items.
- **Saved selections:** cooking-device selections persist when the world is saved and reloaded.

## Requirements

- Minecraft **1.21.1**, Fabric Loader and Fabric API.
- **Farmer's Delight Refabricated:** tested with **3.3.6**.
- **Polymorph:** built against **1.1.0**, or **Polymorph+ 1.4.2** as a replacement, not alongside it.
- **Java 21** for Minecraft. Compilation uses JDK 21.

## Technical documentation

- [Compatibility and requirements](docs/COMPATIBILITY.md)
- [Implementation notes](docs/IMPLEMENTATION.md)

## Build from source

Run **`build.bat`** on Windows. It compiles **once**, using Polymorph's public API. The same output works with **either Polymorph or Polymorph+** (never both at once). There are no provider-specific builds.

The build script selects JDK 21, can provision Gradle 9.4.1, and writes diagnostics to `build-logs/`. Minecraft 1.21.1 requires Java 21.

**Single release JAR:** `dist/polymorphic_delight-1.0.0-fabric-1.21.1.jar`.

The source does not bundle QA recipes. Use an external datapack to create conflicting recipes when testing.

## License

Polymorphic Delight is licensed under the [MIT License](LICENSE).
