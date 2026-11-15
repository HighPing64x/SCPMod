package com.scpmod.content;

import com.scpmod.ScpMod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

/** 自定义声音事件（字幕显示：SCP173：刮石）。 */
public final class ModSounds {
	public static final SoundEvent SCRAPE = SoundEvent.createVariableRangeEvent(ScpMod.id("scrape"));
	/** SCP-131 眼豆的尖锐吱呀声（交流/亲近）。 */
	public static final SoundEvent SCP131_CHIRP = SoundEvent.createVariableRangeEvent(ScpMod.id("scp131_chirp"));
	/** SCP-131 发现危险异常时的恐慌尖叫。 */
	public static final SoundEvent SCP131_ALARM = SoundEvent.createVariableRangeEvent(ScpMod.id("scp131_alarm"));

	private ModSounds() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.SOUND_EVENT, ScpMod.id("scrape"), SCRAPE);
		Registry.register(BuiltInRegistries.SOUND_EVENT, ScpMod.id("scp131_chirp"), SCP131_CHIRP);
		Registry.register(BuiltInRegistries.SOUND_EVENT, ScpMod.id("scp131_alarm"), SCP131_ALARM);
	}
}
