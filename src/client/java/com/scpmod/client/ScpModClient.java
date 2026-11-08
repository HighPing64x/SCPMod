package com.scpmod.client;

import com.scpmod.client.model.Scp173Model;
import com.scpmod.client.screen.ScpArchiveScreen;
import com.scpmod.client.render.Scp173Renderer;
import com.scpmod.content.ModEntities;
import com.scpmod.content.ModItems;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;

public class ScpModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(Scp173Model.LAYER_LOCATION, Scp173Model::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.SCP_173, Scp173Renderer::new);
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (level.isClientSide() && player.getItemInHand(hand).is(ModItems.SCP_DOCUMENT)) {
				net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
				// 调度到客户端主线程打开界面（避免在服务端线程调用 setScreen）。
				Minecraft.getInstance().execute(() ->
						Minecraft.getInstance().setScreenAndShow(new ScpArchiveScreen(stack)));
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});
	}
}
