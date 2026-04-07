package net.imjeck.client.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class MutableQuadView extends QuadView {

	public MutableQuadView pos(int vertexIndex, float x, float y, float z) {
		vertexData[vertexIndex * VERTEX_STRIDE] = Float.floatToRawIntBits(x);
		vertexData[vertexIndex * VERTEX_STRIDE + 1] = Float.floatToRawIntBits(y);
		vertexData[vertexIndex * VERTEX_STRIDE + 2] = Float.floatToRawIntBits(z);
		return this;
	}

	public MutableQuadView color(int vertexIndex, int color) {
		vertexData[vertexIndex * VERTEX_STRIDE + 3] = color;
		return this;
	}

	public MutableQuadView color(int c0, int c1, int c2, int c3) {
		color(0, c0);
		color(1, c1);
		color(2, c2);
		color(3, c3);
		return this;
	}

	public MutableQuadView uv(int vertexIndex, float u, float v) {
		vertexData[vertexIndex * VERTEX_STRIDE + 4] = Float.floatToRawIntBits(u);
		vertexData[vertexIndex * VERTEX_STRIDE + 5] = Float.floatToRawIntBits(v);
		return this;
	}

	public MutableQuadView lightmap(int vertexIndex, int lightmap) {
		vertexData[vertexIndex * VERTEX_STRIDE + 6] = lightmap;
		return this;
	}

	public MutableQuadView normal(int vertexIndex, int normal) {
		vertexData[vertexIndex * VERTEX_STRIDE + 7] = normal;
		return this;
	}

	public MutableQuadView normal(int vertexIndex, float normalX, float normalY, float normalZ) {
		int nx = (int)(normalX * 127.0f) & 0xFF;
		int ny = (int)(normalY * 127.0f) & 0xFF;
		int nz = (int)(normalZ * 127.0f) & 0xFF;
		vertexData[vertexIndex * VERTEX_STRIDE + 7] = nx | (ny << 8) | (nz << 16);
		return this;
	}

	public MutableQuadView material(RenderMaterial material) {
		this.material = material;
		return this;
	}

	public MutableQuadView tintIndex(int tintIndex) {
		this.tintIndex = tintIndex;
		return this;
	}

	public MutableQuadView cullFace(Direction face) {
		this.cullFace = face;
		return this;
	}

	public MutableQuadView shade(boolean shade) {
		this.shade = shade;
		return this;
	}

	public MutableQuadView sprite(TextureAtlasSprite sprite) {
		this.sprite = sprite;
		return this;
	}

	public MutableQuadView copyFrom(QuadView source) {
		System.arraycopy(source.vertexData, 0, this.vertexData, 0, 32);
		this.tintIndex = source.tintIndex;
		this.direction = source.direction;
		this.sprite = source.sprite;
		this.shade = source.shade;
		this.cullFace = source.cullFace;
		this.material = source.material;
		return this;
	}
}
