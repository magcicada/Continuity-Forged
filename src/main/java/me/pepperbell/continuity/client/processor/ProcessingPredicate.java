package me.pepperbell.continuity.client.processor;

import me.pepperbell.continuity.api.client.ProcessingDataProvider;
import me.pepperbell.continuity.client.render.QuadView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface ProcessingPredicate {
  boolean shouldProcessQuad(
      QuadView quad,
      TextureAtlasSprite sprite,
      BlockAndTintGetter blockView,
      BlockState appearanceState,
      BlockState state,
      BlockPos pos,
      ProcessingDataProvider dataProvider);
}
