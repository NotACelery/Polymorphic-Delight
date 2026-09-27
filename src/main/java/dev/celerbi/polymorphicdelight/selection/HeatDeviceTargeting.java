package dev.celerbi.polymorphicdelight.selection;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.items.ItemStackHandler;
import vectorwing.farmersdelight.common.block.AbstractStoveBlock;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;

public final class HeatDeviceTargeting {
    private static final double CAMPFIRE_RADIUS_SQUARED = 0.055;
    private static final double STOVE_RADIUS_SQUARED = 0.035;

    private HeatDeviceTargeting() {
    }

    public static int findCampfireSlot(
            CampfireBlockEntity campfire,
            BlockHitResult hitResult
    ) {
        if (hitResult.getDirection() != Direction.UP) {
            return -1;
        }

        BlockState state = campfire.getBlockState();
        Direction facing = state.hasProperty(CampfireBlock.FACING)
                ? state.getValue(CampfireBlock.FACING)
                : Direction.NORTH;
        double localX = hitResult.getLocation().x - campfire.getBlockPos().getX();
        double localZ = hitResult.getLocation().z - campfire.getBlockPos().getZ();
        int bestSlot = -1;
        double bestDistance = Double.MAX_VALUE;

        for (int slot = 0; slot < campfire.getItems().size(); slot++) {
            ItemStack stack = campfire.getItems().get(slot);
            if (stack.isEmpty()) {
                continue;
            }

            Direction direction = Direction.from2DDataValue(
                    Math.floorMod(slot + facing.get2DDataValue(), 4)
            );
            double centerX = 0.5
                    - direction.getStepX() * 0.3125
                    + direction.getClockWise().getStepX() * 0.3125;
            double centerZ = 0.5
                    - direction.getStepZ() * 0.3125
                    + direction.getClockWise().getStepZ() * 0.3125;
            double distance = squaredDistance(localX, localZ, centerX, centerZ);

            if (distance <= CAMPFIRE_RADIUS_SQUARED && distance < bestDistance) {
                bestDistance = distance;
                bestSlot = slot;
            }
        }

        return bestSlot;
    }

    public static int findStoveSlot(
            AbstractStoveBlockEntity stove,
            BlockHitResult hitResult
    ) {
        if (hitResult.getDirection() != Direction.UP) {
            return -1;
        }

        BlockState state = stove.getBlockState();
        Direction facing = state.hasProperty(AbstractStoveBlock.FACING)
                ? state.getValue(AbstractStoveBlock.FACING)
                : Direction.NORTH;
        double localX = hitResult.getLocation().x - stove.getBlockPos().getX();
        double localZ = hitResult.getLocation().z - stove.getBlockPos().getZ();
        ItemStackHandler items = stove.getItems();
        int bestSlot = -1;
        double bestDistance = Double.MAX_VALUE;

        for (int slot = 0; slot < items.getSlots(); slot++) {
            if (items.getStackInSlot(slot).isEmpty()) {
                continue;
            }

            Vec2 offset = stove.getStoveItemOffset(slot);
            if (facing.get2DDataValue() % 2 != 0) {
                offset = new Vec2(offset.y, offset.x);
            }

            double centerX = 0.5
                    - facing.getStepX() * offset.x
                    + facing.getClockWise().getStepX() * offset.x;
            double centerZ = 0.5
                    - facing.getStepZ() * offset.y
                    + facing.getClockWise().getStepZ() * offset.y;
            double distance = squaredDistance(localX, localZ, centerX, centerZ);

            if (distance <= STOVE_RADIUS_SQUARED && distance < bestDistance) {
                bestDistance = distance;
                bestSlot = slot;
            }
        }

        return bestSlot;
    }

    private static double squaredDistance(
            double firstX,
            double firstZ,
            double secondX,
            double secondZ
    ) {
        double deltaX = firstX - secondX;
        double deltaZ = firstZ - secondZ;
        return deltaX * deltaX + deltaZ * deltaZ;
    }
}
