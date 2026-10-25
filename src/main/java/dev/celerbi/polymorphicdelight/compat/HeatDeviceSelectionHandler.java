package dev.celerbi.polymorphicdelight.compat;

import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import dev.celerbi.polymorphicdelight.selection.HeatDeviceTargeting;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.ItemStackHandler;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.utility.ItemUtils;

public final class HeatDeviceSelectionHandler {
    private HeatDeviceSelectionHandler() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND || !player.isSecondaryUseActive()) {
            return;
        }
        if (!ItemUtils.isKnife(player.getMainHandItem())) {
            return;
        }

        Level level = event.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(event.getPos());

        if (blockEntity instanceof SkilletBlockEntity skillet) {
            handleSkillet(event, skillet);
            return;
        }

        if (blockEntity instanceof CampfireBlockEntity campfire) {
            handleCampfire(event, campfire);
            return;
        }

        if (blockEntity instanceof AbstractStoveBlockEntity stove) {
            handleStove(event, stove);
        }
    }

    private static void handleSkillet(
            PlayerInteractEvent.RightClickBlock event,
            SkilletBlockEntity skillet
    ) {
        ItemStack inputStack = skillet.getStoredStack();
        if (inputStack.isEmpty()) {
            return;
        }

        Level level = event.getLevel();
        List<CampfireCookingRecipe> matches =
                CampfireRecipeSelector.getMatches(level, inputStack);
        if (matches.size() < 2) {
            return;
        }

        cancel(event);
        if (level.isClientSide()) {
            return;
        }

        applySelection(
                event,
                skillet,
                inputStack,
                List.of(0),
                (CampfireSelectionAccess) skillet,
                matches
        );
    }

    private static void handleCampfire(
            PlayerInteractEvent.RightClickBlock event,
            CampfireBlockEntity campfire
    ) {
        List<Integer> occupiedSlots = getOccupiedSlots(
                campfire.getItems().size(),
                slot -> campfire.getItems().get(slot)
        );

        if (occupiedSlots.isEmpty()) {
            return;
        }

        int targetSlot = HeatDeviceTargeting.findCampfireSlot(campfire, event.getHitVec());
        handleMultiSlot(
                event,
                campfire,
                occupiedSlots,
                targetSlot,
                slot -> campfire.getItems().get(slot),
                stack -> true,
                (CampfireSelectionAccess) campfire
        );
    }

    private static void handleStove(
            PlayerInteractEvent.RightClickBlock event,
            AbstractStoveBlockEntity stove
    ) {
        ItemStackHandler items = stove.getItems();
        List<Integer> occupiedSlots = getOccupiedSlots(items.getSlots(), items::getStackInSlot);

        if (occupiedSlots.isEmpty()) {
            return;
        }

        int targetSlot = HeatDeviceTargeting.findStoveSlot(stove, event.getHitVec());
        handleMultiSlot(
                event,
                stove,
                occupiedSlots,
                targetSlot,
                items::getStackInSlot,
                stack -> stove.getCookingRecipe(stack)
                        .map(recipe -> recipe instanceof CampfireCookingRecipe)
                        .orElse(false),
                (CampfireSelectionAccess) stove
        );
    }

    private static void handleMultiSlot(
            PlayerInteractEvent.RightClickBlock event,
            BlockEntity owner,
            List<Integer> occupiedSlots,
            int targetSlot,
            IntFunction<ItemStack> stackGetter,
            Predicate<ItemStack> supportedInput,
            CampfireSelectionAccess access
    ) {
        Level level = event.getLevel();

        if (targetSlot >= 0) {
            ItemStack targetStack = stackGetter.apply(targetSlot);
            if (!supportedInput.test(targetStack)) {
                return;
            }

            List<CampfireCookingRecipe> matches =
                    CampfireRecipeSelector.getMatches(level, targetStack);
            if (matches.size() < 2) {
                return;
            }

            cancel(event);
            if (!level.isClientSide()) {
                applySelection(
                        event,
                        owner,
                        targetStack,
                        List.of(targetSlot),
                        access,
                        matches
                );
            }
            return;
        }

        ItemStack firstStack = stackGetter.apply(occupiedSlots.get(0));
        if (allInputsMatch(occupiedSlots, stackGetter, firstStack)) {
            if (!supportedInput.test(firstStack)) {
                return;
            }

            List<CampfireCookingRecipe> matches =
                    CampfireRecipeSelector.getMatches(level, firstStack);
            if (matches.size() < 2) {
                return;
            }

            cancel(event);
            if (!level.isClientSide()) {
                applySelection(event, owner, firstStack, occupiedSlots, access, matches);
            }
            return;
        }

        if (!hasAnyConflict(level, occupiedSlots, stackGetter, supportedInput)) {
            return;
        }

        cancel(event);
        if (!level.isClientSide()) {
            event.getEntity().displayClientMessage(
                    Component.translatable("message.polymorphic_delight.point_at_food")
                            .withStyle(ChatFormatting.GRAY),
                    true
            );
        }
    }

    private static void applySelection(
            PlayerInteractEvent.RightClickBlock event,
            BlockEntity owner,
            ItemStack inputStack,
            List<Integer> slots,
            CampfireSelectionAccess access,
            List<CampfireCookingRecipe> matches
    ) {
        Optional<CampfireRecipeSelector.SelectionResult> selection = CampfireRecipeSelector.cycle(
                event.getLevel(),
                inputStack,
                slots,
                access,
                matches
        );
        if (selection.isEmpty()) {
            return;
        }

        CampfireRecipeSelector.SelectionResult result = selection.get();
        markUpdated(event.getLevel(), event.getPos(), owner);
        displaySelection(event.getEntity(), result);
    }

    private static List<Integer> getOccupiedSlots(
            int slotCount,
            IntFunction<ItemStack> stackGetter
    ) {
        List<Integer> occupiedSlots = new ArrayList<>(slotCount);
        for (int slot = 0; slot < slotCount; slot++) {
            if (!stackGetter.apply(slot).isEmpty()) {
                occupiedSlots.add(slot);
            }
        }
        return occupiedSlots;
    }

    private static boolean allInputsMatch(
            List<Integer> occupiedSlots,
            IntFunction<ItemStack> stackGetter,
            ItemStack expected
    ) {
        for (int slot : occupiedSlots) {
            if (!ItemStack.isSameItemSameTags(expected, stackGetter.apply(slot))) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasAnyConflict(
            Level level,
            List<Integer> occupiedSlots,
            IntFunction<ItemStack> stackGetter,
            Predicate<ItemStack> supportedInput
    ) {
        for (int slot : occupiedSlots) {
            ItemStack stack = stackGetter.apply(slot);
            if (supportedInput.test(stack)
                    && CampfireRecipeSelector.getMatches(level, stack).size() >= 2) {
                return true;
            }
        }
        return false;
    }

    private static void displaySelection(
            Player player,
            CampfireRecipeSelector.SelectionResult result
    ) {
        ItemStack output = result.output();
        Component resultName = output.isEmpty()
                ? Component.translatable("message.polymorphic_delight.unknown_output")
                : output.getHoverName();
        Component message;

        if (result.affectedSlots() > 1) {
            message = Component.translatable(
                    "message.polymorphic_delight.selection_bulk",
                    resultName,
                    result.selectedIndex() + 1,
                    result.recipeCount(),
                    result.affectedSlots()
            );
        } else {
            message = Component.translatable(
                    "message.polymorphic_delight.selection",
                    resultName,
                    result.selectedIndex() + 1,
                    result.recipeCount()
            );
        }

        player.displayClientMessage(message.copy().withStyle(ChatFormatting.AQUA), true);
    }

    private static void markUpdated(Level level, BlockPos pos, BlockEntity blockEntity) {
        blockEntity.setChanged();
        level.sendBlockUpdated(
                pos,
                blockEntity.getBlockState(),
                blockEntity.getBlockState(),
                Block.UPDATE_CLIENTS
        );
    }

    private static void cancel(PlayerInteractEvent.RightClickBlock event) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
