# Implementation

## Overview

The Forge 1.20.1 port preserves the behavior of the Minecraft 1.21.1 NeoForge branch while adapting the implementation to the older Minecraft, Forge, Farmer's Delight and Polymorph APIs.

The port uses two selection paths:

- Polymorph recipe data and widgets for screen-based systems.
- Persistent in-world recipe-ID selection for cooking blocks without a suitable screen.

## Cooking Pot

`CookingPotRecipeData` exposes Cooking Pot input state to Polymorph. `CookingPotBlockEntityMixin` routes Farmer's Delight recipe lookup through Polymorph when recipe data is attached.

`CookingPotRecipesWidget` places the standard Polymorph selector beside the Cooking Pot output slot.

## Cutting Board

`CuttingBoardSelectionHandler` gathers all cutting recipes that match both the stored item and the held tool. Shift + Right Click cycles those recipes without processing the item.

`CuttingBoardBlockEntityMixin` stores the selected recipe ID in the Cutting Board block entity and gives that recipe priority during Farmer's Delight's recipe lookup.

The selected recipe is authoritative by ID rather than by its displayed output. Two recipes with the same primary result but different secondary results can therefore remain distinct selections.

## Campfire cooking systems

`CampfireRecipeSelector` resolves and cycles conflicting `campfire_cooking` recipes.

`SlotRecipeSelections` stores recipe IDs by slot and serializes them to NBT.

`CampfireBlockEntityMixin`, `SkilletBlockEntityMixin` and `AbstractStoveBlockEntityMixin` route the final cooking lookup through the stored selection when one exists.

## Cooking progress

When a selected recipe has a different cooking duration, the current progress is converted proportionally to the new duration.

For example, changing at 50 percent progress from a 600-tick recipe to a 1200-tick recipe produces approximately 600 ticks of progress rather than restarting the item or preserving the old raw tick count.

The Skillet maps the selected recipe duration through Farmer's Delight's Skillet cooking-time calculation before applying the progress conversion.

## Multi-slot targeting

Campfires and Stoves can contain several active items. `HeatDeviceTargeting` maps the interaction position to the nearest occupied cooking slot.

If a specific occupied slot is targeted, only that slot is changed. If no slot is resolved and all occupied inputs match, the selection can be changed together for those matching slots.

## Persistence

In-world selections are serialized with the owning block entity. Invalid selections are ignored when they no longer match the current input recipe set.

## Forge 1.20.1 port details

The port targets Java 17 and Forge 47. Farmer's Delight members referenced directly by Mixins use the appropriate non-remapped targets, while Minecraft members continue to use normal mappings.

The Cutting Board recipe wrapper uses a modifiable snapshot of the exposed item handler because Forge 1.20.1 `RecipeWrapper` requires an `IItemHandlerModifiable` implementation.
