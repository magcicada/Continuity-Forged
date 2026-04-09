package me.pepperbell.continuity.client.resource;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface AtlasLoaderLoadContext {
  ThreadLocal<AtlasLoaderLoadContext> THREAD_LOCAL = new ThreadLocal<>();

  void setEmissiveIdMap(@Nullable Map<ResourceLocation, ResourceLocation> map);
}
