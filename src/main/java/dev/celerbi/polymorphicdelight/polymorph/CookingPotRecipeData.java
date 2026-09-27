package dev.celerbi.polymorphicdelight.polymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import javax.annotation.Nonnull;
import java.util.TreeSet;

public final class CookingPotRecipeData
        extends AbstractBlockEntityRecipeData<CookingPotBlockEntity> {
    private NonNullList<ItemStack> previousInput = emptyInput();
    private boolean inputSnapshotInitialized;

    public CookingPotRecipeData(CookingPotBlockEntity owner) {
        super(owner);
    }

    @Override
    protected NonNullList<ItemStack> getInput() {
        ItemStackHandler inventory = getOwner().getInventory();
        NonNullList<ItemStack> input = emptyInput();

        for (int slot = 0; slot < CookingPotRecipe.INPUT_SLOTS; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            input.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        }

        return input;
    }

    @Override
    public void addListener(@Nonnull ServerPlayer player) {
        super.addListener(player);
        refreshRecipes(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (getListeners().isEmpty()) {
            return;
        }

        refreshRecipes(false);
    }

    private void refreshRecipes(boolean force) {
        CookingPotBlockEntity cookingPot = getOwner();
        Level level = cookingPot.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        NonNullList<ItemStack> currentInput = getInput();
        if (!force
                && inputSnapshotInitialized
                && sameRecipeInput(previousInput, currentInput)) {
            return;
        }

        previousInput = currentInput;
        inputSnapshotInitialized = true;

        if (isInputEmpty(currentInput)) {
            updateRecipesList(new TreeSet<>());
            return;
        }

        PolymorphApi.getInstance()
                .getRecipeManager()
                .getBlockEntityRecipe(
                        ModRecipeTypes.COOKING.get(),
                        new RecipeWrapper(cookingPot.getInventory()),
                        level,
                        cookingPot
                );
    }

    private static NonNullList<ItemStack> emptyInput() {
        return NonNullList.withSize(CookingPotRecipe.INPUT_SLOTS, ItemStack.EMPTY);
    }

    private static boolean isInputEmpty(NonNullList<ItemStack> input) {
        for (ItemStack stack : input) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameRecipeInput(
            NonNullList<ItemStack> first,
            NonNullList<ItemStack> second
    ) {
        if (first.size() != second.size()) {
            return false;
        }

        for (int slot = 0; slot < first.size(); slot++) {
            ItemStack firstStack = first.get(slot);
            ItemStack secondStack = second.get(slot);

            if (firstStack.isEmpty() != secondStack.isEmpty()) {
                return false;
            }
            if (!firstStack.isEmpty()
                    && !ItemStack.isSameItemSameComponents(firstStack, secondStack)) {
                return false;
            }
        }

        return true;
    }
}
