package dev.celerbi.polymorphicdelight.compat.inworld;

import com.illusivesoulworks.polymorph.api.inworld.Choices;
import com.illusivesoulworks.polymorph.api.inworld.IChoiceStore;
import com.illusivesoulworks.polymorph.api.inworld.IInWorldProvider;
import com.illusivesoulworks.polymorph.api.inworld.IndicatorSpot;
import com.illusivesoulworks.polymorph.api.inworld.Option;
import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import dev.celerbi.polymorphicdelight.access.StoveGlobalChoiceAccess;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import dev.celerbi.polymorphicdelight.selection.MajoritySelection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;

public final class HeatInWorldProvider implements IInWorldProvider {
  private static final int STOVE_SLOTS = 6;
  private static final int CARDINAL_FACES = 4;
  private static final int STOVE_TOTAL = STOVE_SLOTS + CARDINAL_FACES;
  private static final Identifier[] STOVE_KEYS = new Identifier[STOVE_SLOTS];
  private static final Identifier[] STOVE_GLOBAL_KEYS = new Identifier[CARDINAL_FACES];
  private static final Identifier[] SKILLET_KEYS = new Identifier[CARDINAL_FACES];
  private static final IndicatorSpot[] STOVE_SPOTS = new IndicatorSpot[STOVE_SLOTS];
  private static final IndicatorSpot[] STOVE_GLOBAL_SPOTS = new IndicatorSpot[CARDINAL_FACES];
  private static final IndicatorSpot[] SKILLET_SPOTS = new IndicatorSpot[CARDINAL_FACES];

  static {
    float[] x = {0.30f, 0.0f, -0.30f, 0.30f, 0.0f, -0.30f};
    float[] z = {0.20f, 0.20f, 0.20f, -0.20f, -0.20f, -0.20f};
    for (int i = 0; i < STOVE_SLOTS; i++) {
      STOVE_KEYS[i] = Identifier.fromNamespaceAndPath("polymorphic_delight", "stove_slot_" + i);
      STOVE_SPOTS[i] =
          new IndicatorSpot(0.5f - x[i] + 0.105f, 0.5f - z[i] + 0.095f, 0.075f, 1.035f, true);
    }

    for (int face = 0; face < CARDINAL_FACES; face++) {
      SKILLET_KEYS[face] =
          Identifier.fromNamespaceAndPath("polymorphic_delight", "skillet_face_" + face);
      STOVE_GLOBAL_KEYS[face] =
          Identifier.fromNamespaceAndPath("polymorphic_delight", "stove_global_face_" + face);

      SKILLET_SPOTS[face] = IndicatorSpot.onFrontFace(0.81f, 0.205f, 0.078f).turned(face);
      STOVE_GLOBAL_SPOTS[face] = IndicatorSpot.onFrontFace(0.84f, 0.83f, 0.105f).turned(face);
    }
  }

  private final boolean stove;
  private final IChoiceStore store =
      new IChoiceStore() {
        @Override
        public Identifier get(Level level, BlockPos pos, Identifier key) {
          BlockEntity entity = level.getBlockEntity(pos);
          if (isGlobalKey(key)) {
            return entity instanceof StoveGlobalChoiceAccess access
                ? access.polymorphicDelight$getGlobalChoice()
                : null;
          }
          int slot = slotOf(key);
          if (slot < 0 || !(entity instanceof CampfireSelectionAccess access)) return null;
          return access.polymorphicDelight$getSelections().get(slot);
        }

        @Override
        public void set(Level level, BlockPos pos, Identifier key, Identifier selected) {
          BlockEntity entity = level.getBlockEntity(pos);
          if (isGlobalKey(key)) {
            if (entity instanceof StoveGlobalChoiceAccess access) {
              access.polymorphicDelight$setGlobalChoice(selected);
              entity.setChanged();
            }
            return;
          }
          int slot = slotOf(key);
          if (slot < 0 || !(entity instanceof CampfireSelectionAccess access)) return;
          access.polymorphicDelight$getSelections().set(slot, selected);
          entity.setChanged();
        }
      };

  public HeatInWorldProvider(boolean stove) {
    this.stove = stove;
  }

  private boolean isGlobalKey(Identifier key) {
    if (!stove) return false;
    for (Identifier candidate : STOVE_GLOBAL_KEYS) {
      if (candidate.equals(key)) return true;
    }
    return false;
  }

  private int slotOf(Identifier key) {
    Identifier[] keys = stove ? STOVE_KEYS : SKILLET_KEYS;
    for (int i = 0; i < keys.length; i++) {
      if (keys[i].equals(key)) return stove ? i : 0;
    }
    return -1;
  }

  private ItemStack stack(BlockEntity entity, int slot) {
    if (stove && entity instanceof AbstractStoveBlockEntity stoveEntity) {
      return slot >= 0 && slot < stoveEntity.getItems().getSlotCount()
          ? stoveEntity.getItems().getStackInSlot(slot)
          : ItemStack.EMPTY;
    }
    if (!stove && slot == 0 && entity instanceof SkilletBlockEntity skillet) {
      return skillet.getStoredStack();
    }
    return ItemStack.EMPTY;
  }

  @Override
  public int count(BlockState state) {
    return stove ? STOVE_TOTAL : CARDINAL_FACES;
  }

  @Override
  public Choices choices(ServerLevel level, BlockPos pos, BlockState state) {
    return choices(level, pos, state, 0);
  }

  @Override
  public Choices choices(ServerLevel level, BlockPos pos, BlockState state, int index) {
    if (index < 0 || index >= (stove ? STOVE_TOTAL : CARDINAL_FACES)) return null;
    BlockEntity entity = level.getBlockEntity(pos);
    if (!(entity instanceof CampfireSelectionAccess access)) return null;
    if (stove && index >= STOVE_SLOTS) {
      return globalChoices(level, entity, access, index - STOVE_SLOTS);
    }
    int slot = stove ? index : 0;
    ItemStack inputStack = stack(entity, slot);
    if (inputStack.isEmpty()) return null;
    var matches = CampfireRecipeSelector.getMatches(level, inputStack);
    if (matches.size() < 2) return null;
    List<Option> options = new ArrayList<>(matches.size());
    for (var match : matches) {
      ItemStack output = match.value().assemble(new SingleRecipeInput(inputStack));
      if (!output.isEmpty()) options.add(new Option(match.id().identifier(), output));
    }
    if (options.size() < 2) return null;
    Identifier key = stove ? STOVE_KEYS[slot] : SKILLET_KEYS[index];
    return new Choices(key, options, access.polymorphicDelight$getSelections().get(slot));
  }

  private Choices globalChoices(
      ServerLevel level, BlockEntity entity, CampfireSelectionAccess access, int face) {
    if (!(entity instanceof AbstractStoveBlockEntity)
        || !(entity instanceof StoveGlobalChoiceAccess memory)) return null;
    Map<Identifier, Option> common = new LinkedHashMap<>();
    List<Identifier> votes = new ArrayList<>();
    boolean first = true;
    for (int slot = 0; slot < STOVE_SLOTS; slot++) {
      ItemStack input = stack(entity, slot);
      if (input.isEmpty()) continue;
      var recipes = CampfireRecipeSelector.getMatches(level, input);
      if (recipes.size() < 2) continue;
      Map<Identifier, Option> available = new LinkedHashMap<>();
      for (var recipe : recipes) {
        ItemStack output = recipe.value().assemble(new SingleRecipeInput(input));
        if (!output.isEmpty()) {
          available.put(recipe.id().identifier(), new Option(recipe.id().identifier(), output));
        }
      }
      if (available.size() < 2) continue;
      if (first) {
        common.putAll(available);
        first = false;
      } else {
        common.keySet().retainAll(available.keySet());
      }
      Identifier selected = access.polymorphicDelight$getSelections().get(slot);
      votes.add(
          selected != null && available.containsKey(selected)
              ? selected
              : available.keySet().iterator().next());
    }
    if (common.size() < 2) return null;
    Identifier chosen =
        MajoritySelection.choose(
            votes, common.keySet(), memory.polymorphicDelight$getGlobalChoice());
    if (chosen == null) return null;
    if (!chosen.equals(memory.polymorphicDelight$getGlobalChoice())) {
      memory.polymorphicDelight$setGlobalChoice(chosen);
      entity.setChanged();
    }
    return new Choices(STOVE_GLOBAL_KEYS[face], List.copyOf(common.values()), chosen);
  }

  @Override
  public IndicatorSpot spot(BlockState state) {
    return spot(state, 0);
  }

  @Override
  public IndicatorSpot spot(BlockState state, int index) {
    if (!stove) return SKILLET_SPOTS[Math.max(0, Math.min(index, CARDINAL_FACES - 1))];
    if (index < STOVE_SLOTS) return STOVE_SPOTS[Math.max(0, index)];
    return STOVE_GLOBAL_SPOTS[Math.max(0, Math.min(index - STOVE_SLOTS, CARDINAL_FACES - 1))];
  }

  @Override
  public IChoiceStore store() {
    return store;
  }

  @Override
  public void onSelected(Level level, BlockPos pos, Identifier key, Identifier id) {
    if (!(level instanceof ServerLevel server)) return;
    BlockEntity entity = level.getBlockEntity(pos);
    if (!(entity instanceof CampfireSelectionAccess access)) return;
    if (isGlobalKey(key)) {
      applyGlobally(server, pos, entity, access, id);
      return;
    }
    int slot = slotOf(key);
    if (slot < 0) return;
    applySlot(server, pos, entity, access, slot, id);
  }

  private void applyGlobally(
      ServerLevel level,
      BlockPos pos,
      BlockEntity entity,
      CampfireSelectionAccess access,
      Identifier id) {

    boolean changed = false;
    for (int slot = 0; slot < STOVE_SLOTS; slot++) {
      ItemStack input = stack(entity, slot);
      if (input.isEmpty()) continue;
      var recipes = CampfireRecipeSelector.getMatches(level, input);
      if (recipes.size() < 2) continue;
      changed |= applySlot(level, entity, access, slot, id);
    }
    if (changed) notifyChanged(level, pos, entity);
  }

  private static void notifyChanged(ServerLevel level, BlockPos pos, BlockEntity entity) {
    entity.setChanged();
    level.sendBlockUpdated(
        pos, entity.getBlockState(), entity.getBlockState(), Block.UPDATE_CLIENTS);
  }

  private void applySlot(
      ServerLevel level,
      BlockPos pos,
      BlockEntity entity,
      CampfireSelectionAccess access,
      int slot,
      Identifier id) {
    if (applySlot(level, entity, access, slot, id)) notifyChanged(level, pos, entity);
  }

  private boolean applySlot(
      ServerLevel level,
      BlockEntity entity,
      CampfireSelectionAccess access,
      int slot,
      Identifier id) {
    ItemStack input = stack(entity, slot);
    if (input.isEmpty()) return false;
    for (var recipe : CampfireRecipeSelector.getMatches(level, input)) {
      if (!recipe.id().identifier().equals(id)) continue;
      int duration =
          Math.max(1, access.polymorphicDelight$mapRecipeCookingTime(recipe.value().cookingTime()));
      int progress =
          CampfireRecipeSelector.rescaleProgress(
              access.polymorphicDelight$getCookingProgress(slot),
              access.polymorphicDelight$getCookingTime(slot),
              duration);
      access.polymorphicDelight$getSelections().set(slot, id);
      access.polymorphicDelight$setCookingProgress(slot, progress);
      access.polymorphicDelight$setCookingTime(slot, duration);
      return true;
    }
    return false;
  }
}
