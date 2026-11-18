package dev.celerbi.polymorphicdelight.compat.inworld;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.inworld.Choices;
import com.illusivesoulworks.polymorph.api.inworld.IChoiceStore;
import com.illusivesoulworks.polymorph.api.inworld.IInWorldProvider;
import com.illusivesoulworks.polymorph.api.inworld.IndicatorSpot;
import com.illusivesoulworks.polymorph.api.inworld.Option;
import dev.celerbi.polymorphicdelight.selection.RecipeMatches;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipeInput;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

public final class CuttingBoardInWorldProvider implements IInWorldProvider {

  private static final int CORNERS = 4;
  private static final Identifier[] KEYS = new Identifier[CORNERS];
  private static final IndicatorSpot[] SPOTS = new IndicatorSpot[CORNERS];

  static {
    IndicatorSpot corner = new IndicatorSpot(0.82f, 0.82f, 0.085f, 0.074f, true);
    for (int i = 0; i < CORNERS; i++) {
      KEYS[i] = Identifier.fromNamespaceAndPath("polymorphic_delight", "cutting_board_corner_" + i);
      SPOTS[i] = corner.turned(i);
    }
  }

  private static final List<Supplier<Item>> KNIVES =
      List.of(
          ModItems.FLINT_KNIFE,
          ModItems.COPPER_KNIFE,
          ModItems.IRON_KNIFE,
          ModItems.GOLDEN_KNIFE,
          ModItems.DIAMOND_KNIFE,
          ModItems.NETHERITE_KNIFE);

  private final IChoiceStore store =
      new IChoiceStore() {
        @Override
        public Identifier get(Level level, BlockPos pos, Identifier key) {
          var data = recipeData(level, pos);
          return data == null ? null : data.getSelectedRecipeId();
        }

        @Override
        public void set(Level level, BlockPos pos, Identifier key, Identifier id) {
          var data = recipeData(level, pos);
          if (data == null) return;
          data.chooseRecipe(id);
          data.getOwner().setChanged();
        }
      };

  private static com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData
      recipeData(Level level, BlockPos pos) {
    if (!(level.getBlockEntity(pos) instanceof CuttingBoardBlockEntity board)) return null;
    return PolymorphApi.getInstance().getBlockEntityRecipeData(board);
  }

  private static List<RecipeHolder<CuttingBoardRecipe>> matches(
      ServerLevel level, CuttingBoardBlockEntity board) {
    if (board.isEmpty() || board.isItemCarvingBoard()) return List.of();

    Map<Identifier, RecipeHolder<CuttingBoardRecipe>> candidates = new LinkedHashMap<>();
    for (Supplier<Item> knife : KNIVES) {
      var input = new CuttingBoardRecipeInput(board.getStoredItem(), new ItemStack(knife.get()));
      for (var recipe : RecipeMatches.find(level, ModRecipeTypes.CUTTING.get(), input)) {
        if (recipe.value().getTool().test(new ItemStack(knife.get()))) {
          candidates.putIfAbsent(recipe.id().identifier(), recipe);
        }
      }
    }
    return candidates.values().stream()
        .sorted(java.util.Comparator.comparing(r -> r.id().identifier().toString()))
        .toList();
  }

  @Override
  public int count(BlockState state) {
    return CORNERS;
  }

  @Override
  public Choices choices(ServerLevel level, BlockPos pos, BlockState state) {
    return choices(level, pos, state, 0);
  }

  @Override
  public Choices choices(ServerLevel level, BlockPos pos, BlockState state, int index) {
    if (index < 0 || index >= CORNERS) return null;
    if (!(level.getBlockEntity(pos) instanceof CuttingBoardBlockEntity board)) return null;
    var matches = matches(level, board);
    if (matches.size() < 2) return null;
    List<Option> options = new ArrayList<>(matches.size());
    for (var entry : matches) {

      ItemStack result = ItemStack.EMPTY;
      for (Supplier<Item> knife : KNIVES) {
        var input = new CuttingBoardRecipeInput(board.getStoredItem(), new ItemStack(knife.get()));
        if (entry.value().matches(input, level)
            && entry.value().getTool().test(new ItemStack(knife.get()))) {
          result = entry.value().assemble(input);
          break;
        }
      }
      if (!result.isEmpty()) options.add(new Option(entry.id().identifier(), result));
    }
    if (options.size() < 2) return null;
    var data = recipeData(level, pos);
    return new Choices(KEYS[index], options, data == null ? null : data.getSelectedRecipeId());
  }

  @Override
  public IndicatorSpot spot(BlockState state) {
    return SPOTS[0];
  }

  @Override
  public IndicatorSpot spot(BlockState state, int index) {
    return SPOTS[Math.max(0, Math.min(index, CORNERS - 1))];
  }

  @Override
  public IChoiceStore store() {
    return store;
  }
}
