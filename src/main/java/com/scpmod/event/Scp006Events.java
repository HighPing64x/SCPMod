package com.scpmod.event;

import com.scpmod.content.ModFluids;
import com.scpmod.content.ModItems;
import com.scpmod.item.ScpDocumentItem;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FluidState;

/** 玻璃瓶右键 SCP-006 液体可装瓶。 */
public final class Scp006Events {
	private Scp006Events() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (level.isClientSide()) {
				return InteractionResult.PASS;
			}
			ItemStack stack = player.getItemInHand(hand);
			if (!stack.is(Items.GLASS_BOTTLE)) {
				return InteractionResult.PASS;
			}
			FluidState fluid = level.getFluidState(hitResult.getBlockPos());
			if (fluid.getType() != ModFluids.SCP_006 && fluid.getType() != ModFluids.SCP_006_FLOWING) {
				return InteractionResult.PASS;
			}
			if (player instanceof ServerPlayer serverPlayer) {
				if (!serverPlayer.getAbilities().instabuild) {
					stack.shrink(1);
				}
				ItemStack bottle = new ItemStack(ModItems.SCP_006_BOTTLE);
				if (!serverPlayer.getInventory().add(bottle)) {
					serverPlayer.drop(bottle, false);
				}
				ScpDocumentItem.record(serverPlayer, "scp_006");
				serverPlayer.sendSystemMessage(Component.translatable("message.scpmod.scp006.bottle_collected"));
				level.playSound(null, hitResult.getBlockPos(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});
	}
}
