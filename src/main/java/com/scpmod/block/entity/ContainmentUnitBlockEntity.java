package com.scpmod.block.entity;

import com.scpmod.content.ModBlockEntities;
import com.scpmod.scp.ScpRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Stores which anomaly is contained inside a unit ("" = vacant). */
public class ContainmentUnitBlockEntity extends BlockEntity {
	private static final String KEY_SCP = "scp";
	private static final String KEY_SEALED = "sealed";

	private String containedScp = "";
	private boolean sealed = true;

	public ContainmentUnitBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CONTAINMENT_UNIT, pos, state);
	}

	public boolean isVacant() {
		return containedScp == null || containedScp.isEmpty();
	}

	public String containedScp() {
		return containedScp == null ? "" : containedScp;
	}

	public String containedScpNameKey() {
		return ScpRegistry.nameKey(containedScp());
	}

	public void setContainedScp(String scpId) {
		this.containedScp = scpId == null ? "" : scpId;
		setChanged();
	}

	public boolean isSealed() {
		return sealed;
	}

	public void setSealed(boolean sealed) {
		this.sealed = sealed;
		setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString(KEY_SCP, containedScp);
		output.putBoolean(KEY_SEALED, sealed);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.containedScp = input.getStringOr(KEY_SCP, "");
		this.sealed = input.getBooleanOr(KEY_SEALED, true);
	}
}
