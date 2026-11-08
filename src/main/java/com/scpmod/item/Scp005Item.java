package com.scpmod.item;

import com.scpmod.ScpMod;
import com.scpmod.api.Scp005Events;
import com.scpmod.block.entity.ContainmentUnitBlockEntity;
import com.scpmod.block.entity.TerminalBlockEntity;
import com.scpmod.event.BreachEventManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * SCP-005 万能钥匙：右键方块尝试开锁。
 * 支持原版门/活板门/栅栏门、SCPMod 收容单元与终端、以及通过标签/事件接入的其他模组锁。
 */
public class Scp005Item extends Item {
	/** 其他模组可把“可被万能钥匙打开”的方块加入该标签。 */
	public static final TagKey<Block> KEY_OPENABLE =
			TagKey.create(Registries.BLOCK, ScpMod.id("key_openable"));
	private static final int COOLDOWN_TICKS = 20;

	public Scp005Item(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide() || !(context.getPlayer() instanceof ServerPlayer player)) {
			return InteractionResult.SUCCESS;
		}
		ItemStack stack = context.getItemInHand();
		if (player.getCooldowns().isOnCooldown(stack)) {
			return InteractionResult.FAIL;
		}
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);

		// 其他模组的锁：优先交给事件处理。
		if (Scp005Events.TRY_UNLOCK.invoker().onTryUnlock(player, level, pos)) {
			finish(level, player, stack, pos);
			return InteractionResult.SUCCESS;
		}

		// SCPMod 收容单元：解除密封（若里面有异常会直接引发收容失效）。
		if (level.getBlockEntity(pos) instanceof ContainmentUnitBlockEntity unit) {
			if (!unit.isSealed()) {
				player.sendSystemMessage(Component.translatable("message.scpmod.scp005.already_unlocked"));
				return InteractionResult.SUCCESS;
			}
			unit.setSealed(false);
			player.sendSystemMessage(Component.translatable("message.scpmod.scp005.unit_unlocked"));
			if (!unit.isVacant()) {
				BreachEventManager.start(level, pos, unit.containedScp());
				player.sendSystemMessage(Component.translatable("message.scpmod.scp005.unit_breached"));
			}
			finish(level, player, stack, pos);
			return InteractionResult.SUCCESS;
		}

		// SCPMod 终端：解除武装状态。
		if (level.getBlockEntity(pos) instanceof TerminalBlockEntity terminal) {
			if (!terminal.isArmed()) {
				player.sendSystemMessage(Component.translatable("message.scpmod.scp005.already_unlocked"));
				return InteractionResult.SUCCESS;
			}
			terminal.toggleArmed();
			player.sendSystemMessage(Component.translatable("message.scpmod.scp005.terminal_unlocked"));
			finish(level, player, stack, pos);
			return InteractionResult.SUCCESS;
		}

		// 原版门 / 活板门 / 栅栏门：无视红石与权限直接切换开合。
		if ((state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock
				|| state.getBlock() instanceof FenceGateBlock) && state.hasProperty(BlockStateProperties.OPEN)) {
			boolean open = state.getValue(BlockStateProperties.OPEN);
			level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, !open), Block.UPDATE_ALL);
			player.sendSystemMessage(Component.translatable("message.scpmod.scp005.door_unlocked"));
			finish(level, player, stack, pos);
			return InteractionResult.SUCCESS;
		}

		// 数据包标签标记的方块（供其他模组扩展）。
		if (state.is(KEY_OPENABLE)) {
			player.sendSystemMessage(Component.translatable("message.scpmod.scp005.tag_unlocked"));
			finish(level, player, stack, pos);
			return InteractionResult.SUCCESS;
		}

		player.sendSystemMessage(Component.translatable("message.scpmod.scp005.no_lock"));
		return InteractionResult.SUCCESS;
	}

	private static void finish(Level level, ServerPlayer player, ItemStack stack, BlockPos pos) {
		player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
		level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
		ScpMod.LOGGER.info("[scpmod] scp005 used by {} at {}", player.getName().getString(), pos);
	}
}
