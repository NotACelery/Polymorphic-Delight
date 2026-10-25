package dev.celerbi.polymorphicdelight.mixin;

import java.util.Optional;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.refabricated.inventory.RecipeWrapper;

@Mixin(CookingPotBlockEntity.class)
public abstract class CookingPotBlockEntityMixin {
    @Inject(method = "getMatchingRecipe", at = @At("HEAD"), cancellable = true)
    private void polymorphicDelight$select(RecipeWrapper inventory,
        CallbackInfoReturnable<Optional<CookingPotRecipe>> cir) {
        CookingPotBlockEntity pot = (CookingPotBlockEntity)(Object)this;
        Level level = pot.getLevel();
        if (level == null) return;
        var data = PolymorphApi.common().getRecipeData(pot);
        if (data.isEmpty()) return;
        var matches = level.getRecipeManager().getRecipesFor(ModRecipeTypes.COOKING.get(), inventory, level);
        cir.setReturnValue(data.get().getRecipe(ModRecipeTypes.COOKING.get(), inventory, level, matches));
    }
}
