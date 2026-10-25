# Implementation — Fabric 1.20.1

## Integration model

The port uses Farmer's Delight Refabricated and keeps the normal recipe and cooking paths. It intercepts recipe resolution only to apply an available Polymorph selection. A single build supports either Polymorph implementation through its compatible API.

`PolymorphicDelight` registers `PolymorphCompat` and Fabric's `UseBlockCallback` through `InteractionHandlers`. `PolymorphicDelightClient` registers `CookingPotRecipesWidget` for the Cooking Pot screen.

## Cardinal Components and Polymorph

The `cardinal-components` entrypoint is `PolymorphBlockComponents`. It registers `CookingPotRecipeData` and `CuttingBoardRecipeData` under Polymorph's `PolymorphFabricComponents.BLOCK_ENTITY_RECIPE_DATA` key.

Both data classes extend `AbstractBlockEntityRecipeDataComponent`, allowing Polymorph to use the same persistent component it queries during recipe selection. This was necessary for the Cooking Pot widget to receive the selected recipes instead of an unrelated data instance.

This version uses Cardinal Components **5.2.1 (`dev.onyxstudios.cca.api.v3`)**. These compile-time API libraries are provided through `libs/`, while Polymorph provides the runtime components. They are not repackaged inside the Polymorphic Delight release JAR.

## Cooking Pot

`CookingPotBlockEntityMixin` routes matching Cooking Pot recipes through Polymorph recipe data. `CookingPotRecipeData` provides an inventory snapshot and refreshes the available recipes when listeners open the menu or the ingredients change. The selection widget appears beside the output slot when applicable.

## Cutting Board

`InteractionHandlers` handles Shift + Right Click before the normal cutting action, finds recipes compatible with both the stored food and the held tool, and advances the selected recipe. `CuttingBoardBlockEntityMixin` applies the Polymorph-selected recipe to the subsequent normal cutting action.

Recipe compatibility is based on the held tool as well as the input food. Changing to an incompatible knife or other tool can remove recipes from the matching set.

## Skillet, Campfire and Stove

`CampfireRecipeSelector` finds matching `minecraft:campfire_cooking` recipes. `SlotRecipeSelections` stores recipe IDs by slot and serializes the selection to block-entity NBT. The Skillet, Campfire and `AbstractStoveBlockEntity` Mixins consult those selections when calculating cooking results.

`HeatDeviceTargeting` resolves the clicked cooking slot from the hit position. Per-slot selections remain independent; bulk cycling affects only occupied slots with matching inputs. Recipes that no longer match the current food are not used.

Changing recipes preserves cooking progress proportionally to the durations of the old and new recipes.

## Version-specific API

- Minecraft 1.20.1 uses Java **17** at runtime; the source build uses **JDK 21** with Loom 1.16.3.
- Farmer's Delight Refabricated tested version: **2.5.7**.
- Polymorph compile API: **0.49.11+1.20.1**.
- Cardinal Components: **5.2.1**.
- The 1.20.1 recipe flow uses the older recipe manager interfaces and inventory `RecipeWrapper`.

## Building

Run `build.bat` once. Gradle compiles against Polymorph original and Loom remaps the single release artifact to `dist/polymorphic_delight-1.0.0-fabric-1.20.1.jar`. The intermediate development JAR is not a separate user-facing build. Do not package local compile dependencies or QA recipes with the mod.
