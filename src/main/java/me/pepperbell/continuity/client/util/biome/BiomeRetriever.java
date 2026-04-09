package me.pepperbell.continuity.client.util.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

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
