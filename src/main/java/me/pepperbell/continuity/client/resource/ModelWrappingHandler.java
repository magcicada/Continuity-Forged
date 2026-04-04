package me.pepperbell.continuity.client.resource;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;

import me.pepperbell.continuity.client.mixinterface.ModelLoaderExtension;
import me.pepperbell.continuity.client.model.CtmBakedModel;
import me.pepperbell.continuity.client.model.EmissiveBakedModel;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ModelWrappingHandler {
	private final boolean wrapCtm;
	private final boolean wrapEmissive;
	private final ImmutableMap<ModelResourceLocation, BlockState> blockStateModelIds;

	private ModelWrappingHandler(boolean wrapCtm, boolean wrapEmissive) {
		this.wrapCtm = wrapCtm;
		this.wrapEmissive = wrapEmissive;
		blockStateModelIds = createBlockStateModelIdMap();
	}

	@Nullable
	public static ModelWrappingHandler create(boolean wrapCtm, boolean wrapEmissive) {
		if (!wrapCtm && !wrapEmissive) {
			return null;
		}
		return new ModelWrappingHandler(wrapCtm, wrapEmissive);
	}

	private static ImmutableMap<ModelResourceLocation, BlockState> createBlockStateModelIdMap() {
		ImmutableMap.Builder<ModelResourceLocation, BlockState> builder = ImmutableMap.builder();
		// Match code of ModelManager#bake
		for (Block block : BuiltInRegistries.BLOCK) {
			ResourceLocation blockId = block.getRegistryEntry().registryKey().getValue();
			for (BlockState state : block.getStateDefinition().getPossibleStates()) {
				ModelResourceLocation modelId = BlockModelShaper.stateToModelLocation(blockId, state);
				builder.put(modelId, state);
			}
		}
		return builder.build();
	}

	public BakedModel wrap(@Nullable BakedModel model, ResourceLocation modelId) {
		if (model != null && !model.isCustomRenderer() && !modelId.equals(ModelBakery.MISSING_MODEL_LOCATION)) {
			if (wrapCtm) {
				if (modelId instanceof ModelResourceLocation) {
					BlockState state = blockStateModelIds.get(modelId);
					if (state != null) {
						model = new CtmBakedModel(model, state);
					}
				}
			}
			if (wrapEmissive) {
				model = new EmissiveBakedModel(model);
			}
		}
		return model;
	}

	public static void init() {
		ModelLoadingPlugin.register(pluginCtx -> {
			pluginCtx.modifyModelAfterBake().register(ModelModifier.WRAP_LAST_PHASE, (model, ctx) -> {
				ModelBakery modelLoader = ctx.loader();
				ModelWrappingHandler wrappingHandler = ((ModelLoaderExtension) modelLoader).continuity$getModelWrappingHandler();
				if (wrappingHandler != null) {
					return wrappingHandler.wrap(model, ctx.id());
				}
				return model;
			});
		});
	}
}
