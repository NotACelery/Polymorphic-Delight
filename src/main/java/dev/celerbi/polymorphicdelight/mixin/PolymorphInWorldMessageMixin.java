package dev.celerbi.polymorphicdelight.mixin;

import com.illusivesoulworks.polymorph.api.inworld.Choices;
import com.illusivesoulworks.polymorph.api.inworld.IInWorldProvider;
import com.illusivesoulworks.polymorph.api.inworld.Option;
import com.illusivesoulworks.polymorph.common.inworld.InWorldServer;
import dev.celerbi.polymorphicdelight.selection.CampfireRecipeSelector;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;
import vectorwing.farmersdelight.common.registry.ModBlocks;

@Mixin(value = InWorldServer.class, remap = false)
public abstract class PolymorphInWorldMessageMixin {

  @Redirect(
      method = "cycle",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/server/level/ServerPlayer;sendOverlayMessage(Lnet/minecraft/network/chat/Component;)V"),
      require = 1,
      remap = false)
  private static void polymorphicDelight$redirectSelectionMessage(
      ServerPlayer recipient, Component original, ServerPlayer player, BlockPos pos, int index) {
    recipient.sendOverlayMessage(
        polymorphicDelight$customSelectionMessage(original, player, pos, index));
  }

  private static Component polymorphicDelight$customSelectionMessage(
      Component original, ServerPlayer player, BlockPos pos, int index) {
    ServerLevel level = player.level();
    var block = level.getBlockState(pos).getBlock();
    boolean stove = block == ModBlocks.STOVE.get();
    if (!stove && block != ModBlocks.CUTTING_BOARD.get() && block != ModBlocks.SKILLET.get()) {
      return original;
    }

    IInWorldProvider provider = InWorldServer.provider(block);
    if (provider == null || index < 0 || index >= provider.count(level.getBlockState(pos))) {
      return original;
    }
    Choices choices = provider.choices(level, pos, level.getBlockState(pos), index);
    if (choices == null || choices.options().size() < 2) return original;

    Identifier selected = provider.store().get(level, pos, choices.key());
    if (selected == null) selected = choices.current();
    int selectedIndex = -1;
    for (int i = 0; i < choices.options().size(); i++) {
      if (choices.options().get(i).id().equals(selected)) {
        selectedIndex = i;
        break;
      }
    }
    if (selectedIndex < 0) return original;

    Option option = choices.options().get(selectedIndex);
    if (stove && index >= 6) {

      int affectedSlots = 0;
      var entity = level.getBlockEntity(pos);
      if (entity instanceof AbstractStoveBlockEntity stoveEntity) {
        for (int slot = 0; slot < stoveEntity.getItems().getSlotCount(); slot++) {
          ItemStack item = stoveEntity.getItems().getStackInSlot(slot);
          if (!item.isEmpty() && CampfireRecipeSelector.getMatches(level, item).size() > 1) {
            affectedSlots++;
          }
        }
      }
      return Component.translatable(
              "message.polymorphic_delight.selection_bulk",
              option.icon().getHoverName(),
              selectedIndex + 1,
              choices.options().size(),
              affectedSlots)
          .withStyle(ChatFormatting.AQUA);
    }
    return Component.translatable(
            "message.polymorphic_delight.selection",
            option.icon().getHoverName(),
            selectedIndex + 1,
            choices.options().size())
        .withStyle(ChatFormatting.AQUA);
  }
}
