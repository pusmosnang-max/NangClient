package com.nangclient.module;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public final class ModuleManager {
	private static final List<Module> MODULES = new ArrayList<>();

	private ModuleManager() {}

	public static void init() {
		// HUD modules
		MODULES.add(new Modules.Fps());
		MODULES.add(new Modules.Cps());
		MODULES.add(new Modules.Coords());
		MODULES.add(new Modules.Facing());
		MODULES.add(new Modules.Ping());
		MODULES.add(new Modules.Clock());
		MODULES.add(new Modules.Memory());
		MODULES.add(new Modules.Keystrokes());
		// Gameplay / visual modules
		MODULES.add(new Modules.ToggleSprint());
		MODULES.add(new Modules.Zoom());
		MODULES.add(new Modules.Fullbright());
	}

	public static List<Module> all() {
		return MODULES;
	}

	public static void tick(Minecraft mc) {
		for (Module m : MODULES) {
			if (m.enabled) m.onTick(mc);
		}
	}
}
