package com.scpmod.config;

import com.scpmod.ScpMod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** JSON configuration (D20): no third-party config library. */
public final class ScpModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ScpModConfig instance = new ScpModConfig();

	/** Master switch for breach events. */
	public boolean breachEventsEnabled = true;
	/** Probability per player per minute of an environmental breach trigger (0 disables). */
	public double environmentalTriggerChance = 0.02;
	/** Verbose logging for development. */
	public boolean debugLogging = false;

	private ScpModConfig() {
	}

	public static ScpModConfig get() {
		return instance;
	}

	public static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("scpmod.json");
	}

	public static void load() {
		Path path = path();
		try {
			if (Files.exists(path)) {
				ScpModConfig loaded = GSON.fromJson(Files.readString(path), ScpModConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} else {
				save();
			}
			ScpMod.LOGGER.info("[scpmod] config loaded from {}", path);
		} catch (Exception e) {
			ScpMod.LOGGER.error("[scpmod] failed to load config, using defaults", e);
		}
	}

	public static void reload() {
		load();
	}

	public static void save() {
		Path path = path();
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(instance));
		} catch (IOException e) {
			ScpMod.LOGGER.error("[scpmod] failed to save config", e);
		}
	}
}
