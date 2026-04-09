package me.pepperbell.continuity.api.client;

import me.pepperbell.continuity.impl.client.EmissiveSpriteApiImpl;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface EmissiveSpriteApi {
  static EmissiveSpriteApi get() {
    return EmissiveSpriteApiImpl.INSTANCE;
  }

  @Nullable
  TextureAtlasSprite getEmissiveSprite(TextureAtlasSprite sprite);
}
