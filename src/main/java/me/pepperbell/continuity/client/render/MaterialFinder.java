package me.pepperbell.continuity.client.render;

public class MaterialFinder {
  private BlendMode blendMode = BlendMode.DEFAULT;
  private boolean emissive = false;
  private boolean disableDiffuse = false;
  private TriState ambientOcclusion = TriState.DEFAULT;

  public MaterialFinder blendMode(BlendMode blendMode) {
    this.blendMode = blendMode;
    return this;
  }

  public MaterialFinder emissive(boolean emissive) {
    this.emissive = emissive;
    return this;
  }

  public MaterialFinder disableDiffuse(boolean disableDiffuse) {
    this.disableDiffuse = disableDiffuse;
    return this;
  }

  public MaterialFinder ambientOcclusion(TriState ambientOcclusion) {
    this.ambientOcclusion = ambientOcclusion;
    return this;
  }

  public RenderMaterial find() {
    RenderMaterial material =
        new RenderMaterial(blendMode, emissive, disableDiffuse, ambientOcclusion);
    return material;
  }

  public MaterialFinder clear() {
    blendMode = BlendMode.DEFAULT;
    emissive = false;
    disableDiffuse = false;
    ambientOcclusion = TriState.DEFAULT;
    return this;
  }
}
