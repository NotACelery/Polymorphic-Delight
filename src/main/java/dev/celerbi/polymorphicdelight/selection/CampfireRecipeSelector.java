package dev.celerbi.polymorphicdelight.selection;

import java.util.List;
import java.util.Optional;

import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public final class CampfireRecipeSelector {
    private CampfireRecipeSelector() {
    }

    public static List<RecipeHolder<CampfireCookingRecipe>> getMatches(Level level, ItemStack stack) {
        if (stack.isEmpty()) return List.of();
        return level.getRecipeManager().getRecipesFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(stack), level);
    }

    public static Optional<RecipeHolder<CampfireCookingRecipe>> resolve(Level level, ItemStack stack, int slot, CampfireSelectionAccess access) {
        if (stack.isEmpty()) {
            access.polymorphicDelight$getSelections().clear(slot);
            return Optional.empty();
        }
        List<RecipeHolder<CampfireCookingRecipe>> matches = getMatches(level, stack);
        if (matches.isEmpty()) {
            access.polymorphicDelight$getSelections().clear(slot);
            return Optional.empty();
        }
        ResourceLocation id = access.polymorphicDelight$getSelections().get(slot);
        if (id != null) {
            for (RecipeHolder<CampfireCookingRecipe> candidate : matches) if (candidate.id().equals(id)) return Optional.of(candidate);
            access.polymorphicDelight$getSelections().clear(slot);
        }
        return level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(stack), level);
    }

    public static Optional<SelectionResult> cycle(Level level, ItemStack stack, List<Integer> slots,
        CampfireSelectionAccess access, List<RecipeHolder<CampfireCookingRecipe>> matches) {
        if (slots.isEmpty() || matches.size() < 2) return Optional.empty();
        int current = 0;
        ResourceLocation id = access.polymorphicDelight$getSelections().get(slots.get(0));
        if (id == null) {
            var vanilla = level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(stack), level);
            if (vanilla.isPresent()) id = vanilla.get().id();
        }
        for (int i = 0; i < matches.size(); i++) if (matches.get(i).id().equals(id)) {
            current = i;
            break;
        }
        int next = (current + 1) % matches.size();
        RecipeHolder<CampfireCookingRecipe> recipe = matches.get(next);
        int newTime = Math.max(1, access.polymorphicDelight$mapRecipeCookingTime(recipe.value().getCookingTime()));
        for (int slot : slots) {
            int progress = access.polymorphicDelight$getCookingProgress(slot);
            int oldTime = access.polymorphicDelight$getCookingTime(slot);
            int normalized = oldTime > 0 ? Mth.clamp(Math.round((float) progress / oldTime * newTime), 0, newTime) : 0;
            access.polymorphicDelight$setCookingProgress(slot, normalized);
            access.polymorphicDelight$setCookingTime(slot, newTime);
            access.polymorphicDelight$getSelections().set(slot, recipe.id());
        }
        ItemStack output = recipe.value().assemble(new SingleRecipeInput(stack), level.registryAccess());
        return Optional.of(new SelectionResult(output, next, matches.size(), slots.size()));
    }

    public record SelectionResult(ItemStack output, int selectedIndex, int recipeCount, int affectedSlots) {
    }
}
