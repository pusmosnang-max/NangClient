package com.nangclient;

import com.nangclient.module.Module;
import com.nangclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class NangClient implements ClientModInitializer {
	public static final String MOD_ID = "nangclient";

	@Override
	public void onInitializeClient() {
		Keys.register();
		ModuleManager.init();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (Keys.menu.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new ClientMenuScreen());
				}
			}
			ModuleManager.tick(client);
		});

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "hud"), (graphics, tickCounter) -> {
			Minecraft mc = Minecraft.getInstance();
			int y = 4;
			for (Module m : ModuleManager.all()) {
				if (!m.enabled) continue;
				for (String line : m.hudLines(mc)) {
					graphics.drawString(mc.font, line, 4, y, 0xFFFFFFFF);
					y += 10;
				}
			}
		});
	}
}
