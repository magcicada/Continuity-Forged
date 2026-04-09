package me.pepperbell.continuity.client.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import me.pepperbell.continuity.client.resource.AtlasLoaderInitContext;
import me.pepperbell.continuity.client.resource.AtlasLoaderLoadContext;
import me.pepperbell.continuity.client.resource.EmissiveSuffixLoader;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SpriteResourceLoader.class)
abstract class AtlasLoaderMixin {
  @ModifyVariable(
      method =
          "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/atlas/SpriteResourceLoader;",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/renderer/texture/atlas/SpriteResourceLoader;<init>(Ljava/util/List;)V"),
      argsOnly = false,
      ordinal = 0)
  private static List<SpriteSource> continuity$modifySources(List<SpriteSource> sources) {
    AtlasLoaderInitContext context = AtlasLoaderInitContext.THREAD_LOCAL.get();
    if (context != null) {
      Set<ResourceLocation> extraIds = context.getExtraIds();
      if (extraIds != null && !extraIds.isEmpty()) {
        List<SpriteSource> extraSources = new ObjectArrayList<>();
        for (ResourceLocation extraId : extraIds) {
          extraSources.add(new SingleFile(extraId, Optional.empty()));
        }

        if (sources instanceof ArrayList) {
          sources.addAll(0, extraSources);
        } else {
          List<SpriteSource> mutableSources = new ArrayList<>(extraSources);
          mutableSources.addAll(sources);
          return mutableSources;
        }
      }
    }
    return sources;
  }

  @Inject(
      method = "list(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/List;",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lcom/google/common/collect/ImmutableList;builder()Lcom/google/common/collect/ImmutableList$Builder;",
              remap = false),
      locals = LocalCapture.CAPTURE_FAILHARD)
  private void continuity$afterList(
      ResourceManager resourceManager,
      CallbackInfoReturnable<List<Supplier<SpriteContents>>> cir,
      Map<ResourceLocation, SpriteSource.SpriteSupplier> suppliers) {
    AtlasLoaderLoadContext context = AtlasLoaderLoadContext.THREAD_LOCAL.get();
    if (context != null) {
      String emissiveSuffix = EmissiveSuffixLoader.getEmissiveSuffix();
      if (emissiveSuffix != null) {
        Map<ResourceLocation, SpriteSource.SpriteSupplier> emissiveSuppliers =
            new Object2ObjectOpenHashMap<>();
        Map<ResourceLocation, ResourceLocation> emissiveIdMap = new Object2ObjectOpenHashMap<>();
        suppliers.forEach(
            (id, supplier) -> {
              if (!id.getPath().endsWith(emissiveSuffix)) {
                ResourceLocation emissiveId = id.withPath(id.getPath() + emissiveSuffix);
                if (!suppliers.containsKey(emissiveId)) {
                  ResourceLocation emissiveLocation =
                      emissiveId.withPath("textures/" + emissiveId.getPath() + ".png");
                  Optional<Resource> optionalResource =
                      resourceManager.getResource(emissiveLocation);
                  if (optionalResource.isPresent()) {
                    Resource resource = optionalResource.get();
                    emissiveSuppliers.put(
                        emissiveId, () -> SpriteLoader.loadSprite(emissiveId, resource));
                    emissiveIdMap.put(id, emissiveId);
                  }
                } else {
                  emissiveIdMap.put(id, emissiveId);
                }
              }
            });
        suppliers.putAll(emissiveSuppliers);
        context.setEmissiveIdMap(emissiveIdMap.isEmpty() ? null : emissiveIdMap);
      }
    }
  }
}
