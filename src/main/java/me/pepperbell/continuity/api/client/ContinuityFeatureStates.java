package me.pepperbell.continuity.api.client;

import me.pepperbell.continuity.impl.client.ContinuityFeatureStatesImpl;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ContinuityFeatureStates {
  static ContinuityFeatureStates get() {
    return ContinuityFeatureStatesImpl.get();
  }

  FeatureState getConnectedTexturesState();

  FeatureState getEmissiveTexturesState();

  @ApiStatus.NonExtendable
  interface FeatureState {
    boolean isEnabled();

    void enable();

    void disable();
  }
}
