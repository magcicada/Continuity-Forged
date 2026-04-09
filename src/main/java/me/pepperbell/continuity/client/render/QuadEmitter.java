package me.pepperbell.continuity.client.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

public class QuadEmitter extends MutableQuadView {
  private final List<BakedQuad> output;

  public QuadEmitter(List<BakedQuad> output) {
    this.output = output;
  }

  public QuadEmitter() {
    this(new ArrayList<>());
  }

  public QuadEmitter square(
      Direction face, float left, float bottom, float right, float top, float depth) {
    this.direction = face;
    this.cullFace = depth == 0 ? face : null;

    switch (face) {
      case DOWN -> {
        pos(0, left, depth, top);
        pos(1, left, depth, bottom);
        pos(2, right, depth, bottom);
        pos(3, right, depth, top);
      }
      case UP -> {
        pos(0, left, 1 - depth, bottom);
        pos(1, left, 1 - depth, top);
        pos(2, right, 1 - depth, top);
        pos(3, right, 1 - depth, bottom);
      }
      case NORTH -> {
        pos(0, right, top, depth);
        pos(1, right, bottom, depth);
        pos(2, left, bottom, depth);
        pos(3, left, top, depth);
      }
      case SOUTH -> {
        pos(0, left, top, 1 - depth);
        pos(1, left, bottom, 1 - depth);
        pos(2, right, bottom, 1 - depth);
        pos(3, right, top, 1 - depth);
      }
      case WEST -> {
        pos(0, depth, top, left);
        pos(1, depth, bottom, left);
        pos(2, depth, bottom, right);
        pos(3, depth, top, right);
      }
      case EAST -> {
        pos(0, 1 - depth, top, right);
        pos(1, 1 - depth, bottom, right);
        pos(2, 1 - depth, bottom, left);
        pos(3, 1 - depth, top, left);
      }
    }
    return this;
  }

  public void emit() {
    output.add(toBakedQuad());
  }

  public List<BakedQuad> getOutput() {
    return output;
  }

  public void clearOutput() {
    output.clear();
  }
}
