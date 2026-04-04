package me.pepperbell.continuity.client.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

public class RenderContext {
	private final Deque<QuadTransform> transformStack = new ArrayDeque<>();
	private final QuadEmitter emitter;
	private final List<BakedQuad> processedQuads = new ArrayList<>();

	public RenderContext() {
		this.emitter = new QuadEmitter(processedQuads);
	}

	public void pushTransform(QuadTransform transform) {
		transformStack.push(transform);
	}

	public void popTransform() {
		transformStack.poll();
	}

	public QuadEmitter getEmitter() {
		return emitter;
	}

	public boolean isFaceCulled(@Nullable Direction face) {
		return false;
	}

	public List<BakedQuad> processQuads(List<BakedQuad> inputQuads, @Nullable Direction cullFace) {
		processedQuads.clear();
		MutableQuadView mqv = new MutableQuadView();

		for (BakedQuad quad : inputQuads) {
			mqv.fromBakedQuad(quad, cullFace);
			boolean keep = true;
			for (QuadTransform transform : transformStack) {
				if (!transform.transform(mqv)) {
					keep = false;
					break;
				}
			}
			if (keep) {
				processedQuads.add(mqv.toBakedQuad());
			}
		}

		List<BakedQuad> result = new ArrayList<>(processedQuads);
		processedQuads.clear();
		return result;
	}

	public interface QuadTransform {
		boolean transform(MutableQuadView quad);
	}
}
