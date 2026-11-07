package com.scpmod.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

/**
 * 公开 API：SCP-500 治愈事件。
 * 其他模组可以监听该事件来处理自己的“疾病/感染”机制（例如清除自定义感染数据）。
 */
public final class Scp500Events {
	/** 在 SCP-500 完成原版效果清除后触发。 */
	public interface Cure {
		void onCure(ServerPlayer player, boolean resistant);
	}

	public static final Event<Cure> CURE = EventFactory.createArrayBacked(Cure.class,
			callbacks -> (player, resistant) -> {
				for (Cure callback : callbacks) {
					callback.onCure(player, resistant);
				}
			});

	private Scp500Events() {
	}
}
