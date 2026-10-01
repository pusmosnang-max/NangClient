package com.nangclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nangclient.module.HudModule;
import com.nangclient.module.Module;
import com.nangclient.module.ModuleManager;
import com.nangclient.module.Setting;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Saves and loads module on/off state, settings and HUD positions. */
public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private Config() {}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("nangclient.json");
	}

	public static void load() {
		Path p = path();
		if (!Files.exists(p)) return;
		try (Reader r = Files.newBufferedReader(p)) {
			JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
			for (Module m : ModuleManager.all()) {
				if (!root.has(m.name)) continue;
				JsonObject o = root.getAsJsonObject(m.name);
				if (o.has("enabled")) m.enabled = o.get("enabled").getAsBoolean();
				if (o.has("settings")) {
					JsonObject s = o.getAsJsonObject("settings");
					for (Setting st : m.settings) {
						if (s.has(st.name)) st.set(s.get(st.name).getAsInt());
					}
				}
				if (m instanceof HudModule h) {
					if (o.has("x")) h.x = o.get("x").getAsInt();
					if (o.has("y")) h.y = o.get("y").getAsInt();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void save() {
		try {
			JsonObject root = new JsonObject();
			for (Module m : ModuleManager.all()) {
				JsonObject o = new JsonObject();
				o.addProperty("enabled", m.enabled);
				JsonObject s = new JsonObject();
				for (Setting st : m.settings) s.addProperty(st.name, st.index);
				o.add("settings", s);
				if (m instanceof HudModule h) {
					o.addProperty("x", h.x);
					o.addProperty("y", h.y);
				}
				root.add(m.name, o);
			}
			try (Writer w = Files.newBufferedWriter(path())) {
				GSON.toJson(root, w);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
