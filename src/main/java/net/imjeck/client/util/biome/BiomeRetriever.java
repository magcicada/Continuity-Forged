package net.imjeck.client.util.biome;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

public final class BiomeRetriever {
	@Nullable
	public static Biome getBiome(BlockAndTintGetter blockView, BlockPos pos) {
		try {
			if (blockView instanceof LevelReader levelReader) {
				return levelReader.getBiome(pos).value();
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}
}
