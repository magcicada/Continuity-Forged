package me.pepperbell.continuity.client.mixin;

import me.pepperbell.continuity.client.resource.InvalidIdentifierStateHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallbackResourceManager.class)
abstract class NamespaceResourceManagerMixin {
  @Inject(
      method =
          "getMetadataLocation(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;",
      at = @At("HEAD"))
  private static void continuity$onHeadGetMetadataPath(
      CallbackInfoReturnable<ResourceLocation> cir) {
    InvalidIdentifierStateHolder.get().enable();
  }

  @Inject(
      method =
          "getMetadataLocation(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceLocation;",
      at = @At("TAIL"))
  private static void continuity$onTailGetMetadataPath(
      CallbackInfoReturnable<ResourceLocation> cir) {
    InvalidIdentifierStateHolder.get().disable();
  }
}
