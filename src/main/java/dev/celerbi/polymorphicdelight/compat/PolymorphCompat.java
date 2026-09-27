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
        PolymorphApi api = PolymorphApi.getInstance();

        api.registerBlockEntity(
                CookingPotBlockEntity.class,
                blockEntity -> blockEntity instanceof CookingPotBlockEntity cookingPot
                        ? new CookingPotRecipeData(cookingPot)
                        : null
        );

        api.registerBlockEntity(
                CuttingBoardBlockEntity.class,
                blockEntity -> blockEntity instanceof CuttingBoardBlockEntity cuttingBoard
                        ? new CuttingBoardRecipeData(cuttingBoard)
                        : null
        );

        api.registerMenu(menu -> {
            if (menu instanceof CookingPotMenu cookingPotMenu) {
                return cookingPotMenu.blockEntity;
            }
            return null;
        });
    }
}
