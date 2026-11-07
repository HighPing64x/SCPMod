package com.scpmod.block;

import com.scpmod.block.entity.ContainmentUnitBlockEntity;
import com.scpmod.event.BreachEventManager;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Containment unit: a physical cell that holds exactly one anomaly.
 * Breaking it vents the cell and releases whatever is inside (M1: starts a breach event).
 */
public class ContainmentUnitBlock extends BaseEntityBlock {
	public static final MapCodec<ContainmentUnitBlock> CODEC = simpleCodec(ContainmentUnitBlock::new);

	public ContainmentUnitBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ContainmentUnitBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (!(level.getBlockEntity(pos) instanceof ContainmentUnitBlockEntity unit)) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			if (unit.isVacant()) {
				player.sendSystemMessage(Component.translatable("message.scpmod.unit.vacant"));
			} else {
				player.sendSystemMessage(Component.translatable("message.scpmod.unit.occupied",
						Component.translatable(unit.containedScpNameKey()), Component.translatable(unit.isSealed() ? "message.scpmod.sealed.yes" : "message.scpmod.sealed.no")));
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ContainmentUnitBlockEntity unit && !unit.isVacant()) {
			BreachEventManager.start(level, pos, unit.containedScp());
		}
		return super.playerWillDestroy(level, pos, state, player);
	}
}
