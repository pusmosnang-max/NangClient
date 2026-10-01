package com.nangclient.module;

import com.nangclient.Theme;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;

/** All on-screen (HUD) modules. Values are cached so almost nothing is allocated per frame. */
public final class HudModules {
	private HudModules() {}

	/** A simple "LABEL value" box. */
	public abstract static class TextHud extends HudModule {
		private final String label;
		private int labelW = -1;
		private String cached = "";
		private int valueW;

		protected TextHud(String name, String label, String description, boolean enabled, int x, int y) {
			super(name, description, enabled, x, y);
			this.label = label;
		}

		/** Return the text to show. Return the same String object when nothing changed. */
		protected abstract String value(Minecraft mc);

		@Override
		protected void update(Minecraft mc) {
			if (labelW < 0) labelW = mc.font.width(label);
			String v = value(mc);
			if (!v.equals(cached)) {
				cached = v;
				valueW = mc.font.width(v);
			}
		}

		@Override
		protected int contentWidth(Minecraft mc) {
			return Math.max(labelW, 0) + 6 + valueW;
		}

		@Override
		protected int contentHeight(Minecraft mc) {
			return 9;
		}

		@Override
		protected void drawContent(GuiGraphics g, Minecraft mc, int ox, int oy) {
			text(g, mc, label, ox, oy, accentColor());
			text(g, mc, cached, ox + Math.max(labelW, 0) + 6, oy, 0xFFFFFFFF);
		}
	}

	public static final class Fps extends TextHud {
		private int frames;
		private String text = "--";
		private long last = System.currentTimeMillis();

		public Fps() { super("FPS", "FPS", "Shows your frames|per second.", true, 6, 6); }

		@Override
		protected String value(Minecraft mc) {
			frames++;
			long now = System.currentTimeMillis();
			if (now - last >= 1000) {
				text = Integer.toString(frames);
				frames = 0;
				last = now;
			}
			return text;
		}
	}

	public static final class Cps extends TextHud {
		private final ArrayDeque<Long> left = new ArrayDeque<>();
		private final ArrayDeque<Long> right = new ArrayDeque<>();
		private boolean wasLeft;
		private boolean wasRight;
		private int lastL = -1;
		private int lastR = -1;
		private String text = "0 | 0";

		public Cps() { super("CPS", "CPS", "Clicks per second|(left | right).", true, 6, 28); }

		@Override
		protected String value(Minecraft mc) {
			long now = System.currentTimeMillis();
			boolean l = mc.options.keyAttack.isDown();
			boolean r = mc.options.keyUse.isDown();
			if (l && !wasLeft) left.addLast(now);
			if (r && !wasRight) right.addLast(now);
			wasLeft = l;
			wasRight = r;
			while (!left.isEmpty() && now - left.peekFirst() > 1000) left.pollFirst();
			while (!right.isEmpty() && now - right.peekFirst() > 1000) right.pollFirst();
			if (left.size() != lastL || right.size() != lastR) {
				lastL = left.size();
				lastR = right.size();
				text = lastL + " | " + lastR;
			}
			return text;
		}
	}

	public static final class Coords extends TextHud {
		private int lx = Integer.MIN_VALUE;
		private int ly;
		private int lz;
		private String text = "-";

		public Coords() { super("Coordinates", "XYZ", "Your position.", true, 6, 50); }

		@Override
		protected String value(Minecraft mc) {
			if (mc.player == null) return "-";
			int x = (int) Math.floor(mc.player.getX());
			int y = (int) Math.floor(mc.player.getY());
			int z = (int) Math.floor(mc.player.getZ());
			if (x != lx || y != ly || z != lz) {
				lx = x;
				ly = y;
				lz = z;
				text = x + " " + y + " " + z;
			}
			return text;
		}
	}

	public static final class Direction extends TextHud {
		private String lastName = "";
		private String text = "-";

		public Direction() { super("Direction", "DIR", "The way you are facing.", false, 6, 72); }

		@Override
		protected String value(Minecraft mc) {
			if (mc.player == null) return "-";
			String d = mc.player.getDirection().getName();
			if (!d.equals(lastName)) {
				lastName = d;
				text = Character.toUpperCase(d.charAt(0)) + d.substring(1);
			}
			return text;
		}
	}

	public static final class Ping extends TextHud {
		private long next;
		private String text = "-";

		public Ping() { super("Ping", "PING", "Connection delay to|the server.", true, 6, 94); }

		@Override
		protected String value(Minecraft mc) {
			long now = System.currentTimeMillis();
			if (now >= next) {
				next = now + 1000;
				if (mc.player == null || mc.getConnection() == null) {
					text = "-";
				} else {
					PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
					text = info == null ? "-" : info.getLatency() + " ms";
				}
			}
			return text;
		}
	}

	public static final class Clock extends TextHud {
		private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
		private long nextSecond;
		private String text = "--:--:--";

		public Clock() { super("Clock", "TIME", "Your real-world time.", false, 6, 116); }

		@Override
		protected String value(Minecraft mc) {
			long now = System.currentTimeMillis();
			if (now >= nextSecond) {
				nextSecond = now - (now % 1000) + 1000;
				text = LocalTime.now().format(FORMAT);
			}
			return text;
		}
	}

	public static final class Memory extends TextHud {
		private long next;
		private String text = "-";

		public Memory() { super("Memory", "RAM", "Memory used by the game.", false, 6, 138); }

		@Override
		protected String value(Minecraft mc) {
			long now = System.currentTimeMillis();
			if (now >= next) {
				next = now + 1000;
				Runtime rt = Runtime.getRuntime();
				long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
				long max = rt.maxMemory() / (1024 * 1024);
				text = used + " / " + max + " MB";
			}
			return text;
		}
	}

	/** WASD + mouse buttons + space bar display. */
	public static final class Keystrokes extends HudModule {
		private final Setting mouse = add(Setting.toggle("Mouse Buttons", true));
		private final Setting space = add(Setting.toggle("Space Bar", true));

		public Keystrokes() { super("Keystrokes", "WASD, mouse and space|display.", true, 6, 164); }

		@Override
		protected boolean drawsBox() {
			return false;
		}

		@Override
		protected int contentWidth(Minecraft mc) {
			return 64;
		}

		@Override
		protected int contentHeight(Minecraft mc) {
			return 42 + (mouse.on() ? 20 : 0) + (space.on() ? 12 : 0);
		}

		@Override
		protected void drawContent(GuiGraphics g, Minecraft mc, int ox, int oy) {
			var o = mc.options;
			key(g, mc, ox + 22, oy, 20, 20, "W", o.keyUp.isDown());
			key(g, mc, ox, oy + 22, 20, 20, "A", o.keyLeft.isDown());
			key(g, mc, ox + 22, oy + 22, 20, 20, "S", o.keyDown.isDown());
			key(g, mc, ox + 44, oy + 22, 20, 20, "D", o.keyRight.isDown());
			int yy = oy + 44;
			if (mouse.on()) {
				key(g, mc, ox, yy, 31, 18, "LMB", o.keyAttack.isDown());
				key(g, mc, ox + 33, yy, 31, 18, "RMB", o.keyUse.isDown());
				yy += 20;
			}
			if (space.on()) {
				key(g, mc, ox, yy, 64, 10, "", o.keyJump.isDown());
			}
		}

		private void key(GuiGraphics g, Minecraft mc, int kx, int ky, int kw, int kh, String label, boolean down) {
			if (down) {
				Theme.rounded(g, kx, ky, kw, kh, Theme.withAlpha(accentColor(), 0xDD));
			} else if (background.on()) {
				Theme.rounded(g, kx, ky, kw, kh, bgColor());
			}
			if (!label.isEmpty()) {
				int tw = mc.font.width(label);
				text(g, mc, label, kx + (kw - tw) / 2, ky + (kh - 8) / 2, down ? 0xFFFFFFFF : 0xFFB8C4E0);
			}
		}
	}
}
