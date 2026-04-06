package net.imjeck.api.client;

public interface CtmLoader<T extends CtmProperties> {
	CtmProperties.Factory<T> getPropertiesFactory();

	QuadProcessor.Factory<T> getProcessorFactory();

	CachingPredicates.Factory<T> getPredicatesFactory();
}
