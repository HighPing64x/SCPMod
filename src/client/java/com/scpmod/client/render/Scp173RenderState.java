package com.scpmod.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Render state for SCP-173 (adds the body rotation we need for the submit step). */
public class Scp173RenderState extends LivingEntityRenderState {
	public float bodyRotation;
}
