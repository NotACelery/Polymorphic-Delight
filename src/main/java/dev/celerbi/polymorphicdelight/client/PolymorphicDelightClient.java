package dev.celerbi.polymorphicdelight.client;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import net.fabricmc.api.ClientModInitializer;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;

public final class PolymorphicDelightClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PolymorphApi.client().registerWidget(screen ->
            screen instanceof CookingPotScreen pot ? new CookingPotRecipesWidget(pot) : null);
    }
}
