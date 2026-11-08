package com.scpmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;

/** Drives server-side state machines (breach event timer + anomaly controller). */
public final class ServerTickHandler {
	private ServerTickHandler() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			BreachEventManager.tick();
			for (ServerLevel level : server.getAllLevels()) {
				AnomalyController.ensureAdopted(level);
			}
			AnomalyController.tick();
			Scp006Handler.tick(server);
		});
	}
}
