package com.scpmod.block;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;

/** SCP-006 液体方块（LiquidBlock 构造为 protected，需要子类）。 */
public class Scp006LiquidBlock extends LiquidBlock {
	public Scp006LiquidBlock(FlowingFluid fluid, BlockBehaviour.Properties properties) {
		super(fluid, properties);
	}
}
