package me.pepperbell.continuity.client.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class QuadView {
  protected static final int VERTEX_STRIDE = 8;
  protected int[] vertexData;
  protected int tintIndex;
  protected Direction direction;
  protected TextureAtlasSprite sprite;
  protected boolean shade;
  protected Direction cullFace;
  protected RenderMaterial material;

  public QuadView() {
    this.vertexData = new int[32];
    this.material = new RenderMaterial(BlendMode.DEFAULT, false, false, TriState.DEFAULT);
  }

  public void fromBakedQuad(BakedQuad quad, Direction cullFace) {
    System.arraycopy(quad.getVertices(), 0, this.vertexData, 0, 32);
    this.tintIndex = quad.getTintIndex();
    this.direction = quad.getDirection();
    this.sprite = quad.getSprite();
    this.shade = quad.isShade();
    this.cullFace = cullFace;
  }

  public float posByIndex(int vertexIndex, int coordinateIndex) {
    return Float.intBitsToFloat(vertexData[vertexIndex * VERTEX_STRIDE + coordinateIndex]);
  }

  public float x(int vertexIndex) {
    return Float.intBitsToFloat(vertexData[vertexIndex * VERTEX_STRIDE]);
  }

  public float y(int vertexIndex) {
    return Float.intBitsToFloat(vertexData[vertexIndex * VERTEX_STRIDE + 1]);
  }

  public float z(int vertexIndex) {
    return Float.intBitsToFloat(vertexData[vertexIndex * VERTEX_STRIDE + 2]);
  }

  public int color(int vertexIndex) {
    return vertexData[vertexIndex * VERTEX_STRIDE + 3];
  }

  public float u(int vertexIndex) {
    return Float.intBitsToFloat(vertexData[vertexIndex * VERTEX_STRIDE + 4]);
  }

  public float v(int vertexIndex) {
    return Float.intBitsToFloat(vertexData[vertexIndex * VERTEX_STRIDE + 5]);
  }

  public int lightmap(int vertexIndex) {
    return vertexData[vertexIndex * VERTEX_STRIDE + 6];
  }

  public int normal(int vertexIndex) {
    return vertexData[vertexIndex * VERTEX_STRIDE + 7];
  }

  public boolean hasNormal(int vertexIndex) {
    return (vertexData[vertexIndex * VERTEX_STRIDE + 7] & 0xFFFFFF) != 0;
  }

  public float normalX(int vertexIndex) {
    return ((byte) (vertexData[vertexIndex * VERTEX_STRIDE + 7])) / 127.0f;
  }

  public float normalY(int vertexIndex) {
    return ((byte) (vertexData[vertexIndex * VERTEX_STRIDE + 7] >> 8)) / 127.0f;
  }

  public float normalZ(int vertexIndex) {
    return ((byte) (vertexData[vertexIndex * VERTEX_STRIDE + 7] >> 16)) / 127.0f;
  }

  public Direction lightFace() {
    return direction;
  }

  public Direction cullFace() {
    return cullFace;
  }

  public TextureAtlasSprite getSprite() {
    return sprite;
  }

  public int tintIndex() {
    return tintIndex;
  }

  public boolean shade() {
    return shade;
  }

  public RenderMaterial material() {
    return material;
  }

  public BakedQuad toBakedQuad() {
    int[] data = new int[32];
    System.arraycopy(vertexData, 0, data, 0, 32);
    return new BakedQuad(data, tintIndex, direction, sprite, shade);
  }
}
