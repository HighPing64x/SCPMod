package com.scpmod.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

/**
 * SCP-006 水瓶：和普通水瓶/食物一样需要饮用时间（32 tick 饮用动画），
 * 不额外加冷却；喝完获得再生 II（60 秒）并清除负面效果，返还玻璃瓶。
 */
public class Scp006BottleItem extends Item {
	private static final int USE_DURATION = 32;

	public Scp006BottleItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.DRINK;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return USE_DURATION;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (!level.isClientSide() && entity instanceof ServerPlayer serverPlayer) {
			serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 1, false, true, true));
			for (MobEffectInstance effect : new ArrayList<>(serverPlayer.getActiveEffects())) {
				Holder<MobEffect> holder = effect.getEffect();
				if (holder.value().getCategory() == MobEffectCategory.HARMFUL) {
					serverPlayer.removeEffect(holder);
				}
			}
			ScpDocumentItem.record(serverPlayer, "scp_006");
			serverPlayer.sendSystemMessage(Component.translatable("message.scpmod.scp006.drink"));
			level.playSound(null, serverPlayer.blockPosition(), SoundEvents.GENERIC_DRINK.value(),
					SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		return new ItemStack(Items.GLASS_BOTTLE);
	}
}
