package com.scpmod.config;

import com.scpmod.ScpMod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Per-anomaly behaviour tuning (separate settings file: config/scpmod-anomalies.json). */
public final class AnomalyConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static AnomalyConfig instance = new AnomalyConfig();

	/** Default field of view (vertical degrees) used to decide whether the anomaly is on screen. */
	public double observationFovDegrees = 100.0;
	/** Max distance at which the anomaly notices / can be observed. */
	public double viewDistance = 40.0;
	/** Within this distance the anomaly always counts as observed while in front of the player. */
	public double closeRangeObserved = 3.0;
	/** Max travel per tick while unobserved. */
	public double maxStep = 12.0;
	/** Distance at which it strikes. */
	public double killRange = 2.5;
	/** Damage applied on contact (1000 = neck snap). */
	public float neckSnapDamage = 1000.0F;
	/** Creative players are never targeted nor chased. */
	public boolean ignoreCreativePlayers = true;
	/** After (re)spawning, players are safe from the anomaly for this long (anti spawn-kill). */
	public int respawnProtectionSeconds = 5;
	/** Players within this distance get the ambient "something is nearby" subtitle. */
	public double ambientWarningRange = 32.0;
	/** Hysteresis so the subtitle does not flicker at the boundary. */
	public double ambientWarningHysteresis = 4.0;
	/** Enable the ambient proximity messages. */
	public boolean ambientMessagesEnabled = true;

	/** Minimum ticks between two attacks. */
	public int attackCooldownTicks = 20;

	private AnomalyConfig() {
	}

	public static AnomalyConfig get() {
		return instance;
	}

	public static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("scpmod-anomalies.json");
	}

	public static void load() {
		Path path = path();
		try {
			if (Files.exists(path)) {
				AnomalyConfig loaded = GSON.fromJson(Files.readString(path), AnomalyConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} else {
				save();
			}
		} catch (Exception e) {
			ScpMod.LOGGER.error("[scpmod] failed to load anomaly config", e);
		}
	}

	public static void save() {
		Path path = path();
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(instance));
		} catch (Exception e) {
			ScpMod.LOGGER.error("[scpmod] failed to save anomaly config", e);
		}
	}
}
