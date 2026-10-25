# Compatibility — Fabric 1.21.1

## Requirements and tested environment

| Component | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| Fabric Loader | 0.19.5 (tested) |
| Fabric API | 0.116.6+1.21.1 (tested) |
| Farmer's Delight Refabricated | 3.3.6 (tested) |
| Polymorph original | 1.2.0+1.21.1 (tested) |
| Polymorph+ | 1.4.2+1.21.1 (tested as an alternative) |
| Java to run Minecraft | 21 |
| JDK used for compilation | 21 |

Install exactly **one** recipe-selection provider: Polymorph **or** Polymorph+. Both providers were tested with the same Polymorphic Delight JAR; provider-specific variants are not necessary. The runtime metadata uses the `polymorph` dependency ID, which a compatible Polymorph+ build supplies.

The project is compiled against Polymorph **1.1.0+1.21.1**; its other tested provider versions are runtime compatibility results, not additional build variants. Versions not listed here have not been independently validated.

## Supported cooking systems

- **Cooking Pot:** uses the normal Polymorph recipe selector inside the Cooking Pot screen when multiple recipes match its input.
- **Cutting Board:** Shift + Right Click cycles matching cutting recipes with the tool in hand. Cutting recipes requiring a different tool are not offered; use normal Right Click to process the selection.
- **Placed Skillet:** select between conflicting `minecraft:campfire_cooking` recipes while food is cooking.
- **Campfire and Soul Campfire:** one recipe selection per occupied cooking slot.
- **Farmer's Delight-style Stoves:** support applies to subclasses of `AbstractStoveBlockEntity` using the compatible campfire-cooking recipe path.

For the placed Skillet, Campfires and Stoves, hold a Farmer's Delight-compatible knife and use **Shift + Right Click**. Targeting food changes an individual slot. Without an individual target, a bulk change applies only if occupied slots contain matching input items. When occupied foods differ, an individual food must be targeted.

Stored selections survive saving and reloading the world. Switching between recipes with different cooking durations preserves proportional cooking progress.

## Limitations

- The handheld Skillet is not the same as a placed Skillet and is not covered by this block-entity integration.
- Custom Stoves with unrelated block entities, or cooking systems with other recipe types, need their own integration.
- Normal crafting-grid recipe conflicts are handled by Polymorph itself.
- Sample QA recipes and testing datapacks are **not** included in the release JAR or source distribution.
