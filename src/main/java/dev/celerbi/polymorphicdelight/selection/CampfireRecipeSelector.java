package dev.celerbi.polymorphicdelight.selection;

import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class CampfireRecipeSelector {
    private CampfireRecipeSelector() {
    }

    public static List<CampfireCookingRecipe> getMatches(Level level, ItemStack inputStack) {
        if (inputStack.isEmpty()) {
            return List.of();
        }

        return level.getRecipeManager().getRecipesFor(
                RecipeType.CAMPFIRE_COOKING,
                new SimpleContainer(inputStack),
                level
        );
    }

    public static Optional<CampfireCookingRecipe> resolve(
            Level level,
            ItemStack inputStack,
            int slot,
            CampfireSelectionAccess access
    ) {
        if (inputStack.isEmpty()) {
            access.polymorphicDelight$getSelections().clear(slot);
            return Optional.empty();
        }

        List<CampfireCookingRecipe> matches = getMatches(level, inputStack);
        if (matches.isEmpty()) {
            access.polymorphicDelight$getSelections().clear(slot);
            return Optional.empty();
        }

        ResourceLocation selectedId = access.polymorphicDelight$getSelections().get(slot);
        if (selectedId != null) {
            for (CampfireCookingRecipe match : matches) {
                if (match.getId().equals(selectedId)) {
                    return Optional.of(match);
                }
            }
            access.polymorphicDelight$getSelections().clear(slot);
        }

        return level.getRecipeManager().getRecipeFor(
                RecipeType.CAMPFIRE_COOKING,
                new SimpleContainer(inputStack),
                level
        );
    }

    public static Optional<SelectionResult> cycle(
            Level level,
            ItemStack inputStack,
            List<Integer> slots,
            CampfireSelectionAccess access,
            List<CampfireCookingRecipe> matches
    ) {
        if (slots.isEmpty() || matches.size() < 2) {
            return Optional.empty();
        }

        int currentIndex = findCurrentIndex(
                level,
                inputStack,
                slots.get(0),
                access,
                matches
        );
        int nextIndex = (currentIndex + 1) % matches.size();
        CampfireCookingRecipe nextRecipe = matches.get(nextIndex);
        int nextCookingTime = Math.max(
                1,
                access.polymorphicDelight$mapRecipeCookingTime(nextRecipe.getCookingTime())
        );

        for (int slot : slots) {
            rescaleProgress(access, slot, nextCookingTime);
            access.polymorphicDelight$getSelections().set(slot, nextRecipe.getId());
        }

        ItemStack output = nextRecipe.assemble(
                new SimpleContainer(inputStack),
                level.registryAccess()
        );
        return Optional.of(new SelectionResult(
                output,
                nextIndex,
                matches.size(),
                slots.size()
        ));
    }

    private static int findCurrentIndex(
            Level level,
            ItemStack inputStack,
            int slot,
            CampfireSelectionAccess access,
            List<CampfireCookingRecipe> matches
    ) {
        ResourceLocation selectedId = access.polymorphicDelight$getSelections().get(slot);
        if (selectedId != null) {
            for (int index = 0; index < matches.size(); index++) {
                if (matches.get(index).getId().equals(selectedId)) {
                    return index;
                }
            }
        }

        Optional<CampfireCookingRecipe> defaultRecipe = level.getRecipeManager().getRecipeFor(
                RecipeType.CAMPFIRE_COOKING,
                new SimpleContainer(inputStack),
                level
        );
        if (defaultRecipe.isPresent()) {
            ResourceLocation defaultId = defaultRecipe.get().getId();
            for (int index = 0; index < matches.size(); index++) {
                if (matches.get(index).getId().equals(defaultId)) {
                    return index;
                }
            }
        }
        return 0;
    }

    private static void rescaleProgress(
            CampfireSelectionAccess access,
            int slot,
            int newCookingTime
    ) {
        int previousProgress = access.polymorphicDelight$getCookingProgress(slot);
        int previousCookingTime = access.polymorphicDelight$getCookingTime(slot);
        int newProgress = 0;

        if (previousProgress > 0 && previousCookingTime > 0) {
            float ratio = (float) previousProgress / previousCookingTime;
            newProgress = Mth.clamp(
                    Math.round(ratio * newCookingTime),
                    0,
                    newCookingTime
            );
        }

        access.polymorphicDelight$setCookingProgress(slot, newProgress);
        access.polymorphicDelight$setCookingTime(slot, newCookingTime);
    }

    public record SelectionResult(
            ItemStack output,
            int selectedIndex,
            int recipeCount,
            int affectedSlots
    ) {
    }
}
