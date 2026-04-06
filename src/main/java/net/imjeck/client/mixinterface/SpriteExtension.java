package net.imjeck.client.mixinterface;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface SpriteExtension {
	@Nullable
	TextureAtlasSprite continuity_forged$getEmissiveSprite();

	void continuity_forged$setEmissiveSprite(TextureAtlasSprite sprite);
}
