# Implementation notes

Polymorphic Delight keeps Farmer's Delight and vanilla cooking behavior in place and only changes recipe resolution where a conflict exists.

Cooking Pots and Cutting Boards use Polymorph block-entity recipe data. The Cooking Pot also registers a Polymorph recipe widget next to the output slot.

Campfires, placed Skillets and Farmer's Delight-style Stoves keep recipe selections per active cooking slot. A stored selection is only used while it still matches the current input. Empty or replaced slots discard their previous selection.

Multi-slot blocks support two interaction modes. Targeting a rendered food item changes one slot. If no food item is targeted, a bulk change is allowed only when every occupied slot contains the same ItemStack.

Recipe changes preserve the current cooking ratio. If two matching recipes use different cooking times, the current progress is mapped to the new duration instead of restarting the slot.

Stove targeting uses the food offsets provided by the concrete `AbstractStoveBlockEntity`, which allows compatible addon Stoves to use their own slot count and layout.
