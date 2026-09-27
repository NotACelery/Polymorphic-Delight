package dev.celerbi.polymorphicdelight.client;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;

public final class PolymorphicDelightClient {
    private PolymorphicDelightClient() {
    }

    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PolymorphWidgets.getInstance().registerWidget(screen -> {
            if (screen instanceof CookingPotScreen cookingPotScreen) {
                return new CookingPotRecipesWidget(cookingPotScreen);
            }
            return null;
        }));
    }
}
