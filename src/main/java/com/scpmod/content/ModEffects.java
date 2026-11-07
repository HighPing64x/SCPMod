package com.scpmod.content;

import com.scpmod.ScpMod;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import com.scpmod.effect.Scp500ResistanceEffect;
import net.minecraft.world.effect.MobEffect;

/** 自定义状态效果。 */
public final class ModEffects {
	/** SCP-500 抗药性：持有该效果的玩家再次服用时效果减弱。 */
	public static MobEffect SCP500_RESISTANCE;
	public static Holder<MobEffect> SCP500_RESISTANCE_HOLDER;

	private ModEffects() {
	}

	public static void register() {
		SCP500_RESISTANCE = Registry.register(BuiltInRegistries.MOB_EFFECT, ScpMod.id("scp500_resistance"),
				new Scp500ResistanceEffect());
		SCP500_RESISTANCE_HOLDER = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(SCP500_RESISTANCE);
	}
}
