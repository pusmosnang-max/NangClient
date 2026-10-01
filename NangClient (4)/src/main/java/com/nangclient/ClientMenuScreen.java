package com.nangclient;

import com.nangclient.module.Module;
import com.nangclient.module.ModuleManager;
import com.nangclient.module.Setting;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** The NangClient module menu: tabs, module list and a settings panel. Everything is clickable. */
public class ClientMenuScreen extends Screen {
	private final Screen parent;
	private Module.Category tab = Module.Category.HUD;
	private Module selected;
	private boolean clickPending;
	private boolean click;
	private int clickX;
	private int clickY;

	public ClientMenuScreen(Screen parent) {
		super(Component.literal("NangClient"));
		this.parent = parent;
	}

	@Override
	public void onClose() {
		Config.save();
		Minecraft.getInstance().setScreen(parent);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			clickPending = true;
			clickX = (int) event.x();
			clickY = (int) event.y();
		}
		return true;
	}

	/** True once if the mouse was clicked inside this rectangle during this frame. */
	private boolean clicked(int x, int y, int w, int h) {
		if (click && Theme.inside(clickX, clickY, x, y, w, h)) {
			click = false;
			return true;
		}
		return false;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		click = clickPending;
		clickPending = false;

		g.fill(0, 0, this.width, this.height, 0xB0000000);

		int pw = Math.min(480, this.width - 16);
		int ph = Math.min(272, this.height - 16);
		int px = (this.width - pw) / 2;
		int py = (this.height - ph) / 2;

		// Panel
		Theme.rounded(g, px - 1, py - 1, pw + 2, ph + 2, Theme.BORDER);
		Theme.rounded(g, px, py, pw, ph, Theme.BG);
		Theme.glow(g, px + 1, py + 2, pw - 2, 30, Theme.ACCENT);
		g.fill(px + 2, py, px + pw - 2, py + 2, Theme.ACCENT);

		// Logo: blue square with a white N
		Theme.rounded(g, px + 12, py + 9, 20, 20, Theme.ACCENT);
		Component letterN = Theme.bold("N");
		g.drawString(font, letterN, px + 12 + (20 - font.width(letterN)) / 2, py + 15, 0xFFFFFFFF);
		int tx = px + 40;
		Component nang = Theme.bold("NANG");
		Component client = Theme.bold("CLIENT");
		g.drawString(font, nang, tx, py + 9, Theme.ACCENT_LIGHT);
		g.drawString(font, client, tx + font.width(nang) + 1, py + 9, Theme.TEXT);
		g.drawString(font, "by NangDev", tx, py + 20, Theme.MUTED);

		// Edit HUD button
		int bx = px + pw - 92;
		int by = py + 10;
		boolean hovEdit = Theme.inside(mouseX, mouseY, bx, by, 80, 18);
		Theme.rounded(g, bx - 1, by - 1, 82, 20, hovEdit ? Theme.ACCENT_LIGHT : Theme.BORDER);
		Theme.rounded(g, bx, by, 80, 18, hovEdit ? Theme.ACCENT : Theme.ROW);
		String edit = "Edit HUD";
		g.drawString(font, edit, bx + (80 - font.width(edit)) / 2, by + 5, Theme.TEXT);
		if (clicked(bx, by, 80, 18)) {
			mc.setScreen(new HudEditorScreen(this));
			return;
		}

		g.fill(px + 10, py + 37, px + pw - 10, py + 38, Theme.BORDER);

		int y0 = py + 44;
		int bodyH = ph - 66;

		// Sidebar tabs
		int sbX = px + 10;
		int sbW = 88;
		int i = 0;
		for (Module.Category c : Module.Category.values()) {
			int ty = y0 + i * 22;
			boolean sel = c == tab;
			boolean hov = Theme.inside(mouseX, mouseY, sbX, ty, sbW, 20);
			if (sel) {
				Theme.rounded(g, sbX, ty, sbW, 20, Theme.ROW_SELECTED);
				g.fill(sbX, ty + 4, sbX + 2, ty + 16, Theme.ACCENT);
			} else if (hov) {
				Theme.rounded(g, sbX, ty, sbW, 20, Theme.ROW);
			}
			g.drawString(font, c.label, sbX + 10, ty + 6, sel ? Theme.TEXT : Theme.MUTED);
			List<Module> inTab = ModuleManager.byCategory(c);
			int on = 0;
			for (Module m : inTab) {
				if (m.enabled) on++;
			}
			String count = on + "/" + inTab.size();
			g.drawString(font, count, sbX + sbW - 8 - font.width(count), ty + 6, Theme.MUTED);
			if (clicked(sbX, ty, sbW, 20)) {
				tab = c;
				selected = null;
			}
			i++;
		}

		// Module list
		List<Module> mods = ModuleManager.byCategory(tab);
		if (selected == null || selected.category != tab) {
			selected = mods.isEmpty() ? null : mods.get(0);
		}
		int lx = px + 106;
		int lw = Math.min(172, (int) ((pw - 116) * 0.46f));
		int row = 0;
		for (Module m : mods) {
			int ry = y0 + row * 26;
			boolean hov = Theme.inside(mouseX, mouseY, lx, ry, lw, 24);
			boolean sel = m == selected;
			Theme.rounded(g, lx, ry, lw, 24, sel ? Theme.ROW_SELECTED : hov ? Theme.ROW_HOVER : Theme.ROW);
			if (sel) g.fill(lx, ry + 4, lx + 2, ry + 20, Theme.ACCENT);
			g.drawString(font, m.name, lx + 10, ry + 8, m.enabled ? Theme.TEXT : Theme.MUTED);
			int swx = lx + lw - 38;
			Theme.toggle(g, swx, ry + 5, m.enabled);
			if (clicked(swx - 4, ry, 42, 24)) {
				m.toggle();
				Config.save();
			} else if (clicked(lx, ry, lw, 24)) {
				selected = m;
			}
			row++;
		}

		// Settings panel
		int sx = lx + lw + 8;
		int sw = px + pw - 10 - sx;
		Theme.rounded(g, sx, y0, sw, bodyH, Theme.SIDEBAR);
		if (selected != null) {
			int cy = y0 + 12;
			g.drawString(font, Theme.bold(selected.name), sx + 12, cy, Theme.ACCENT_LIGHT);
			cy += 14;
			for (String line : selected.descLines) {
				g.drawString(font, line, sx + 12, cy, Theme.MUTED);
				cy += 10;
			}
			cy += 4;
			g.fill(sx + 12, cy, sx + sw - 12, cy + 1, Theme.BORDER);
			cy += 8;
			if (selected.settings.isEmpty()) {
				g.drawString(font, "No settings.", sx + 12, cy + 2, Theme.MUTED);
			}
			for (Setting s : selected.settings) {
				g.drawString(font, s.name, sx + 12, cy + 6, Theme.TEXT);
				if (s.isToggle()) {
					Theme.toggle(g, sx + sw - 12 - 28, cy + 3, s.on());
					if (clicked(sx + 8, cy, sw - 16, 20)) {
						s.next();
						Config.save();
					}
				} else if (s.name.equals("Color")) {
					int n = s.options.length;
					int startX = sx + sw - 12 - (n * 12 + (n - 1) * 5);
					for (int o = 0; o < n; o++) {
						int cx = startX + o * 17;
						Theme.rounded(g, cx, cy + 4, 12, 12, Theme.accent(o));
						if (o == s.index) Theme.outline(g, cx - 2, cy + 2, 16, 16, 0xFFFFFFFF);
						if (clicked(cx - 2, cy + 2, 16, 16)) {
							s.set(o);
							Config.save();
						}
					}
				} else {
					int n = s.options.length;
					int cw = Math.max(40, sw - 24 - 52);
					int cx = sx + sw - 12 - cw;
					Theme.rounded(g, cx, cy + 2, cw, 16, Theme.ROW);
					int seg = cw / n;
					for (int o = 0; o < n; o++) {
						int sgx = cx + o * seg;
						int sgw = o == n - 1 ? cw - seg * (n - 1) : seg;
						boolean hov = Theme.inside(mouseX, mouseY, sgx, cy + 2, sgw, 16);
						if (o == s.index) {
							Theme.rounded(g, sgx, cy + 2, sgw, 16, Theme.ACCENT);
						} else if (hov) {
							Theme.rounded(g, sgx, cy + 2, sgw, 16, Theme.ROW_HOVER);
						}
						String t = s.options[o];
						g.drawString(font, t, sgx + (sgw - font.width(t)) / 2, cy + 6, o == s.index ? 0xFFFFFFFF : Theme.MUTED);
						if (clicked(sgx, cy + 2, sgw, 16)) {
							s.set(o);
							Config.save();
						}
					}
				}
				cy += 20;
			}
		}

		g.drawString(font, "Esc to close   |   Right Shift opens this menu", px + 12, py + ph - 14, Theme.MUTED);
	}
}
