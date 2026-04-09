package me.pepperbell.continuity.client.config;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public class ModMenuApiImpl {
  public static void register() {
    ModLoadingContext.get()
        .registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () ->
                new ConfigScreenHandler.ConfigScreenFactory(
                    (mc, parent) -> new ContinuityConfigScreen(parent, ContinuityConfig.INSTANCE)));
  }
}
