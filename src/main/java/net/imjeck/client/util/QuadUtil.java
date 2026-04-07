package net.imjeck.client.util;

import net.imjeck.client.render.RenderMaterial;
import net.imjeck.client.render.MutableQuadView;
import net.imjeck.client.render.QuadEmitter;
import net.imjeck.client.render.QuadView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class QuadUtil {
	public static void interpolate(MutableQuadView quad, TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite) {
		float oldMinU = oldSprite.getU0();
		float oldMinV = oldSprite.getV0();
		float newMinU = newSprite.getU0();
		float newMinV = newSprite.getV0();
		float uFactor = (newSprite.getU1() - newMinU) / (oldSprite.getU1() - oldMinU);
		float vFactor = (newSprite.getV1() - newMinV) / (oldSprite.getV1() - oldMinV);
		for (int i = 0; i < 4; i++) {
			quad.uv(i,
					newMinU + (quad.u(i) - oldMinU) * uFactor,
					newMinV + (quad.v(i) - oldMinV) * vFactor
			);
		}
		quad.sprite(newSprite);
	}

	public static void assignLerpedUvs(MutableQuadView quad, TextureAtlasSprite sprite) {
		float delta = sprite.uvShrinkRatio();
		float centerU = (sprite.getU0() + sprite.getU1()) * 0.5f;
		float centerV = (sprite.getV0() + sprite.getV1()) * 0.5f;
		float lerpedMinU = Mth.lerp(delta, sprite.getU0(), centerU);
		float lerpedMaxU = Mth.lerp(delta, sprite.getU1(), centerU);
		float lerpedMinV = Mth.lerp(delta, sprite.getV0(), centerV);
		float lerpedMaxV = Mth.lerp(delta, sprite.getV1(), centerV);
		quad.uv(0, lerpedMinU, lerpedMinV);
		quad.uv(1, lerpedMinU, lerpedMaxV);
		quad.uv(2, lerpedMaxU, lerpedMaxV);
		quad.uv(3, lerpedMaxU, lerpedMinV);
	}

	public static void emitOverlayQuad(QuadEmitter emitter, Direction face, TextureAtlasSprite sprite, int color, RenderMaterial material) {
		emitter.square(face, 0, 0, 1, 1, 0);
		// Convert ARGB (from BlockColors) to vertex data format (ABGR byte order)
		int vertexColor = (color & 0xFF00FF00) | ((color & 0xFF) << 16) | ((color >> 16) & 0xFF);
		emitter.color(vertexColor, vertexColor, vertexColor, vertexColor);
		assignLerpedUvs(emitter, sprite);
		emitter.material(material);
		emitter.sprite(sprite);
		emitter.tintIndex(-1);
		// Match OptiFine: shade=true enables AO and face-dependent dimming
		emitter.shade(true);
		// Set face normal for all vertices (required for correct lighting)
		float nx = face.getStepX();
		float ny = face.getStepY();
		float nz = face.getStepZ();
		for (int i = 0; i < 4; i++) {
			emitter.normal(i, nx, ny, nz);
		}
		emitter.emit();
	}

	public static boolean isQuadUnitSquare(QuadView quad) {
		int indexA;
		int indexB;
		int indexPerp;
		switch (quad.lightFace().getAxis()) {
			case X:
				indexA = 1;
				indexB = 2;
				indexPerp = 0;
				break;
			case Y:
				indexA = 0;
				indexB = 2;
				indexPerp = 1;
				break;
			case Z:
				indexA = 1;
				indexB = 0;
				indexPerp = 2;
				break;
			default:
				return false;
		}

		// Planarity check: all vertices must share the same depth (perpendicular) coordinate.
		// A slope or angled face will have varying depth values even if its in-plane
		// coordinates are all at the unit corners.
		float perp0 = quad.posByIndex(0, indexPerp);
		for (int i = 1; i < 4; i++) {
			if (Math.abs(quad.posByIndex(i, indexPerp) - perp0) > 0.0001f) {
				return false;
			}
		}

		// Allow shell expansion up to 0.001 for anti-z-fighting bilayer models
		final float epsilon = 0.001f;
		for (int i = 0; i < 4; i++) {
			float a = quad.posByIndex(i, indexA);
			if ((a >= epsilon || a <= -epsilon) && (a >= 1 + epsilon || a <= 1 - epsilon)) {
				return false;
			}
			float b = quad.posByIndex(i, indexB);
			if ((b >= epsilon || b <= -epsilon) && (b >= 1 + epsilon || b <= 1 - epsilon)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns an int in range [0, 7] representing the texture orientation of the given quad relative to the world.
	 *
	 * <ul>
	 *     <li>0 - 0 degree counterclockwise rotation, counterclockwise UV winding order</li>
	 *     <li>1 - 90 degree counterclockwise rotation, counterclockwise UV winding order</li>
	 *     <li>2 - 180 degree counterclockwise rotation, counterclockwise UV winding order</li>
	 *     <li>3 - 270 degree counterclockwise rotation, counterclockwise UV winding order</li>
	 *     <li>4 - 0 degree counterclockwise rotation, clockwise UV winding order</li>
	 *     <li>5 - 90 degree counterclockwise rotation, clockwise UV winding order</li>
	 *     <li>6 - 180 degree counterclockwise rotation, clockwise UV winding order</li>
	 *     <li>7 - 270 degree counterclockwise rotation, clockwise UV winding order</li>
	 * </ul>
	 */
	public static int getTextureOrientation(QuadView quad) {
		// Texture matrix
		float tm00 = quad.u(3) - quad.u(1);
		float tm01 = quad.v(3) - quad.v(1);
		float tm10 = quad.u(2) - quad.u(0);
		float tm11 = quad.v(2) - quad.v(0);
		// Determinant of texture matrix; also cross product of its column vectors
		float determinant = tm00 * tm11 - tm10 * tm01;
		if (determinant == 0) {
			return 0;
		}
		float s = 1 / determinant;
		// Second column of inverse texture matrix
		float itm10 = -tm10 * s;
		float itm11 = tm00 * s;

		int xAxis;
		int xAxisSign;
		int yAxis;
		int yAxisSign;
		switch (quad.lightFace()) {
			case DOWN -> {
				xAxis = 0; // +X
				xAxisSign = 1;
				yAxis = 2; // +Z
				yAxisSign = 1;
			}
			case UP -> {
				xAxis = 0; // +X
				xAxisSign = 1;
				yAxis = 2; // -Z
				yAxisSign = -1;
			}
			case NORTH -> {
				xAxis = 0; // -X
				xAxisSign = -1;
				yAxis = 1; // +Y
				yAxisSign = 1;
			}
			case SOUTH -> {
				xAxis = 0; // +X
				xAxisSign = 1;
				yAxis = 1; // +Y
				yAxisSign = 1;
			}
			case WEST -> {
				xAxis = 2; // +Z
				xAxisSign = 1;
				yAxis = 1; // +Y
				yAxisSign = 1;
			}
			case EAST -> {
				xAxis = 2; // -Z
				xAxisSign = -1;
				yAxis = 1; // +Y
				yAxisSign = 1;
			}
			default -> {
				return 0;
			}
		}
		// Position matrix
		float pm00 = quad.posByIndex(3, xAxis) - quad.posByIndex(1, xAxis);
		float pm01 = quad.posByIndex(3, yAxis) - quad.posByIndex(1, yAxis);
		float pm10 = quad.posByIndex(2, xAxis) - quad.posByIndex(0, xAxis);
		float pm11 = quad.posByIndex(2, yAxis) - quad.posByIndex(0, yAxis);

		// Texture up vector in projected world space
		// Computed as (position matrix * inverse texture matrix * [0; -1]); [0; -1] is the texture up vector in texture space
		// Axis signs should be multiplied into position matrix values, but multiplying here instead saves 2 multiplications
		float x = -(pm00 * itm10 + pm10 * itm11) * xAxisSign;
		float y = -(pm01 * itm10 + pm11 * itm11) * yAxisSign;

		// Clamp vector to nearest axis-aligned direction
		// up/+y -> 0, left/-x -> 1, down/-y -> 2, right/+x -> 3
		// Add 4 if the UV winding order is clockwise
		return (Math.abs(y) >= Math.abs(x) ? (y > 0 ? 0 : 2) : (x > 0 ? 3 : 1)) + (determinant < 0 ? 4 : 0);
	}
}
