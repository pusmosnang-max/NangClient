package com.nangclient;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class Keys {
	public static KeyMapping zoom;
	public static KeyMapping menu;

	private Keys() {}

	public static void register() {
		zoom = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.nangclient.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, KeyMapping.Category.MISC));
		menu = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.nangclient.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC));
	}
}
