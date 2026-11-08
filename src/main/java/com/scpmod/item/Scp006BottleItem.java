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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

/** SCP-006 水瓶：饮用获得再生 II（60 秒）并清除负面效果，长于接触效果。 */
public class Scp006BottleItem extends Item {
	public Scp006BottleItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}
		serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 1, false, true, true));
		// 清除所有负面效果
		for (MobEffectInstance effect : new ArrayList<>(serverPlayer.getActiveEffects())) {
			Holder<MobEffect> holder = effect.getEffect();
			if (holder.value().getCategory() == MobEffectCategory.HARMFUL) {
				serverPlayer.removeEffect(holder);
			}
		}
		if (!serverPlayer.getAbilities().instabuild) {
			ItemStack stack = serverPlayer.getItemInHand(hand);
			stack.shrink(1);
			ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
			if (stack.isEmpty()) {
				serverPlayer.setItemInHand(hand, bottle);
			} else if (!serverPlayer.getInventory().add(bottle)) {
				serverPlayer.drop(bottle, false);
			}
		}
		ScpDocumentItem.record(serverPlayer, "scp_006");
		serverPlayer.sendSystemMessage(Component.translatable("message.scpmod.scp006.drink"));
		level.playSound(null, serverPlayer.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
		return InteractionResult.SUCCESS;
	}
}
