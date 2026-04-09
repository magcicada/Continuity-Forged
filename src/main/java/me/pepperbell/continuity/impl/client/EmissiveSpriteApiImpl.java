package me.pepperbell.continuity.impl.client;

import me.pepperbell.continuity.api.client.EmissiveSpriteApi;
import me.pepperbell.continuity.client.mixinterface.SpriteExtension;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;

public final class EmissiveSpriteApiImpl implements EmissiveSpriteApi {
  public static final EmissiveSpriteApiImpl INSTANCE = new EmissiveSpriteApiImpl();

  @Override
  @Nullable
  public TextureAtlasSprite getEmissiveSprite(TextureAtlasSprite sprite) {
    return ((SpriteExtension) sprite).continuity$getEmissiveSprite();
  }
}
