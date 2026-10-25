package dev.celerbi.polymorphicdelight.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import dev.celerbi.polymorphicdelight.selection.HeatDeviceTargeting;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.common.utility.ItemUtils;
import vectorwing.farmersdelight.refabricated.inventory.ItemStackHandler;
import vectorwing.farmersdelight.refabricated.inventory.RecipeWrapper;

public final class InteractionHandlers {
    private InteractionHandlers() {
    }

    public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) return InteractionResult.PASS;
        BlockEntity entity = level.getBlockEntity(hit.getBlockPos());
        if (entity instanceof CuttingBoardBlockEntity board) return selectCuttingBoard(player, level, board);
        if (!ItemUtils.isKnife(player.getMainHandItem())) return InteractionResult.PASS;
        if (entity instanceof SkilletBlockEntity skillet) return selectHeat(player, level, hit, skillet, List.of(0), i -> skillet.getStoredStack());
        if (entity instanceof CampfireBlockEntity fire) {
            int target = HeatDeviceTargeting.findCampfireSlot(fire, hit);
            return selectHeat(player, level, hit, fire, slots(fire.getItems().size(), i -> fire.getItems().get(i)),
                i -> fire.getItems().get(i), target);
        }
        if (entity instanceof AbstractStoveBlockEntity stove) {
            ItemStackHandler items = stove.getItems();
            int target = HeatDeviceTargeting.findStoveSlot(stove, hit);
            return selectHeat(player, level, hit, stove, slots(items.getSlotCount(), items::getStackInSlot),
                items::getStackInSlot, target);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult selectCuttingBoard(Player player, Level level, CuttingBoardBlockEntity board) {
        if (board.isEmpty() || board.isItemCarvingBoard() || player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        ItemStack tool = player.getMainHandItem();
        RecipeWrapper input = new RecipeWrapper(board.getInventory());
        var matches = level.getRecipeManager().getRecipesFor(ModRecipeTypes.CUTTING.get(), input, level)
            .stream().filter(recipe -> recipe.getTool().test(tool)).toList();
        if (matches.size() < 2) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        var data = PolymorphApi.common().getRecipeData(board);
        if (data.isEmpty()) return InteractionResult.PASS;
        int current = 0;
        var old = data.get().getSelectedRecipe();
        if (old.isPresent()) for (int i = 0; i < matches.size(); i++)
        if (matches.get(i).getId().equals(old.get().getId())) {
            current = i;
            break;
        }
        int next = (current + 1) % matches.size();
        var recipe = matches.get(next);
        data.get().selectRecipe(recipe);
        player.displayClientMessage(Component.translatable("message.polymorphic_delight.selection",
            recipe.assemble(input, level.registryAccess()).getHoverName(), next + 1, matches.size())
            .withStyle(ChatFormatting.AQUA), true);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult selectHeat(Player player, Level level, BlockHitResult hit,
        BlockEntity entity, List<Integer> slots, IntFunction<ItemStack> getter) {
        return selectHeat(player, level, hit, entity, slots, getter, slots.isEmpty() ? -1 : slots.get(0));
    }

    private static InteractionResult selectHeat(Player player, Level level, BlockHitResult hit,
        BlockEntity entity, List<Integer> occupied, IntFunction<ItemStack> getter, int targetSlot) {
        if (occupied.isEmpty()) return InteractionResult.PASS;
        CampfireSelectionAccess access = (CampfireSelectionAccess) entity;
        List<Integer> affected;
        if (targetSlot >= 0 && occupied.contains(targetSlot)) {
            affected = List.of(targetSlot);
        }
        else {
            ItemStack first = getter.apply(occupied.get(0));
            boolean same = occupied.stream().allMatch(i -> ItemStack.isSameItemSameTags(first, getter.apply(i)));
            if (!same) {
                boolean hasConflicts = occupied.stream().anyMatch(i -> CampfireRecipeSelector.getMatches(level, getter.apply(i)).size() > 1);
                if (!hasConflicts) return InteractionResult.PASS;
                if (!level.isClientSide()) player.displayClientMessage(
                    Component.translatable("message.polymorphic_delight.point_at_food").withStyle(ChatFormatting.GRAY), true);
                return InteractionResult.SUCCESS;
            }
            affected = occupied;
        }
        ItemStack stack = getter.apply(affected.get(0));
        if (entity instanceof AbstractStoveBlockEntity stove &&
            stove.getCookingRecipe(stack).isEmpty()) return InteractionResult.PASS;
        var matches = CampfireRecipeSelector.getMatches(level, stack);
        if (matches.size() < 2) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        Optional<CampfireRecipeSelector.SelectionResult> selected = CampfireRecipeSelector.cycle(
            level, stack, affected, access, matches);
        if (selected.isEmpty()) return InteractionResult.PASS;
        entity.setChanged();
        BlockPos pos = entity.getBlockPos();
        level.sendBlockUpdated(pos, entity.getBlockState(), entity.getBlockState(), Block.UPDATE_CLIENTS);
        CampfireRecipeSelector.SelectionResult result = selected.get();
        Component label = result.output().isEmpty() ? Component.literal("Recipe") : result.output().getHoverName();
        Component message = result.affectedSlots() == 1
        ? Component.translatable("message.polymorphic_delight.selection", label,
            result.selectedIndex() + 1, result.recipeCount())
        : Component.translatable("message.polymorphic_delight.selection_bulk", label,
            result.selectedIndex() + 1, result.recipeCount(), result.affectedSlots());
        player.displayClientMessage(message.copy().withStyle(ChatFormatting.AQUA), true);
        return InteractionResult.SUCCESS;
    }

    private static List<Integer> slots(int size, IntFunction<ItemStack> getter) {
        List<Integer> occupied = new ArrayList<>();
        for (int i = 0; i < size; i++) if (!getter.apply(i).isEmpty()) occupied.add(i);
        return occupied;
    }
}
