# Compatibility

Polymorphic Delight supports Farmer's Delight recipe conflicts in the Cooking Pot, Cutting Board and placed Skillet, plus campfire cooking conflicts on Campfires and Farmer's Delight-style Stoves.

## Polymorph providers

The mod works with either of these on NeoForge 1.21.1:

- Polymorph 1.2.0+1.21.1
- Polymorph+ 1.3.1+1.21.1

Polymorph+ provides the `polymorph` compatibility alias expected by addons. Installing both Polymorph implementations together is not supported.

## Stoves

Stove support is based on Farmer's Delight `AbstractStoveBlockEntity` rather than a list of mod IDs. Compatible subclasses that use `minecraft:campfire_cooking` inherit the same slot selection behavior.

Tested Stoves:

- Farmer's Delight Stove
- My Nether's Delight Nether Stove
- End's Delight End Stove

Each occupied slot keeps its own selected recipe. Bulk selection only affects the foods already present when the action is performed.

## Campfires

Vanilla Campfires and Soul Campfires are supported with independent selection for all four cooking positions.

A custom Campfire from another mod is only covered automatically when it keeps the vanilla `CampfireBlockEntity` cooking path. Blocks with unrelated inventories, timers or recipe handling need a separate integration.

## Not handled by this mod

The handheld Farmer's Delight Skillet uses item and player-use state rather than a world block entity, so it is not part of the current integration.

Normal crafting-grid conflicts are already handled by Polymorph itself.
