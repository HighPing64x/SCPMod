package com.scpmod.item;

import com.scpmod.ScpMod;
import com.scpmod.api.Scp500Events;
import com.scpmod.content.ModEffects;
import com.scpmod.item.ScpDocumentItem;

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
 * 效果由【服用前】已有的抗药性等级决定：
 * 无抗药性 = 完整治愈；I = 回血减半；II = 随机清 2 个；III = 随机清 1 个且回血更少；
 * IV = 只能获得缓慢生命恢复；V = 完全无效。每次服用后抗药性等级 +1（上限 V），时间重置。
 */
public class Scp500Item extends Item {
	private static final int RESISTANCE_TICKS = 20 * 60 * 10;
	/** 抗药性最高等级：amplifier 4 = V 级。 */
	private static final int MAX_RESISTANCE_LEVEL = 4;
	private static final int REGEN_TICKS = 200;
	private static final Random RANDOM = new Random();
	private static final TagKey<MobEffect> CURABLE_EFFECTS =
			TagKey.create(Registries.MOB_EFFECT, ScpMod.id("curable_effects"));

	public Scp500Item(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			MobEffectInstance existing = serverPlayer.getEffect(ModEffects.SCP500_RESISTANCE_HOLDER);
			// currentLevel: -1 = 无抗药性，0 = I 级，1 = II 级，2 = III 级，3 = IV 级，4 = V 级
			int currentLevel = existing == null ? -1 : existing.getAmplifier();
			int nextLevel = Math.min(currentLevel + 1, MAX_RESISTANCE_LEVEL);
			applyCure(serverLevel, serverPlayer, currentLevel, nextLevel);
			ScpDocumentItem.record(serverPlayer, "scp_500");
			if (!serverPlayer.getAbilities().instabuild) {
				serverPlayer.getItemInHand(hand).shrink(1);
			}
		}
		return InteractionResult.SUCCESS;
	}

	private static void applyCure(ServerLevel level, ServerPlayer player, int currentLevel, int nextLevel) {
		// V 级：完全无效，只刷新抗药性时间。
		if (currentLevel >= MAX_RESISTANCE_LEVEL) {
			player.addEffect(new MobEffectInstance(ModEffects.SCP500_RESISTANCE_HOLDER, RESISTANCE_TICKS, nextLevel, false, true, true));
			level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.6F, 0.7F);
			player.sendSystemMessage(Component.translatable("message.scpmod.scp500.no_effect"));
			ScpMod.LOGGER.info("[scpmod] scp500 used by {} level=5 cleared=0 (no effect)", player.getName().getString());
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
		if (currentLevel <= 0) {
			// 无抗药性 / I 级：清除全部负面效果。
			for (MobEffectInstance instance : curable) {
				player.removeEffect(instance.getEffect());
				removed++;
			}
		} else {
			// II 级随机 2 个；III / IV 级随机 1 个。
			Collections.shuffle(curable, RANDOM);
			int count = currentLevel == 1 ? 2 : 1;
			for (int i = 0; i < curable.size() && i < count; i++) {
				player.removeEffect(curable.get(i).getEffect());
				removed++;
			}
		}

		if (currentLevel >= 3) {
			// IV 级：不瞬间回血，只给缓慢生命恢复。
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_TICKS, 0, false, true, true));
		} else {
			float factor = switch (currentLevel) {
				case -1 -> 1.0F;
				case 0 -> 0.5F;
				case 1 -> 0.35F;
				default -> 0.25F;
			};
			float heal = player.getMaxHealth() * factor;
			player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + heal));
		}
		if (currentLevel <= 0) {
			player.clearFire();
			player.setTicksFrozen(0);
		}

		player.addEffect(new MobEffectInstance(ModEffects.SCP500_RESISTANCE_HOLDER, RESISTANCE_TICKS, nextLevel, false, true, true));
		level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
				player.getX(), player.getY() + 1.0D, player.getZ(), 24, 0.5D, 0.8D, 0.5D, 0.02D);

		Scp500Events.CURE.invoker().onCure(player, currentLevel >= 0);

		Component message = switch (currentLevel) {
			case -1 -> Component.translatable("message.scpmod.scp500.healed", removed);
			case 0 -> Component.translatable("message.scpmod.scp500.weak", removed);
			case 1 -> Component.translatable("message.scpmod.scp500.random_two", removed);
			case 2 -> Component.translatable("message.scpmod.scp500.random_one", removed);
			default -> Component.translatable("message.scpmod.scp500.regen");
		};
		player.sendSystemMessage(message);
		ScpMod.LOGGER.info("[scpmod] scp500 used by {} level={} cleared={}",
				player.getName().getString(), nextLevel + 1, removed);
	}
}
