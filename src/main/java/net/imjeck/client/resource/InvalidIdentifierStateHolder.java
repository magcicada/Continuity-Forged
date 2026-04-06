package net.imjeck.client.resource;

import net.imjeck.client.util.BooleanState;

public final class InvalidIdentifierStateHolder {
	private static final ThreadLocal<BooleanState> STATES = ThreadLocal.withInitial(BooleanState::new);

	public static BooleanState get() {
		return STATES.get();
	}
}
