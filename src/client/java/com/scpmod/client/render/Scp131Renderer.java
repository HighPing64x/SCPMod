package com.scpmod.client.render;

import com.scpmod.client.model.Scp131Model;
import com.scpmod.entity.Scp131Entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** SCP-131 渲染器：A/B 两种颜色用同一张贴图的两个区域。 */
public class Scp131Renderer extends EntityRenderer<Scp131Entity, Scp131RenderState> {
	private static final Identifier TEXTURE_A =
			Identifier.fromNamespaceAndPath("scpmod", "textures/entity/scp131_a.png");
	private static final Identifier TEXTURE_B =
			Identifier.fromNamespaceAndPath("scpmod", "textures/entity/scp131_b.png");

	private final Scp131Model model;

	public Scp131Renderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new Scp131Model(context.bakeLayer(Scp131Model.LAYER_LOCATION));
		this.shadowRadius = 0.25F;
		this.shadowStrength = 0.4F;
	}

	@Override
	public Scp131RenderState createRenderState() {
		return new Scp131RenderState();
	}

	@Override
	public void extractRenderState(Scp131Entity entity, Scp131RenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.variant = entity.getVariant();
		state.warning = entity.isWarning();
		state.bodyRotation = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
		state.wheelRotation = (entity.tickCount + partialTick) * 0.9F;
	}

	@Override
	public void submit(Scp131RenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRotation));
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.translate(0.0F, -1.5F, 0.0F);
		collector.submitModel(this.model, state, poseStack,
				state.variant == 0 ? TEXTURE_A : TEXTURE_B, state.lightCoords,
				OverlayTexture.NO_OVERLAY, state.outlineColor, null);
		poseStack.popPose();
	}
}
