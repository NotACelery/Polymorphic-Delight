package dev.celerbi.polymorphicdelight.mixin;

import com.illusivesoulworks.polymorph.api.inworld.IndicatorSpot;
import com.illusivesoulworks.polymorph.client.inworld.InWorldClient;
import com.illusivesoulworks.polymorph.common.inworld.InWorldState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import vectorwing.farmersdelight.common.registry.ModBlocks;

@Mixin(value = InWorldClient.class, remap = false)
public abstract class PolymorphInWorldIndicatorScaleMixin {

  private static boolean polymorphicDelight$isOurIndicator(BlockState state) {
    return state.getBlock() == ModBlocks.SKILLET.get()
        || state.getBlock() == ModBlocks.CUTTING_BOARD.get()
        || state.getBlock() == ModBlocks.STOVE.get();
  }

  @Inject(
      method = "renderIndicator",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitCustomGeometry(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V",
              ordinal = 1,
              shift = At.Shift.BEFORE),
      remap = false,
      require = 1)
  private static void polymorphicDelight$compactBadgeBefore(
      Minecraft minecraft,
      ClientLevel level,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      CameraRenderState camera,
      BlockPos pos,
      BlockState state,
      IndicatorSpot spot,
      InWorldState indicator,
      boolean hovered,
      CallbackInfo ci) {
    if (polymorphicDelight$isOurIndicator(state)) {
      poseStack.pushPose();
      poseStack.translate(0.015625f, -0.03125f, -0.015625f);
    }
  }

  @Inject(
      method = "renderIndicator",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitCustomGeometry(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V",
              ordinal = 1,
              shift = At.Shift.AFTER),
      remap = false,
      require = 1)
  private static void polymorphicDelight$compactBadgeAfter(
      Minecraft minecraft,
      ClientLevel level,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      CameraRenderState camera,
      BlockPos pos,
      BlockState state,
      IndicatorSpot spot,
      InWorldState indicator,
      boolean hovered,
      CallbackInfo ci) {
    if (polymorphicDelight$isOurIndicator(state)) {
      poseStack.popPose();
    }
  }

  @ModifyArgs(
      method = "renderIndicator",
      at =
          @At(
              value = "INVOKE",
              target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
              ordinal = 1),
      remap = false,
      require = 0)
  private static void polymorphicDelight$alignSkilletMarkerWithRim(
      Args args,
      Minecraft minecraft,
      ClientLevel level,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      CameraRenderState camera,
      BlockPos pos,
      BlockState state,
      IndicatorSpot spot,
      InWorldState indicator,
      boolean hovered) {
    if (state.getBlock() == ModBlocks.SKILLET.get() && spot.frontFace()) {

      args.set(2, 0.440f);
    }
  }

  @ModifyArgs(
      method = "renderIndicator",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"),
      remap = false,
      require = 0)
  private static void polymorphicDelight$scaleOwnMarkers(
      Args args,
      Minecraft minecraft,
      ClientLevel level,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      CameraRenderState camera,
      BlockPos pos,
      BlockState state,
      IndicatorSpot spot,
      InWorldState indicator,
      boolean hovered) {

    float size;
    if (state.getBlock() == ModBlocks.STOVE.get()) {
      size = spot.frontFace() ? 0.205f : 0.145f;
    } else if (state.getBlock() == ModBlocks.CUTTING_BOARD.get()) {
      size = 0.155f;
    } else if (state.getBlock() == ModBlocks.SKILLET.get()) {
      size = 0.19f;
    } else {
      return;
    }
    args.set(0, size);
    args.set(1, size);
    args.set(2, size);
  }

  @Shadow
  private static void renderHint(
      Minecraft minecraft,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      CameraRenderState camera,
      float y) {
    throw new AssertionError("Polymorph+ renderHint shadow was not applied");
  }

  @Redirect(
      method = "renderIndicator",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lcom/illusivesoulworks/polymorph/client/inworld/InWorldClient;renderHint("
                      + "Lnet/minecraft/client/Minecraft;"
                      + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                      + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                      + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;F)V"),
      remap = false,
      require = 1)
  private static void polymorphicDelight$compactHint(
      Minecraft targetMinecraft,
      PoseStack targetPose,
      SubmitNodeCollector targetCollector,
      CameraRenderState targetCamera,
      float originalY,
      Minecraft minecraft,
      ClientLevel level,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      CameraRenderState camera,
      BlockPos pos,
      BlockState state,
      IndicatorSpot spot,
      InWorldState indicator,
      boolean hovered) {
    if (!polymorphicDelight$isOurIndicator(state)) {
      renderHint(targetMinecraft, targetPose, targetCollector, targetCamera, originalY);
      return;
    }

    FormattedCharSequence hint =
        Component.translatable(
                "message.polymorphic_delight.inworld_hint",
                minecraft.options.keyAttack.getTranslatedKeyMessage())
            .getVisualOrderText();
    Font font = minecraft.font;
    poseStack.pushPose();
    poseStack.translate(0.5f, spot.height() + 0.20f, 0.5f);
    poseStack.rotate(camera.orientation);
    poseStack.scale(0.012f, -0.012f, 0.012f);
    collector.submitText(
        poseStack,
        -font.width(hint) / 2.0f,
        0.0f,
        hint,
        false,
        Font.DisplayMode.SEE_THROUGH,
        LightCoordsUtil.FULL_BRIGHT,
        0xFFFFFFFF,
        0x40000000,
        0);
    poseStack.popPose();
  }
}
