package com.nangclient;

import com.nangclient.module.HudModule;
import com.nangclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class NangClient implements ClientModInitializer {
	public static final String MOD_ID = "nangclient";

	@Override
	public void onInitializeClient() {
		Keys.register();
		ModuleManager.init();
		Config.load();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (Keys.menu.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new ClientMenuScreen(null));
				}
			}
			ModuleManager.tick(client);
		});

		// "NangClient" button in the ESC (pause) menu
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof PauseScreen) {
				Screens.getButtons(screen).add(
						Button.builder(
								Component.literal("NangClient").withStyle(ChatFormatting.AQUA),
								button -> client.setScreen(new ClientMenuScreen(screen)))
								.bounds(scaledWidth / 2 - 100, 8, 200, 20)
								.build());
			}
		});

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "hud"), (graphics, tickCounter) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.options.hideGui || mc.screen instanceof HudEditorScreen) return;
			for (HudModule m : ModuleManager.hud()) {
				if (m.enabled) m.render(graphics, mc, false);
			}
		});
	}
}
