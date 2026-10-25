package dev.celerbi.polymorphicdelight.compat;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import dev.celerbi.polymorphicdelight.polymorph.CookingPotRecipeData;
import dev.celerbi.polymorphicdelight.polymorph.CuttingBoardRecipeData;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

public final class PolymorphCompat {
    private PolymorphCompat() {
    }

    public static void register() {
        PolymorphApi.common().registerBlockEntity2RecipeData(blockEntity ->
                blockEntity instanceof CookingPotBlockEntity cookingPot
                        ? new CookingPotRecipeData(cookingPot) : null);
        PolymorphApi.common().registerBlockEntity2RecipeData(blockEntity ->
                blockEntity instanceof CuttingBoardBlockEntity cuttingBoard
                        ? new CuttingBoardRecipeData(cuttingBoard) : null);
        PolymorphApi.common().registerContainer2BlockEntity(menu ->
                menu instanceof CookingPotMenu cookingPotMenu ? cookingPotMenu.blockEntity : null);
    }
}
