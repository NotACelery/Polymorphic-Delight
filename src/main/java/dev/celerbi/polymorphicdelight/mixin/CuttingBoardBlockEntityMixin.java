package dev.celerbi.polymorphicdelight.mixin;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.inworld.PolymorphInWorld;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
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
  private void polymorphicDelight$select(
      ItemStack tool,
      Player player,
      ServerLevel level,
      CallbackInfoReturnable<Optional<RecipeHolder<CuttingBoardRecipe>>> cir) {
    CuttingBoardBlockEntity board = (CuttingBoardBlockEntity) (Object) this;
    CuttingBoardRecipeInput input = new CuttingBoardRecipeInput(board.getStoredItem(), tool);
    cir.setReturnValue(
        PolymorphApi.getInstance()
            .getRecipeManager()
            .getBlockEntityRecipe(ModRecipeTypes.CUTTING.get(), input, level, board));
  }

  @Inject(method = "addItem", at = @At("RETURN"))
  private void polymorphicDelight$itemInserted(
      ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
    polymorphicDelight$refresh();
  }

  @Inject(method = "removeItem", at = @At("RETURN"))
  private void polymorphicDelight$itemRemoved(CallbackInfoReturnable<ItemStack> cir) {
    polymorphicDelight$refresh();
  }

  @Inject(method = "processStoredItemUsingTool", at = @At("RETURN"))
  private void polymorphicDelight$processed(
      ItemStack tool, Player player, CallbackInfoReturnable<Boolean> cir) {
    polymorphicDelight$refresh();
  }

  private void polymorphicDelight$refresh() {
    CuttingBoardBlockEntity board = (CuttingBoardBlockEntity) (Object) this;
    if (board.getLevel() instanceof ServerLevel server) {
      PolymorphInWorld.refresh(server, board.getBlockPos());
    }
  }
}
