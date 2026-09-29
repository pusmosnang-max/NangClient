package com.nangclient;

import com.nangclient.module.Module;
import com.nangclient.module.ModuleManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** The module menu (press Right Shift in-game). Click a button to turn a module on or off. */
public class ClientMenuScreen extends Screen {
	public ClientMenuScreen() {
		super(Component.literal("Nang Client - Modules"));
	}

	private static Component label(Module m) {
		return Component.literal(m.name + ": " + (m.enabled ? "ON" : "OFF"));
	}

	@Override
	protected void init() {
		int index = 0;
		for (Module m : ModuleManager.all()) {
			int x = this.width / 2 - 155 + (index % 2) * 160;
			int y = 40 + (index / 2) * 24;
			addRenderableWidget(Button.builder(label(m), button -> {
				m.toggle();
				button.setMessage(label(m));
			}).bounds(x, y, 150, 20).build());
			index++;
		}
	}
}
