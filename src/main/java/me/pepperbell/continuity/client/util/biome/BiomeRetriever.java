package me.pepperbell.continuity.client.util.biome;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;

public final class BiomeRetriever {
	@Nullable
	public static Biome getBiome(BlockAndTintGetter blockView, BlockPos pos) {
		try {
			return blockView.getBiome(pos).value();
		} catch (Exception e) {
			return null;
		}
	}
}
