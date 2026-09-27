package dev.celerbi.polymorphicdelight.compat;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipeInput;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.List;

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

        CuttingBoardRecipeInput input = new CuttingBoardRecipeInput(
                board.getStoredItem(),
                toolStack
        );
        List<RecipeHolder<CuttingBoardRecipe>> matches = level.getRecipeManager()
                .getRecipesFor(ModRecipeTypes.CUTTING.get(), input, level);

        if (matches.size() < 2) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (level.isClientSide()) {
            return;
        }

        PolymorphApi api = PolymorphApi.getInstance();
        IBlockEntityRecipeData recipeData = api.getBlockEntityRecipeData(board);
        if (recipeData == null) {
            return;
        }

        api.getRecipeManager()
                .getBlockEntityRecipe(ModRecipeTypes.CUTTING.get(), input, level, board);

        int currentIndex = findSelectedIndex(matches, recipeData.getSelectedRecipe());
        int nextIndex = (currentIndex + 1) % matches.size();
        RecipeHolder<CuttingBoardRecipe> nextRecipe = matches.get(nextIndex);
        recipeData.selectRecipe(nextRecipe);

        ItemStack preview = nextRecipe.value().assemble(input, level.registryAccess());
        Component resultName = preview.isEmpty()
                ? Component.literal(nextRecipe.id().toString())
                : preview.getHoverName();

        player.displayClientMessage(
                Component.literal("Polymorph: ")
                        .withStyle(ChatFormatting.AQUA)
                        .append(resultName.copy().withStyle(ChatFormatting.WHITE))
                        .append(
                                Component.literal(
                                                " [" + (nextIndex + 1) + "/" + matches.size() + "]"
                                        )
                                        .withStyle(ChatFormatting.GRAY)
                        ),
                true
        );
    }

    private static int findSelectedIndex(
            List<RecipeHolder<CuttingBoardRecipe>> matches,
            RecipeHolder<?> selectedRecipe
    ) {
        if (selectedRecipe == null) {
            return 0;
        }

        for (int index = 0; index < matches.size(); index++) {
            if (matches.get(index).id().equals(selectedRecipe.id())) {
                return index;
            }
        }

        return 0;
    }
}
