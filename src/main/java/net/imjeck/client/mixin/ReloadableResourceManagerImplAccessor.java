package net.imjeck.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

@Mixin(ReloadableResourceManager.class)
public interface ReloadableResourceManagerImplAccessor {
	@Accessor("resources")
	CloseableResourceManager getActiveManager();
}
