package net.imjeck.client.render;

public enum TriState {
	TRUE,
	FALSE,
	DEFAULT;

	public static TriState of(boolean value) {
		return value ? TRUE : FALSE;
	}
}
