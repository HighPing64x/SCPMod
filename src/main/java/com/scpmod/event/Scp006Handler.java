package com.scpmod.event;

import com.scpmod.content.ModFluids;
import com.scpmod.item.ScpDocumentItem;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.material.FluidState;

/** SCP-006 液体接触效果：站在液体中持续获得再生 I（5 秒刷新）。 */
public final class Scp006Handler {
	private static int counter;

	private Scp006Handler() {
	}

	private static boolean isScp006(FluidState fluid) {
		return fluid.getType() == ModFluids.SCP_006 || fluid.getType() == ModFluids.SCP_006_FLOWING;
	}

	public static void tick(MinecraftServer server) {
		if (++counter % 20 != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.isSpectator()) {
				continue;
			}
			// 液体只占下方部分，玩家站在齐腰深的水里时 blockPosition() 可能是空气，
			// 因此同时检测脚部与腿部高度。
			boolean inFluid = isScp006(player.level().getFluidState(player.blockPosition()))
					|| isScp006(player.level().getFluidState(
							player.blockPosition().below()));
			if (inFluid) {
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, true, true));
				ScpDocumentItem.record(player, "scp_006");
			}
		}
	}
}
