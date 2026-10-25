package dev.celerbi.polymorphicdelight.access;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public interface CuttingBoardSelectionAccess {
    @Nullable
    ResourceLocation polymorphicDelight$getSelectedCuttingRecipe();

    void polymorphicDelight$setSelectedCuttingRecipe(@Nullable ResourceLocation recipeId);
}
