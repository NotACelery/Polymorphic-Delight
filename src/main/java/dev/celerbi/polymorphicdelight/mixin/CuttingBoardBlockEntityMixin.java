package dev.celerbi.polymorphicdelight.mixin;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import dev.celerbi.polymorphicdelight.access.CuttingBoardSelectionAccess;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

@Mixin(CuttingBoardBlockEntity.class)
public abstract class CuttingBoardBlockEntityMixin implements CuttingBoardSelectionAccess {
    @Unique
    private static final String POLYMORPHIC_DELIGHT_SELECTED_RECIPE =
            "PolymorphicDelightCuttingRecipe";

    @Unique
    @Nullable
    private ResourceLocation polymorphicDelight$selectedCuttingRecipe;

    @Override
    @Nullable
    public ResourceLocation polymorphicDelight$getSelectedCuttingRecipe() {
        return polymorphicDelight$selectedCuttingRecipe;
    }

    @Override
    public void polymorphicDelight$setSelectedCuttingRecipe(@Nullable ResourceLocation recipeId) {
        polymorphicDelight$selectedCuttingRecipe = recipeId;
        ((CuttingBoardBlockEntity) (Object) this).setChanged();
    }

    @Inject(method = "getMatchingRecipe", remap = false, at = @At("HEAD"), cancellable = true)
    private void polymorphicDelight$selectMatchingRecipe(
            RecipeWrapper inventoryWrapper,
            ItemStack toolStack,
            @Nullable Player player,
            CallbackInfoReturnable<Optional<CuttingBoardRecipe>> cir
    ) {
        CuttingBoardBlockEntity board = (CuttingBoardBlockEntity) (Object) this;
        Level level = board.getLevel();
        if (level == null) {
            return;
        }

        List<CuttingBoardRecipe> matches = level.getRecipeManager()
                .getRecipesFor(ModRecipeTypes.CUTTING.get(), inventoryWrapper, level)
                .stream()
                .filter(recipe -> recipe.getTool().test(toolStack))
                .toList();
        if (matches.isEmpty()) {
            return;
        }

        ResourceLocation selectedId = polymorphicDelight$selectedCuttingRecipe;
        if (selectedId != null) {
            for (CuttingBoardRecipe recipe : matches) {
                if (recipe.getId().equals(selectedId)) {
                    IBlockEntityRecipeData data = PolymorphApi.common()
                            .getRecipeData(board)
                            .orElse(null);
                    if (data != null) {
                        data.selectRecipe(recipe);
                    }
                    cir.setReturnValue(Optional.of(recipe));
                    return;
                }
            }
        }

        IBlockEntityRecipeData data = PolymorphApi.common().getRecipeData(board).orElse(null);
        if (data != null) {
            cir.setReturnValue(data.getRecipe(
                    ModRecipeTypes.CUTTING.get(),
                    inventoryWrapper,
                    level,
                    matches
            ));
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void polymorphicDelight$loadSelection(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(POLYMORPHIC_DELIGHT_SELECTED_RECIPE)) {
            polymorphicDelight$selectedCuttingRecipe = ResourceLocation.tryParse(
                    tag.getString(POLYMORPHIC_DELIGHT_SELECTED_RECIPE)
            );
        } else {
            polymorphicDelight$selectedCuttingRecipe = null;
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void polymorphicDelight$saveSelection(CompoundTag tag, CallbackInfo ci) {
        if (polymorphicDelight$selectedCuttingRecipe != null) {
            tag.putString(
                    POLYMORPHIC_DELIGHT_SELECTED_RECIPE,
                    polymorphicDelight$selectedCuttingRecipe.toString()
            );
        } else {
            tag.remove(POLYMORPHIC_DELIGHT_SELECTED_RECIPE);
        }
    }
}
