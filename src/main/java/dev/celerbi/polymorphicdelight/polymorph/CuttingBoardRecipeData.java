package dev.celerbi.polymorphicdelight.polymorph;

import com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

public final class CuttingBoardRecipeData
        extends AbstractBlockEntityRecipeData<CuttingBoardBlockEntity> {
    public CuttingBoardRecipeData(CuttingBoardBlockEntity owner) {
        super(owner);
    }

    @Override
    protected NonNullList<ItemStack> getInput() {
        ItemStack stored = getOwner().getStoredItem();
        if (stored.isEmpty()) {
            return NonNullList.of(ItemStack.EMPTY, ItemStack.EMPTY);
        }

        ItemStack copy = stored.copy();
        copy.setCount(1);
        return NonNullList.of(ItemStack.EMPTY, copy);
    }
}
