package dev.celerbi.polymorphicdelight.mixin;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
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
  private void polymorphicDelight$select(
      RecipeWrapper inventory,
      ServerLevel level,
      CallbackInfoReturnable<Optional<RecipeHolder<CookingPotRecipe>>> cir) {
    CookingPotBlockEntity pot = (CookingPotBlockEntity) (Object) this;
    cir.setReturnValue(
        PolymorphApi.getInstance()
            .getRecipeManager()
            .getBlockEntityRecipe(ModRecipeTypes.COOKING.get(), inventory, level, pot));
  }
}
