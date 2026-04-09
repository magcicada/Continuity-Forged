package me.pepperbell.continuity.client.resource;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface AtlasLoaderInitContext {
  ThreadLocal<AtlasLoaderInitContext> THREAD_LOCAL = new ThreadLocal<>();

  @Nullable
  Set<ResourceLocation> getExtraIds();
}
