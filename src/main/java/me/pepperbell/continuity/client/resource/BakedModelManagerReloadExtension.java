package me.pepperbell.continuity.client.resource;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import me.pepperbell.continuity.client.ContinuityClient;
import me.pepperbell.continuity.client.mixinterface.ModelLoaderExtension;
import me.pepperbell.continuity.client.model.QuadProcessors;
import me.pepperbell.continuity.client.util.RenderUtil;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

public class BakedModelManagerReloadExtension {
  private final CompletableFuture<CtmPropertiesLoader.LoadingResult> ctmLoadingResultFuture;
  private final AtomicBoolean wrapEmissiveModels = new AtomicBoolean();
  private final SpriteLoaderLoadContextImpl spriteLoaderLoadContext;
  private volatile List<QuadProcessors.ProcessorHolder> processorHolders;

  public BakedModelManagerReloadExtension(
      ResourceManager resourceManager, Executor prepareExecutor) {
    ctmLoadingResultFuture =
        CompletableFuture.supplyAsync(
            () -> CtmPropertiesLoader.loadAllWithState(resourceManager), prepareExecutor);
    spriteLoaderLoadContext =
        new SpriteLoaderLoadContextImpl(
            ctmLoadingResultFuture.thenApply(
                CtmPropertiesLoader.LoadingResult::getTextureDependencies),
            wrapEmissiveModels);
    EmissiveSuffixLoader.load(resourceManager);
  }

  public void setContext() {
    SpriteLoaderLoadContext.THREAD_LOCAL.set(spriteLoaderLoadContext);
  }

  public void clearContext() {
    SpriteLoaderLoadContext.THREAD_LOCAL.set(null);
  }

  public void beforeBaking(
      Map<ResourceLocation, AtlasSet.StitchResult> preparations, ModelBakery modelLoader) {
    CtmPropertiesLoader.LoadingResult result;
    try {
      result = ctmLoadingResultFuture.join();
    } catch (Exception e) {
      ContinuityClient.LOGGER.error("CTM properties loading failed, skipping CTM processing", e);
      ((ModelLoaderExtension) modelLoader).continuity$setModelWrappingHandler(null);
      return;
    }

    List<QuadProcessors.ProcessorHolder> processorHolders =
        result.createProcessorHolders(
            spriteId -> {
              AtlasSet.StitchResult preparation = preparations.get(spriteId.atlasLocation());
              if (preparation == null) {
                return null;
              }
              TextureAtlasSprite sprite = preparation.getSprite(spriteId.texture());
              if (sprite != null) {
                return sprite;
              }
              return preparation.missing();
            });

    this.processorHolders = processorHolders;

    ModelWrappingHandler wrappingHandler =
        ModelWrappingHandler.create(!processorHolders.isEmpty(), wrapEmissiveModels.get());
    ((ModelLoaderExtension) modelLoader).continuity$setModelWrappingHandler(wrappingHandler);
  }

  public void apply() {
    List<QuadProcessors.ProcessorHolder> processorHolders = this.processorHolders;
    if (processorHolders != null) {
      QuadProcessors.reload(processorHolders);
    }
    RenderUtil.ReloadListener.reload();
  }

  private static class SpriteLoaderLoadContextImpl implements SpriteLoaderLoadContext {
    private final CompletableFuture<Map<ResourceLocation, Set<ResourceLocation>>> allExtraIdsFuture;
    private final Map<ResourceLocation, CompletableFuture<Set<ResourceLocation>>> extraIdsFutures =
        new Object2ObjectOpenHashMap<>();
    private final EmissiveControl blockAtlasEmissiveControl;

    public SpriteLoaderLoadContextImpl(
        CompletableFuture<Map<ResourceLocation, Set<ResourceLocation>>> allExtraIdsFuture,
        AtomicBoolean blockAtlasHasEmissivesHolder) {
      this.allExtraIdsFuture = allExtraIdsFuture;
      blockAtlasEmissiveControl = new EmissiveControlImpl(blockAtlasHasEmissivesHolder);
    }

    @Override
    public CompletableFuture<@Nullable Set<ResourceLocation>> getExtraIdsFuture(
        ResourceLocation atlasId) {
      return extraIdsFutures.computeIfAbsent(
          atlasId, id -> allExtraIdsFuture.thenApply(allExtraIds -> allExtraIds.get(id)));
    }

    @Override
    @Nullable
    public EmissiveControl getEmissiveControl(ResourceLocation atlasId) {
      if (atlasId.equals(TextureAtlas.LOCATION_BLOCKS)) {
        return blockAtlasEmissiveControl;
      }
      return null;
    }

    private static class EmissiveControlImpl implements EmissiveControl {
      @Nullable private volatile Map<ResourceLocation, ResourceLocation> emissiveIdMap;
      private final AtomicBoolean hasEmissivesHolder;

      public EmissiveControlImpl(AtomicBoolean hasEmissivesHolder) {
        this.hasEmissivesHolder = hasEmissivesHolder;
      }

      @Override
      @Nullable
      public Map<ResourceLocation, ResourceLocation> getEmissiveIdMap() {
        return emissiveIdMap;
      }

      @Override
      public void setEmissiveIdMap(Map<ResourceLocation, ResourceLocation> emissiveIdMap) {
        this.emissiveIdMap = emissiveIdMap;
      }

      @Override
      public void markHasEmissives() {
        hasEmissivesHolder.set(true);
      }
    }
  }
}
