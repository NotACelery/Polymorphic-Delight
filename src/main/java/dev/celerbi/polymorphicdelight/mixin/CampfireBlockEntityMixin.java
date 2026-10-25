package dev.celerbi.polymorphicdelight.mixin;

import java.util.Optional;

import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import dev.celerbi.polymorphicdelight.selection.SlotRecipeSelections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin implements CampfireSelectionAccess {
    @Shadow @Final private NonNullList<ItemStack> items;
    @Shadow @Final private int[] cookingProgress;
    @Shadow @Final private int[] cookingTime;
    @Unique private final SlotRecipeSelections polymorphicDelight$selections = new SlotRecipeSelections();
    @Unique private int polymorphicDelight$cursor;
    @Inject(method = "cookTick", at = @At("HEAD"))
    private static void polymorphicDelight$begin(Level level, BlockPos pos, BlockState state,
        CampfireBlockEntity fire, CallbackInfo ci) {
        ((CampfireBlockEntityMixin)(Object)fire).polymorphicDelight$cursor = 0;
    }
    @Redirect(method = "cookTick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;getRecipeFor(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"))
    private static Optional<CampfireCookingRecipe> polymorphicDelight$resolve(
        RecipeManager.CachedCheck<Container, CampfireCookingRecipe> quick,
        Container input, Level lookupLevel, Level level, BlockPos pos, BlockState state, CampfireBlockEntity fire) {
        CampfireBlockEntityMixin mixin = (CampfireBlockEntityMixin)(Object)fire;
        ItemStack stack = input.getItem(0);
        int slot = mixin.polymorphicDelight$findSlot(stack);
        if (slot < 0 || mixin.polymorphicDelight$selections.get(slot) == null)
        return quick.getRecipeFor(input, lookupLevel);
        return CampfireRecipeSelector.resolve(lookupLevel, stack, slot, mixin);
    }

    @Inject(method = "cookTick", at = @At("RETURN"))
    private static void polymorphicDelight$end(Level level, BlockPos pos, BlockState state,
        CampfireBlockEntity fire, CallbackInfo ci) {
        CampfireBlockEntityMixin mixin = (CampfireBlockEntityMixin)(Object)fire;
        for (int i = 0; i < mixin.items.size(); i++)
        if (mixin.items.get(i).isEmpty()) mixin.polymorphicDelight$selections.clear(i);
    }

    @Inject(method = "placeFood", at = @At("HEAD"))
    private void polymorphicDelight$place(Entity entity, ItemStack stack, int time,
        CallbackInfoReturnable<Boolean> cir) {
        for (int i = 0; i < items.size(); i++)
        if (items.get(i).isEmpty()) {
            polymorphicDelight$selections.clear(i);
            return;
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void polymorphicDelight$load(CompoundTag tag, CallbackInfo ci) {
        polymorphicDelight$selections.load(tag, items.size());
        for (int i = 0; i < items.size(); i++)
        if (items.get(i).isEmpty()) polymorphicDelight$selections.clear(i);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void polymorphicDelight$save(CompoundTag tag, CallbackInfo ci) {
        polymorphicDelight$selections.save(tag);
    }

    @Unique private int polymorphicDelight$findSlot(ItemStack expected) {
        for (int i = polymorphicDelight$cursor; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (item.isEmpty() || !ItemStack.isSameItemSameTags(item, expected)) continue;
            if (cookingProgress[i] < cookingTime[i]) continue;
            polymorphicDelight$cursor = i+1;
            return i;
        }
        return -1;
    }

    @Override
    public SlotRecipeSelections polymorphicDelight$getSelections() {
        return polymorphicDelight$selections;
    }

    @Override
    public int polymorphicDelight$getCookingProgress(int slot) {
        return cookingProgress[slot];
    }

    @Override
    public int polymorphicDelight$getCookingTime(int slot) {
        return cookingTime[slot];
    }

    @Override
    public void polymorphicDelight$setCookingProgress(int slot, int value) {
        cookingProgress[slot] = value;
    }

    @Override
    public void polymorphicDelight$setCookingTime(int slot, int value) {
        cookingTime[slot] = value;
    }
}
