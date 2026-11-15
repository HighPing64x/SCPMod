package com.scpmod.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** SCP-131 渲染状态：变体（A 燃橙 / B 芥末黄）、警告姿态与底轮角度。 */
public class Scp131RenderState extends LivingEntityRenderState {
	public int variant;
	public boolean warning;
	/** 底轮旋转角度。 */
	public float wheelRotation;
	/** 身体朝向（渲染用，LivingEntityRenderState 自带 yRot 但用我们自己的更直观）。 */
	public float bodyRotation;
}
