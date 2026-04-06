package net.imjeck.client.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

public class Mesh {
	private final List<BakedQuad> quads;

	public Mesh(List<BakedQuad> quads) {
		this.quads = Collections.unmodifiableList(new ArrayList<>(quads));
	}

	public void outputTo(QuadEmitter emitter) {
		for (BakedQuad quad : quads) {
			emitter.fromBakedQuad(quad, null);
			emitter.emit();
		}
	}

	public List<BakedQuad> getQuads() {
		return quads;
	}
}
