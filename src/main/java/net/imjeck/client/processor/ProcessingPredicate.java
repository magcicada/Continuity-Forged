package net.imjeck.client.processor;

import net.imjeck.api.client.ProcessingDataProvider;
import net.imjeck.client.render.QuadView;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public interface ProcessingPredicate {
	boolean shouldProcessQuad(QuadView quad, TextureAtlasSprite sprite, BlockAndTintGetter blockView, BlockState appearanceState, BlockState state, BlockPos pos, ProcessingDataProvider dataProvider);
}
