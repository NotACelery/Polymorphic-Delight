package dev.celerbi.polymorphicdelight.mixin;

import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import dev.celerbi.polymorphicdelight.selection.SlotRecipeSelections;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(AbstractStoveBlockEntity.class)
public abstract class AbstractStoveBlockEntityMixin implements CampfireSelectionAccess {
    @Shadow
    @Final
    private ItemStackHandler items;

    @Shadow
    @Final
    private int[] cookingProgress;

    @Shadow
    @Final
    private int[] cookingTime;

    @Unique
    private final SlotRecipeSelections polymorphicDelight$selections = new SlotRecipeSelections();

    @Unique
    private int polymorphicDelight$lookupCursor;

    @Inject(method = "cookAndOutputItems", at = @At("HEAD"))
    private void polymorphicDelight$beginCookingPass(CallbackInfo ci) {
        polymorphicDelight$lookupCursor = 0;
    }

    @Redirect(
            method = "cookAndOutputItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;"
                            + "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeInput;"
                            + "Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"
            )
    )
    private Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>>
            polymorphicDelight$resolveCookingRecipe(
                    RecipeManager.CachedCheck<
                            SingleRecipeInput,
                            ? extends AbstractCookingRecipe
                    > quickCheck,
                    RecipeInput rawInput,
                    Level level
            ) {
        if (!(rawInput instanceof SingleRecipeInput input)) {
            return Optional.empty();
        }

        int slot = polymorphicDelight$findCurrentCookingSlot(input.item());
        if (slot < 0 || polymorphicDelight$selections.get(slot) == null) {
            return quickCheck.getRecipeFor(input, level);
        }

        return CampfireRecipeSelector.resolve(level, input.item(), slot, this);
    }

    @Inject(method = "cookAndOutputItems", at = @At("RETURN"))
    private void polymorphicDelight$endCookingPass(CallbackInfo ci) {
        for (int slot = 0; slot < items.getSlots(); slot++) {
            if (items.getStackInSlot(slot).isEmpty()) {
                polymorphicDelight$selections.clear(slot);
            }
        }
    }

    @Inject(method = "placeFood", at = @At("HEAD"))
    private void polymorphicDelight$clearIncomingSlot(
            @Nullable Entity entity,
            ItemStack stack,
            RecipeHolder<? extends AbstractCookingRecipe> recipe,
            CallbackInfoReturnable<Boolean> cir
    ) {
        for (int slot = 0; slot < items.getSlots(); slot++) {
            if (items.getStackInSlot(slot).isEmpty()) {
                polymorphicDelight$selections.clear(slot);
                return;
            }
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void polymorphicDelight$loadSelections(
            CompoundTag tag,
            HolderLookup.Provider registries,
            CallbackInfo ci
    ) {
        polymorphicDelight$selections.load(tag, items.getSlots());
        for (int slot = 0; slot < items.getSlots(); slot++) {
            if (items.getStackInSlot(slot).isEmpty()) {
                polymorphicDelight$selections.clear(slot);
            }
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void polymorphicDelight$saveSelections(
            CompoundTag tag,
            HolderLookup.Provider registries,
            CallbackInfo ci
    ) {
        polymorphicDelight$selections.save(tag);
    }

    @Inject(method = "clearContent", at = @At("TAIL"))
    private void polymorphicDelight$clearSelections(CallbackInfo ci) {
        polymorphicDelight$selections.clearAll();
    }

    @Inject(method = "dropAllItems", at = @At("TAIL"))
    private void polymorphicDelight$clearDroppedSelections(CallbackInfo ci) {
        polymorphicDelight$selections.clearAll();
    }

    @Unique
    private int polymorphicDelight$findCurrentCookingSlot(ItemStack expected) {
        for (int slot = polymorphicDelight$lookupCursor; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, expected)) {
                continue;
            }
            if (cookingProgress[slot] < cookingTime[slot]) {
                continue;
            }

            polymorphicDelight$lookupCursor = slot + 1;
            return slot;
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
