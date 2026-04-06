package net.imjeck.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.imjeck.client.resource.InvalidIdentifierStateHolder;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.resources.ResourceLocation;

@Mixin(FallbackResourceManager.class)
abstract class NamespaceResourceManagerMixin {
	@Inject(method = "getMetadataLocation(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"))
	private static void continuity_forged$onHeadGetMetadataPath(CallbackInfoReturnable<ResourceLocation> cir) {
		InvalidIdentifierStateHolder.get().enable();
	}

	@Inject(method = "getMetadataLocation(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;", at = @At("TAIL"))
	private static void continuity_forged$onTailGetMetadataPath(CallbackInfoReturnable<ResourceLocation> cir) {
		InvalidIdentifierStateHolder.get().disable();
	}
}
