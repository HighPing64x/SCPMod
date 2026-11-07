package com.scpmod.client;

import com.scpmod.client.model.Scp173Model;
import com.scpmod.client.render.Scp173Renderer;
import com.scpmod.content.ModEntities;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

public class ScpModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(Scp173Model.LAYER_LOCATION, Scp173Model::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.SCP_173, Scp173Renderer::new);
	}
}
