package me.pepperbell.continuity.client.model;

import me.pepperbell.continuity.impl.client.ContinuityFeatureStatesImpl;
import me.pepperbell.continuity.client.render.MeshBuilder;

public class ModelObjectsContainer {
	public static final ThreadLocal<ModelObjectsContainer> THREAD_LOCAL = ThreadLocal.withInitial(ModelObjectsContainer::new);

	public final CtmBakedModel.CtmQuadTransform ctmQuadTransform = new CtmBakedModel.CtmQuadTransform();
	public final EmissiveBakedModel.EmissiveBlockQuadTransform emissiveBlockQuadTransform = new EmissiveBakedModel.EmissiveBlockQuadTransform();
	public final EmissiveBakedModel.EmissiveItemQuadTransform emissiveItemQuadTransform = new EmissiveBakedModel.EmissiveItemQuadTransform();

	public final ContinuityFeatureStatesImpl featureStates = new ContinuityFeatureStatesImpl();
	public final MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();

	public static ModelObjectsContainer get() {
		return THREAD_LOCAL.get();
	}
}
