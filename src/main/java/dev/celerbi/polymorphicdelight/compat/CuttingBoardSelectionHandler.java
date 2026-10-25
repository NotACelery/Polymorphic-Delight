package dev.celerbi.polymorphicdelight.compat;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import dev.celerbi.polymorphicdelight.access.CuttingBoardSelectionAccess;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

public final class CuttingBoardSelectionHandler {
    private CuttingBoardSelectionHandler() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND || !player.isSecondaryUseActive()) {
            return;
        }

        Level level = event.getLevel();
        if (!(level.getBlockEntity(event.getPos()) instanceof CuttingBoardBlockEntity board)) {
            return;
        }
        if (board.isEmpty() || board.isItemCarvingBoard()) {
            return;
        }

        ItemStack toolStack = player.getMainHandItem();
        if (toolStack.isEmpty()) {
            return;
        }

        var boardInventory = board.getInventory();
        ItemStackHandler recipeInventory = new ItemStackHandler(boardInventory.getSlots());
        for (int slot = 0; slot < boardInventory.getSlots(); slot++) {
            recipeInventory.setStackInSlot(slot, boardInventory.getStackInSlot(slot).copy());
        }

        RecipeWrapper input = new RecipeWrapper(recipeInventory);
        List<CuttingBoardRecipe> matches = level.getRecipeManager()
                .getRecipesFor(ModRecipeTypes.CUTTING.get(), input, level)
                .stream()
                .filter(recipe -> recipe.getTool().test(toolStack))
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();

        if (matches.size() < 2) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) {
            return;
        }

        CuttingBoardSelectionAccess access = (CuttingBoardSelectionAccess) board;
        ResourceLocation selectedId = access.polymorphicDelight$getSelectedCuttingRecipe();
        int currentIndex = findSelectedIndex(matches, selectedId);
        int nextIndex = (currentIndex + 1) % matches.size();
        CuttingBoardRecipe nextRecipe = matches.get(nextIndex);
        access.polymorphicDelight$setSelectedCuttingRecipe(nextRecipe.getId());

        IBlockEntityRecipeData data = PolymorphApi.common().getRecipeData(board).orElse(null);
        if (data != null) {
            data.getRecipe(ModRecipeTypes.CUTTING.get(), input, level, matches);
            data.selectRecipe(nextRecipe);
        }

        ItemStack preview = nextRecipe.getResultItem(level.registryAccess());
        if (preview.isEmpty()) {
            preview = nextRecipe.assemble(input, level.registryAccess());
        }

        Component resultName = preview.isEmpty()
                ? Component.translatable("message.polymorphic_delight.unknown_output")
                : preview.getHoverName();
        Component message = Component.translatable(
                "message.polymorphic_delight.selection",
                resultName,
                nextIndex + 1,
                matches.size()
        );
        player.displayClientMessage(message.copy().withStyle(ChatFormatting.AQUA), true);
    }

    private static int findSelectedIndex(
            List<CuttingBoardRecipe> matches,
            ResourceLocation selectedId
    ) {
        if (selectedId == null) {
            return 0;
        }

        for (int index = 0; index < matches.size(); index++) {
            if (matches.get(index).getId().equals(selectedId)) {
                return index;
            }
        }
        return 0;
    }
}
