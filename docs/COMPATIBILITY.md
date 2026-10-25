# Compatibility

## Runtime matrix

| Component | Requirement |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47 or newer |
| Farmer's Delight | 1.3.0 or newer |
| Polymorph | 0.49.10 or newer |
| Java | 17 for development and building |

The project is compiled against Farmer's Delight 1.3.4. The runtime dependency intentionally has no upper Farmer's Delight version limit.

Polymorphic Delight is compiled against Polymorph 0.49.10 and accepts newer 1.20.1 Forge releases with no upper version limit. The minimum is intentionally kept at 0.49.10 because Polymorph 0.49.11 changed its JEI integration and can require a newer JEI build. Polymorphic Delight does not depend on that JEI-specific change.

A future Farmer's Delight release can still require an update if it changes internal classes or methods used by the integration. The absence of an upper version limit avoids blocking compatible releases pre-emptively.

## Farmer's Delight systems

### Cooking Pot

The Cooking Pot uses Polymorph block-entity recipe data and a dedicated Polymorph recipe widget attached to the Cooking Pot screen.

### Cutting Board

The Cutting Board has no normal container screen for recipe selection. Polymorphic Delight therefore provides an in-world selector and stores the selected recipe ID directly on the block entity. The selection is also mirrored to Polymorph recipe data when available.

Tool compatibility is evaluated before a cutting recipe is offered. A recipe that matches the stored item but not the held cutting tool is not included in the conflict set.

### Skillet

Placed Skillets use the in-world selector for conflicting `minecraft:campfire_cooking` recipes.

### Campfire and Soul Campfire

Both vanilla campfire block entities are supported. Selection state is tracked independently for each cooking slot.

### Stoves

Stoves based on Farmer's Delight `AbstractStoveBlockEntity` receive the same per-slot selection support. Addons that extend this class can inherit the integration without a dedicated adapter.

A cooking block that implements its own independent block entity and recipe logic requires a separate integration.

## Polymorph+

The Forge 1.20.1 port compiles against Polymorph's original API (minimum 0.49.10). It does not require Polymorph+. A Polymorph+ release may work as an alternative when it supplies the compatible Polymorph API and mod ID; this Forge version has not been independently validated against a specific Polymorph+ release. Do not install both providers simultaneously.

## Recipe types

The Cooking Pot and Cutting Board use their Farmer's Delight recipe types.

Skillet, Campfire, Soul Campfire and supported Stoves use `minecraft:campfire_cooking` conflicts. Recipes implemented through a different custom cooking recipe type are outside this integration unless a dedicated adapter is added.
