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
 * SCP-131 眼豆模型。
 *
 * <p>几何直接来自用户提供的 Blockbench 工程（model.bbmodel，32x32 纹理，4 个立方体）：
 * 主体 6x6x6、底部轮座 4x1x4、颈部柱 2x3x2、顶部小方块 1x1x1。
 * 原导出代码里 UV 与 bbmodel 不一致（bbmodel 用的是 box_uv，导出时被展开成逐面 UV），
 * 这里按 bbmodel 的 box_uv 规则重新用 texOffs + addBox 生成，保证贴图对齐。
 */
public class Scp131Model extends EntityModel<Scp131RenderState> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Identifier.fromNamespaceAndPath("scpmod", "scp_131"), "main");

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart wheel;
	private final ModelPart neck;
	private final ModelPart cap;

	public Scp131Model(ModelPart root) {
		super(root);
		this.root = root;
		ModelPart main = root.getChild("bb_main");
		this.body = main.getChild("body");
		this.wheel = main.getChild("wheel");
		this.neck = main.getChild("neck");
		this.cap = main.getChild("cap");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition main = root.addOrReplaceChild("bb_main", CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		// 主体：6x6x6，UV 起点 (0,0)
		main.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		// 底部轮座：露出的 1 格 + 埋进本体内部的 1 格（避免仰视时看到轮子与本体之间的缝隙）。
		// 必须拆成两个 4x1x4 立方体并各自使用原始 UV(0,12)：
		// 若合成一个 4x2x4，box_uv 需要的贴图区域会变高，越界压到 neck/cap 的 UV，导致贴图错乱。
		main.addOrReplaceChild("wheel", CubeListBuilder.create()
				.texOffs(0, 12).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 12).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		// 颈部柱：2x3x2，UV (16,12)
		main.addOrReplaceChild("neck", CubeListBuilder.create()
				.texOffs(16, 12).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		// 顶部小方块：1x1x1，UV (0,17)
		main.addOrReplaceChild("cap", CubeListBuilder.create()
				.texOffs(0, 17).addBox(0.0F, -11.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(Scp131RenderState state) {
		// 底轮旋转；发现危险时身体轻微前倾（警告姿态）。
		this.wheel.yRot = state.wheelRotation;
		float tilt = state.warning ? -0.25F : 0.0F;
		this.body.xRot = tilt;
		this.neck.xRot = tilt;
		this.cap.xRot = tilt;
	}
}
