package net.imjeck.client.util;

import org.jetbrains.annotations.Nullable;

import net.imjeck.client.render.BlendMode;
import net.imjeck.client.render.MaterialFinder;
import net.imjeck.client.render.RenderMaterial;
import net.imjeck.client.render.SpriteFinder;
import net.imjeck.client.render.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class RenderUtil {
	private static final BlockColors BLOCK_COLORS = Minecraft.getInstance().getBlockColors();
	private static final ModelManager MODEL_MANAGER = Minecraft.getInstance().getModelManager();

	private static final ThreadLocal<MaterialFinder> MATERIAL_FINDER = ThreadLocal.withInitial(MaterialFinder::new);

	private static SpriteFinder blockAtlasSpriteFinder;

	public static int getTintColor(@Nullable BlockState state, BlockAndTintGetter blockView, BlockPos pos, int tintIndex) {
		if (state == null || tintIndex == -1) {
			return -1;
		}
		return 0xFF000000 | BLOCK_COLORS.getColor(state, blockView, pos, tintIndex);
	}

	public static RenderMaterial findOverlayMaterial(BlendMode blendMode, @Nullable BlockState tintBlock) {
		MaterialFinder finder = getMaterialFinder();
		finder.blendMode(blendMode);
		if (tintBlock != null) {
			finder.ambientOcclusion(TriState.of(canHaveAO(tintBlock)));
		} else {
			finder.ambientOcclusion(TriState.TRUE);
		}
		return finder.find();
	}

	public static boolean canHaveAO(BlockState state) {
		return state.getLightEmission() == 0;
	}

	public static MaterialFinder getMaterialFinder() {
		return MATERIAL_FINDER.get().clear();
	}

	public static SpriteFinder getSpriteFinder() {
		return blockAtlasSpriteFinder;
	}

	public static class ReloadListener {
		public static void init() {
			// In Forge, reload is triggered via model baking events
		}

		public static void reload() {
			blockAtlasSpriteFinder = SpriteFinder.get(MODEL_MANAGER.getAtlas(TextureAtlas.LOCATION_BLOCKS));
		}
	}
}
