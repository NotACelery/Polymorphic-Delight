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

More details are in [Compatibility](docs/COMPATIBILITY.md).

## Building

Java 21 is required.

On Windows, `build.bat` can build against either Polymorph implementation:

```text
build.bat original
build.bat plus
build.bat all
```

A normal Gradle build also works when the required dependencies are available.

## Credits

- [Farmer's Delight](https://github.com/vectorwing/FarmersDelight) by vectorwing
- [Polymorph](https://github.com/illusivesoulworks/polymorph) by Illusive Soulworks
- [Polymorph+](https://github.com/Aminoquiz/polymorph_plus) by Aminoquiz

## License

Polymorphic Delight is licensed under the [MIT License](LICENSE).
