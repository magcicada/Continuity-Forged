package me.pepperbell.continuity.client.mixin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.pepperbell.continuity.client.mixinterface.SpriteExtension;
import me.pepperbell.continuity.client.resource.AtlasLoaderInitContext;
import me.pepperbell.continuity.client.resource.AtlasLoaderLoadContext;
import me.pepperbell.continuity.client.resource.SpriteLoaderLoadContext;
import me.pepperbell.continuity.client.resource.SpriteLoaderStitchContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.ResourceLocation;

@Mixin(SpriteLoader.class)
abstract class SpriteLoaderMixin {
	@Shadow
	@Final
	private ResourceLocation id;

	@ModifyArg(method = "load(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/Identifier;ILjava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", ordinal = 0), index = 0)
	private Supplier<List<Supplier<SpriteContents>>> continuity$modifySupplier(Supplier<List<Supplier<SpriteContents>>> supplier) {
		SpriteLoaderLoadContext context = SpriteLoaderLoadContext.THREAD_LOCAL.get();
		if (context != null) {
			CompletableFuture<@Nullable Set<Identifier>> extraIdsFuture = context.getExtraIdsFuture(id);
			SpriteLoaderLoadContext.EmissiveControl emissiveControl = context.getEmissiveControl(id);
			if (emissiveControl != null) {
				return () -> {
					AtlasLoaderInitContext.THREAD_LOCAL.set(extraIdsFuture::join);
					AtlasLoaderLoadContext.THREAD_LOCAL.set(emissiveControl::setEmissiveIdMap);
					List<Supplier<SpriteContents>> list = supplier.get();
					AtlasLoaderInitContext.THREAD_LOCAL.set(null);
					AtlasLoaderLoadContext.THREAD_LOCAL.set(null);
					return list;
				};
			}
			return () -> {
				AtlasLoaderInitContext.THREAD_LOCAL.set(extraIdsFuture::join);
				List<Supplier<SpriteContents>> list = supplier.get();
				AtlasLoaderInitContext.THREAD_LOCAL.set(null);
				return list;
			};
		}
		return supplier;
	}

	@ModifyArg(method = "load(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/Identifier;ILjava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenApply(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;", ordinal = 0), index = 0)
	private Function<List<SpriteContents>, SpriteLoader.StitchResult> continuity$modifyFunction(Function<List<SpriteContents>, SpriteLoader.StitchResult> function) {
		SpriteLoaderLoadContext context = SpriteLoaderLoadContext.THREAD_LOCAL.get();
		if (context != null) {
			SpriteLoaderLoadContext.EmissiveControl emissiveControl = context.getEmissiveControl(id);
			if (emissiveControl != null) {
				return spriteContentsList -> {
					Map<Identifier, Identifier> emissiveIdMap = emissiveControl.getEmissiveIdMap();
					if (emissiveIdMap != null) {
						SpriteLoaderStitchContext.THREAD_LOCAL.set(new SpriteLoaderStitchContext() {
							@Override
							public Map<Identifier, Identifier> getEmissiveIdMap() {
								return emissiveIdMap;
							}

							@Override
							public void markHasEmissives() {
								emissiveControl.markHasEmissives();
							}
						});
						SpriteLoader.StitchResult result = function.apply(spriteContentsList);
						SpriteLoaderStitchContext.THREAD_LOCAL.set(null);
						return result;
					}
					return function.apply(spriteContentsList);
				};
			}
		}
		return function;
	}

	@Inject(method = "stitch(Ljava/util/List;ILjava/util/concurrent/Executor;)Lnet/minecraft/client/texture/SpriteLoader$StitchResult;", at = @At("RETURN"))
	private void continuity$onReturnStitch(List<SpriteContents> spriteContentsList, int mipmapLevels, Executor executor, CallbackInfoReturnable<SpriteLoader.StitchResult> cir) {
		SpriteLoaderStitchContext context = SpriteLoaderStitchContext.THREAD_LOCAL.get();
		if (context != null) {
			Map<Identifier, Identifier> emissiveIdMap = context.getEmissiveIdMap();
			Map<Identifier, TextureAtlasSprite> sprites = cir.getReturnValue().regions();
			emissiveIdMap.forEach((id, emissiveId) -> {
				TextureAtlasSprite sprite = sprites.get(id);
				if (sprite != null) {
					TextureAtlasSprite emissiveSprite = sprites.get(emissiveId);
					if (emissiveSprite != null) {
						((SpriteExtension) sprite).continuity$setEmissiveSprite(emissiveSprite);
						context.markHasEmissives();
					}
				}
			});
		}
	}
}
