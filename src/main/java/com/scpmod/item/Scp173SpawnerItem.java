package com.scpmod.item;

import com.scpmod.event.AnomalyController;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Debug / creative item: right-click a block to spawn the SCP-173 prototype there. */
public class Scp173SpawnerItem extends Item {
	public Scp173SpawnerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level instanceof ServerLevel serverLevel) {
			BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
			AnomalyController.spawn(serverLevel, pos);
		}
		return InteractionResult.SUCCESS;
	}
}
