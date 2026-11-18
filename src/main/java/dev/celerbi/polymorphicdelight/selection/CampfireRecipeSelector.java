package dev.celerbi.polymorphicdelight.selection;

import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public final class CampfireRecipeSelector {
  private CampfireRecipeSelector() {}

  public static List<RecipeHolder<CampfireCookingRecipe>> getMatches(
      ServerLevel level, ItemStack stack) {
    if (stack.isEmpty()) return List.of();
    return RecipeMatches.find(level, RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(stack));
  }

  public static Optional<RecipeHolder<CampfireCookingRecipe>> resolve(
      ServerLevel level, ItemStack stack, int slot, CampfireSelectionAccess access) {
    if (stack.isEmpty()) {
      access.polymorphicDelight$getSelections().clear(slot);
      return Optional.empty();
    }
    List<RecipeHolder<CampfireCookingRecipe>> matches = getMatches(level, stack);
    if (matches.isEmpty()) {
      access.polymorphicDelight$getSelections().clear(slot);
      return Optional.empty();
    }
    Identifier id = access.polymorphicDelight$getSelections().get(slot);
    if (id != null) {
      for (RecipeHolder<CampfireCookingRecipe> candidate : matches) {
        if (candidate.id().identifier().equals(id)) return Optional.of(candidate);
      }
      access.polymorphicDelight$getSelections().clear(slot);
    }
    return Optional.of(matches.get(0));
  }

  public static Optional<SelectionResult> cycle(
      ServerLevel level,
      ItemStack stack,
      List<Integer> slots,
      CampfireSelectionAccess access,
      List<RecipeHolder<CampfireCookingRecipe>> matches) {
    if (slots.isEmpty() || matches.size() < 2) return Optional.empty();
    int current = 0;
    Identifier id = access.polymorphicDelight$getSelections().get(slots.get(0));
    if (id == null) {
      if (!matches.isEmpty()) id = matches.get(0).id().identifier();
    }
    for (int i = 0; i < matches.size(); i++) {
      if (matches.get(i).id().identifier().equals(id)) {
        current = i;
        break;
      }
    }
    int next = (current + 1) % matches.size();
    RecipeHolder<CampfireCookingRecipe> recipe = matches.get(next);
    int newTime =
        Math.max(1, access.polymorphicDelight$mapRecipeCookingTime(recipe.value().cookingTime()));
    for (int slot : slots) {
      int progress = access.polymorphicDelight$getCookingProgress(slot);
      int oldTime = access.polymorphicDelight$getCookingTime(slot);
      int normalized = rescaleProgress(progress, oldTime, newTime);
      access.polymorphicDelight$setCookingProgress(slot, normalized);
      access.polymorphicDelight$setCookingTime(slot, newTime);
      access.polymorphicDelight$getSelections().set(slot, recipe.id().identifier());
    }
    ItemStack output = recipe.value().assemble(new SingleRecipeInput(stack));
    return Optional.of(new SelectionResult(output, next, matches.size(), slots.size()));
  }

  public static int rescaleProgress(int elapsed, int previousDuration, int newDuration) {
    if (previousDuration <= 0 || newDuration <= 0) return 0;
    return Mth.clamp(Math.round((float) elapsed / previousDuration * newDuration), 0, newDuration);
  }

  public record SelectionResult(
      ItemStack output, int selectedIndex, int recipeCount, int affectedSlots) {}
}
