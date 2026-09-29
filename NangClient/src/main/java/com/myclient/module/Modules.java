package com.nangclient.module;

import com.nangclient.Keys;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** All built-in modules. Each one is a small class. */
public final class Modules {
	private Modules() {}

	// ---------- HUD modules ----------

	public static final class Fps extends Module {
		private int frames;
		private int fps;
		private long last = System.currentTimeMillis();

		public Fps() { super("FPS", true); }

		@Override
		public List<String> hudLines(Minecraft mc) {
			frames++;
			long now = System.currentTimeMillis();
			if (now - last >= 1000) {
				fps = frames;
				frames = 0;
				last = now;
			}
			return List.of("FPS: " + fps);
		}
	}

	public static final class Cps extends Module {
		private final ArrayDeque<Long> left = new ArrayDeque<>();
		private final ArrayDeque<Long> right = new ArrayDeque<>();
		private boolean wasLeft;
		private boolean wasRight;

		public Cps() { super("CPS", true); }

		@Override
		public List<String> hudLines(Minecraft mc) {
			long now = System.currentTimeMillis();
			boolean l = mc.options.keyAttack.isDown();
			boolean r = mc.options.keyUse.isDown();
			if (l && !wasLeft) left.addLast(now);
			if (r && !wasRight) right.addLast(now);
			wasLeft = l;
			wasRight = r;
			while (!left.isEmpty() && now - left.peekFirst() > 1000) left.pollFirst();
			while (!right.isEmpty() && now - right.peekFirst() > 1000) right.pollFirst();
			return List.of("CPS: " + left.size() + " | " + right.size());
		}
	}

	public static final class Coords extends Module {
		public Coords() { super("Coordinates", true); }

		@Override
		public List<String> hudLines(Minecraft mc) {
			if (mc.player == null) return List.of();
			return List.of(String.format(Locale.ROOT, "XYZ: %.1f / %.1f / %.1f",
					mc.player.getX(), mc.player.getY(), mc.player.getZ()));
		}
	}

	public static final class Facing extends Module {
		public Facing() { super("Direction", true); }

		@Override
		public List<String> hudLines(Minecraft mc) {
			if (mc.player == null) return List.of();
			String dir = mc.player.getDirection().getName();
			return List.of("Facing: " + Character.toUpperCase(dir.charAt(0)) + dir.substring(1));
		}
	}

	public static final class Ping extends Module {
		public Ping() { super("Ping", true); }

		@Override
		public List<String> hudLines(Minecraft mc) {
			if (mc.player == null || mc.getConnection() == null) return List.of();
			PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
			if (info == null) return List.of();
			return List.of("Ping: " + info.getLatency() + " ms");
		}
	}

	public static final class Clock extends Module {
		private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

		public Clock() { super("Clock", true); }

		@Override
		public List<String> hudLines(Minecraft mc) {
			return List.of("Time: " + LocalTime.now().format(FORMAT));
		}
	}

	public static final class Memory extends Module {
		public Memory() { super("Memory", false); }

		@Override
		public List<String> hudLines(Minecraft mc) {
			Runtime rt = Runtime.getRuntime();
			long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
			long max = rt.maxMemory() / (1024 * 1024);
			return List.of("RAM: " + used + " / " + max + " MB");
		}
	}

	public static final class Keystrokes extends Module {
		public Keystrokes() { super("Keystrokes", true); }

		private static String k(boolean down, String label) {
			return down ? label : ".";
		}

		@Override
		public List<String> hudLines(Minecraft mc) {
			var o = mc.options;
			return List.of("Keys: "
					+ k(o.keyUp.isDown(), "W") + " "
					+ k(o.keyLeft.isDown(), "A") + " "
					+ k(o.keyDown.isDown(), "S") + " "
					+ k(o.keyRight.isDown(), "D") + " | "
					+ k(o.keyJump.isDown(), "SPACE") + " | "
					+ k(o.keyAttack.isDown(), "LMB") + " "
					+ k(o.keyUse.isDown(), "RMB"));
		}
	}

	// ---------- Gameplay / visual modules ----------

	public static final class ToggleSprint extends Module {
		public ToggleSprint() { super("Toggle Sprint", true); }

		@Override
		public void onTick(Minecraft mc) {
			if (mc.player == null) return;
			if (mc.options.keyUp.isDown()) {
				mc.options.keySprint.setDown(true);
			}
		}

		@Override
		public void onDisable() {
			Minecraft.getInstance().options.keySprint.setDown(false);
		}
	}

	public static final class Zoom extends Module {
		private boolean zooming;
		private int savedFov;

		public Zoom() { super("Zoom", true); }

		@Override
		public void onTick(Minecraft mc) {
			boolean held = Keys.zoom != null && Keys.zoom.isDown();
			if (held && !zooming && mc.player != null) {
				savedFov = mc.options.fov().get();
				mc.options.fov().set(30);
				zooming = true;
			} else if (!held && zooming) {
				restore(mc);
			}
		}

		@Override
		public void onDisable() {
			if (zooming) restore(Minecraft.getInstance());
		}

		private void restore(Minecraft mc) {
			mc.options.fov().set(savedFov);
			zooming = false;
		}
	}

	public static final class Fullbright extends Module {
		private boolean applied;

		public Fullbright() { super("Fullbright", false); }

		@Override
		public void onTick(Minecraft mc) {
			if (mc.player == null) return;
			MobEffectInstance current = mc.player.getEffect(MobEffects.NIGHT_VISION);
			if (current == null || current.getDuration() < 300) {
				mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0, false, false, false));
				applied = true;
			}
		}

		@Override
		public void onDisable() {
			Minecraft mc = Minecraft.getInstance();
			if (applied && mc.player != null) {
				mc.player.removeEffect(MobEffects.NIGHT_VISION);
			}
			applied = false;
		}
	}
}
