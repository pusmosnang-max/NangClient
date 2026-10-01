package com.nangclient;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Colors and small drawing helpers used by the whole client. */
public final class Theme {
	public static final int BG = 0xFF090D16;
	public static final int BORDER = 0xFF1B2540;
	public static final int SIDEBAR = 0xFF0E1422;
	public static final int ROW = 0xFF111827;
	public static final int ROW_HOVER = 0xFF182036;
	public static final int ROW_SELECTED = 0xFF16233F;
	public static final int ACCENT = 0xFF3B82F6;
	public static final int ACCENT_LIGHT = 0xFF60A5FA;
	public static final int TEXT = 0xFFEAF0FF;
	public static final int MUTED = 0xFF7C8AA8;
	public static final int OFF = 0xFF2A3350;

	private Theme() {}

	/** A filled rectangle with slightly cut corners (looks rounded). */
	public static void rounded(GuiGraphics g, int x, int y, int w, int h, int color) {
		g.fill(x + 1, y, x + w - 1, y + 1, color);
		g.fill(x, y + 1, x + w, y + h - 1, color);
		g.fill(x + 1, y + h - 1, x + w - 1, y + h, color);
	}

	public static void outline(GuiGraphics g, int x, int y, int w, int h, int color) {
		g.fill(x, y, x + w, y + 1, color);
		g.fill(x, y + h - 1, x + w, y + h, color);
		g.fill(x, y, x + 1, y + h, color);
		g.fill(x + w - 1, y, x + w, y + h, color);
	}

	/** A soft glow that fades out downwards. */
	public static void glow(GuiGraphics g, int x, int y, int w, int h, int rgb) {
		for (int i = 0; i < h; i++) {
			int alpha = (int) (0x38 * (1.0f - (float) i / h));
			if (alpha <= 0) break;
			g.fill(x, y + i, x + w, y + i + 1, withAlpha(rgb, alpha));
		}
	}

	public static boolean inside(int mx, int my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	public static int withAlpha(int rgb, int alpha) {
		return (alpha << 24) | (rgb & 0xFFFFFF);
	}

	public static int accent(int index) {
		return switch (index) {
			case 1 -> 0xFF22D3EE; // cyan
			case 2 -> 0xFFA78BFA; // purple
			case 3 -> 0xFF34D399; // green
			case 4 -> 0xFFF87171; // red
			case 5 -> 0xFFFFFFFF; // white
			default -> ACCENT; // blue
		};
	}

	public static Component bold(String s) {
		return Component.literal(s).withStyle(ChatFormatting.BOLD);
	}

	/** A 28x14 on/off switch. */
	public static void toggle(GuiGraphics g, int x, int y, boolean on) {
		rounded(g, x, y, 28, 14, on ? ACCENT : OFF);
		rounded(g, on ? x + 16 : x + 2, y + 2, 10, 10, 0xFFFFFFFF);
	}
}
