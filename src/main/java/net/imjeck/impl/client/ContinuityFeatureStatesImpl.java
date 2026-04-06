package net.imjeck.impl.client;

import net.imjeck.api.client.ContinuityFeatureStates;
import net.imjeck.client.model.ModelObjectsContainer;
import net.imjeck.client.util.BooleanState;

public class ContinuityFeatureStatesImpl implements ContinuityFeatureStates {
	private final FeatureStateImpl connectedTexturesState = new FeatureStateImpl();
	private final FeatureStateImpl emissiveTexturesState = new FeatureStateImpl();
	{
		connectedTexturesState.enable();
		emissiveTexturesState.enable();
	}

	public static ContinuityFeatureStatesImpl get() {
		return ModelObjectsContainer.get().featureStates;
	}

	@Override
	public FeatureState getConnectedTexturesState() {
		return connectedTexturesState;
	}

	@Override
	public FeatureState getEmissiveTexturesState() {
		return emissiveTexturesState;
	}

	public static class FeatureStateImpl extends BooleanState implements FeatureState {
	}
}
