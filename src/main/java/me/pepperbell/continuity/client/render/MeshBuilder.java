package me.pepperbell.continuity.client.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

public class MeshBuilder {
	private final List<BakedQuad> quads = new ArrayList<>();
	private final QuadEmitter emitter = new QuadEmitter(quads);

	public QuadEmitter getEmitter() {
		return emitter;
	}

	public Mesh build() {
		Mesh mesh = new Mesh(quads);
		quads.clear();
		return mesh;
	}
}
