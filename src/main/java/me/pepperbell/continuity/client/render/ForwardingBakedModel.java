package me.pepperbell.continuity.client.render;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public abstract class ForwardingBakedModel implements BakedModel {
  protected BakedModel wrapped;

  @Override
  public List<BakedQuad> getQuads(
      @Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
    return wrapped.getQuads(state, side, rand);
  }

  @Override
  public boolean useAmbientOcclusion() {
    return wrapped.useAmbientOcclusion();
  }

  @Override
  public boolean isGui3d() {
    return wrapped.isGui3d();
  }

  @Override
  public boolean usesBlockLight() {
    return wrapped.usesBlockLight();
  }

  @Override
  public boolean isCustomRenderer() {
    return wrapped.isCustomRenderer();
  }

  @Override
  public TextureAtlasSprite getParticleIcon() {
    return wrapped.getParticleIcon();
  }

  @Override
  public ItemTransforms getTransforms() {
    return wrapped.getTransforms();
  }

  @Override
  public ItemOverrides getOverrides() {
    return wrapped.getOverrides();
  }

  @Override
  public List<BakedQuad> getQuads(
      @Nullable BlockState state,
      @Nullable Direction side,
      RandomSource rand,
      ModelData data,
      @Nullable net.minecraft.client.renderer.RenderType renderType) {
    return wrapped.getQuads(state, side, rand, data, renderType);
  }

  @Override
  public boolean useAmbientOcclusion(BlockState state) {
    return wrapped.useAmbientOcclusion(state);
  }

  @Override
  public boolean useAmbientOcclusion(
      BlockState state, net.minecraft.client.renderer.RenderType renderType) {
    return wrapped.useAmbientOcclusion(state, renderType);
  }

  @Override
  public TextureAtlasSprite getParticleIcon(ModelData data) {
    return wrapped.getParticleIcon(data);
  }

  @Override
  public ModelData getModelData(
      BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
    return wrapped.getModelData(level, pos, state, modelData);
  }

  @Override
  public net.minecraftforge.client.ChunkRenderTypeSet getRenderTypes(
      BlockState state, RandomSource rand, ModelData data) {
    return wrapped.getRenderTypes(state, rand, data);
  }

  @Override
  public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
    return wrapped.getRenderPasses(itemStack, fabulous);
  }

  @Override
  public List<net.minecraft.client.renderer.RenderType> getRenderTypes(
      ItemStack itemStack, boolean fabulous) {
    return wrapped.getRenderTypes(itemStack, fabulous);
  }
}
