package com.nangclient.module;

/** A setting that cycles through a list of options when clicked (an on/off switch is just Off/On). */
public final class Setting {
	public final String name;
	public final String[] options;
	public int index;

	public Setting(String name, int defaultIndex, String... options) {
		this.name = name;
		this.options = options;
		this.index = defaultIndex;
	}

	public static Setting toggle(String name, boolean on) {
		return new Setting(name, on ? 1 : 0, "Off", "On");
	}

	public boolean isToggle() {
		return options.length == 2 && options[0].equals("Off") && options[1].equals("On");
	}

	public boolean on() {
		return index == 1;
	}

	public String value() {
		return options[index];
	}

	public void next() {
		index = (index + 1) % options.length;
	}

	public void set(int i) {
		if (i >= 0 && i < options.length) index = i;
	}
}
