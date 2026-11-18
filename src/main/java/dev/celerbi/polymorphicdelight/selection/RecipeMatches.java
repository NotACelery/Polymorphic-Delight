package dev.celerbi.polymorphicdelight.selection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

public final class RecipeMatches {
  private RecipeMatches() {}

  @SuppressWarnings("unchecked")
  public static <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> find(
      ServerLevel level, RecipeType<T> type, I input) {
    List<RecipeHolder<T>> result = new ArrayList<>();
    for (RecipeHolder<?> entry : level.recipeAccess().getRecipes()) {
      Recipe<?> recipe = entry.value();
      if (recipe.getType() != type) continue;
      if (!((Recipe<I>) recipe).matches(input, level)) continue;
      result.add((RecipeHolder<T>) entry);
    }
    result.sort(Comparator.comparing(entry -> entry.id().identifier().toString()));
    return result;
  }
}
