package me.pepperbell.continuity.client.mixinterface;

import me.pepperbell.continuity.client.resource.ModelWrappingHandler;
import org.jetbrains.annotations.Nullable;

public interface ModelLoaderExtension {
  @Nullable
  ModelWrappingHandler continuity$getModelWrappingHandler();

  void continuity$setModelWrappingHandler(@Nullable ModelWrappingHandler handler);
}
