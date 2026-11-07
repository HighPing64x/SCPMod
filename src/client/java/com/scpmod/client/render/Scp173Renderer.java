package com.scpmod.client.render;

import com.scpmod.client.model.Scp173Model;
import com.scpmod.entity.Scp173Entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** SCP-173 渲染器（26.2 的 submit / SubmitNodeCollector 体系）。 */
public class Scp173Renderer extends EntityRenderer<Scp173Entity, Scp173RenderState> {
	private static final Identifier TEXTURE =
			Identifier.fromNamespaceAndPath("scpmod", "textures/entity/scp173.png");

	private final Scp173Model model;

	public Scp173Renderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new Scp173Model(context.bakeLayer(Scp173Model.LAYER_LOCATION));
		this.shadowRadius = 0.6F;
		this.shadowStrength = 0.7F;
	}

	@Override
	public Scp173RenderState createRenderState() {
		return new Scp173RenderState();
	}

	@Override
	public void extractRenderState(Scp173Entity entity, Scp173RenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.bodyRotation = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
	}

	@Override
	public void submit(Scp173RenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRotation));
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.translate(0.0F, -1.5F, 0.0F);
		collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords,
				OverlayTexture.NO_OVERLAY, state.outlineColor, null);
		poseStack.popPose();
	}
}
