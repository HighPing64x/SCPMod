package com.scpmod.block.entity;

import com.scpmod.content.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Terminal state (M1: armed flag; M2: links to breach event). */
public class TerminalBlockEntity extends BlockEntity {
	private static final String KEY_ARMED = "armed";

	private boolean armed = false;

	public TerminalBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TERMINAL, pos, state);
	}

	public boolean isArmed() {
		return armed;
	}

	public void toggleArmed() {
		this.armed = !this.armed;
		setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putBoolean(KEY_ARMED, armed);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.armed = input.getBooleanOr(KEY_ARMED, false);
	}
}
