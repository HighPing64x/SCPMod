package com.scpmod.item;

import com.scpmod.entity.Scp131Entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** SCP-131 眼豆生成器：右键方块生成一对 A/B（燃橙 / 芥末黄）。 */
public class Scp131SpawnerItem extends Item {
	public Scp131SpawnerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
		for (int variant = 0; variant <= 1; variant++) {
			Scp131Entity bean = com.scpmod.content.ModEntities.SCP_131.create(serverLevel,
					net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
			if (bean == null) {
				continue;
			}
			bean.setVariant(variant);
			bean.snapTo(pos.getX() + 0.5D + variant * 0.8D, (double) pos.getY(), pos.getZ() + 0.5D,
					serverLevel.getRandom().nextFloat() * 360.0F, 0.0F);
			serverLevel.addFreshEntity(bean);
		}
		context.getPlayer().sendSystemMessage(Component.translatable("message.scpmod.scp131.spawned"));
		return InteractionResult.SUCCESS;
	}
}
