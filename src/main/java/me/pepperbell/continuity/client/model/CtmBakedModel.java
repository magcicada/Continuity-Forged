package me.pepperbell.continuity.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import me.pepperbell.continuity.api.client.QuadProcessor;
import me.pepperbell.continuity.client.config.ContinuityConfig;
import me.pepperbell.continuity.client.render.ForwardingBakedModel;
import me.pepperbell.continuity.client.render.MutableQuadView;
import me.pepperbell.continuity.client.render.QuadEmitter;
import me.pepperbell.continuity.client.render.RenderContext;
import me.pepperbell.continuity.client.util.RenderUtil;
import me.pepperbell.continuity.impl.client.ProcessingContextImpl;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

public class CtmBakedModel extends ForwardingBakedModel {
  public static final int PASSES = 4;

  public static final ModelProperty<BlockAndTintGetter> BLOCK_VIEW_PROPERTY = new ModelProperty<>();
  public static final ModelProperty<BlockPos> BLOCK_POS_PROPERTY = new ModelProperty<>();

  /** Thread-local context set by ModelBlockRendererMixin for Embeddium compatibility. */
  public static final ThreadLocal<BlockAndTintGetter> THREAD_LOCAL_LEVEL = new ThreadLocal<>();

  public static final ThreadLocal<BlockPos> THREAD_LOCAL_POS = new ThreadLocal<>();
  public static final ThreadLocal<RenderType> THREAD_LOCAL_RENDER_TYPE = new ThreadLocal<>();

  /**
   * Extra render types added so overlay quads (e.g. cutoutMipped overlays on solid blocks) can be
   * emitted correctly.
   */
  private static final ChunkRenderTypeSet OVERLAY_EXTRA_RENDER_TYPES =
      ChunkRenderTypeSet.of(
          RenderType.cutoutMipped(), RenderType.cutout(), RenderType.translucent());

  protected final BlockState defaultState;
  protected volatile Function<TextureAtlasSprite, QuadProcessors.Slice> defaultSliceFunc;

  public CtmBakedModel(BakedModel wrapped, BlockState defaultState) {
    this.wrapped = wrapped;
    this.defaultState = defaultState;
  }

  @Override
  public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
    if (!ContinuityConfig.INSTANCE.connectedTextures.get()) {
      return wrapped.getRenderTypes(state, rand, data);
    }
    return ChunkRenderTypeSet.union(
        wrapped.getRenderTypes(state, rand, data), OVERLAY_EXTRA_RENDER_TYPES);
  }

  @Override
  public ModelData getModelData(
      BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
    return modelData
        .derive()
        .with(BLOCK_VIEW_PROPERTY, level)
        .with(BLOCK_POS_PROPERTY, pos)
        .build();
  }

  @Override
  public List<BakedQuad> getQuads(
      @Nullable BlockState state,
      @Nullable Direction side,
      RandomSource rand,
      ModelData data,
      @Nullable RenderType renderType) {
    if (state == null || !ContinuityConfig.INSTANCE.connectedTextures.get()) {
      return wrapped.getQuads(state, side, rand, data, renderType);
    }

    ModelObjectsContainer container = ModelObjectsContainer.get();
    if (!container.featureStates.getConnectedTexturesState().isEnabled()) {
      return wrapped.getQuads(state, side, rand, data, renderType);
    }

    CtmQuadTransform quadTransform = container.ctmQuadTransform;
    if (quadTransform.isActive()) {
      return wrapped.getQuads(state, side, rand, data, renderType);
    }

    BlockAndTintGetter blockView = data.get(BLOCK_VIEW_PROPERTY);
    BlockPos pos = data.get(BLOCK_POS_PROPERTY);
    // Fallback for Embeddium: ModelDataSnapshotter provides EMPTY for non-block-entity blocks
    if (blockView == null) blockView = THREAD_LOCAL_LEVEL.get();
    if (pos == null) pos = THREAD_LOCAL_POS.get();
    if (blockView == null || pos == null) {
      return wrapped.getQuads(state, side, rand, data, renderType);
    }

    BlockState appearanceState = state.getAppearance(blockView, pos, Direction.DOWN, state, pos);

    RenderContext context = new RenderContext();
    quadTransform.prepare(
        blockView,
        appearanceState,
        state,
        pos,
        rand.nextLong(),
        context,
        ContinuityConfig.INSTANCE.useManualCulling.get(),
        getSliceFunc(appearanceState));

    // Determine if this render type belongs to the wrapped model (base geometry pass).
    // For non-native types (e.g. cutoutMipped on a solid block), we still process base quads
    // to trigger overlay generation but discard them from the final result.
    boolean isNativeRenderType =
        renderType == null || wrapped.getRenderTypes(state, rand, data).contains(renderType);

    THREAD_LOCAL_RENDER_TYPE.set(renderType);
    try {
      context.pushTransform(quadTransform);
      List<BakedQuad> baseQuads = wrapped.getQuads(state, side, rand, data, renderType);
      List<BakedQuad> result = context.processQuads(baseQuads, side);
      context.popTransform();

      // Separate overlay quads (from emitterConsumers) and mesh quads (from compact CTM splits).
      QuadEmitter emitter = context.getEmitter();

      // 1. Collect overlay quads
      quadTransform.processingContext.outputOverlaysTo(emitter);
      List<BakedQuad> overlayQuads = new ArrayList<>(emitter.getOutput());
      emitter.clearOutput();

      // 2. Collect mesh quads (extra quads from compact CTM quad splitting etc.)
      quadTransform.processingContext.outputMeshesTo(emitter);
      List<BakedQuad> meshQuads = new ArrayList<>(emitter.getOutput());
      emitter.clearOutput();

      if (!isNativeRenderType) {
        // Non-native render type: only return overlay quads (base geometry is in the native pass).
        quadTransform.reset();
        return overlayQuads.isEmpty() ? List.of() : overlayQuads;
      }

      // 3. Re-process mesh quads through the transform pipeline so multipass processors
      //    (e.g. random) can apply to compact CTM sub-quads.
      if (!meshQuads.isEmpty()) {
        quadTransform.processingContext.reset();
        quadTransform.processingContext.prepare();

        context.pushTransform(quadTransform);
        meshQuads = context.processQuads(meshQuads, side);
        context.popTransform();

        // Collect any overlay quads generated during reprocessing
        quadTransform.processingContext.outputOverlaysTo(emitter);
        overlayQuads.addAll(emitter.getOutput());
        emitter.clearOutput();

        // Collect any further mesh quads (rare but handle gracefully)
        quadTransform.processingContext.outputMeshesTo(emitter);
        meshQuads.addAll(emitter.getOutput());
        emitter.clearOutput();
      }

      // 4. Combine all quads
      if (!meshQuads.isEmpty() || !overlayQuads.isEmpty()) {
        result = new ArrayList<>(result);
        result.addAll(meshQuads);
        result.addAll(overlayQuads);
      }

      quadTransform.reset();

      return result;
    } finally {
      THREAD_LOCAL_RENDER_TYPE.remove();
    }
  }

  protected Function<TextureAtlasSprite, QuadProcessors.Slice> getSliceFunc(BlockState state) {
    if (state == defaultState) {
      Function<TextureAtlasSprite, QuadProcessors.Slice> sliceFunc = defaultSliceFunc;
      if (sliceFunc == null) {
        synchronized (this) {
          sliceFunc = defaultSliceFunc;
          if (sliceFunc == null) {
            sliceFunc = QuadProcessors.getCache(state);
            defaultSliceFunc = sliceFunc;
          }
        }
      }
      return sliceFunc;
    }
    return QuadProcessors.getCache(state);
  }

  protected static class CtmQuadTransform implements RenderContext.QuadTransform {
    protected final ProcessingContextImpl processingContext = new ProcessingContextImpl();
    protected final Supplier<RandomSource> randomSupplier =
        new Supplier<>() {
          private final RandomSource random = RandomSource.createNewThreadLocalInstance();

          @Override
          public RandomSource get() {
            random.setSeed(randomSeed);
            return random;
          }
        };

    protected BlockAndTintGetter blockView;
    protected BlockState appearanceState;
    protected BlockState state;
    protected BlockPos pos;
    protected long randomSeed;
    protected RenderContext renderContext;
    protected boolean useManualCulling;
    protected Function<TextureAtlasSprite, QuadProcessors.Slice> sliceFunc;

    protected boolean active;

    @Override
    public boolean transform(MutableQuadView quad) {
      if (useManualCulling && renderContext.isFaceCulled(quad.cullFace())) {
        return false;
      }

      for (int pass = 0; pass < PASSES; pass++) {
        Boolean result = transformOnce(quad, pass);
        if (result != null) {
          return result;
        }
      }

      return true;
    }

    protected Boolean transformOnce(MutableQuadView quad, int pass) {
      TextureAtlasSprite sprite = RenderUtil.getSpriteFinder().find(quad);
      QuadProcessors.Slice slice = sliceFunc.apply(sprite);
      QuadProcessor[] processors = pass == 0 ? slice.processors() : slice.multipassProcessors();
      for (QuadProcessor processor : processors) {
        QuadProcessor.ProcessingResult result =
            processor.processQuad(
                quad,
                sprite,
                blockView,
                appearanceState,
                state,
                pos,
                randomSupplier,
                pass,
                processingContext);
        if (result == QuadProcessor.ProcessingResult.NEXT_PROCESSOR) {
          continue;
        }
        if (result == QuadProcessor.ProcessingResult.NEXT_PASS) {
          return null;
        }
        if (result == QuadProcessor.ProcessingResult.STOP) {
          return true;
        }
        if (result == QuadProcessor.ProcessingResult.DISCARD) {
          return false;
        }
      }
      return true;
    }

    public boolean isActive() {
      return active;
    }

    public void prepare(
        BlockAndTintGetter blockView,
        BlockState appearanceState,
        BlockState state,
        BlockPos pos,
        long randomSeed,
        RenderContext renderContext,
        boolean useManualCulling,
        Function<TextureAtlasSprite, QuadProcessors.Slice> sliceFunc) {
      this.blockView = blockView;
      this.appearanceState = appearanceState;
      this.state = state;
      this.pos = pos;
      this.randomSeed = randomSeed;
      this.renderContext = renderContext;
      this.useManualCulling = useManualCulling;
      this.sliceFunc = sliceFunc;

      active = true;

      processingContext.prepare();
    }

    public void reset() {
      blockView = null;
      appearanceState = null;
      state = null;
      pos = null;
      renderContext = null;
      useManualCulling = false;
      sliceFunc = null;

      active = false;

      processingContext.reset();
    }
  }
}
