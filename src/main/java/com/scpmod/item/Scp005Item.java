package com.scpmod.item;

import com.scpmod.ScpMod;
import com.scpmod.api.Scp005Events;
import com.scpmod.block.entity.ContainmentUnitBlockEntity;
import com.scpmod.block.entity.TerminalBlockEntity;
import com.scpmod.event.BreachEventManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * SCP-005 万能钥匙：右键方块尝试开锁。
 * 支持原版门/活板门/栅栏门、末地传送门与下界传送门（需结构完整）、SCPMod 收容单元/终端，
 * 以及通过标签/事件接入的其他模组锁。
 */
public class Scp005Item extends Item {
	public static final TagKey<Block> KEY_OPENABLE =
			TagKey.create(Registries.BLOCK, ScpMod.id("key_openable"));
	private static final int COOLDOWN_TICKS = 20;

	public Scp005Item(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide() || !(context.getPlayer() instanceof ServerPlayer player)
				|| !(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		ItemStack stack = context.getItemInHand();
		if (player.getCooldowns().isOnCooldown(stack)) {
			return InteractionResult.FAIL;
		}
		BlockPos pos = context.getClickedPos();
		BlockState state = serverLevel.getBlockState(pos);

		// 其他模组的锁：优先交给事件处理。
		if (Scp005Events.TRY_UNLOCK.invoker().onTryUnlock(player, serverLevel, pos)) {
			finish(serverLevel, player, stack, pos);
			return InteractionResult.SUCCESS;
		}

		// 末地传送门框架：结构完整（12 框 + 全部末影之眼）时激活传送门。
		if (state.is(Blocks.END_PORTAL_FRAME)) {
			if (tryActivateEndPortal(player, serverLevel, pos)) {
				finish(serverLevel, player, stack, pos);
			} else {
				player.sendSystemMessage(Component.translatable("message.scpmod.scp005.end_portal_incomplete"));
			}
			return InteractionResult.SUCCESS;
		}

		// 黑曜石：完整 4x5 框架时点燃下界传送门。
		if (state.is(Blocks.OBSIDIAN)) {
			if (tryActivateNetherPortal(player, serverLevel, pos)) {
				finish(serverLevel, player, stack, pos);
			} else {
				player.sendSystemMessage(Component.translatable("message.scpmod.scp005.nether_portal_incomplete"));
			}
			return InteractionResult.SUCCESS;
		}

		// SCPMod 收容单元：解除密封（若里面有异常会直接引发收容失效）。
		if (serverLevel.getBlockEntity(pos) instanceof ContainmentUnitBlockEntity unit) {
			if (!unit.isSealed()) {
				player.sendSystemMessage(Component.translatable("message.scpmod.scp005.already_unlocked"));
				return InteractionResult.SUCCESS;
			}
			unit.setSealed(false);
			player.sendSystemMessage(Component.translatable("message.scpmod.scp005.unit_unlocked"));
			if (!unit.isVacant()) {
				BreachEventManager.start(serverLevel, pos, unit.containedScp());
				player.sendSystemMessage(Component.translatable("message.scpmod.scp005.unit_breached"));
			}
			finish(serverLevel, player, stack, pos);
			return InteractionResult.SUCCESS;
		}

		// SCPMod 终端：解除武装状态。
		if (serverLevel.getBlockEntity(pos) instanceof TerminalBlockEntity terminal) {
			if (!terminal.isArmed()) {
				player.sendSystemMessage(Component.translatable("message.scpmod.scp005.already_unlocked"));
				return InteractionResult.SUCCESS;
			}
			terminal.toggleArmed();
			player.sendSystemMessage(Component.translatable("message.scpmod.scp005.terminal_unlocked"));
			finish(serverLevel, player, stack, pos);
			return InteractionResult.SUCCESS;
		}

		// 原版门 / 活板门 / 栅栏门：无视红石与权限直接切换开合。
		if ((state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock
				|| state.getBlock() instanceof FenceGateBlock) && state.hasProperty(BlockStateProperties.OPEN)) {
			boolean open = state.getValue(BlockStateProperties.OPEN);
			serverLevel.setBlock(pos, state.setValue(BlockStateProperties.OPEN, !open), Block.UPDATE_ALL);
			player.sendSystemMessage(Component.translatable("message.scpmod.scp005.door_unlocked"));
			finish(serverLevel, player, stack, pos);
			return InteractionResult.SUCCESS;
		}

		// 数据包标签标记的方块（供其他模组扩展）。
		if (state.is(KEY_OPENABLE)) {
			player.sendSystemMessage(Component.translatable("message.scpmod.scp005.tag_unlocked"));
			finish(serverLevel, player, stack, pos);
			return InteractionResult.SUCCESS;
		}

		player.sendSystemMessage(Component.translatable("message.scpmod.scp005.no_lock"));
		return InteractionResult.SUCCESS;
	}

	/** 末地传送门：12 个框架（全部带眼）围成 5x5 环时，在中心 3x3 生成传送门。 */
	private static boolean tryActivateEndPortal(ServerPlayer player, ServerLevel level, BlockPos clicked) {
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				boolean edge = (Math.abs(dx) == 2 || Math.abs(dz) == 2) && !(Math.abs(dx) == 2 && Math.abs(dz) == 2);
				if (!edge) {
					continue;
				}
				BlockPos center = clicked.offset(-dx, 0, -dz);
				if (!isCompleteEndPortal(level, center)) {
					continue;
				}
				for (int ix = -1; ix <= 1; ix++) {
					for (int iz = -1; iz <= 1; iz++) {
						level.setBlock(center.offset(ix, 0, iz), Blocks.END_PORTAL.defaultBlockState(), Block.UPDATE_ALL);
					}
				}
				level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
				player.sendSystemMessage(Component.translatable("message.scpmod.scp005.end_portal_open"));
				return true;
			}
		}
		return false;
	}

	private static boolean isCompleteEndPortal(ServerLevel level, BlockPos center) {
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				boolean edge = (Math.abs(dx) == 2 || Math.abs(dz) == 2) && !(Math.abs(dx) == 2 && Math.abs(dz) == 2);
				if (!edge) {
					continue;
				}
				BlockState state = level.getBlockState(center.offset(dx, 0, dz));
				if (!state.is(Blocks.END_PORTAL_FRAME) || !state.getValue(EndPortalFrameBlock.HAS_EYE)) {
					return false;
				}
			}
		}
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (!level.getBlockState(center.offset(dx, 0, dz)).isAir()) {
					return false;
				}
			}
		}
		return true;
	}

	/** 下界传送门：4x5 黑曜石框架 + 内部 2x3 空间时点燃。 */
	private static boolean tryActivateNetherPortal(ServerPlayer player, ServerLevel level, BlockPos clicked) {
		for (boolean alongX : new boolean[] { true, false }) {
			for (int ox = -3; ox <= 0; ox++) {
				for (int oy = -4; oy <= 0; oy++) {
					BlockPos base = clicked.offset(alongX ? ox : 0, oy, alongX ? 0 : ox);
					if (!isCompleteNetherFrame(level, base, alongX)) {
						continue;
					}
					Direction.Axis axis = alongX ? Direction.Axis.X : Direction.Axis.Z;
					for (int w = 1; w <= 2; w++) {
						for (int h = 1; h <= 3; h++) {
							BlockPos portal = base.offset(alongX ? w : 0, h, alongX ? 0 : w);
							level.setBlock(portal, Blocks.NETHER_PORTAL.defaultBlockState()
									.setValue(NetherPortalBlock.AXIS, axis), Block.UPDATE_ALL);
						}
					}
					level.playSound(null, base, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
					player.sendSystemMessage(Component.translatable("message.scpmod.scp005.nether_portal_open"));
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isCompleteNetherFrame(ServerLevel level, BlockPos base, boolean alongX) {
		for (int w = 0; w < 4; w++) {
			for (int h = 0; h < 5; h++) {
				BlockPos pos = base.offset(alongX ? w : 0, h, alongX ? 0 : w);
				boolean border = w == 0 || w == 3 || h == 0 || h == 4;
				BlockState state = level.getBlockState(pos);
				if (border) {
					if (!state.is(Blocks.OBSIDIAN)) {
						return false;
					}
				} else if (!state.isAir()) {
					return false;
				}
			}
		}
		return true;
	}

	private static void finish(Level level, ServerPlayer player, ItemStack stack, BlockPos pos) {
		player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
		level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
		ScpMod.LOGGER.info("[scpmod] scp005 used by {} at {}", player.getName().getString(), pos);
	}
}
