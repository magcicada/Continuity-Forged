package net.imjeck.client.mixinterface;

import org.jetbrains.annotations.Nullable;

import net.imjeck.client.resource.ModelWrappingHandler;

public interface ModelLoaderExtension {
	@Nullable
	ModelWrappingHandler continuity_forged$getModelWrappingHandler();

	void continuity_forged$setModelWrappingHandler(@Nullable ModelWrappingHandler handler);
}
