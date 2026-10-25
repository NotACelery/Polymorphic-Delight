package dev.celerbi.polymorphicdelight.compat;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

public final class PolymorphCompat {
    private PolymorphCompat() {
    }

    public static void register() {
        PolymorphApi.getInstance().registerMenu(menu ->
            menu instanceof CookingPotMenu pot ? pot.blockEntity : null);
    }
}
