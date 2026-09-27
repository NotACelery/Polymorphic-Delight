package dev.celerbi.polymorphicdelight.mixin;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipeInput;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(CuttingBoardBlockEntity.class)
public abstract class CuttingBoardBlockEntityMixin {
    @Inject(method = "getMatchingRecipe", at = @At("HEAD"), cancellable = true)
    private void polymorphicDelight$selectMatchingRecipe(
            ItemStack toolStack,
            @Nullable Player player,
            CallbackInfoReturnable<Optional<RecipeHolder<CuttingBoardRecipe>>> cir
    ) {
        CuttingBoardBlockEntity cuttingBoard = (CuttingBoardBlockEntity) (Object) this;
        Level level = cuttingBoard.getLevel();
        if (level == null) {
            return;
        }

        PolymorphApi api = PolymorphApi.getInstance();
        CuttingBoardRecipeInput input = new CuttingBoardRecipeInput(
                cuttingBoard.getStoredItem(),
                toolStack
        );

        cir.setReturnValue(
                api.getRecipeManager().getBlockEntityRecipe(
                        ModRecipeTypes.CUTTING.get(),
                        input,
                        level,
                        cuttingBoard
                )
        );
    }
}
