package net.imjeck.api.client;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.imjeck.impl.client.CtmLoaderRegistryImpl;

@ApiStatus.NonExtendable
public interface CtmLoaderRegistry {
	static CtmLoaderRegistry get() {
		return CtmLoaderRegistryImpl.INSTANCE;
	}

	void registerLoader(String method, CtmLoader<?> loader);

	@Nullable
	CtmLoader<?> getLoader(String method);
}
