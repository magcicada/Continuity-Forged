package me.pepperbell.continuity.client.resource;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import me.pepperbell.continuity.client.mixinterface.ModelLoaderExtension;
import me.pepperbell.continuity.client.model.CtmBakedModel;
import me.pepperbell.continuity.client.model.EmissiveBakedModel;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

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
    for (Block block : BuiltInRegistries.BLOCK) {
      ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
      for (BlockState state : block.getStateDefinition().getPossibleStates()) {
        ModelResourceLocation modelId = BlockModelShaper.stateToModelLocation(blockId, state);
        builder.put(modelId, state);
            }
        }
    return builder.build();
    }

  public BakedModel wrap(@Nullable BakedModel model, ResourceLocation modelId) {
    if (model != null
        && !model.isCustomRenderer()
        && !modelId.equals(ModelBakery.MISSING_MODEL_LOCATION)) {

        if (modelId instanceof ModelResourceLocation) {
          BlockState state = blockStateModelIds.get(modelId);
          if (state != null) {
            if (wrapCtm) {
            model = new CtmBakedModel(model, state);
            }
      if (wrapEmissive) {
        model = new EmissiveBakedModel(model);
                    }
                }
            }
        }
    return model;
    }

  public static void onModifyBakingResult(
      net.minecraftforge.client.event.ModelEvent.ModifyBakingResult event) {
    Map<ResourceLocation, BakedModel> models = event.getModels();
    ModelBakery modelLoader = event.getModelBakery();
    ModelWrappingHandler wrappingHandler =
        ((ModelLoaderExtension) modelLoader).continuity$getModelWrappingHandler();
    if (wrappingHandler != null) {
      for (Map.Entry<ResourceLocation, BakedModel> entry : models.entrySet()) {
        BakedModel wrapped = wrappingHandler.wrap(entry.getValue(), entry.getKey());
        if (wrapped != entry.getValue()) {
          entry.setValue(wrapped);
                }
            }
        }
    }
}
