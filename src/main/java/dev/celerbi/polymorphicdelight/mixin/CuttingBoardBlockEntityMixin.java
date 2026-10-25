package dev.celerbi.polymorphicdelight.mixin;

import java.util.Optional;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.refabricated.inventory.RecipeWrapper;

@Mixin(CuttingBoardBlockEntity.class)
public abstract class CuttingBoardBlockEntityMixin {
    @Inject(method = "getMatchingRecipe", at = @At("HEAD"), cancellable = true)
    private void polymorphicDelight$select(RecipeWrapper inventory, ItemStack tool, Player player,
        CallbackInfoReturnable<Optional<CuttingBoardRecipe>> cir) {
        CuttingBoardBlockEntity board = (CuttingBoardBlockEntity)(Object)this;
        Level level = board.getLevel();
        if (level == null) return;
        var data = PolymorphApi.common().getRecipeData(board);
        if (data.isEmpty()) return;
        var matches = level.getRecipeManager().getRecipesFor(ModRecipeTypes.CUTTING.get(), inventory, level)
            .stream().filter(recipe -> recipe.getTool().test(tool)).toList();
        cir.setReturnValue(data.get().getRecipe(ModRecipeTypes.CUTTING.get(), inventory, level, matches));
    }
}
