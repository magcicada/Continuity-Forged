package me.pepperbell.continuity.client.util.biome;

import org.jetbrains.annotations.Nullable;

import net.minecraftforge.fml.ModList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;

// TODO: Inline this class and always use the API once Canvas properly supports it.
public final class BiomeRetriever {
	private static final Provider PROVIDER = createProvider();

	private static Provider createProvider() {
		ClassLoader classLoader = BiomeRetriever.class.getClassLoader();

		if (ModList.get().isLoaded("canvas")) {
			try {
				Class<?> inputRegionClass = Class.forName("grondag.canvas.terrain.region.input.InputRegion", false, classLoader);
				inputRegionClass.getMethod("getBiome", BlockPos.class);
				return BiomeRetriever::getBiomeByInputRegion;
			} catch (ClassNotFoundException | NoSuchMethodException e) {
				//
			}
		}

		return BiomeRetriever::getBiomeByAPI;
	}

	@Nullable
	public static Biome getBiome(BlockAndTintGetter blockView, BlockPos pos) {
		return PROVIDER.getBiome(blockView, pos);
	}

	@Nullable
	private static Biome getBiomeByAPI(BlockAndTintGetter blockView, BlockPos pos) {
		try {
			return blockView.getBiome(pos).value();
		} catch (Exception e) {
			return null;
		}
	}

	// Canvas
	@Nullable
	private static Biome getBiomeByInputRegion(BlockAndTintGetter blockView, BlockPos pos) {
		try {
			if (blockView instanceof grondag.canvas.terrain.region.input.InputRegion inputRegion) {
				return inputRegion.getBiome(pos);
			}
		} catch (Exception e) {
			//
		}
		return getBiomeByAPI(blockView, pos);
	}

	private interface Provider {
		@Nullable
		Biome getBiome(BlockAndTintGetter blockView, BlockPos pos);
	}
}
