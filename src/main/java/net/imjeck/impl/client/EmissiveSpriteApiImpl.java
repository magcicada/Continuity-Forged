package net.imjeck.impl.client;

import org.jetbrains.annotations.Nullable;

import net.imjeck.api.client.EmissiveSpriteApi;
import net.imjeck.client.mixinterface.SpriteExtension;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class EmissiveSpriteApiImpl implements EmissiveSpriteApi {
	public static final EmissiveSpriteApiImpl INSTANCE = new EmissiveSpriteApiImpl();

	@Override
	@Nullable
	public TextureAtlasSprite getEmissiveSprite(TextureAtlasSprite sprite) {
		return ((SpriteExtension) sprite).continuity_forged$getEmissiveSprite();
	}
}
