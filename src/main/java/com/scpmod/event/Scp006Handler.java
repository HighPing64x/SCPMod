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

	public static void tick(MinecraftServer server) {
		if (++counter % 20 != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.isSpectator()) {
				continue;
			}
			FluidState fluid = player.level().getFluidState(player.blockPosition());
			if (fluid.getType() == ModFluids.SCP_006 || fluid.getType() == ModFluids.SCP_006_FLOWING) {
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, true, true));
				ScpDocumentItem.record(player, "scp_006");
			}
		}
	}
}
