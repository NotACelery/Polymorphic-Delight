package dev.celerbi.polymorphicdelight.polymorph;

import com.illusivesoulworks.polymorph.common.components.AbstractBlockEntityRecipeDataComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

public final class CuttingBoardRecipeData extends AbstractBlockEntityRecipeDataComponent<CuttingBoardBlockEntity> {
    public CuttingBoardRecipeData(CuttingBoardBlockEntity owner) {
        super(owner);
    }

    @Override
    protected NonNullList<ItemStack> getInput() {
        ItemStack stack = getOwner().getStoredItem();
        return NonNullList.of(ItemStack.EMPTY, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }
}
