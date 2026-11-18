package dev.celerbi.polymorphicdelight.mixin;

import com.illusivesoulworks.polymorph.api.inworld.PolymorphInWorld;
import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import dev.celerbi.polymorphicdelight.selection.SlotRecipeSelections;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.SkilletBlock;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;

@Mixin(SkilletBlockEntity.class)
public abstract class SkilletBlockEntityMixin implements CampfireSelectionAccess {
  @Shadow private int cookingTime;
  @Shadow private int cookingTimeTotal;
  @Shadow private int fireAspectLevel;

  @Unique
  private final SlotRecipeSelections polymorphicDelight$selections = new SlotRecipeSelections();

  @Unique private ItemStack polymorphicDelight$previousStack;

  @Inject(method = "getMatchingRecipe", at = @At("HEAD"), cancellable = true)
  private void polymorphicDelight$recipe(
      ItemStack stack,
      ServerLevel level,
      CallbackInfoReturnable<Optional<RecipeHolder<CampfireCookingRecipe>>> cir) {
    if (polymorphicDelight$selections.get(0) != null)
      cir.setReturnValue(CampfireRecipeSelector.resolve(level, stack, 0, this));
  }

  @Inject(method = "addItemToCook", at = @At("HEAD"))
  private void polymorphicDelight$add(
      ItemStack stack, Player player, ServerLevel level, CallbackInfoReturnable<ItemStack> cir) {
    if (((SkilletBlockEntity) (Object) this).getStoredStack().isEmpty())
      polymorphicDelight$selections.clear(0);
  }

  @Inject(method = "cookAndOutputItems", at = @At("TAIL"))
  private void polymorphicDelight$finish(ItemStack stack, ServerLevel level, CallbackInfo ci) {
    if (((SkilletBlockEntity) (Object) this).getStoredStack().isEmpty())
      polymorphicDelight$selections.clear(0);
    polymorphicDelight$refresh();
  }

  @Inject(method = "removeItem", at = @At("RETURN"))
  private void polymorphicDelight$remove(CallbackInfoReturnable<ItemStack> cir) {
    if (((SkilletBlockEntity) (Object) this).getStoredStack().isEmpty())
      polymorphicDelight$selections.clear(0);
    polymorphicDelight$refresh();
  }

  @Inject(method = "cookingTick", at = @At("RETURN"))
  private static void polymorphicDelight$checkEmpty(
      ServerLevel level,
      BlockPos pos,
      BlockState state,
      SkilletBlockEntity skillet,
      CallbackInfo ci) {
    if (skillet.getStoredStack().isEmpty()) {
      ((CampfireSelectionAccess) skillet).polymorphicDelight$getSelections().clear(0);
    }
    SkilletBlockEntityMixin mixin = (SkilletBlockEntityMixin) (Object) skillet;
    ItemStack current = skillet.getStoredStack();
    if (mixin.polymorphicDelight$previousStack == null
        || !ItemStack.matches(mixin.polymorphicDelight$previousStack, current)) {
      mixin.polymorphicDelight$previousStack = current.copy();
      PolymorphInWorld.refresh(level, pos);
    }
  }

  @Inject(method = "loadAdditional", at = @At("TAIL"))
  private void polymorphicDelight$load(ValueInput input, CallbackInfo ci) {
    polymorphicDelight$selections.load(input, 1);
    if (((SkilletBlockEntity) (Object) this).getStoredStack().isEmpty())
      polymorphicDelight$selections.clear(0);
  }

  @Inject(method = "saveAdditional", at = @At("TAIL"))
  private void polymorphicDelight$save(ValueOutput output, CallbackInfo ci) {
    polymorphicDelight$selections.save(output);
  }

  @Override
  public SlotRecipeSelections polymorphicDelight$getSelections() {
    return polymorphicDelight$selections;
  }

  @Override
  public int polymorphicDelight$getCookingProgress(int slot) {
    return cookingTime;
  }

  @Override
  public int polymorphicDelight$getCookingTime(int slot) {
    return cookingTimeTotal;
  }

  @Override
  public void polymorphicDelight$setCookingProgress(int slot, int value) {
    cookingTime = value;
  }

  @Override
  public void polymorphicDelight$setCookingTime(int slot, int value) {
    cookingTimeTotal = value;
  }

  @Override
  public int polymorphicDelight$mapRecipeCookingTime(int time) {
    return SkilletBlock.getSkilletCookingTime(time, fireAspectLevel);
  }

  @Inject(method = "addItemToCook", at = @At("RETURN"))
  private void polymorphicDelight$inserted(
      ItemStack stack, Player player, ServerLevel level, CallbackInfoReturnable<ItemStack> cir) {
    polymorphicDelight$refresh();
  }

  @Unique
  private void polymorphicDelight$refresh() {
    SkilletBlockEntity skillet = (SkilletBlockEntity) (Object) this;
    if (skillet.getLevel() instanceof ServerLevel server) {
      PolymorphInWorld.refresh(server, skillet.getBlockPos());
    }
  }
}
