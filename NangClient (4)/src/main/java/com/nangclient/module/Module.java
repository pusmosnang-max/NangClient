package com.nangclient.module;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

/** Base class for every feature. Extend this to add a new module. */
public abstract class Module {
	public enum Category {
		HUD("HUD"),
		GAMEPLAY("Gameplay"),
		VISUAL("Visual"),
		PERFORMANCE("Performance");

		public final String label;

		Category(String label) {
			this.label = label;
		}
	}

	public final String name;
	/** Up to two short lines separated by '|'. */
	public final String description;
	public final String[] descLines;
	public final Category category;
	public boolean enabled;
	public final List<Setting> settings = new ArrayList<>();

	protected Module(String name, String description, Category category, boolean enabledByDefault) {
		this.name = name;
		this.description = description;
		this.descLines = description.split("\\|");
		this.category = category;
		this.enabled = enabledByDefault;
	}

	protected Setting add(Setting s) {
		settings.add(s);
		return s;
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
}
