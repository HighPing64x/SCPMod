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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

/** SCP-500 万能药：右键立即服用，清除疾病/负面效果并回血；多次服用会产生逐级抗药性。 */
public class Scp500Item extends Item {
	private static final int RESISTANCE_TICKS = 20 * 60 * 10;
	private static final int MAX_RESISTANCE_LEVEL = 4;
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
		int removed = 0;
		for (MobEffectInstance instance : new ArrayList<>(player.getActiveEffects())) {
			Holder<MobEffect> effect = instance.getEffect();
			// 抗药性本身不算“被清除的负面效果”。
			if (effect.is(ModEffects.SCP500_RESISTANCE_HOLDER)) {
				continue;
			}
			boolean harmful = effect.value().getCategory() == MobEffectCategory.HARMFUL || effect.is(CURABLE_EFFECTS);
			if (!harmful) {
				continue;
			}
			player.removeEffect(effect);
			removed++;
		}

		float factor = switch (resistanceLevel) {
			case 0 -> 0.5F;
			case 1 -> 0.35F;
			case 2 -> 0.25F;
			case 3 -> 0.15F;
			default -> 0.10F;
		};
		float heal = resistant ? player.getMaxHealth() * factor : player.getMaxHealth();
		player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + heal));
		if (!resistant) {
			player.clearFire();
			player.setTicksFrozen(0);
		}

		player.addEffect(new MobEffectInstance(ModEffects.SCP500_RESISTANCE_HOLDER, RESISTANCE_TICKS, resistanceLevel, false, true, true));
		level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
				player.getX(), player.getY() + 1.0D, player.getZ(), 24, 0.5D, 0.8D, 0.5D, 0.02D);

		// 给其他模组的兼容回调（清除自定义感染/疾病机制）。
		Scp500Events.CURE.invoker().onCure(player, resistant);

		if (resistant) {
			player.sendSystemMessage(Component.translatable("message.scpmod.scp500.weak",
					resistanceLevel + 1, removed));
		} else {
			player.sendSystemMessage(Component.translatable("message.scpmod.scp500.healed", removed));
		}
		ScpMod.LOGGER.info("[scpmod] scp500 used by {} resistant={} level={} cleared={}",
				player.getName().getString(), resistant, resistanceLevel + 1, removed);
	}
}
