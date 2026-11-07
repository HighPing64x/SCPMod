package com.scpmod.config;

import com.scpmod.ScpMod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Breach-event timing (separate settings file: config/scpmod-events.json). */
public final class EventConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static EventConfig instance = new EventConfig();

	/** Seconds between the breach warning and the anomaly being released. */
	public int breachWarningSeconds = 30;
	/** How long the "re-contained" banner stays in CONTAINED phase. */
	public int containedDisplaySeconds = 5;
	/** Enable/disable the whole event chain (kept here as well for convenience). */
	public boolean eventsEnabled = true;

	private EventConfig() {
	}

	public static EventConfig get() {
		return instance;
	}

	public static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("scpmod-events.json");
	}

	public static void load() {
		Path path = path();
		try {
			if (Files.exists(path)) {
				EventConfig loaded = GSON.fromJson(Files.readString(path), EventConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} else {
				save();
			}
		} catch (Exception e) {
			ScpMod.LOGGER.error("[scpmod] failed to load event config", e);
		}
	}

	public static void save() {
		Path path = path();
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(instance));
		} catch (Exception e) {
			ScpMod.LOGGER.error("[scpmod] failed to save event config", e);
		}
	}
}
