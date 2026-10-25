package dev.celerbi.polymorphicdelight.compat;

import com.illusivesoulworks.polymorph.common.components.PolymorphFabricComponents;
import dev.celerbi.polymorphicdelight.polymorph.CookingPotRecipeData;
import dev.celerbi.polymorphicdelight.polymorph.CuttingBoardRecipeData;
import org.ladysnake.cca.api.v3.block.BlockComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.block.BlockComponentInitializer;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

public final class PolymorphBlockComponents implements BlockComponentInitializer {
    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
        registry.registerFor(CookingPotBlockEntity.class,
            PolymorphFabricComponents.BLOCK_ENTITY_RECIPE_DATA,
            CookingPotRecipeData::new);
        registry.registerFor(CuttingBoardBlockEntity.class,
            PolymorphFabricComponents.BLOCK_ENTITY_RECIPE_DATA,
            CuttingBoardRecipeData::new);
    }
}
