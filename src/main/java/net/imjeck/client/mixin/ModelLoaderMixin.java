package net.imjeck.client.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.imjeck.client.mixinterface.ModelLoaderExtension;
import net.imjeck.client.resource.ModelWrappingHandler;
import net.minecraft.client.resources.model.ModelBakery;

@Mixin(ModelBakery.class)
abstract class ModelLoaderMixin implements ModelLoaderExtension {
	@Unique
	@Nullable
	private ModelWrappingHandler continuity_forged$modelWrappingHandler;

	@Override
	@Nullable
	public ModelWrappingHandler continuity_forged$getModelWrappingHandler() {
		return continuity_forged$modelWrappingHandler;
	}

	@Override
	public void continuity_forged$setModelWrappingHandler(@Nullable ModelWrappingHandler handler) {
		this.continuity_forged$modelWrappingHandler = handler;
	}
}
