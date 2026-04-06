package net.imjeck.api.client;

public interface ProcessingDataProvider {
	<T> T getData(ProcessingDataKey<T> key);
}
