package dev.celerbi.polymorphicdelight.polymorph;

import com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData;
import java.util.List;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

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
            if (!stack.isEmpty()) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                input.set(slot, copy);
            }
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
        if (!getListeners().isEmpty()) {
            refreshRecipes(false);
        }
    }

    private void refreshRecipes(boolean force) {
        CookingPotBlockEntity cookingPot = getOwner();
        Level level = cookingPot.getLevel();
        if (level == null || level.isClientSide) {
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
            setRecipesList(new TreeSet<>());
            sendRecipesListToListeners(true);
            return;
        }

        getRecipe(
                ModRecipeTypes.COOKING.get(),
                new RecipeWrapper(cookingPot.getInventory()),
                level,
                List.of()
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
                    && !ItemStack.isSameItemSameTags(firstStack, secondStack)) {
                return false;
            }
        }
        return true;
    }
}
