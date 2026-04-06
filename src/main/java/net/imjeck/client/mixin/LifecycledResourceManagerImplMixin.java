package net.imjeck.client.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.imjeck.client.mixinterface.MultiPackResourceManagerExtension;
import net.imjeck.client.resource.ResourceRedirectHandler;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.ResourceLocation;

@Mixin(MultiPackResourceManager.class)
abstract class LifecycledResourceManagerImplMixin implements MultiPackResourceManagerExtension {
	@Unique
	private ResourceRedirectHandler continuity_forged$redirectHandler;

	@Override
	@Nullable
	public ResourceRedirectHandler continuity_forged$getRedirectHandler() {
		return continuity_forged$redirectHandler;
	}

	@Inject(method = "<init>(Lnet/minecraft/server/packs/PackType;Ljava/util/List;)V", at = @At("TAIL"))
	private void continuity_forged$onTailInit(PackType type, List<PackResources> packs, CallbackInfo ci) {
		if (type == PackType.CLIENT_RESOURCES) {
			continuity_forged$redirectHandler = new ResourceRedirectHandler();
		}
	}

	@ModifyVariable(method = "getResource(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;", at = @At("HEAD"), argsOnly = true)
	private ResourceLocation continuity_forged$redirectGetResourceId(ResourceLocation id) {
		if (continuity_forged$redirectHandler != null) {
			return continuity_forged$redirectHandler.redirect(id);
		}
		return id;
	}

	@ModifyVariable(method = "getResourceStack(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/List;", at = @At("HEAD"), argsOnly = true)
	private ResourceLocation continuity_forged$redirectGetAllResourcesId(ResourceLocation id) {
		if (continuity_forged$redirectHandler != null) {
			return continuity_forged$redirectHandler.redirect(id);
		}
		return id;
	}
}
