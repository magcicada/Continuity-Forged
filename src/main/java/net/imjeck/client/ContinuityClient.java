package net.imjeck.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.imjeck.api.client.CachingPredicates;
import net.imjeck.api.client.CtmLoader;
import net.imjeck.api.client.CtmLoaderRegistry;
import net.imjeck.api.client.CtmProperties;
import net.imjeck.api.client.QuadProcessor;
import net.imjeck.client.processor.BaseCachingPredicates;
import net.imjeck.client.processor.CompactCtmQuadProcessor;
import net.imjeck.client.processor.ProcessingDataKeys;
import net.imjeck.client.processor.TopQuadProcessor;
import net.imjeck.client.processor.overlay.SimpleOverlayQuadProcessor;
import net.imjeck.client.processor.overlay.StandardOverlayQuadProcessor;
import net.imjeck.client.processor.simple.CtmSpriteProvider;
import net.imjeck.client.processor.simple.FixedSpriteProvider;
import net.imjeck.client.processor.simple.HorizontalSpriteProvider;
import net.imjeck.client.processor.simple.HorizontalVerticalSpriteProvider;
import net.imjeck.client.processor.simple.RandomSpriteProvider;
import net.imjeck.client.processor.simple.RepeatSpriteProvider;
import net.imjeck.client.processor.simple.SimpleQuadProcessor;
import net.imjeck.client.processor.simple.VerticalHorizontalSpriteProvider;
import net.imjeck.client.processor.simple.VerticalSpriteProvider;
import net.imjeck.client.properties.BaseCtmProperties;
import net.imjeck.client.properties.CompactConnectingCtmProperties;
import net.imjeck.client.properties.ConnectingCtmProperties;
import net.imjeck.client.properties.OrientedConnectingCtmProperties;
import net.imjeck.client.properties.PropertiesParsingHelper;
import net.imjeck.client.properties.RandomCtmProperties;
import net.imjeck.client.properties.RepeatCtmProperties;
import net.imjeck.client.properties.TileAmountValidator;
import net.imjeck.client.properties.overlay.BaseOverlayCtmProperties;
import net.imjeck.client.properties.overlay.OrientedConnectingOverlayCtmProperties;
import net.imjeck.client.properties.overlay.RandomOverlayCtmProperties;
import net.imjeck.client.properties.overlay.RepeatOverlayCtmProperties;
import net.imjeck.client.properties.overlay.StandardOverlayCtmProperties;
import net.imjeck.client.resource.CustomBlockLayers;
import net.imjeck.client.resource.ModelWrappingHandler;
import net.imjeck.client.util.RenderUtil;
import net.imjeck.client.util.biome.BiomeHolderManager;
import net.imjeck.impl.client.ProcessingDataKeyRegistryImpl;
import java.nio.file.Path;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ContinuityClient.ID)
public class ContinuityClient {
	public static final String ID = "continuity_forged";
	public static final String NAME = "Continuity";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public ContinuityClient() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::onClientSetup);
		modEventBus.addListener(this::onAddPackFinders);
		modEventBus.addListener(this::onModifyBakingResult);

		// Register loaders immediately so they are available during resource reload
		registerLoaders();
	}

	private void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
		ModelWrappingHandler.onModifyBakingResult(event);
	}

	private void onAddPackFinders(AddPackFindersEvent event) {
		if (event.getPackType() == PackType.CLIENT_RESOURCES) {
			registerBuiltinResourcePack(event, "default",
				Component.translatable("resourcePack.continuity_forged.default.name"));
			registerBuiltinResourcePack(event, "glass_pane_culling_fix",
				Component.translatable("resourcePack.continuity_forged.glass_pane_culling_fix.name"));
		}
	}

	private static void registerBuiltinResourcePack(AddPackFindersEvent event, String packName, Component title) {
		try {
			Path path = ModList.get().getModFileById(ID).getFile().findResource("resourcepacks", packName);
			Pack pack = Pack.readMetaAndCreate(
				"builtin/continuity_forged/" + packName,
				title,
				false,
				packId -> new PathPackResources(packId, path, false),
				PackType.CLIENT_RESOURCES,
				Pack.Position.TOP,
				PackSource.BUILT_IN
			);
			if (pack != null) {
				event.addRepositorySource(consumer -> consumer.accept(pack));
			} else {
				LOGGER.error("Failed to read metadata for built-in resource pack: {}", packName);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to register built-in resource pack: {}", packName, e);
		}
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ProcessingDataKeys.init();
			ProcessingDataKeyRegistryImpl.INSTANCE.init();
			BiomeHolderManager.init();
			RenderUtil.ReloadListener.init();
			CustomBlockLayers.ReloadListener.init();
		});
	}

	private void registerLoaders() {
		CtmLoaderRegistry registry = CtmLoaderRegistry.get();
		CtmLoader<?> loader;

		// Standard simple methods

		loader = createLoader(
				OrientedConnectingCtmProperties::new,
				new TileAmountValidator.AtLeast<>(47),
				new SimpleQuadProcessor.Factory<>(new CtmSpriteProvider.Factory())
		);
		registry.registerLoader("ctm", loader);
		registry.registerLoader("glass", loader);

		loader = createLoader(
				CompactConnectingCtmProperties::new,
				new TileAmountValidator.AtLeast<>(5),
				new CompactCtmQuadProcessor.Factory(),
				false
		);
		registry.registerLoader("ctm_compact", loader);

		loader = createLoader(
				OrientedConnectingCtmProperties::new,
				new TileAmountValidator.Exactly<>(4),
				new SimpleQuadProcessor.Factory<>(new HorizontalSpriteProvider.Factory())
		);
		registry.registerLoader("horizontal", loader);
		registry.registerLoader("bookshelf", loader);

		loader = createLoader(
				OrientedConnectingCtmProperties::new,
				new TileAmountValidator.Exactly<>(4),
				new SimpleQuadProcessor.Factory<>(new VerticalSpriteProvider.Factory())
		);
		registry.registerLoader("vertical", loader);

		loader = createLoader(
				OrientedConnectingCtmProperties::new,
				new TileAmountValidator.Exactly<>(7),
				new SimpleQuadProcessor.Factory<>(new HorizontalVerticalSpriteProvider.Factory())
		);
		registry.registerLoader("horizontal+vertical", loader);
		registry.registerLoader("h+v", loader);

		loader = createLoader(
				OrientedConnectingCtmProperties::new,
				new TileAmountValidator.Exactly<>(7),
				new SimpleQuadProcessor.Factory<>(new VerticalHorizontalSpriteProvider.Factory())
		);
		registry.registerLoader("vertical+horizontal", loader);
		registry.registerLoader("v+h", loader);

		loader = createLoader(
				ConnectingCtmProperties::new,
				new TileAmountValidator.Exactly<>(1),
				new TopQuadProcessor.Factory()
		);
		registry.registerLoader("top", loader);

		loader = createLoader(
				RandomCtmProperties::new,
				new SimpleQuadProcessor.Factory<>(new RandomSpriteProvider.Factory())
		);
		registry.registerLoader("random", loader);

		loader = createLoader(
				RepeatCtmProperties::new,
				new RepeatCtmProperties.Validator<>(),
				new SimpleQuadProcessor.Factory<>(new RepeatSpriteProvider.Factory())
		);
		registry.registerLoader("repeat", loader);

		loader = createLoader(
				BaseCtmProperties::new,
				new TileAmountValidator.Exactly<>(1),
				new SimpleQuadProcessor.Factory<>(new FixedSpriteProvider.Factory())
		);
		registry.registerLoader("fixed", loader);

		// Standard overlay methods

		loader = createLoader(
				StandardOverlayCtmProperties::new,
				new TileAmountValidator.AtLeast<>(17),
				new StandardOverlayQuadProcessor.Factory()
		);
		registry.registerLoader("overlay", loader);

		loader = createLoader(
				OrientedConnectingOverlayCtmProperties::new,
				new TileAmountValidator.AtLeast<>(47),
				new SimpleOverlayQuadProcessor.Factory<>(new CtmSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_ctm", loader);

		loader = createLoader(
				RandomOverlayCtmProperties::new,
				new SimpleOverlayQuadProcessor.Factory<>(new RandomSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_random", loader);

		loader = createLoader(
				RepeatOverlayCtmProperties::new,
				new RepeatCtmProperties.Validator<>(),
				new SimpleOverlayQuadProcessor.Factory<>(new RepeatSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_repeat", loader);

		loader = createLoader(
				BaseOverlayCtmProperties::new,
				new TileAmountValidator.Exactly<>(1),
				new SimpleOverlayQuadProcessor.Factory<>(new FixedSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_fixed", loader);

		// Custom methods

		loader = createCustomLoader(
				OrientedConnectingOverlayCtmProperties::new,
				new TileAmountValidator.Exactly<>(4),
				new SimpleOverlayQuadProcessor.Factory<>(new HorizontalSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_horizontal", loader);

		loader = createCustomLoader(
				OrientedConnectingOverlayCtmProperties::new,
				new TileAmountValidator.Exactly<>(4),
				new SimpleOverlayQuadProcessor.Factory<>(new VerticalSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_vertical", loader);

		loader = createCustomLoader(
				OrientedConnectingOverlayCtmProperties::new,
				new TileAmountValidator.Exactly<>(7),
				new SimpleOverlayQuadProcessor.Factory<>(new HorizontalVerticalSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_horizontal+vertical", loader);
		registry.registerLoader("overlay_h+v", loader);

		loader = createCustomLoader(
				OrientedConnectingOverlayCtmProperties::new,
				new TileAmountValidator.Exactly<>(7),
				new SimpleOverlayQuadProcessor.Factory<>(new VerticalHorizontalSpriteProvider.Factory())
		);
		registry.registerLoader("overlay_vertical+horizontal", loader);
		registry.registerLoader("overlay_v+h", loader);
	}

	private static <T extends CtmProperties> CtmLoader<T> createLoader(CtmProperties.Factory<T> propertiesFactory, QuadProcessor.Factory<T> processorFactory, CachingPredicates.Factory<T> predicatesFactory) {
		return new CtmLoader<>() {
			@Override
			public CtmProperties.Factory<T> getPropertiesFactory() {
				return propertiesFactory;
			}

			@Override
			public QuadProcessor.Factory<T> getProcessorFactory() {
				return processorFactory;
			}

			@Override
			public CachingPredicates.Factory<T> getPredicatesFactory() {
				return predicatesFactory;
			}
		};
	}

	private static <T extends BaseCtmProperties> CtmLoader<T> createLoader(CtmProperties.Factory<T> propertiesFactory, TileAmountValidator<T> validator, QuadProcessor.Factory<T> processorFactory, boolean isValidForMultipass) {
		return createLoader(wrapWithOptifineOnlyCheck(TileAmountValidator.wrapFactory(BaseCtmProperties.wrapFactory(propertiesFactory), validator)), processorFactory, new BaseCachingPredicates.Factory<>(isValidForMultipass));
	}

	private static <T extends BaseCtmProperties> CtmLoader<T> createLoader(CtmProperties.Factory<T> propertiesFactory, TileAmountValidator<T> validator, QuadProcessor.Factory<T> processorFactory) {
		return createLoader(propertiesFactory, validator, processorFactory, true);
	}

	private static <T extends BaseCtmProperties> CtmLoader<T> createLoader(CtmProperties.Factory<T> propertiesFactory, QuadProcessor.Factory<T> processorFactory, boolean isValidForMultipass) {
		return createLoader(wrapWithOptifineOnlyCheck(BaseCtmProperties.wrapFactory(propertiesFactory)), processorFactory, new BaseCachingPredicates.Factory<>(isValidForMultipass));
	}

	private static <T extends BaseCtmProperties> CtmLoader<T> createLoader(CtmProperties.Factory<T> propertiesFactory, QuadProcessor.Factory<T> processorFactory) {
		return createLoader(propertiesFactory, processorFactory, true);
	}

	private static <T extends BaseCtmProperties> CtmLoader<T> createCustomLoader(CtmProperties.Factory<T> propertiesFactory, TileAmountValidator<T> validator, QuadProcessor.Factory<T> processorFactory, boolean isValidForMultipass) {
		return createLoader(TileAmountValidator.wrapFactory(BaseCtmProperties.wrapFactory(propertiesFactory), validator), processorFactory, new BaseCachingPredicates.Factory<>(isValidForMultipass));
	}

	private static <T extends BaseCtmProperties> CtmLoader<T> createCustomLoader(CtmProperties.Factory<T> propertiesFactory, TileAmountValidator<T> validator, QuadProcessor.Factory<T> processorFactory) {
		return createCustomLoader(propertiesFactory, validator, processorFactory, true);
	}

	private static <T extends CtmProperties> CtmProperties.Factory<T> wrapWithOptifineOnlyCheck(CtmProperties.Factory<T> factory) {
		return (properties, resourceId, pack, packPriority, resourceManager, method) -> {
			if (PropertiesParsingHelper.parseOptifineOnly(properties, resourceId)) {
				return null;
			}
			return factory.createProperties(properties, resourceId, pack, packPriority, resourceManager, method);
		};
	}

	public static ResourceLocation asId(String path) {
		return new ResourceLocation(ID, path);
	}
}
