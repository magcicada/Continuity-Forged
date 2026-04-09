package me.pepperbell.continuity.client.render;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SpriteFinder {
  private final TextureAtlas atlas;

  private SpriteFinder(TextureAtlas atlas) {
    this.atlas = atlas;
  }

  public static SpriteFinder get(TextureAtlas atlas) {
    return new SpriteFinder(atlas);
  }

  public TextureAtlasSprite find(QuadView quad) {
    // The sprite is already stored in BakedQuad, return it directly
    TextureAtlasSprite sprite = quad.getSprite();
    if (sprite != null) {
      return sprite;
    }
    // Fallback: try to find sprite from UV coordinates
    float u = quad.u(0);
    float v = quad.v(0);
    return atlas.getSprite(findTextureLocation(u, v));
  }

  private net.minecraft.resources.ResourceLocation findTextureLocation(float u, float v) {
    // Simple fallback - return missing texture
    return net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation();
  }
}
