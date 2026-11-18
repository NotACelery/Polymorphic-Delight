package dev.celerbi.polymorphicdelight.mixin;

import com.illusivesoulworks.polymorph.api.inworld.PolymorphInWorld;
import dev.celerbi.polymorphicdelight.access.CampfireSelectionAccess;
import dev.celerbi.polymorphicdelight.access.StoveGlobalChoiceAccess;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import dev.celerbi.polymorphicdelight.selection.SlotRecipeSelections;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;
import vectorwing.farmersdelight.refabricated.inventory.ItemStackHandler;

@Mixin(AbstractStoveBlockEntity.class)
public abstract class AbstractStoveBlockEntityMixin
    implements CampfireSelectionAccess, StoveGlobalChoiceAccess {
  @Shadow @Final private ItemStackHandler items;

  @Shadow @Final private int[] cookingTimesTotal;
  @Shadow @Final private int[] cookingTimes;

  @Unique
  private final SlotRecipeSelections polymorphicDelight$selections = new SlotRecipeSelections();

  @Unique private int polymorphicDelight$cursor;
  @Unique private ItemStack[] polymorphicDelight$previousContents;
  @Unique private Identifier polymorphicDelight$globalChoice;

  @Unique
  private static final String polymorphicDelight$GLOBAL_KEY = "PolymorphicDelightGlobalChoice";

  @Inject(method = "cookAndOutputItems", at = @At("HEAD"))
  private void polymorphicDelight$begin(ServerLevel level, CallbackInfo ci) {
    polymorphicDelight$cursor = 0;
  }

  @Redirect(
      method = "cookAndOutputItems",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/server/level/ServerLevel;)Ljava/util/Optional;"))
  private Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>>
      polymorphicDelight$resolve(
          RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quick,
          RecipeInput raw,
          ServerLevel level) {
    if (!(raw instanceof SingleRecipeInput input)) return Optional.empty();
    int slot = polymorphicDelight$findSlot(input.item());
    if (slot < 0 || polymorphicDelight$selections.get(slot) == null)
      return quick.getRecipeFor(input, level);
    return CampfireRecipeSelector.resolve(level, input.item(), slot, this);
  }

  @Inject(method = "cookAndOutputItems", at = @At("RETURN"))
  private void polymorphicDelight$end(ServerLevel level, CallbackInfo ci) {
    for (int i = 0; i < items.getSlotCount(); i++) {
      if (items.getStackInSlot(i).isEmpty()) polymorphicDelight$selections.clear(i);
    }
  }

  @Inject(method = "placeFood", at = @At("HEAD"))
  private void polymorphicDelight$beforePlaceFood(
      net.minecraft.world.entity.Entity source,
      ItemStack stack,
      RecipeHolder<? extends AbstractCookingRecipe> recipe,
      CallbackInfoReturnable<Boolean> cir) {
    int slot = ((AbstractStoveBlockEntity) (Object) this).getNextEmptySlot();
    if (slot >= 0) polymorphicDelight$selections.clear(slot);
  }

  @Inject(method = "serverTick", at = @At("RETURN"))
  private static void polymorphicDelight$syncEmptySlots(
      ServerLevel level,
      BlockPos pos,
      BlockState state,
      AbstractStoveBlockEntity stove,
      CallbackInfo ci) {
    CampfireSelectionAccess access = (CampfireSelectionAccess) stove;
    for (int slot = 0; slot < stove.getItems().getSlotCount(); slot++) {
      if (stove.getItems().getStackInSlot(slot).isEmpty()) {
        access.polymorphicDelight$getSelections().clear(slot);
      }
    }
    if (((AbstractStoveBlockEntityMixin) (Object) stove)
        .polymorphicDelight$contentsChanged(stove.getItems())) {
      PolymorphInWorld.refresh(level, pos);
    }
  }

  @Unique
  private boolean polymorphicDelight$contentsChanged(ItemStackHandler contents) {
    int count = contents.getSlotCount();
    if (polymorphicDelight$previousContents == null
        || polymorphicDelight$previousContents.length != count) {
      polymorphicDelight$previousContents = new ItemStack[count];
      for (int i = 0; i < count; i++)
        polymorphicDelight$previousContents[i] = contents.getStackInSlot(i).copy();
      return true;
    }
    boolean changed = false;
    for (int i = 0; i < count; i++) {
      ItemStack current = contents.getStackInSlot(i);
      if (!ItemStack.matches(polymorphicDelight$previousContents[i], current)) {
        polymorphicDelight$previousContents[i] = current.copy();
        changed = true;
      }
    }
    return changed;
  }

  @Inject(method = "clearContent", at = @At("TAIL"))
  private void polymorphicDelight$clearContent(CallbackInfo ci) {
    polymorphicDelight$selections.clearAll();
  }

  @Inject(method = "loadAdditional", at = @At("TAIL"))
  private void polymorphicDelight$load(ValueInput input, CallbackInfo ci) {
    polymorphicDelight$selections.load(input, items.getSlotCount());
    polymorphicDelight$globalChoice =
        input.getString(polymorphicDelight$GLOBAL_KEY).map(Identifier::tryParse).orElse(null);
    for (int i = 0; i < items.getSlotCount(); i++) {
      if (items.getStackInSlot(i).isEmpty()) polymorphicDelight$selections.clear(i);
    }
  }

  @Inject(method = "saveAdditional", at = @At("TAIL"))
  private void polymorphicDelight$save(ValueOutput output, CallbackInfo ci) {
    polymorphicDelight$selections.save(output);
    if (polymorphicDelight$globalChoice != null) {
      output.putString(polymorphicDelight$GLOBAL_KEY, polymorphicDelight$globalChoice.toString());
    }
  }

  @Override
  public Identifier polymorphicDelight$getGlobalChoice() {
    return polymorphicDelight$globalChoice;
  }

  @Override
  public void polymorphicDelight$setGlobalChoice(Identifier choice) {
    polymorphicDelight$globalChoice = choice;
  }

  @Unique
  private int polymorphicDelight$findSlot(ItemStack stack) {
    for (int i = polymorphicDelight$cursor; i < items.getSlotCount(); i++) {
      ItemStack item = items.getStackInSlot(i);
      if (item.isEmpty() || !ItemStack.isSameItemSameComponents(item, stack)) continue;
      if (cookingTimesTotal[i] < cookingTimes[i]) continue;
      polymorphicDelight$cursor = i + 1;
      return i;
    }
    return -1;
  }

  @Override
  public SlotRecipeSelections polymorphicDelight$getSelections() {
    return polymorphicDelight$selections;
  }

  @Override
  public int polymorphicDelight$getCookingProgress(int slot) {
    return cookingTimesTotal[slot];
  }

  @Override
  public int polymorphicDelight$getCookingTime(int slot) {
    return cookingTimes[slot];
  }

  @Override
  public void polymorphicDelight$setCookingProgress(int slot, int value) {
    cookingTimesTotal[slot] = value;
  }

  @Override
  public void polymorphicDelight$setCookingTime(int slot, int value) {
    cookingTimes[slot] = value;
  }
}
