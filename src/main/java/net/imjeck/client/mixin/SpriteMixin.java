package net.imjeck.client.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.imjeck.client.mixinterface.SpriteExtension;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Mixin(TextureAtlasSprite.class)
abstract class SpriteMixin implements SpriteExtension {
	@Unique
	private TextureAtlasSprite continuity_forged$emissiveSprite;

	@Override
	@Nullable
	public TextureAtlasSprite continuity_forged$getEmissiveSprite() {
		return continuity_forged$emissiveSprite;
	}

	@Override
	public void continuity_forged$setEmissiveSprite(TextureAtlasSprite sprite) {
		continuity_forged$emissiveSprite = sprite;
	}
}
