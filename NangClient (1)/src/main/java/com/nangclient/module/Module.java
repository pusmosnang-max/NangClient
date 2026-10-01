package com.nangclient.module;

import java.util.List;
import net.minecraft.client.Minecraft;

/** Base class for every feature. Extend this to add a new module. */
public abstract class Module {
	public final String name;
	public boolean enabled;

	protected Module(String name, boolean enabledByDefault) {
		this.name = name;
		this.enabled = enabledByDefault;
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	public void setEnabled(boolean value) {
		if (enabled == value) return;
		enabled = value;
		if (value) onEnable(); else onDisable();
	}

	public void onEnable() {}

	public void onDisable() {}

	/** Called every game tick while the module is enabled. */
	public void onTick(Minecraft mc) {}

	/** Text lines shown on the HUD while the module is enabled. */
	public List<String> hudLines(Minecraft mc) {
		return List.of();
	}
}
