package com.nangclient;

import com.nangclient.module.HudModule;
import com.nangclient.module.ModuleManager;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Drag the HUD boxes anywhere you like. */
public class HudEditorScreen extends Screen {
	private final Screen parent;
	private HudModule dragging;
	private int offX;
	private int offY;

	public HudEditorScreen(Screen parent) {
		super(Component.literal("Edit HUD"));
		this.parent = parent;
	}

	@Override
	public void onClose() {
		Config.save();
		Minecraft.getInstance().setScreen(parent);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0) return true;
		Minecraft mc = Minecraft.getInstance();
		int mx = (int) event.x();
		int my = (int) event.y();
		List<HudModule> mods = ModuleManager.hud();
		for (int i = mods.size() - 1; i >= 0; i--) {
			HudModule m = mods.get(i);
			if (m.enabled && m.contains(mc, mx, my)) {
				dragging = m;
				offX = mx - m.x;
				offY = my - m.y;
				break;
			}
		}
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (dragging == null) return true;
		Minecraft mc = Minecraft.getInstance();
		int maxX = Math.max(0, this.width - dragging.screenWidth(mc));
		int maxY = Math.max(0, this.height - dragging.screenHeight(mc));
		dragging.x = Math.max(0, Math.min(maxX, (int) event.x() - offX));
		dragging.y = Math.max(0, Math.min(maxY, (int) event.y() - offY));
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging != null) {
			dragging = null;
			Config.save();
		}
		return true;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		g.fill(0, 0, this.width, this.height, 0x70000000);

		for (HudModule m : ModuleManager.hud()) {
			if (!m.enabled) continue;
			m.render(g, mc, true);
			if (m == dragging || m.contains(mc, mouseX, mouseY)) {
				g.drawString(font, m.name, m.x, Math.max(0, m.y - 10), Theme.ACCENT_LIGHT);
			}
		}

		String msg = "Drag the boxes to move them   |   Esc to finish";
		g.drawString(font, msg, (this.width - font.width(msg)) / 2, 10, Theme.TEXT);
	}
}
