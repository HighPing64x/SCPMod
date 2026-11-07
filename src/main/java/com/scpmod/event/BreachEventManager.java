package com.scpmod.event;

import com.scpmod.ScpMod;
import com.scpmod.config.EventConfig;
import com.scpmod.config.ScpModConfig;
import com.scpmod.scp.ScpRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Breach event state machine (D13/D14/D15).
 *
 * <p>IDLE -> WARNING (controllable window, boss bar) -> BREACHED (anomaly loose) -> CONTAINED
 * -> IDLE. M2 keeps the anomaly abstract: the state machine, presentation and settlement stats
 * are complete; spawning/modelling the actual SCP entity is the next increment.
 */
public final class BreachEventManager {
	public enum Phase {
		IDLE, WARNING, BREACHED, CONTAINED
	}

	private static Phase phase = Phase.IDLE;
	private static int ticksRemaining;
	private static int breachedTicks;
	private static Identifier activeScp;
	private static BlockPos origin = BlockPos.ZERO;
	private static ServerLevel activeLevel;
	private static ServerBossEvent bossBar;

	private BreachEventManager() {
	}

	public static void init() {
		ScpModStats.load();
		reset();
	}

	public static Phase phase() {
		return phase;
	}

	public static Identifier activeScp() {
		return activeScp;
	}

	public static int ticksRemaining() {
		return ticksRemaining;
	}

	public static int breachedSeconds() {
		return breachedTicks / 20;
	}

	public static BlockPos origin() {
		return origin;
	}

	/** Called when a containment unit is destroyed while occupied (or manually by command). */
	public static boolean start(Level level, BlockPos pos, String scpId) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return false;
		}
		if (!ScpModConfig.get().breachEventsEnabled) {
			return false;
		}
		if (phase != Phase.IDLE) {
			ScpMod.LOGGER.info("[scpmod] breach ignored: event already in phase {}", phase);
			return false;
		}

		activeLevel = serverLevel;
		activeScp = Identifier.tryParse(scpId == null ? "" : scpId);
		origin = pos.immutable();
		phase = Phase.WARNING;
		ticksRemaining = Math.max(1, EventConfig.get().breachWarningSeconds) * 20;
		breachedTicks = 0;
		ScpModStats.get().eventsTriggered++;
		ScpModStats.save();

		bossBar = new ServerBossEvent(UUID.randomUUID(),
				Component.translatable("message.scpmod.bossbar.warning", ScpRegistry.displayName(scpId)),
				BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
		bossBar.setProgress(1.0F);
		addAllPlayers();
		broadcast(Component.translatable("message.scpmod.event.warning", EventConfig.get().breachWarningSeconds));
		ScpMod.LOGGER.info("[scpmod] breach warning started at {} for {}", origin, scpId);
		return true;
	}

	private static void addAllPlayers() {
		if (activeLevel == null || bossBar == null) {
			return;
		}
		for (ServerPlayer player : activeLevel.players()) {
			bossBar.addPlayer(player);
		}
	}

	public static void tick() {
		switch (phase) {
			case WARNING -> {
				if (activeLevel == null || activeLevel.getServer() == null) {
					reset();
					return;
				}
				addAllPlayers();
				if (bossBar != null) {
					bossBar.setProgress(Math.max(0.0F, ticksRemaining / (float) (Math.max(1, EventConfig.get().breachWarningSeconds) * 20)));
				}
				if (--ticksRemaining <= 0) {
					phase = Phase.BREACHED;
					breachedTicks = 0;
					if (bossBar != null) {
						bossBar.setName(Component.translatable("message.scpmod.bossbar.breached", ScpRegistry.displayName(activeScp == null ? "" : activeScp.toString())));
						bossBar.setColor(BossEvent.BossBarColor.RED);
						bossBar.setOverlay(BossEvent.BossBarOverlay.NOTCHED_10);
						bossBar.setProgress(1.0F);
					}
					broadcast(Component.translatable("message.scpmod.event.breached",
							ScpRegistry.displayName(activeScp == null ? "" : activeScp.toString())));
					AnomalyController.spawn(activeLevel, origin);
					ScpMod.LOGGER.info("[scpmod] breach realised at {}: anomaly body released", origin);
				}
			}
			case BREACHED -> {
				breachedTicks++;
				if (activeLevel != null && breachedTicks % (20 * 30) == 0) {
					broadcast(Component.translatable("message.scpmod.event.stillbreached", breachedSeconds()));
				}
			}
			case CONTAINED -> {
				if (--ticksRemaining <= 0) {
					reset();
				}
			}
			default -> {
			}
		}
	}

	/** Player-side countermeasure: try to re-contain the loose anomaly from a terminal. */
	public static boolean tryRecontain(Level level) {
		if (phase != Phase.BREACHED) {
			return false;
		}
		phase = Phase.CONTAINED;
		ticksRemaining = Math.max(1, EventConfig.get().containedDisplaySeconds) * 20;
		AnomalyController.despawn();
		ScpModStats.get().eventsRecontained++;
		ScpModStats.get().totalBreachedSeconds += breachedSeconds();
		ScpModStats.save();
		broadcast(Component.translatable("message.scpmod.event.recontained", breachedSeconds()));
		ScpMod.LOGGER.info("[scpmod] anomaly re-contained after {}s; stats: {}", breachedSeconds(), ScpModStats.summary());
		if (bossBar != null) {
			bossBar.setName(Component.translatable("message.scpmod.bossbar.contained"));
			bossBar.setColor(BossEvent.BossBarColor.GREEN);
			bossBar.setProgress(1.0F);
		}
		return true;
	}

	public static boolean stop() {
		if (phase == Phase.IDLE) {
			return false;
		}
		if (phase == Phase.BREACHED || phase == Phase.WARNING) {
			ScpModStats.get().eventsEndedByForce++;
			ScpModStats.get().totalBreachedSeconds += breachedSeconds();
			ScpModStats.save();
		}
		broadcast(Component.translatable("message.scpmod.event.stopped"));
		reset();
		return true;
	}

	private static void reset() {
		AnomalyController.despawn();
		phase = Phase.IDLE;
		ticksRemaining = 0;
		breachedTicks = 0;
		activeScp = null;
		origin = BlockPos.ZERO;
		if (bossBar != null) {
			bossBar.removeAllPlayers();
			bossBar = null;
		}
		activeLevel = null;
	}

	private static void broadcast(Component message) {
		if (activeLevel == null) {
			return;
		}
		for (ServerPlayer player : activeLevel.players()) {
			player.sendSystemMessage(message);
		}
	}
}
