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
        if (!parent.contains(NBT_KEY, Tag.TAG_COMPOUND)) return;
        CompoundTag saved = parent.getCompound(NBT_KEY);
        for (int slot = 0; slot < slotCount; slot++) {
            String key = Integer.toString(slot);
            if (!saved.contains(key, Tag.TAG_STRING)) continue;
            ResourceLocation id = ResourceLocation.tryParse(saved.getString(key));
            if (id != null) selections.put(slot, id);
        }
    }

    public void save(CompoundTag parent) {
        if (selections.isEmpty()) {
            parent.remove(NBT_KEY);
            return;
        }
        CompoundTag saved = new CompoundTag();
        selections.forEach((slot, id) -> saved.putString(Integer.toString(slot), id.toString()));
        parent.put(NBT_KEY, saved);
    }
}
