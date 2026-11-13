package com.scpmod.event;

import com.scpmod.content.ModFluids;
import com.scpmod.content.ModItems;
import com.scpmod.item.ScpDocumentItem;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** 玻璃瓶右键 SCP-006 液体（含流动液体）可装瓶。 */
public final class Scp006Events {
	private Scp006Events() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (level.isClientSide()) {
				return InteractionResult.PASS;
			}
			if (!player.getItemInHand(hand).is(Items.GLASS_BOTTLE)) {
				return InteractionResult.PASS;
			}
			// 瓶子的原版行为使用「只命中源方块」的射线；液体表面常被判定为 MISS，
			// 这里放宽为任意流体命中，并用射线结果回退到点击方块，保证能装到流动液体。
			BlockPos pos = resolveFluidPos(level, player, hitResult.getBlockPos());
			if (pos == null) {
				return InteractionResult.PASS;
			}
			FluidState fluid = level.getFluidState(pos);
			if (!isScp006(fluid)) {
				return InteractionResult.PASS;
			}
			if (!(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.PASS;
			}
			ItemStack stack = player.getItemInHand(hand);
			if (!serverPlayer.getAbilities().instabuild) {
				stack.shrink(1);
			}
			ItemStack bottle = new ItemStack(ModItems.SCP_006_BOTTLE);
			if (stack.isEmpty()) {
				serverPlayer.setItemInHand(hand, bottle);
			} else if (!serverPlayer.getInventory().add(bottle)) {
				serverPlayer.drop(bottle, false);
			}
			ScpDocumentItem.record(serverPlayer, "scp_006");
			serverPlayer.sendSystemMessage(Component.translatable("message.scpmod.scp006.bottle_collected"));
			level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
			return InteractionResult.SUCCESS;
		});
	}

	/** 从点击位置或视线射线中找出真正的液体方块位置。 */
	private static BlockPos resolveFluidPos(Level level, net.minecraft.world.entity.player.Player player, BlockPos clicked) {
		if (isScp006(level.getFluidState(clicked))) {
			return clicked;
		}
		// Entity.pick(距离, 部分时间, 是否包含流体) —— 第三个参数为 true 时射线会命中液体。
		HitResult hit = player.pick(6.0D, 0.0F, true);
		if (hit instanceof BlockHitResult blockHit && isScp006(level.getFluidState(blockHit.getBlockPos()))) {
			return blockHit.getBlockPos();
		}
		return null;
	}

	private static boolean isScp006(FluidState fluid) {
		return fluid.getType() == ModFluids.SCP_006 || fluid.getType() == ModFluids.SCP_006_FLOWING;
	}
}
