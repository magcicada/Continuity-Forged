package me.pepperbell.continuity.client.mixinterface;

import me.pepperbell.continuity.client.resource.ResourceRedirectHandler;
import org.jetbrains.annotations.Nullable;

public interface MultiPackResourceManagerExtension {
  @Nullable
  ResourceRedirectHandler continuity$getRedirectHandler();
}
