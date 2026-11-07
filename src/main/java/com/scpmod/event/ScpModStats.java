package com.scpmod.event;

import com.scpmod.ScpMod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Persistent settlement statistics for breach events (D14). */
public final class ScpModStats {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ScpModStats instance = new ScpModStats();

	public int eventsTriggered;
	public int eventsRecontained;
	public int eventsEndedByForce;
	public int totalBreachedSeconds;

	private ScpModStats() {
	}

	public static ScpModStats get() {
		return instance;
	}

	public static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("scpmod_stats.json");
	}

	public static void load() {
		Path path = path();
		try {
			if (Files.exists(path)) {
				ScpModStats loaded = GSON.fromJson(Files.readString(path), ScpModStats.class);
				if (loaded != null) {
					instance = loaded;
				}
			}
		} catch (Exception e) {
			ScpMod.LOGGER.error("[scpmod] failed to load stats", e);
		}
	}

	public static void save() {
		Path path = path();
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(instance));
		} catch (Exception e) {
			ScpMod.LOGGER.error("[scpmod] failed to save stats", e);
		}
	}

	public static String summary() {
		return "triggered=" + instance.eventsTriggered + ", recontained=" + instance.eventsRecontained
				+ ", forced=" + instance.eventsEndedByForce + ", breachedSeconds=" + instance.totalBreachedSeconds;
	}
}
