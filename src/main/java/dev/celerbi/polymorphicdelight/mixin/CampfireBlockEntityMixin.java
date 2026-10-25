package dev.celerbi.polymorphicdelight.mixin;

import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import dev.celerbi.polymorphicdelight.selection.SlotRecipeSelections;
import java.util.Optional;
import javax.annotation.Nullable;
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
    @Shadow
    @Final
    private NonNullList<ItemStack> items;

    @Shadow
    @Final
    private int[] cookingProgress;

    @Shadow
    @Final
    private int[] cookingTime;

    @Unique
    private final SlotRecipeSelections polymorphicDelight$selections =
            new SlotRecipeSelections();

    @Unique
    private int polymorphicDelight$lookupCursor;

    @Inject(method = "cookTick", at = @At("HEAD"))
    private static void polymorphicDelight$beginCookTick(
            Level level,
            BlockPos pos,
            BlockState state,
            CampfireBlockEntity campfire,
            CallbackInfo ci
    ) {
        ((CampfireBlockEntityMixin) (Object) campfire).polymorphicDelight$lookupCursor = 0;
    }

    @Redirect(
            method = "cookTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;"
                            + "getRecipeFor(Lnet/minecraft/world/Container;"
                            + "Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"
            )
    )
    private static Optional<CampfireCookingRecipe> polymorphicDelight$resolveCookTickRecipe(
            RecipeManager.CachedCheck<Container, CampfireCookingRecipe> quickCheck,
            Container input,
            Level lookupLevel,
            Level level,
            BlockPos pos,
            BlockState state,
            CampfireBlockEntity campfire
    ) {
        CampfireBlockEntityMixin mixin = (CampfireBlockEntityMixin) (Object) campfire;
        ItemStack expected = input.getItem(0);
        int slot = mixin.polymorphicDelight$findCurrentCookingSlot(expected);
        if (slot < 0 || mixin.polymorphicDelight$selections.get(slot) == null) {
            return quickCheck.getRecipeFor(input, lookupLevel);
        }
        return CampfireRecipeSelector.resolve(lookupLevel, expected, slot, mixin);
    }

    @Inject(method = "cookTick", at = @At("RETURN"))
    private static void polymorphicDelight$endCookTick(
            Level level,
            BlockPos pos,
            BlockState state,
            CampfireBlockEntity campfire,
            CallbackInfo ci
    ) {
        ((CampfireBlockEntityMixin) (Object) campfire).clearEmptySelections();
    }

    @Inject(method = "placeFood", at = @At("HEAD"))
    private void polymorphicDelight$clearIncomingSlot(
            @Nullable Entity entity,
            ItemStack stack,
            int cookingTime,
            CallbackInfoReturnable<Boolean> cir
    ) {
        for (int slot = 0; slot < items.size(); slot++) {
            if (items.get(slot).isEmpty()) {
                polymorphicDelight$selections.clear(slot);
                return;
            }
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void polymorphicDelight$loadSelections(CompoundTag tag, CallbackInfo ci) {
        polymorphicDelight$selections.load(tag, items.size());
        clearEmptySelections();
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void polymorphicDelight$saveSelections(CompoundTag tag, CallbackInfo ci) {
        polymorphicDelight$selections.save(tag);
    }

    @Inject(method = "clearContent", at = @At("TAIL"))
    private void polymorphicDelight$clearSelections(CallbackInfo ci) {
        polymorphicDelight$selections.clearAll();
    }

    @Unique
    private void clearEmptySelections() {
        for (int slot = 0; slot < items.size(); slot++) {
            if (items.get(slot).isEmpty()) {
                polymorphicDelight$selections.clear(slot);
            }
        }
    }

    @Unique
    private int polymorphicDelight$findCurrentCookingSlot(ItemStack expected) {
        for (int slot = polymorphicDelight$lookupCursor; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty() || !ItemStack.isSameItemSameTags(stack, expected)) {
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
