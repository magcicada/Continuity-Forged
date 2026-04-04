package me.pepperbell.continuity.client.model;

import java.util.function.Supplier;

import me.pepperbell.continuity.api.client.EmissiveSpriteApi;
import me.pepperbell.continuity.client.config.ContinuityConfig;
import me.pepperbell.continuity.client.util.QuadUtil;
import me.pepperbell.continuity.client.util.RenderUtil;
import me.pepperbell.continuity.client.render.BlendMode;
import me.pepperbell.continuity.client.render.MaterialFinder;
import me.pepperbell.continuity.client.render.RenderMaterial;
import me.pepperbell.continuity.client.render.MeshBuilder;
import me.pepperbell.continuity.client.render.MutableQuadView;
import me.pepperbell.continuity.client.render.QuadEmitter;
import me.pepperbell.continuity.client.render.ForwardingBakedModel;
import me.pepperbell.continuity.client.render.RenderContext;
import me.pepperbell.continuity.client.render.TriState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;

public class EmissiveBakedModel extends ForwardingBakedModel {
	protected static final RenderMaterial[] EMISSIVE_MATERIALS;
	protected static final RenderMaterial DEFAULT_EMISSIVE_MATERIAL;
	protected static final RenderMaterial CUTOUT_MIPPED_EMISSIVE_MATERIAL;

	static {
		BlendMode[] blendModes = BlendMode.values();
		EMISSIVE_MATERIALS = new RenderMaterial[blendModes.length];
		MaterialFinder finder = RenderUtil.getMaterialFinder();
		for (BlendMode blendMode : blendModes) {
			EMISSIVE_MATERIALS[blendMode.ordinal()] = finder.emissive(true).disableDiffuse(true).ambientOcclusion(TriState.FALSE).blendMode(blendMode).find();
		}

		DEFAULT_EMISSIVE_MATERIAL = EMISSIVE_MATERIALS[BlendMode.DEFAULT.ordinal()];
		CUTOUT_MIPPED_EMISSIVE_MATERIAL = EMISSIVE_MATERIALS[BlendMode.CUTOUT_MIPPED.ordinal()];
	}

	public EmissiveBakedModel(BakedModel wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		if (!ContinuityConfig.INSTANCE.emissiveTextures.get()) {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			return;
		}

		ModelObjectsContainer container = ModelObjectsContainer.get();
		if (!container.featureStates.getEmissiveTexturesState().isEnabled()) {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			return;
		}

		EmissiveBlockQuadTransform quadTransform = container.emissiveBlockQuadTransform;
		if (quadTransform.isActive()) {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			return;
		}

		MeshBuilder meshBuilder = container.meshBuilder;
		quadTransform.prepare(meshBuilder.getEmitter(), state, context, ContinuityConfig.INSTANCE.useManualCulling.get());

		context.pushTransform(quadTransform);
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();

		if (quadTransform.didEmit()) {
			meshBuilder.build().outputTo(context.getEmitter());
		}
		quadTransform.reset();
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		if (!ContinuityConfig.INSTANCE.emissiveTextures.get()) {
			super.emitItemQuads(stack, randomSupplier, context);
			return;
		}

		ModelObjectsContainer container = ModelObjectsContainer.get();
		if (!container.featureStates.getEmissiveTexturesState().isEnabled()) {
			super.emitItemQuads(stack, randomSupplier, context);
			return;
		}

		EmissiveItemQuadTransform quadTransform = container.emissiveItemQuadTransform;
		if (quadTransform.isActive()) {
			super.emitItemQuads(stack, randomSupplier, context);
			return;
		}

		MeshBuilder meshBuilder = container.meshBuilder;
		quadTransform.prepare(meshBuilder.getEmitter());

		context.pushTransform(quadTransform);
		super.emitItemQuads(stack, randomSupplier, context);
		context.popTransform();

		if (quadTransform.didEmit()) {
			meshBuilder.build().outputTo(context.getEmitter());
		}
		quadTransform.reset();
	}

	@Override
	public boolean isVanillaAdapter() {
		if (!ContinuityConfig.INSTANCE.emissiveTextures.get()) {
			return super.isVanillaAdapter();
		}
		return false;
	}

	protected static class EmissiveBlockQuadTransform implements RenderContext.QuadTransform {
		protected QuadEmitter emitter;
		protected BlockState state;
		protected RenderContext renderContext;
		protected boolean useManualCulling;

		protected boolean active;
		protected boolean didEmit;
		protected boolean calculateDefaultLayer;
		protected boolean isDefaultLayerSolid;

		@Override
		public boolean transform(MutableQuadView quad) {
			if (useManualCulling && renderContext.isFaceCulled(quad.cullFace())) {
				return false;
			}

			TextureAtlasSprite sprite = RenderUtil.getSpriteFinder().find(quad);
			TextureAtlasSprite emissiveSprite = EmissiveSpriteApi.get().getEmissiveSprite(sprite);
			if (emissiveSprite != null) {
				emitter.copyFrom(quad);

				BlendMode blendMode = quad.material().blendMode();
				RenderMaterial emissiveMaterial;
				if (blendMode == BlendMode.DEFAULT) {
					if (calculateDefaultLayer) {
						isDefaultLayerSolid = ItemBlockRenderTypes.getChunkRenderType(state) == RenderType.solid();
						calculateDefaultLayer = false;
					}

					if (isDefaultLayerSolid) {
						emissiveMaterial = CUTOUT_MIPPED_EMISSIVE_MATERIAL;
					} else {
						emissiveMaterial = DEFAULT_EMISSIVE_MATERIAL;
					}
				} else if (blendMode == BlendMode.SOLID) {
					emissiveMaterial = CUTOUT_MIPPED_EMISSIVE_MATERIAL;
				} else {
					emissiveMaterial = EMISSIVE_MATERIALS[blendMode.ordinal()];
				}

				emitter.material(emissiveMaterial);
				QuadUtil.interpolate(emitter, sprite, emissiveSprite);
				emitter.emit();
				didEmit = true;
			}
			return true;
		}

		public boolean isActive() {
			return active;
		}

		public boolean didEmit() {
			return didEmit;
		}

		public void prepare(QuadEmitter emitter, BlockState state, RenderContext renderContext, boolean useManualCulling) {
			this.emitter = emitter;
			this.state = state;
			this.renderContext = renderContext;
			this.useManualCulling = useManualCulling;

			active = true;
			didEmit = false;
			calculateDefaultLayer = true;
			isDefaultLayerSolid = false;
		}

		public void reset() {
			emitter = null;
			state = null;
			renderContext = null;
			useManualCulling = false;

			active = false;
		}
	}

	protected static class EmissiveItemQuadTransform implements RenderContext.QuadTransform {
		protected QuadEmitter emitter;

		protected boolean active;
		protected boolean didEmit;

		@Override
		public boolean transform(MutableQuadView quad) {
			TextureAtlasSprite sprite = RenderUtil.getSpriteFinder().find(quad);
			TextureAtlasSprite emissiveSprite = EmissiveSpriteApi.get().getEmissiveSprite(sprite);
			if (emissiveSprite != null) {
				emitter.copyFrom(quad);
				emitter.material(DEFAULT_EMISSIVE_MATERIAL);
				QuadUtil.interpolate(emitter, sprite, emissiveSprite);
				emitter.emit();
				didEmit = true;
			}
			return true;
		}

		public boolean isActive() {
			return active;
		}

		public boolean didEmit() {
			return didEmit;
		}

		public void prepare(QuadEmitter emitter) {
			this.emitter = emitter;

			active = true;
			didEmit = false;
		}

		public void reset() {
			active = false;
			emitter = null;
		}
	}
}
