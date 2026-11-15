package com.scpmod.client.model;

import com.scpmod.client.render.Scp131RenderState;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

/**
 * SCP-131 眼豆模型：泪滴状身体（直径约 30cm）、中央一只大眼睛、底部轮状突出物。
 */
public class Scp131Model extends EntityModel<Scp131RenderState> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Identifier.fromNamespaceAndPath("scpmod", "scp_131"), "main");

	private final ModelPart body;
	private final ModelPart eye;
	private final ModelPart wheel;

	public Scp131Model(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.eye = this.body.getChild("eye");
		this.wheel = this.body.getChild("wheel");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				// 泪滴状：上窄下宽的近似（3 层方块堆叠出圆润轮廓）
				.texOffs(0, 0).addBox(-2.5F, -8.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 10).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(22, 0).addBox(-2.0F, -10.5F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 23.0F, 0.0F));
		// 中央单只蓝眼（略微凸出）
		body.addOrReplaceChild("eye", CubeListBuilder.create()
				.texOffs(0, 20).addBox(-1.5F, -6.5F, -3.6F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		// 底部轮状突出物
		body.addOrReplaceChild("wheel", CubeListBuilder.create()
				.texOffs(10, 20).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -0.5F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(Scp131RenderState state) {
		// 底轮旋转；发现危险时身体轻微前倾（警告姿态）。
		this.wheel.yRot = state.wheelRotation;
		this.body.xRot = state.warning ? -0.25F : 0.0F;
	}
}
