package dev.celerbi.polymorphicdelight.compat;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.inworld.PolymorphInWorld;
import com.illusivesoulworks.polymorph.common.capability.CampfireRecipeData;
import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import dev.celerbi.polymorphicdelight.mixin.CampfireCookingTimesAccess;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import dev.celerbi.polymorphicdelight.selection.HeatDeviceTargeting;
import dev.celerbi.polymorphicdelight.selection.RecipeMatches;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipeInput;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.common.utility.ItemUtils;
import vectorwing.farmersdelight.refabricated.inventory.ItemStackHandler;

public final class InteractionHandlers {
  private InteractionHandlers() {}

  public static InteractionResult onUseBlock(
      Player player, Level level, InteractionHand hand, BlockHitResult hit) {
    if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown())
      return InteractionResult.PASS;
    BlockEntity entity = level.getBlockEntity(hit.getBlockPos());
    if (entity instanceof CuttingBoardBlockEntity board)
      return selectCuttingBoard(player, level, board);
    if (!ItemUtils.isKnife(player.getMainHandItem())) return InteractionResult.PASS;
    if (entity instanceof SkilletBlockEntity skillet) {
      return selectHeat(player, level, skillet, List.of(0), i -> skillet.getStoredStack(), 0);
    }
    if (entity instanceof CampfireBlockEntity fire) {
      return selectCampfire(player, level, fire, hit);
    }
    if (entity instanceof AbstractStoveBlockEntity stove) {
      ItemStackHandler items = stove.getItems();
      int target = HeatDeviceTargeting.findStoveSlot(stove, hit);
      return selectHeat(
          player,
          level,
          stove,
          slots(items.getSlotCount(), items::getStackInSlot),
          items::getStackInSlot,
          target);
    }
    return InteractionResult.PASS;
  }

  private static InteractionResult selectCuttingBoard(
      Player player, Level level, CuttingBoardBlockEntity board) {
    if (board.isEmpty() || board.isItemCarvingBoard() || player.getMainHandItem().isEmpty())
      return InteractionResult.PASS;
    if (!(level instanceof ServerLevel server)) return InteractionResult.PASS;
    ItemStack tool = player.getMainHandItem();
    CuttingBoardRecipeInput input = new CuttingBoardRecipeInput(board.getStoredItem(), tool);
    List<RecipeHolder<CuttingBoardRecipe>> matches =
        RecipeMatches.find(server, ModRecipeTypes.CUTTING.get(), input);
    if (matches.size() < 2) return InteractionResult.PASS;
    var api = PolymorphApi.getInstance();
    var data = api.getBlockEntityRecipeData(board);
    if (data == null) return InteractionResult.PASS;
    api.getRecipeManager().getBlockEntityRecipe(ModRecipeTypes.CUTTING.get(), input, server, board);
    int current = 0;
    var old = data.getSelectedRecipe();
    if (old != null) {
      for (int i = 0; i < matches.size(); i++) {
        if (matches.get(i).id().equals(old.id())) {
          current = i;
          break;
        }
      }
    }
    int next = (current + 1) % matches.size();
    var recipe = matches.get(next);
    data.selectRecipe(recipe);
    PolymorphInWorld.refresh(server, board.getBlockPos());
    ItemStack output = recipe.value().assemble(input);
    player.sendOverlayMessage(
        Component.translatable(
                "message.polymorphic_delight.selection",
                output.getHoverName(),
                next + 1,
                matches.size())
            .withStyle(ChatFormatting.AQUA));
    return InteractionResult.SUCCESS;
  }

  private static InteractionResult selectCampfire(
      Player player, Level level, CampfireBlockEntity fire, BlockHitResult hit) {
    if (!(level instanceof ServerLevel server)) return InteractionResult.PASS;
    if (!(PolymorphApi.getInstance().getBlockEntityRecipeData(fire)
        instanceof CampfireRecipeData data)) return InteractionResult.PASS;
    int target = HeatDeviceTargeting.findCampfireSlot(fire, hit);
    List<Integer> affected =
        selectAffected(
            player,
            server,
            slots(fire.getItems().size(), i -> fire.getItems().get(i)),
            i -> fire.getItems().get(i),
            target);
    if (affected.isEmpty()) return InteractionResult.PASS;
    ItemStack stack = fire.getItems().get(affected.get(0));
    var matches = CampfireRecipeSelector.getMatches(server, stack);
    if (matches.size() < 2) return InteractionResult.PASS;
    var currentId = data.getSlotPick(affected.get(0));
    int current = 0;
    for (int i = 0; i < matches.size(); i++) {
      if (matches.get(i).id().identifier().equals(currentId)) {
        current = i;
        break;
      }
    }
    int next = (current + 1) % matches.size();
    RecipeHolder<CampfireCookingRecipe> selected = matches.get(next);

    CampfireCookingTimesAccess timers = (CampfireCookingTimesAccess) fire;
    int[] elapsed = timers.polymorphicDelight$getCookingProgress();
    int[] duration = timers.polymorphicDelight$getCookingTime();
    int newDuration = Math.max(1, selected.value().cookingTime());
    for (int slot : affected) {
      elapsed[slot] =
          CampfireRecipeSelector.rescaleProgress(elapsed[slot], duration[slot], newDuration);
      duration[slot] = newDuration;
      data.setSlotPick(slot, selected.id().identifier());
    }
    fire.setChanged();
    server.sendBlockUpdated(
        fire.getBlockPos(), fire.getBlockState(), fire.getBlockState(), Block.UPDATE_CLIENTS);
    PolymorphInWorld.refresh(server, fire.getBlockPos());
    showSelection(
        player,
        selected.value().assemble(new net.minecraft.world.item.crafting.SingleRecipeInput(stack)),
        next,
        matches.size(),
        affected.size());
    return InteractionResult.SUCCESS;
  }

  private static InteractionResult selectHeat(
      Player player,
      Level level,
      BlockEntity entity,
      List<Integer> occupied,
      IntFunction<ItemStack> getter,
      int targetSlot) {
    if (occupied.isEmpty()) return InteractionResult.PASS;
    if (!(level instanceof ServerLevel server)) return InteractionResult.PASS;
    List<Integer> affected = selectAffected(player, server, occupied, getter, targetSlot);
    if (affected.isEmpty()) return InteractionResult.PASS;
    ItemStack stack = getter.apply(affected.get(0));
    if (entity instanceof AbstractStoveBlockEntity stove && stove.getCookingRecipe(stack).isEmpty())
      return InteractionResult.PASS;
    var matches = CampfireRecipeSelector.getMatches(server, stack);
    if (matches.size() < 2) return InteractionResult.PASS;
    if (!(entity instanceof CampfireSelectionAccess access)) return InteractionResult.PASS;
    Optional<CampfireRecipeSelector.SelectionResult> selected =
        CampfireRecipeSelector.cycle(server, stack, affected, access, matches);
    if (selected.isEmpty()) return InteractionResult.PASS;
    entity.setChanged();
    BlockPos pos = entity.getBlockPos();
    server.sendBlockUpdated(
        pos, entity.getBlockState(), entity.getBlockState(), Block.UPDATE_CLIENTS);
    PolymorphInWorld.refresh(server, pos);
    var result = selected.get();
    showSelection(
        player,
        result.output(),
        result.selectedIndex(),
        result.recipeCount(),
        result.affectedSlots());
    return InteractionResult.SUCCESS;
  }

  private static List<Integer> selectAffected(
      Player player,
      ServerLevel level,
      List<Integer> occupied,
      IntFunction<ItemStack> getter,
      int targetSlot) {
    if (occupied.isEmpty()) return List.of();
    if (targetSlot >= 0 && occupied.contains(targetSlot)) return List.of(targetSlot);
    ItemStack first = getter.apply(occupied.get(0));
    if (occupied.stream().allMatch(i -> ItemStack.isSameItemSameComponents(first, getter.apply(i))))
      return occupied;
    boolean hasConflicts =
        occupied.stream()
            .anyMatch(i -> CampfireRecipeSelector.getMatches(level, getter.apply(i)).size() > 1);
    if (hasConflicts)
      player.sendOverlayMessage(
          Component.translatable("message.polymorphic_delight.point_at_food")
              .withStyle(ChatFormatting.GRAY));
    return List.of();
  }

  private static void showSelection(
      Player player, ItemStack output, int next, int count, int affected) {
    Component label = output.isEmpty() ? Component.literal("Recipe") : output.getHoverName();
    Component message =
        affected == 1
            ? Component.translatable(
                "message.polymorphic_delight.selection", label, next + 1, count)
            : Component.translatable(
                "message.polymorphic_delight.selection_bulk", label, next + 1, count, affected);
    player.sendOverlayMessage(message.copy().withStyle(ChatFormatting.AQUA));
  }

  private static List<Integer> slots(int size, IntFunction<ItemStack> getter) {
    List<Integer> occupied = new ArrayList<>();
    for (int i = 0; i < size; i++) if (!getter.apply(i).isEmpty()) occupied.add(i);
    return occupied;
  }
}
