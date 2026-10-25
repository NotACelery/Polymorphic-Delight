# Polymorphic Delight

![Polymorphic Delight](src/main/resources/polymorphed_delight.png)

Polymorphic Delight adds Polymorph recipe selection to Farmer's Delight cooking blocks on NeoForge 1.21.1.

Recipe conflicts are common in larger Farmer's Delight modpacks. When two recipes accept the same ingredients, Polymorphic Delight lets the player choose the result instead of relying on whichever recipe is picked first.

## Features

- Cooking Pot support using the standard Polymorph selector.
- Cutting Board recipe cycling with Shift + Right Click.
- Placed Skillet recipe cycling while food is cooking.
- Individual and bulk recipe selection on Campfires and Soul Campfires.
- Individual and bulk recipe selection on Farmer's Delight-style Stoves.
- Independent recipe choices for every active Campfire or Stove slot.
- Selected recipes persist while the food is cooking and survive world reloads.
- Cooking progress is preserved when switching recipes.
- Works with either Polymorph or Polymorph+.

For in-world cooking blocks, hold a Farmer's Delight-compatible knife and use Shift + Right Click while food is already cooking. Aim at a food item to change only that slot. On multi-slot blocks, clicking away from an individual item changes all occupied slots when they contain the same food.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.244 or newer
- Farmer's Delight 1.3.4
- Polymorph 1.2.0+1.21.1 or Polymorph+ 1.3.1+1.21.1

Use Polymorph or Polymorph+, not both at the same time.

## Compatibility

Stoves based on Farmer's Delight `AbstractStoveBlockEntity` inherit support automatically when they use campfire cooking recipes. Farmer's Delight, My Nether's Delight and End's Delight Stoves have been tested.

Vanilla Campfires and Soul Campfires are supported. Custom cooking blocks with their own unrelated block entity or recipe system need dedicated integration.

The handheld Skillet is not covered; only the placed Skillet is supported. Normal crafting-table recipe conflicts are handled by Polymorph itself.

When targeting multiple food items, the selection is kept independently for each occupied slot. A changed or emptied slot does not inherit an earlier recipe selection, and switching recipes preserves cooking progress proportionally.

Compatible addon Stoves are supported through `AbstractStoveBlockEntity`. Other custom cooking devices with different block entities or recipe systems may need additional integration.

## Building

Java 21 is required.

On Windows, run `build.bat`. The project compiles once against Polymorph's API and produces **one JAR compatible with Polymorph or Polymorph+**. No provider-specific variants are built.

Output: `dist/polymorphic_delight-1.0.0-neoforge-1.21.1.jar`. The build requires JDK 21 and downloads Gradle when necessary.

The Gradle source project can also be built directly with `gradle build` using JDK 21.

## Credits

- [Farmer's Delight](https://github.com/vectorwing/FarmersDelight) by vectorwing
- [Polymorph](https://github.com/illusivesoulworks/polymorph) by Illusive Soulworks
- [Polymorph+](https://github.com/Aminoquiz/polymorph_plus) by Aminoquiz

## License

Polymorphic Delight is licensed under the [MIT License](LICENSE).
