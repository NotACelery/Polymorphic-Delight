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
        ItemStack storedItem = getOwner().getStoredItem();
        return NonNullList.of(
                ItemStack.EMPTY,
                storedItem.isEmpty() ? ItemStack.EMPTY : storedItem.copyWithCount(1)
        );
    }
}
