package dev.celerbi.polymorphicdelight.mixin;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.Optional;

@Mixin(CookingPotBlockEntity.class)
public abstract class CookingPotBlockEntityMixin {
    @Inject(method = "getMatchingRecipe", at = @At("HEAD"), cancellable = true)
    private void polymorphicDelight$selectMatchingRecipe(
            RecipeWrapper inventoryWrapper,
            CallbackInfoReturnable<Optional<RecipeHolder<CookingPotRecipe>>> cir
    ) {
        CookingPotBlockEntity cookingPot = (CookingPotBlockEntity) (Object) this;
        Level level = cookingPot.getLevel();
        if (level == null) {
            return;
        }

        PolymorphApi api = PolymorphApi.getInstance();
        cir.setReturnValue(
                api.getRecipeManager().getBlockEntityRecipe(
                        ModRecipeTypes.COOKING.get(),
                        inventoryWrapper,
                        level,
                        cookingPot
                )
        );
    }
}
