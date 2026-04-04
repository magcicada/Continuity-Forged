package me.pepperbell.continuity.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.pepperbell.continuity.api.client.ContinuityFeatureStates;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;

@Mixin(PistonHeadRenderer.class)
abstract class PistonBlockEntityRendererMixin {
	@Inject(method = "render(Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;enableCaching()V"))
	private void continuity$beforeRenderModels(CallbackInfo ci) {
		ContinuityFeatureStates states = ContinuityFeatureStates.get();
		states.getConnectedTexturesState().disable();
		states.getEmissiveTexturesState().disable();
	}

	@Inject(method = "render(Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;clearCache()V", shift = At.Shift.AFTER))
	private void continuity$afterRenderModels(CallbackInfo ci) {
		ContinuityFeatureStates states = ContinuityFeatureStates.get();
		states.getConnectedTexturesState().enable();
		states.getEmissiveTexturesState().enable();
	}
}
