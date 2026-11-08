package com.scpmod.content;

import com.scpmod.ScpMod;
import com.scpmod.fluid.Scp006Fluid;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;

/** SCP-006 流体：源流体与流动流体。 */
public final class ModFluids {
	public static final Fluid SCP_006 = new Scp006Fluid.Source();
	public static final Fluid SCP_006_FLOWING = new Scp006Fluid.Flowing();

	private ModFluids() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.FLUID, ScpMod.id("scp_006"), SCP_006);
		Registry.register(BuiltInRegistries.FLUID, ScpMod.id("scp_006_flowing"), SCP_006_FLOWING);
	}
}
