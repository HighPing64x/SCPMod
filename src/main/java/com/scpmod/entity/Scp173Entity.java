package com.scpmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * SCP-173 实体：一尊几乎不可摧毁的混凝土雕像。
 * 注意：它没有任何原版 AI（registerGoals 为空），全部行为由服务器端
 * {@code AnomalyController} 驱动（注视冻结 / 未观察时高速逼近 / 接触断颈）。
 */
public class Scp173Entity extends Monster {
	public Scp173Entity(EntityType<? extends Scp173Entity> type, Level level) {
		super(type, level);
		this.setPersistenceRequired();
	}

	@Override
	protected void registerGoals() {
		// 不注册任何 AI 目标：行为完全由 AnomalyController 控制。
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 200.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.0D)
				.add(Attributes.ATTACK_DAMAGE, 0.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void push(double x, double y, double z) {
		// 纹丝不动
	}

	@Override
	public boolean isPickable() {
		return true;
	}
}
