package dev.celerbi.polymorphicdelight.mixin;

import java.util.Optional;

import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import dev.celerbi.polymorphicdelight.selection.SlotRecipeSelections;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;
import vectorwing.farmersdelight.refabricated.inventory.ItemStackHandler;

@Mixin(AbstractStoveBlockEntity.class)
public abstract class AbstractStoveBlockEntityMixin implements CampfireSelectionAccess {
    @Shadow @Final private ItemStackHandler items;
    @Shadow @Final private int[] cookingProgress;
    @Shadow @Final private int[] cookingTime;
    @Unique private final SlotRecipeSelections polymorphicDelight$selections = new SlotRecipeSelections();
    @Unique private int polymorphicDelight$cursor;
    @Inject(method = "cookAndOutputItems", at = @At("HEAD"))
    private void polymorphicDelight$begin(CallbackInfo ci) {
        polymorphicDelight$cursor = 0;
    }
    @Redirect(method = "cookAndOutputItems", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"))
    private Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> polymorphicDelight$resolve(
        RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quick,
        RecipeInput raw, Level level) {
        if (!(raw instanceof SingleRecipeInput input)) return Optional.empty();
        int slot = polymorphicDelight$findSlot(input.item());
        if (slot < 0 || polymorphicDelight$selections.get(slot) == null)
        return quick.getRecipeFor(input, level);
        return CampfireRecipeSelector.resolve(level, input.item(), slot, this);
    }

    @Inject(method = "cookAndOutputItems", at = @At("RETURN"))
    private void polymorphicDelight$end(CallbackInfo ci) {
        for (int i = 0; i < items.getSlotCount(); i++)
        if (items.getStackInSlot(i).isEmpty()) polymorphicDelight$selections.clear(i);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void polymorphicDelight$load(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        polymorphicDelight$selections.load(tag, items.getSlotCount());
        for (int i = 0; i < items.getSlotCount(); i++)
        if (items.getStackInSlot(i).isEmpty()) polymorphicDelight$selections.clear(i);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void polymorphicDelight$save(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        polymorphicDelight$selections.save(tag);
    }

    @Unique private int polymorphicDelight$findSlot(ItemStack stack) {
        for (int i = polymorphicDelight$cursor; i < items.getSlotCount(); i++) {
            ItemStack item = items.getStackInSlot(i);
            if (item.isEmpty() || !ItemStack.isSameItemSameComponents(item, stack)) continue;
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
