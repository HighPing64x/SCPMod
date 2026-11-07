package com.scpmod.client.model;

import com.scpmod.client.render.Scp173RenderState;

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
 * SCP-173 entity model (Blockbench "Modded Entity" export, fixed for 26.2).
 *
 * <p>Derived from scp173.java made with Blockbench 4.12.4 (Mojmaps export).
 * Changes vs the raw export: legal class name, 26.2 package names, Identifier instead of
 * ResourceLocation, EntityRenderState generic parameter, no renderToBuffer override
 * (Model.renderToBuffer is final in 26.2), setupAnim(state) signature.
 */
public class Scp173Model extends EntityModel<Scp173RenderState> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Identifier.fromNamespaceAndPath("scpmod", "scp173"), "main");

	private final ModelPart bbMain;

	public Scp173Model(ModelPart root) {
		super(root);
		this.bbMain = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("bb_main", CubeListBuilder.create()
				.texOffs(26, 23).addBox(-4.0F, -7.0F, -1.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(32, 0).addBox(1.0F, -7.0F, -1.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(0, 17).addBox(-4.0F, -21.0F, -1.0F, 8.0F, 14.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(32, 10).addBox(2.0F, -20.0F, -6.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(26, 33).addBox(-4.0F, -20.0F, -6.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(26, 17).addBox(-3.0F, -23.0F, -1.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-4.0F, -32.0F, -3.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(Scp173RenderState state) {
		// 173 是一尊不动的雕像：不需要任何动画。
	}
}
