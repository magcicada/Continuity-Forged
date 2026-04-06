package net.imjeck.impl.client;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.imjeck.api.client.CtmLoader;
import net.imjeck.api.client.CtmLoaderRegistry;

public final class CtmLoaderRegistryImpl implements CtmLoaderRegistry {
	public static final CtmLoaderRegistryImpl INSTANCE = new CtmLoaderRegistryImpl();

	private final Map<String, CtmLoader<?>> loaderMap = new Object2ObjectOpenHashMap<>();

	@Override
	public void registerLoader(String method, CtmLoader<?> loader) {
		loaderMap.put(method, loader);
	}

	@Override
	@Nullable
	public CtmLoader<?> getLoader(String method) {
		return loaderMap.get(method);
	}
}
