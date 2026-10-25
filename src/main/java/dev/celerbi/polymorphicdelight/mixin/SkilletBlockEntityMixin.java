package dev.celerbi.polymorphicdelight.mixin;

import java.util.Optional;

import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import dev.celerbi.polymorphicdelight.selection.SlotRecipeSelections;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.level.Level;
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
    @Unique private final SlotRecipeSelections polymorphicDelight$selections = new SlotRecipeSelections();
    @Inject(method = "getMatchingRecipe", at = @At("HEAD"), cancellable = true)
    private void polymorphicDelight$recipe(Container input,
        CallbackInfoReturnable<Optional<CampfireCookingRecipe>> cir) {
        SkilletBlockEntity skillet = (SkilletBlockEntity)(Object)this;
        Level level = skillet.getLevel();
        if (level == null || polymorphicDelight$selections.get(0) == null) return;
        cir.setReturnValue(CampfireRecipeSelector.resolve(level, input.getItem(0), 0, this));
    }

    @Inject(method = "addItemToCook", at = @At("HEAD"))
    private void polymorphicDelight$add(ItemStack stack, Player player, CallbackInfoReturnable<ItemStack> cir) {
        if (((SkilletBlockEntity)(Object)this).getStoredStack().isEmpty()) polymorphicDelight$selections.clear(0);
    }

    @Inject(method = "cookAndOutputItems", at = @At("TAIL"))
    private void polymorphicDelight$finish(ItemStack stack, Level level, CallbackInfo ci) {
        if (((SkilletBlockEntity)(Object)this).getStoredStack().isEmpty()) polymorphicDelight$selections.clear(0);
    }

    @Inject(method = "removeItem", at = @At("RETURN"))
    private void polymorphicDelight$remove(CallbackInfoReturnable<ItemStack> cir) {
        if (((SkilletBlockEntity)(Object)this).getStoredStack().isEmpty()) polymorphicDelight$selections.clear(0);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void polymorphicDelight$load(CompoundTag tag, CallbackInfo ci) {
        polymorphicDelight$selections.load(tag, 1);
        if (((SkilletBlockEntity)(Object)this).getStoredStack().isEmpty()) polymorphicDelight$selections.clear(0);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void polymorphicDelight$save(CompoundTag tag, CallbackInfo ci) {
        polymorphicDelight$selections.save(tag);
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
}
