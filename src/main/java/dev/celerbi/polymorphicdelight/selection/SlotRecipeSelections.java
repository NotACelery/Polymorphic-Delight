package dev.celerbi.polymorphicdelight.selection;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class SlotRecipeSelections {
  private static final String DATA_KEY = "PolymorphicDelightSelections";
  private final Map<Integer, Identifier> selections = new HashMap<>();

  public Identifier get(int slot) {
    return selections.get(slot);
  }

  public void set(int slot, Identifier recipeId) {
    selections.put(slot, recipeId);
  }

  public void clear(int slot) {
    selections.remove(slot);
  }

  public void clearAll() {
    selections.clear();
  }

  public void load(ValueInput input, int slotCount) {
    selections.clear();
    ValueInput stored = input.childOrEmpty(DATA_KEY);
    for (int slot = 0; slot < slotCount; slot++) {
      final int index = slot;
      stored
          .getString(Integer.toString(slot))
          .ifPresent(
              value -> {
                Identifier id = Identifier.tryParse(value);
                if (id != null) selections.put(index, id);
              });
    }
  }

  public void save(ValueOutput output) {
    if (selections.isEmpty()) return;
    ValueOutput stored = output.child(DATA_KEY);
    selections.forEach((slot, id) -> stored.putString(Integer.toString(slot), id.toString()));
  }
}
