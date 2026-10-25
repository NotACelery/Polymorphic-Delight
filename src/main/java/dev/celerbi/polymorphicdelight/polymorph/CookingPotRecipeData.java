package dev.celerbi.polymorphicdelight.polymorph;

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
    private NonNullList<ItemStack> previousInput;
    public CookingPotRecipeData(CookingPotBlockEntity owner) {
        super(owner);
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

    private void refresh(boolean force) {
        Level level = getOwner().getLevel();
        if (level == null || level.isClientSide()) return;
        NonNullList<ItemStack> current = getInput();
        if (!force && previousInput != null && sameInput(previousInput, current)) return;
        previousInput = current;
        RecipeWrapper inventory = new RecipeWrapper(getOwner().getInventory());
        var matches = level.getRecipeManager().getRecipesFor(ModRecipeTypes.COOKING.get(), inventory, level);
        getRecipe(ModRecipeTypes.COOKING.get(), inventory, level, matches);
    }

    private static boolean sameInput(NonNullList<ItemStack> a, NonNullList<ItemStack> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).isEmpty() != b.get(i).isEmpty()) return false;
            if (!a.get(i).isEmpty() && !ItemStack.isSameItemSameTags(a.get(i), b.get(i))) return false;
        }
        return true;
    }

    @Override
    protected NonNullList<ItemStack> getInput() {
        NonNullList<ItemStack> result = NonNullList.withSize(CookingPotRecipe.INPUT_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < CookingPotRecipe.INPUT_SLOTS; i++) {
            ItemStack stack = getOwner().getInventory().getStackInSlot(i);
            result.set(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
        return result;
    }
}
