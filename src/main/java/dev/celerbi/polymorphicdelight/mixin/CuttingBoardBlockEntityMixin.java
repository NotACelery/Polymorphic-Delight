package dev.celerbi.polymorphicdelight.mixin;

import java.util.Optional;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipeInput;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

@Mixin(CuttingBoardBlockEntity.class)
public abstract class CuttingBoardBlockEntityMixin {
    @Inject(method = "getMatchingRecipe", at = @At("HEAD"), cancellable = true)
    private void polymorphicDelight$select(ItemStack tool, Player player,
        CallbackInfoReturnable<Optional<RecipeHolder<CuttingBoardRecipe>>> cir) {
        CuttingBoardBlockEntity board = (CuttingBoardBlockEntity)(Object)this;
        Level level = board.getLevel();
        if (level == null) return;
        CuttingBoardRecipeInput input = new CuttingBoardRecipeInput(board.getStoredItem(), tool);
        cir.setReturnValue(PolymorphApi.getInstance().getRecipeManager()
            .getBlockEntityRecipe(ModRecipeTypes.CUTTING.get(), input, level, board));
    }
}
