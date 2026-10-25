package dev.celerbi.polymorphicdelight.selection;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import vectorwing.farmersdelight.common.block.AbstractStoveBlock;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;
import vectorwing.farmersdelight.refabricated.inventory.ItemStackHandler;

public final class HeatDeviceTargeting {
    private HeatDeviceTargeting() {
    }

    public static int findCampfireSlot(CampfireBlockEntity fire, BlockHitResult hit) {
        if (hit.getDirection() != Direction.UP) return -1;
        BlockState state = fire.getBlockState();
        Direction facing = state.hasProperty(CampfireBlock.FACING) ? state.getValue(CampfireBlock.FACING) : Direction.NORTH;
        double x = hit.getLocation().x - fire.getBlockPos().getX();
        double z = hit.getLocation().z - fire.getBlockPos().getZ();
        int closest = -1;
        double minDist = 0.055;
        for (int slot = 0; slot < fire.getItems().size(); slot++) {
            ItemStack stack = fire.getItems().get(slot);
            if (stack.isEmpty()) continue;
            Direction direction = Direction.from2DDataValue(Math.floorMod(slot + facing.get2DDataValue(), 4));
            double cx = 0.5 - direction.getStepX() * 0.3125 + direction.getClockWise().getStepX() * 0.3125;
            double cz = 0.5 - direction.getStepZ() * 0.3125 + direction.getClockWise().getStepZ() * 0.3125;
            double distance = (x-cx)*(x-cx) + (z-cz)*(z-cz);
            if (distance < minDist) {
                minDist = distance;
                closest = slot;
            }
        }
        return closest;
    }

    public static int findStoveSlot(AbstractStoveBlockEntity stove, BlockHitResult hit) {
        if (hit.getDirection() != Direction.UP) return -1;
        BlockState state = stove.getBlockState();
        Direction facing = state.hasProperty(AbstractStoveBlock.FACING) ? state.getValue(AbstractStoveBlock.FACING) : Direction.NORTH;
        double x = hit.getLocation().x - stove.getBlockPos().getX();
        double z = hit.getLocation().z - stove.getBlockPos().getZ();
        ItemStackHandler items = stove.getItems();
        int closest = -1;
        double minDist = 0.035;
        for (int slot = 0; slot < items.getSlotCount(); slot++) {
            if (items.getStackInSlot(slot).isEmpty()) continue;
            Vec2 offset = stove.getStoveItemOffset(slot);
            if (facing.get2DDataValue() % 2 != 0) offset = new Vec2(offset.y, offset.x);
            double cx = 0.5 - facing.getStepX() * offset.x + facing.getClockWise().getStepX() * offset.x;
            double cz = 0.5 - facing.getStepZ() * offset.y + facing.getClockWise().getStepZ() * offset.y;
            double distance = (x-cx)*(x-cx) + (z-cz)*(z-cz);
            if (distance < minDist) {
                minDist = distance;
                closest = slot;
            }
        }
        return closest;
    }
}
