package com.nangclient.module;

import com.nangclient.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** A module that draws a movable box on the screen. */
public abstract class HudModule extends Module {
	private static final int PAD = 4;

	public int x;
	public int y;

	protected final Setting background = add(Setting.toggle("Background", true));
	protected final Setting shadow = add(Setting.toggle("Text Shadow", true));
	protected final Setting color = add(new Setting("Color", 0, "Blue", "Cyan", "Purple", "Green", "Red", "White"));
	protected final Setting scale = add(new Setting("Scale", 1, "S", "M", "L"));
	protected final Setting opacity = add(new Setting("Opacity", 1, "Low", "Mid", "High"));

	private int accentIndex = -1;
	private int accentCache;

	protected HudModule(String name, String description, boolean enabledByDefault, int x, int y) {
		super(name, description, Category.HUD, enabledByDefault);
		this.x = x;
		this.y = y;
	}

	/** Called once per frame before drawing. */
	protected void update(Minecraft mc) {}

	protected abstract int contentWidth(Minecraft mc);

	protected abstract int contentHeight(Minecraft mc);

	protected abstract void drawContent(GuiGraphics g, Minecraft mc, int ox, int oy);

	protected boolean drawsBox() {
		return true;
	}

	protected int accentColor() {
		if (accentIndex != color.index) {
			accentIndex = color.index;
			accentCache = Theme.accent(accentIndex);
		}
		return accentCache;
	}

	protected int bgColor() {
		int alpha = switch (opacity.index) {
			case 0 -> 0x55;
			case 2 -> 0xCC;
			default -> 0x99;
		};
		return Theme.withAlpha(0x0B0F1A, alpha);
	}

	protected void text(GuiGraphics g, Minecraft mc, String s, int px, int py, int argb) {
		g.drawString(mc.font, s, px, py, argb, shadow.on());
	}

	public float scaleValue() {
		return switch (scale.index) {
			case 0 -> 0.8f;
			case 2 -> 1.25f;
			default -> 1.0f;
		};
	}

	private int baseWidth(Minecraft mc) {
		return drawsBox() ? contentWidth(mc) + PAD * 2 + 2 : contentWidth(mc);
	}

	private int baseHeight(Minecraft mc) {
		return drawsBox() ? contentHeight(mc) + PAD * 2 : contentHeight(mc);
	}

	public int screenWidth(Minecraft mc) {
		return Math.round(baseWidth(mc) * scaleValue());
	}

	public int screenHeight(Minecraft mc) {
		return Math.round(baseHeight(mc) * scaleValue());
	}

	public boolean contains(Minecraft mc, int mx, int my) {
		return Theme.inside(mx, my, x, y, screenWidth(mc), screenHeight(mc));
	}

	public void render(GuiGraphics g, Minecraft mc, boolean editing) {
		update(mc);
		int w = baseWidth(mc);
		int h = baseHeight(mc);
		float s = scaleValue();
		g.pose().pushMatrix();
		g.pose().translate((float) x, (float) y);
		if (s != 1.0f) g.pose().scale(s, s);
		int ox = 0;
		int oy = 0;
		if (drawsBox()) {
			if (background.on()) {
				Theme.rounded(g, 0, 0, w, h, bgColor());
				g.fill(0, 1, 2, h - 1, accentColor());
			}
			ox = PAD + 2;
			oy = PAD;
		}
		drawContent(g, mc, ox, oy);
		if (editing) {
			Theme.outline(g, 0, 0, w, h, Theme.ACCENT_LIGHT);
		}
		g.pose().popMatrix();
	}
}
