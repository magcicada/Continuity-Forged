package net.imjeck.client.model;

import net.imjeck.impl.client.ContinuityFeatureStatesImpl;
import net.imjeck.client.render.MeshBuilder;

public class ModelObjectsContainer {
	public static final ThreadLocal<ModelObjectsContainer> THREAD_LOCAL = ThreadLocal.withInitial(ModelObjectsContainer::new);

	public final CtmBakedModel.CtmQuadTransform ctmQuadTransform = new CtmBakedModel.CtmQuadTransform();
	public final EmissiveBakedModel.EmissiveBlockQuadTransform emissiveBlockQuadTransform = new EmissiveBakedModel.EmissiveBlockQuadTransform();
	public final EmissiveBakedModel.EmissiveItemQuadTransform emissiveItemQuadTransform = new EmissiveBakedModel.EmissiveItemQuadTransform();

	public final ContinuityFeatureStatesImpl featureStates = new ContinuityFeatureStatesImpl();
	public final MeshBuilder meshBuilder = new MeshBuilder();

	public static ModelObjectsContainer get() {
		return THREAD_LOCAL.get();
	}
}
