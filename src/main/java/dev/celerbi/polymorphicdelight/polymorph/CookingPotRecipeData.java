package dev.celerbi.polymorphicdelight.polymorph;

import java.util.TreeSet;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.common.components.AbstractBlockEntityRecipeDataComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.refabricated.inventory.RecipeWrapper;

public final class CookingPotRecipeData extends AbstractBlockEntityRecipeDataComponent<CookingPotBlockEntity> {
    private NonNullList<ItemStack> previousInput = emptyInput();
    private boolean inputInitialized;
    public CookingPotRecipeData(CookingPotBlockEntity owner) {
        super(owner);
    }

    @Override
    protected NonNullList<ItemStack> getInput() {
        NonNullList<ItemStack> result = emptyInput();
        for (int i = 0; i < CookingPotRecipe.INPUT_SLOTS; i++) {
            ItemStack stack = getOwner().getInventory().getStackInSlot(i);
            result.set(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        }
        return result;
    }

    @Override
    public void addListener(ServerPlayer player) {
        super.addListener(player);
        refresh(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (!getListeners().isEmpty()) refresh(false);
    }

    private void refresh(boolean forced) {
        Level level = getOwner().getLevel();
        if (level == null || level.isClientSide()) return;
        NonNullList<ItemStack> input = getInput();
        if (!forced && inputInitialized && sameInput(previousInput, input)) return;
        previousInput = input;
        inputInitialized = true;
        boolean empty = input.stream().allMatch(ItemStack::isEmpty);
        if (empty) {
            setRecipesList(new TreeSet<>());
            sendRecipesListToListeners();
            return;
        }
        PolymorphApi.getInstance().getRecipeManager().getBlockEntityRecipe(
            ModRecipeTypes.COOKING.get(), new RecipeWrapper(getOwner().getInventory()), level, getOwner());
    }

    private static NonNullList<ItemStack> emptyInput() {
        return NonNullList.withSize(CookingPotRecipe.INPUT_SLOTS, ItemStack.EMPTY);
    }

    private static boolean sameInput(NonNullList<ItemStack> first, NonNullList<ItemStack> second) {
        if (first.size() != second.size()) return false;
        for (int i = 0; i < first.size(); i++) {
            ItemStack a = first.get(i), b = second.get(i);
            if (a.isEmpty() != b.isEmpty()) return false;
            if (!a.isEmpty() && !ItemStack.isSameItemSameComponents(a, b)) return false;
        }
        return true;
    }
}
