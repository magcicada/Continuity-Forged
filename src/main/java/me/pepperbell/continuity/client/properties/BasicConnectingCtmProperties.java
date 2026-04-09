package me.pepperbell.continuity.client.properties;

import java.util.Locale;
import java.util.Properties;
import me.pepperbell.continuity.client.ContinuityClient;
import me.pepperbell.continuity.client.processor.ConnectionPredicate;
import me.pepperbell.continuity.client.util.SpriteCalculator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class BasicConnectingCtmProperties extends BaseCtmProperties {
  protected ConnectionPredicate connectionPredicate;

  public BasicConnectingCtmProperties(
      Properties properties,
      ResourceLocation resourceId,
      PackResources pack,
      int packPriority,
      ResourceManager resourceManager,
      String method) {
    super(properties, resourceId, pack, packPriority, resourceManager, method);
  }

  @Override
  public void init() {
    super.init();
    parseConnect();
    detectConnect();
    validateConnect();
  }

  protected void parseConnect() {
    String connectStr = properties.getProperty("connect");
    if (connectStr == null) {
      return;
    }

    try {
      connectionPredicate = ConnectionType.valueOf(connectStr.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      ContinuityClient.LOGGER.warn(
          "Unknown 'connect' value '"
              + connectStr
              + "' in file '"
              + resourceId
              + "' in pack '"
              + packId
              + "'");
    }
  }

  protected void detectConnect() {
    if (connectionPredicate == null) {
      if (matchBlocksPredicate != null) {
        connectionPredicate = ConnectionType.BLOCK;
      } else if (matchTilesSet != null) {
        connectionPredicate = ConnectionType.TILE;
      }
    }
  }

  protected void validateConnect() {
    if (connectionPredicate == null) {
      ContinuityClient.LOGGER.error(
          "No valid connection type provided in file '"
              + resourceId
              + "' in pack '"
              + packId
              + "'");
      valid = false;
    }
  }

  public ConnectionPredicate getConnectionPredicate() {
    return connectionPredicate;
  }

  public enum ConnectionType implements ConnectionPredicate {
    BLOCK {
      @Override
      public boolean shouldConnect(
          BlockAndTintGetter blockView,
          BlockState appearanceState,
          BlockState state,
          BlockPos pos,
          BlockState otherAppearanceState,
          BlockState otherState,
          BlockPos otherPos,
          Direction face,
          TextureAtlasSprite quadSprite) {
        return appearanceState.getBlock() == otherAppearanceState.getBlock();
      }
    },
    TILE {
      @Override
      public boolean shouldConnect(
          BlockAndTintGetter blockView,
          BlockState appearanceState,
          BlockState state,
          BlockPos pos,
          BlockState otherAppearanceState,
          BlockState otherState,
          BlockPos otherPos,
          Direction face,
          TextureAtlasSprite quadSprite) {
        if (appearanceState == otherAppearanceState) {
          return true;
        }
        return SpriteCalculator.getSprites(otherAppearanceState, face).contains(quadSprite);
      }
    },
    STATE {
      @Override
      public boolean shouldConnect(
          BlockAndTintGetter blockView,
          BlockState appearanceState,
          BlockState state,
          BlockPos pos,
          BlockState otherAppearanceState,
          BlockState otherState,
          BlockPos otherPos,
          Direction face,
          TextureAtlasSprite quadSprite) {
        return appearanceState == otherAppearanceState;
      }
    },
    MATERIAL {
      @Override
      public boolean shouldConnect(
          BlockAndTintGetter blockView,
          BlockState appearanceState,
          BlockState state,
          BlockPos pos,
          BlockState otherAppearanceState,
          BlockState otherState,
          BlockPos otherPos,
          Direction face,
          TextureAtlasSprite quadSprite) {
        // In 1.20+, Material was removed. Use MapColor as the closest equivalent
        // for material-based connection matching (same visual material category).
        return appearanceState.getMapColor(blockView, pos)
            == otherAppearanceState.getMapColor(blockView, otherPos);
      }
    };
  }
}
