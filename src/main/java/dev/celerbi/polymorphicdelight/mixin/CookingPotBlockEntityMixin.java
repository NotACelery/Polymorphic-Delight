package dev.celerbi.polymorphicdelight.mixin;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

@Mixin(CookingPotBlockEntity.class)
public abstract class CookingPotBlockEntityMixin {
    @Inject(method = "getMatchingRecipe", remap = false, at = @At("HEAD"), cancellable = true)
    private void polymorphicDelight$selectMatchingRecipe(
            RecipeWrapper inventoryWrapper,
            CallbackInfoReturnable<Optional<CookingPotRecipe>> cir
    ) {
        CookingPotBlockEntity cookingPot = (CookingPotBlockEntity) (Object) this;
        Level level = cookingPot.getLevel();
        if (level == null) {
            return;
        }

        IBlockEntityRecipeData data = PolymorphApi.common().getRecipeData(cookingPot).orElse(null);
        if (data == null) {
            return;
        }

        cir.setReturnValue(data.getRecipe(
                ModRecipeTypes.COOKING.get(),
                inventoryWrapper,
                level,
                List.of()
        ));
    }
}
