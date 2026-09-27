package dev.celerbi.polymorphicdelight.mixin;

import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import dev.celerbi.polymorphicdelight.selection.SlotRecipeSelections;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
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

import java.util.Optional;

@Mixin(SkilletBlockEntity.class)
public abstract class SkilletBlockEntityMixin implements CampfireSelectionAccess {
    @Shadow
    private int cookingTime;

    @Shadow
    private int cookingTimeTotal;

    @Shadow
    private int fireAspectLevel;

    @Unique
    private final SlotRecipeSelections polymorphicDelight$selections = new SlotRecipeSelections();

    @Inject(method = "getMatchingRecipe", at = @At("HEAD"), cancellable = true)
    private void polymorphicDelight$selectMatchingRecipe(
            ItemStack stack,
            CallbackInfoReturnable<Optional<RecipeHolder<CampfireCookingRecipe>>> cir
    ) {
        SkilletBlockEntity skillet = (SkilletBlockEntity) (Object) this;
        Level level = skillet.getLevel();
        if (level == null || polymorphicDelight$selections.get(0) == null) {
            return;
        }

        cir.setReturnValue(CampfireRecipeSelector.resolve(level, stack, 0, this));
    }

    @Inject(method = "addItemToCook", at = @At("HEAD"))
    private void polymorphicDelight$clearIncomingSelection(
            ItemStack stack,
            Player player,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        SkilletBlockEntity skillet = (SkilletBlockEntity) (Object) this;
        if (skillet.getStoredStack().isEmpty()) {
            polymorphicDelight$selections.clear(0);
        }
    }

    @Inject(method = "cookAndOutputItems", at = @At("TAIL"))
    private void polymorphicDelight$clearFinishedSelection(
            ItemStack stack,
            Level level,
            CallbackInfo ci
    ) {
        SkilletBlockEntity skillet = (SkilletBlockEntity) (Object) this;
        if (skillet.getStoredStack().isEmpty()) {
            polymorphicDelight$selections.clear(0);
        }
    }

    @Inject(method = "removeItem", at = @At("RETURN"))
    private void polymorphicDelight$clearRemovedSelection(
            CallbackInfoReturnable<ItemStack> cir
    ) {
        SkilletBlockEntity skillet = (SkilletBlockEntity) (Object) this;
        if (skillet.getStoredStack().isEmpty()) {
            polymorphicDelight$selections.clear(0);
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void polymorphicDelight$loadSelections(
            CompoundTag tag,
            HolderLookup.Provider registries,
            CallbackInfo ci
    ) {
        polymorphicDelight$selections.load(tag, 1);
        SkilletBlockEntity skillet = (SkilletBlockEntity) (Object) this;
        if (skillet.getStoredStack().isEmpty()) {
            polymorphicDelight$selections.clear(0);
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
    public int polymorphicDelight$mapRecipeCookingTime(int recipeCookingTime) {
        return SkilletBlock.getSkilletCookingTime(recipeCookingTime, fireAspectLevel);
    }
}
