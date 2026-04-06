package net.imjeck.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.imjeck.client.config.ContinuityConfig;
import net.imjeck.client.resource.CustomBlockLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;

@Mixin(ItemBlockRenderTypes.class)
abstract class RenderLayersMixin {
	@Inject(method = "getChunkRenderType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), cancellable = true)
	private static void continuity_forged$onHeadGetBlockLayer(BlockState state, CallbackInfoReturnable<RenderType> cir) {
		if (!CustomBlockLayers.isEmpty() && ContinuityConfig.INSTANCE.customBlockLayers.get()) {
			RenderType layer = CustomBlockLayers.getLayer(state);
			if (layer != null) {
				cir.setReturnValue(layer);
			}
		}
	}

	@Inject(method = "getMovingBlockRenderType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), cancellable = true)
	private static void continuity_forged$onHeadGetMovingBlockLayer(BlockState state, CallbackInfoReturnable<RenderType> cir) {
		if (!CustomBlockLayers.isEmpty() && ContinuityConfig.INSTANCE.customBlockLayers.get()) {
			RenderType layer = CustomBlockLayers.getLayer(state);
			if (layer != null) {
				cir.setReturnValue(layer == RenderType.translucent() ? RenderType.translucentMovingBlock() : layer);
			}
		}
	}
}
