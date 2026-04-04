package me.pepperbell.continuity.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.pepperbell.continuity.client.config.ContinuityConfig;
import me.pepperbell.continuity.client.resource.CustomBlockLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;

@Mixin(ItemBlockRenderTypes.class)
abstract class RenderLayersMixin {
	@Inject(method = "getBlockLayer(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/RenderType;", at = @At("HEAD"), cancellable = true)
	private static void continuity$onHeadGetBlockLayer(BlockState state, CallbackInfoReturnable<RenderType> cir) {
		if (!CustomBlockLayers.isEmpty() && ContinuityConfig.INSTANCE.customBlockLayers.get()) {
			RenderType layer = CustomBlockLayers.getLayer(state);
			if (layer != null) {
				cir.setReturnValue(layer);
			}
		}
	}

	@Inject(method = "getMovingBlockLayer(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/RenderType;", at = @At("HEAD"), cancellable = true)
	private static void continuity$onHeadGetMovingBlockLayer(BlockState state, CallbackInfoReturnable<RenderType> cir) {
		if (!CustomBlockLayers.isEmpty() && ContinuityConfig.INSTANCE.customBlockLayers.get()) {
			RenderType layer = CustomBlockLayers.getLayer(state);
			if (layer != null) {
				cir.setReturnValue(layer == RenderType.translucent() ? RenderType.getTranslucentMovingBlock() : layer);
			}
		}
	}
}
