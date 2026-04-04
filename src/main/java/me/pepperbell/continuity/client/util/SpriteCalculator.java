package me.pepperbell.continuity.client.util;

import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Unmodifiable;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

public final class SpriteCalculator {
	private static final BlockModelShaper MODELS = Minecraft.getInstance().getModelManager().getBlockModelShaper();
	private static final Direction[] CULL_FACES = ArrayUtils.add(Direction.values(), null);

	private static final EnumMap<Direction, SpriteCache> SPRITE_CACHES = new EnumMap<>(Direction.class);

	static {
		for (Direction direction : Direction.values()) {
			SPRITE_CACHES.put(direction, new SpriteCache(direction));
		}

		net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.level.LevelEvent.Unload event) -> {
			SpriteCalculator.clearCache();
		});
	}

	@Unmodifiable
	public static Set<TextureAtlasSprite> getSprites(BlockState state, Direction face) {
		return SPRITE_CACHES.get(face).getSprites(state);
	}

	@Unmodifiable
	public static Set<TextureAtlasSprite> calculateSprites(BlockState state, Direction face, Supplier<RandomSource> randomSupplier) {
		List<TextureAtlasSprite> sprites = new ReferenceArrayList<>();
		BakedModel model = MODELS.getBlockModel(state);
		try {
			for (Direction cullFace : CULL_FACES) {
				for (BakedQuad quad : model.getQuads(state, cullFace, randomSupplier.get())) {
					if (quad.getDirection() == face) {
						sprites.add(quad.getSprite());
					}
				}
			}
		} catch (Exception e) {
			//
		}
		return !sprites.isEmpty() ? Set.copyOf(sprites) : Set.of(model.getParticleIcon());
	}

	public static void clearCache() {
		for (SpriteCache cache : SPRITE_CACHES.values()) {
			cache.clear();
		}
	}

	private static class SpriteCache {
		private final Direction face;
		private final Reference2ObjectOpenHashMap<BlockState, Set<TextureAtlasSprite>> spritesMap = new Reference2ObjectOpenHashMap<>();
		private final Supplier<RandomSource> randomSupplier = new Supplier<>() {
			private final RandomSource random = RandomSource.create();

			@Override
			public RandomSource get() {
				// Use item rendering seed for consistency
				random.setSeed(42L);
				return random;
			}
		};
		private final StampedLock lock = new StampedLock();

		public SpriteCache(Direction face) {
			this.face = face;
		}

		@Unmodifiable
		public Set<TextureAtlasSprite> getSprites(BlockState state) {
			Set<TextureAtlasSprite> sprites;

			long optimisticReadStamp = lock.tryOptimisticRead();
			if (optimisticReadStamp != 0L) {
				try {
					// This map read could happen at the same time as a map write, so catch any exceptions.
					// This is safe due to the map implementation used, which is guaranteed to not mutate the map during
					// a read.
					sprites = spritesMap.get(state);
					if (sprites != null && lock.validate(optimisticReadStamp)) {
						return sprites;
					}
				} catch (Exception e) {
					//
				}
			}

			long readStamp = lock.readLock();
			try {
				sprites = spritesMap.get(state);
			} finally {
				lock.unlockRead(readStamp);
			}

			if (sprites == null) {
				long writeStamp = lock.writeLock();
				try {
					sprites = spritesMap.get(state);
					if (sprites == null) {
						sprites = calculateSprites(state, face, randomSupplier);
						spritesMap.put(state, sprites);
					}
				} finally {
					lock.unlockWrite(writeStamp);
				}
			}

			return sprites;
		}

		public void clear() {
			long writeStamp = lock.writeLock();
			try {
				spritesMap.clear();
			} finally {
				lock.unlockWrite(writeStamp);
			}
		}
	}
}
