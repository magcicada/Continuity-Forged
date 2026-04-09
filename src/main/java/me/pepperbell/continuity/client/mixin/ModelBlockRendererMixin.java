package me.pepperbell.continuity.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.pepperbell.continuity.client.model.CtmBakedModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into Forge's extended tesselate methods to provide block level/pos context via
 * thread-local variables, enabling CTM to work with Embeddium's rendering pipeline. Embeddium uses
 * ModelDataSnapshotter which provides ModelData.EMPTY for non-block-entity blocks, so we fall back
 * to thread-locals set here.
 */
@Mixin(ModelBlockRenderer.class)
abstract class ModelBlockRendererMixin {

  @Inject(
      method =
          "tesselateWithoutAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
      at = @At("HEAD"),
      remap = false)
  private void continuity$beforeTesselateWithoutAO(
      BlockAndTintGetter level,
      BakedModel model,
      BlockState state,
      BlockPos pos,
      PoseStack poseStack,
      VertexConsumer consumer,
      boolean checkSides,
      RandomSource random,
      long seed,
      int overlay,
      ModelData modelData,
      RenderType renderType,
      CallbackInfo ci) {
    CtmBakedModel.THREAD_LOCAL_LEVEL.set(level);
    CtmBakedModel.THREAD_LOCAL_POS.set(pos.immutable());
  }

  @Inject(
      method =
          "tesselateWithoutAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
      at = @At("RETURN"),
      remap = false)
  private void continuity$afterTesselateWithoutAO(
      BlockAndTintGetter level,
      BakedModel model,
      BlockState state,
      BlockPos pos,
      PoseStack poseStack,
      VertexConsumer consumer,
      boolean checkSides,
      RandomSource random,
      long seed,
      int overlay,
      ModelData modelData,
      RenderType renderType,
      CallbackInfo ci) {
    CtmBakedModel.THREAD_LOCAL_LEVEL.remove();
    CtmBakedModel.THREAD_LOCAL_POS.remove();
  }

  @Inject(
      method =
          "tesselateWithAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
      at = @At("HEAD"),
      remap = false)
  private void continuity$beforeTesselateWithAO(
      BlockAndTintGetter level,
      BakedModel model,
      BlockState state,
      BlockPos pos,
      PoseStack poseStack,
      VertexConsumer consumer,
      boolean checkSides,
      RandomSource random,
      long seed,
      int overlay,
      ModelData modelData,
      RenderType renderType,
      CallbackInfo ci) {
    CtmBakedModel.THREAD_LOCAL_LEVEL.set(level);
    CtmBakedModel.THREAD_LOCAL_POS.set(pos.immutable());
  }

  @Inject(
      method =
          "tesselateWithAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
      at = @At("RETURN"),
      remap = false)
  private void continuity$afterTesselateWithAO(
      BlockAndTintGetter level,
      BakedModel model,
      BlockState state,
      BlockPos pos,
      PoseStack poseStack,
      VertexConsumer consumer,
      boolean checkSides,
      RandomSource random,
      long seed,
      int overlay,
      ModelData modelData,
      RenderType renderType,
      CallbackInfo ci) {
    CtmBakedModel.THREAD_LOCAL_LEVEL.remove();
    CtmBakedModel.THREAD_LOCAL_POS.remove();
  }
}
