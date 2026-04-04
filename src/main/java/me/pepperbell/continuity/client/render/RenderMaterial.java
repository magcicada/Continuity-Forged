package me.pepperbell.continuity.client.render;

public class RenderMaterial {
	private final BlendMode blendMode;
	private final boolean emissive;
	private final boolean disableDiffuse;
	private final TriState ambientOcclusion;

	public RenderMaterial(BlendMode blendMode, boolean emissive, boolean disableDiffuse, TriState ambientOcclusion) {
		this.blendMode = blendMode;
		this.emissive = emissive;
		this.disableDiffuse = disableDiffuse;
		this.ambientOcclusion = ambientOcclusion;
	}

	public BlendMode blendMode() {
		return blendMode;
	}

	public boolean emissive() {
		return emissive;
	}

	public boolean disableDiffuse() {
		return disableDiffuse;
	}

	public TriState ambientOcclusion() {
		return ambientOcclusion;
	}
}
