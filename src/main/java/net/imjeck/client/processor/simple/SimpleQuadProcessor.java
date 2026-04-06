package net.imjeck.client.processor.simple;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.imjeck.api.client.QuadProcessor;
import net.imjeck.client.processor.AbstractQuadProcessorFactory;
import net.imjeck.client.processor.BaseProcessingPredicate;
import net.imjeck.client.processor.ProcessingPredicate;
import net.imjeck.client.properties.BaseCtmProperties;
import net.imjeck.client.util.QuadUtil;
import net.imjeck.client.util.TextureUtil;
import net.imjeck.client.render.MutableQuadView;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;

public class SimpleQuadProcessor implements QuadProcessor {
	protected SpriteProvider spriteProvider;
	protected ProcessingPredicate processingPredicate;

	public SimpleQuadProcessor(SpriteProvider spriteProvider, ProcessingPredicate processingPredicate) {
		this.spriteProvider = spriteProvider;
		this.processingPredicate = processingPredicate;
	}

	@Override
	public ProcessingResult processQuad(MutableQuadView quad, TextureAtlasSprite sprite, BlockAndTintGetter blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, int pass, ProcessingContext context) {
		if (!processingPredicate.shouldProcessQuad(quad, sprite, blockView, appearanceState, state, pos, context)) {
			return ProcessingResult.NEXT_PROCESSOR;
		}
		TextureAtlasSprite newSprite = spriteProvider.getSprite(quad, sprite, blockView, appearanceState, state, pos, randomSupplier, context);
		return process(quad, sprite, newSprite);
	}

	public static ProcessingResult process(MutableQuadView quad, TextureAtlasSprite oldSprite, @Nullable TextureAtlasSprite newSprite) {
		if (newSprite == null) {
			return ProcessingResult.STOP;
		}
		if (TextureUtil.isMissingSprite(newSprite)) {
			return ProcessingResult.NEXT_PROCESSOR;
		}
		QuadUtil.interpolate(quad, oldSprite, newSprite);
		return ProcessingResult.NEXT_PASS;
	}

	public static class Factory<T extends BaseCtmProperties> extends AbstractQuadProcessorFactory<T> {
		protected SpriteProvider.Factory<? super T> spriteProviderFactory;

		public Factory(SpriteProvider.Factory<? super T> spriteProviderFactory) {
			this.spriteProviderFactory = spriteProviderFactory;
		}

		@Override
		public QuadProcessor createProcessor(T properties, TextureAtlasSprite[] sprites) {
			return new SimpleQuadProcessor(spriteProviderFactory.createSpriteProvider(sprites, properties), BaseProcessingPredicate.fromProperties(properties));
		}

		@Override
		public int getTextureAmount(T properties) {
			return spriteProviderFactory.getTextureAmount(properties);
		}
	}
}
