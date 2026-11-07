package com.scpmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** SCP-500 抗药性效果：仅作为标记，具体效果在服用逻辑里处理。 */
public class Scp500ResistanceEffect extends MobEffect {
	public Scp500ResistanceEffect() {
		super(MobEffectCategory.HARMFUL, 0x8B3A3A);
	}
}
