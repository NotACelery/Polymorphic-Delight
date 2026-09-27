package dev.celerbi.polymorphicdelight.selection;

import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public final class CampfireRecipeSelector {
    private CampfireRecipeSelector() {
    }

    public static List<RecipeHolder<CampfireCookingRecipe>> getMatches(
            Level level,
            ItemStack inputStack
    ) {
        if (inputStack.isEmpty()) {
            return List.of();
        }

        return level.getRecipeManager().getRecipesFor(
                RecipeType.CAMPFIRE_COOKING,
                new SingleRecipeInput(inputStack),
                level
        );
    }

    public static Optional<RecipeHolder<CampfireCookingRecipe>> resolve(
            Level level,
            ItemStack inputStack,
            int slot,
            CampfireSelectionAccess access
    ) {
        if (inputStack.isEmpty()) {
            access.polymorphicDelight$getSelections().clear(slot);
            return Optional.empty();
        }

        List<RecipeHolder<CampfireCookingRecipe>> matches = getMatches(level, inputStack);
        if (matches.isEmpty()) {
            access.polymorphicDelight$getSelections().clear(slot);
            return Optional.empty();
        }

        ResourceLocation selectedId = access.polymorphicDelight$getSelections().get(slot);
        if (selectedId != null) {
            for (RecipeHolder<CampfireCookingRecipe> match : matches) {
                if (match.id().equals(selectedId)) {
                    return Optional.of(match);
                }
            }
            access.polymorphicDelight$getSelections().clear(slot);
        }

        return level.getRecipeManager().getRecipeFor(
                RecipeType.CAMPFIRE_COOKING,
                new SingleRecipeInput(inputStack),
                level
        );
    }

    public static Optional<SelectionResult> cycle(
            Level level,
            ItemStack inputStack,
            List<Integer> slots,
            CampfireSelectionAccess access,
            List<RecipeHolder<CampfireCookingRecipe>> matches
    ) {
        if (slots.isEmpty() || matches.size() < 2) {
            return Optional.empty();
        }

        int sourceSlot = slots.getFirst();
        int currentIndex = findCurrentIndex(level, inputStack, sourceSlot, access, matches);
        int nextIndex = (currentIndex + 1) % matches.size();
        RecipeHolder<CampfireCookingRecipe> nextRecipe = matches.get(nextIndex);
        int nextCookingTime = Math.max(
                1,
                access.polymorphicDelight$mapRecipeCookingTime(nextRecipe.value().getCookingTime())
        );

        for (int slot : slots) {
            rescaleProgress(access, slot, nextCookingTime);
            access.polymorphicDelight$getSelections().set(slot, nextRecipe.id());
        }

        ItemStack output = nextRecipe.value().assemble(
                new SingleRecipeInput(inputStack),
                level.registryAccess()
        );

        return Optional.of(new SelectionResult(output, nextIndex, matches.size(), slots.size()));
    }

    private static int findCurrentIndex(
            Level level,
            ItemStack inputStack,
            int slot,
            CampfireSelectionAccess access,
            List<RecipeHolder<CampfireCookingRecipe>> matches
    ) {
        ResourceLocation selectedId = access.polymorphicDelight$getSelections().get(slot);
        if (selectedId != null) {
            for (int index = 0; index < matches.size(); index++) {
                if (matches.get(index).id().equals(selectedId)) {
                    return index;
                }
            }
        }

        Optional<RecipeHolder<CampfireCookingRecipe>> vanillaRecipe =
                level.getRecipeManager().getRecipeFor(
                RecipeType.CAMPFIRE_COOKING,
                new SingleRecipeInput(inputStack),
                level
        );

        if (vanillaRecipe.isPresent()) {
            ResourceLocation vanillaId = vanillaRecipe.get().id();
            for (int index = 0; index < matches.size(); index++) {
                if (matches.get(index).id().equals(vanillaId)) {
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
            float ratio = (float) previousProgress / (float) previousCookingTime;
            newProgress = Mth.clamp(Math.round(ratio * newCookingTime), 0, newCookingTime);
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
