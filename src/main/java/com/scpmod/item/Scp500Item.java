package com.scpmod.item;

import com.scpmod.ScpMod;
import com.scpmod.api.Scp500Events;
import com.scpmod.content.ModEffects;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * SCP-500 万能药：右键立即服用。
 * 无抗药性时完整治愈；抗药性 I~V 级效果逐级减弱（随机清效果 / 回血减少 / 改为生命恢复 / 完全无效）。
 */
public class Scp500Item extends Item {
	private static final int RESISTANCE_TICKS = 20 * 60 * 10;
	private static final int MAX_RESISTANCE_LEVEL = 4;
	private static final int REGEN_TICKS = 200;
	private static final Random RANDOM = new Random();
	/** 可通过数据包标签让其他模组的效果也被 SCP-500 清除。 */
	private static final TagKey<MobEffect> CURABLE_EFFECTS =
			TagKey.create(Registries.MOB_EFFECT, ScpMod.id("curable_effects"));

	public Scp500Item(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			MobEffectInstance existing = serverPlayer.getEffect(ModEffects.SCP500_RESISTANCE_HOLDER);
			boolean resistant = existing != null;
			int resistanceLevel = resistant ? Math.min(existing.getAmplifier() + 1, MAX_RESISTANCE_LEVEL) : 0;
			applyCure(serverLevel, serverPlayer, resistant, resistanceLevel);
			if (!serverPlayer.getAbilities().instabuild) {
				serverPlayer.getItemInHand(hand).shrink(1);
			}
		}
		return InteractionResult.SUCCESS;
	}

	private static void applyCure(ServerLevel level, ServerPlayer player, boolean resistant, int resistanceLevel) {
		// 抗药性 V 级：完全无效（只刷新抗药性持续时间）。
		if (resistant && resistanceLevel >= MAX_RESISTANCE_LEVEL) {
			player.addEffect(new MobEffectInstance(ModEffects.SCP500_RESISTANCE_HOLDER, RESISTANCE_TICKS, resistanceLevel, false, true, true));
			level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.6F, 0.7F);
			player.sendSystemMessage(Component.translatable("message.scpmod.scp500.no_effect"));
			ScpMod.LOGGER.info("[scpmod] scp500 used by {} resistant={} level={} cleared=0 (no effect)",
					player.getName().getString(), true, resistanceLevel + 1);
			return;
		}

		List<MobEffectInstance> curable = new ArrayList<>();
		for (MobEffectInstance instance : player.getActiveEffects()) {
			Holder<MobEffect> effect = instance.getEffect();
			if (effect.is(ModEffects.SCP500_RESISTANCE_HOLDER)) {
				continue;
			}
			if (effect.value().getCategory() == MobEffectCategory.HARMFUL || effect.is(CURABLE_EFFECTS)) {
				curable.add(instance);
			}
		}

		int removed = 0;
		if (!resistant || resistanceLevel <= 1) {
			// 无抗药性 / I 级：清除全部负面效果。
			for (MobEffectInstance instance : curable) {
				player.removeEffect(instance.getEffect());
				removed++;
			}
		} else {
			// II 级随机 2 个；III/IV 级随机 1 个。
			Collections.shuffle(curable, RANDOM);
			int count = resistanceLevel == 2 ? 2 : 1;
			for (int i = 0; i < curable.size() && i < count; i++) {
				player.removeEffect(curable.get(i).getEffect());
				removed++;
			}
		}

		if (resistant && resistanceLevel >= 4) {
			// IV 级：不再瞬间回血，改为缓慢的生命恢复效果。
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_TICKS, 0, false, true, true));
		} else {
			float factor = !resistant ? 1.0F : switch (resistanceLevel) {
				case 1 -> 0.5F;
				case 2 -> 0.35F;
				default -> 0.25F;
			};
			float heal = player.getMaxHealth() * factor;
			player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + heal));
		}
		if (!resistant || resistanceLevel <= 1) {
			player.clearFire();
			player.setTicksFrozen(0);
		}

		player.addEffect(new MobEffectInstance(ModEffects.SCP500_RESISTANCE_HOLDER, RESISTANCE_TICKS, resistanceLevel, false, true, true));
		level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
				player.getX(), player.getY() + 1.0D, player.getZ(), 24, 0.5D, 0.8D, 0.5D, 0.02D);

		Scp500Events.CURE.invoker().onCure(player, resistant);

		Component message = switch (resistanceLevel) {
			case 0 -> Component.translatable("message.scpmod.scp500.healed", removed);
			case 1 -> Component.translatable("message.scpmod.scp500.weak", removed);
			case 2 -> Component.translatable("message.scpmod.scp500.random_two", removed);
			case 3 -> Component.translatable("message.scpmod.scp500.random_one", removed);
			default -> Component.translatable("message.scpmod.scp500.regen");
		};
		player.sendSystemMessage(message);
		ScpMod.LOGGER.info("[scpmod] scp500 used by {} resistant={} level={} cleared={}",
				player.getName().getString(), resistant, resistanceLevel + 1, removed);
	}
}
