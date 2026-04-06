package net.imjeck.client.processor.simple;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.imjeck.api.client.ProcessingDataProvider;
import net.imjeck.client.properties.BaseCtmProperties;
import net.imjeck.client.render.QuadView;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;

public interface SpriteProvider {
	@Nullable
	TextureAtlasSprite getSprite(QuadView quad, TextureAtlasSprite sprite, BlockAndTintGetter blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, ProcessingDataProvider dataProvider);

	interface Factory<T extends BaseCtmProperties> {
		SpriteProvider createSpriteProvider(TextureAtlasSprite[] sprites, T properties);

		int getTextureAmount(T properties);
	}
}
