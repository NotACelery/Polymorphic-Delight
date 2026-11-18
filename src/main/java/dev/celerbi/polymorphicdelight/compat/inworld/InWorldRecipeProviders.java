package dev.celerbi.polymorphicdelight.compat.inworld;

import com.illusivesoulworks.polymorph.api.inworld.PolymorphInWorld;
import vectorwing.farmersdelight.common.registry.ModBlocks;

public final class InWorldRecipeProviders {
  private InWorldRecipeProviders() {}

  public static void register() {
    PolymorphInWorld.register(ModBlocks.CUTTING_BOARD.get(), new CuttingBoardInWorldProvider());
    PolymorphInWorld.register(ModBlocks.SKILLET.get(), new HeatInWorldProvider(false));
    PolymorphInWorld.register(ModBlocks.STOVE.get(), new HeatInWorldProvider(true));
  }
}
