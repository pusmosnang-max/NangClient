package com.nangclient.module;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;

public final class ModuleManager {
	private static final List<Module> MODULES = new ArrayList<>();
	private static final List<HudModule> HUD = new ArrayList<>();
	private static final Map<Module.Category, List<Module>> BY_CATEGORY = new EnumMap<>(Module.Category.class);

	private ModuleManager() {}

	public static void init() {
		for (Module.Category c : Module.Category.values()) {
			BY_CATEGORY.put(c, new ArrayList<>());
		}
		// HUD
		add(new HudModules.Fps());
		add(new HudModules.Cps());
		add(new HudModules.Coords());
		add(new HudModules.Direction());
		add(new HudModules.Ping());
		add(new HudModules.Clock());
		add(new HudModules.Memory());
		add(new HudModules.Keystrokes());
		// Gameplay / visual / performance
		add(new Modules.ToggleSprint());
		add(new Modules.Zoom());
		add(new Modules.Fullbright());
		add(new Modules.TabBadge());
		add(new Modules.FpsBoost());
	}

	private static void add(Module m) {
		MODULES.add(m);
		BY_CATEGORY.get(m.category).add(m);
		if (m instanceof HudModule h) HUD.add(h);
	}

	public static List<Module> all() {
		return MODULES;
	}

	public static List<HudModule> hud() {
		return HUD;
	}

	public static List<Module> byCategory(Module.Category c) {
		return BY_CATEGORY.get(c);
	}

	public static void tick(Minecraft mc) {
		for (int i = 0, n = MODULES.size(); i < n; i++) {
			Module m = MODULES.get(i);
			if (m.enabled) m.onTick(mc);
		}
	}
}
