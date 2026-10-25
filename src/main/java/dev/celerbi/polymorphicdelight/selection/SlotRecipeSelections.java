package dev.celerbi.polymorphicdelight.selection;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public final class SlotRecipeSelections {
    private static final String NBT_KEY = "PolymorphicDelightSelections";

    private final Map<Integer, ResourceLocation> selections = new HashMap<>();

    public ResourceLocation get(int slot) {
        return selections.get(slot);
    }

    public void set(int slot, ResourceLocation recipeId) {
        selections.put(slot, recipeId);
    }

    public void clear(int slot) {
        selections.remove(slot);
    }

    public void clearAll() {
        selections.clear();
    }

    public void load(CompoundTag parent, int slotCount) {
        selections.clear();
        if (!parent.contains(NBT_KEY, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag stored = parent.getCompound(NBT_KEY);
        for (int slot = 0; slot < slotCount; slot++) {
            String key = Integer.toString(slot);
            if (!stored.contains(key, Tag.TAG_STRING)) {
                continue;
            }

            ResourceLocation recipeId = ResourceLocation.tryParse(stored.getString(key));
            if (recipeId != null) {
                selections.put(slot, recipeId);
            }
        }
    }

    public void save(CompoundTag parent) {
        if (selections.isEmpty()) {
            parent.remove(NBT_KEY);
            return;
        }

        CompoundTag stored = new CompoundTag();
        selections.forEach((slot, recipeId) ->
                stored.putString(Integer.toString(slot), recipeId.toString())
        );
        parent.put(NBT_KEY, stored);
    }
}
