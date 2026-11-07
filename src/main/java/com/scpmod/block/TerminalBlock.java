package com.scpmod.block;

import com.scpmod.block.entity.TerminalBlockEntity;
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
 * Operation terminal: the site's control point. M1 wiring: arm/disarm the local containment protocol.
 * M2 will connect it to the breach event state machine.
 */
public class TerminalBlock extends BaseEntityBlock {
	public static final MapCodec<TerminalBlock> CODEC = simpleCodec(TerminalBlock::new);

	public TerminalBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TerminalBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (!(level.getBlockEntity(pos) instanceof TerminalBlockEntity terminal)) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			// During a live breach an armed terminal is the player's countermeasure: re-contain.
			if (BreachEventManager.phase() == BreachEventManager.Phase.BREACHED && terminal.isArmed()) {
				boolean ok = BreachEventManager.tryRecontain(level);
				player.sendSystemMessage(Component.translatable(ok ? "message.scpmod.recontained" : "message.scpmod.recontain.failed"));
				return InteractionResult.SUCCESS;
			}
			terminal.toggleArmed();
			player.sendSystemMessage(Component.translatable(terminal.isArmed() ? "message.scpmod.terminal.armed" : "message.scpmod.terminal.disarmed"));
		}
		return InteractionResult.SUCCESS;
	}
}
