package me.pepperbell.continuity.client.mixin;

import me.pepperbell.continuity.api.client.ContinuityFeatureStates;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockRenderer.class)
abstract class FallingBlockEntityRendererMixin {
  @Inject(
      method =
          "render(Lnet/minecraft/world/entity/item/FallingBlockEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
      at = @At("HEAD"))
  private void continuity$beforeRenderModel(CallbackInfo ci) {
    ContinuityFeatureStates states = ContinuityFeatureStates.get();
    states.getConnectedTexturesState().disable();
    states.getEmissiveTexturesState().disable();
  }

  @Inject(
      method =
          "render(Lnet/minecraft/world/entity/item/FallingBlockEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
      at = @At("RETURN"))
  private void continuity$afterRenderModel(CallbackInfo ci) {
    ContinuityFeatureStates states = ContinuityFeatureStates.get();
    states.getConnectedTexturesState().enable();
    states.getEmissiveTexturesState().enable();
  }
}
