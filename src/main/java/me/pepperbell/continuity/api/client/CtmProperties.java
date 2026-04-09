package me.pepperbell.continuity.api.client;

import java.util.Collection;
import java.util.Properties;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

public interface CtmProperties extends Comparable<CtmProperties> {
  Collection<Material> getTextureDependencies();

  interface Factory<T extends CtmProperties> {
    @Nullable
    T createProperties(
        Properties properties,
        ResourceLocation resourceId,
        PackResources pack,
        int packPriority,
        ResourceManager resourceManager,
        String method);
  }
}
