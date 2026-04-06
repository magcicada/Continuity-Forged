package net.imjeck.client.mixinterface;

import org.jetbrains.annotations.Nullable;

import net.imjeck.client.resource.ResourceRedirectHandler;

public interface MultiPackResourceManagerExtension {
	@Nullable
	ResourceRedirectHandler continuity_forged$getRedirectHandler();
}
