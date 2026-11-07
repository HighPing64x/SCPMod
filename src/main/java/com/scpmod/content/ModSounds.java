package com.scpmod.content;

import com.scpmod.ScpMod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

/** 自定义声音事件（字幕显示：SCP173：刮石）。 */
public final class ModSounds {
	public static final SoundEvent SCRAPE = SoundEvent.createVariableRangeEvent(ScpMod.id("scrape"));

	private ModSounds() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.SOUND_EVENT, ScpMod.id("scrape"), SCRAPE);
	}
}
