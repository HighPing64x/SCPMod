package com.scpmod.scp;

import com.scpmod.ScpMod;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Code-level SCP registry (D23): new anomalies are registered here by adding a definition. */
public final class ScpRegistry {
	private static final Map<Identifier, ScpDefinition> REGISTRY = new LinkedHashMap<>();

	private ScpRegistry() {
	}

	public static void bootstrap() {
		// M1 reference anomaly: SCP-173 (used to validate the framework end to end in M2).
		register(new ScpDefinition(ScpMod.id("scp_173"), "scp.scpmod.scp_173", 3));
	}

	public static ScpDefinition register(ScpDefinition definition) {
		REGISTRY.put(definition.id(), definition);
		return definition;
	}

	public static ScpDefinition get(Identifier id) {
		return id == null ? null : REGISTRY.get(id);
	}

	public static ScpDefinition get(String id) {
		return id == null || id.isEmpty() ? null : get(Identifier.tryParse(id));
	}

	public static Collection<ScpDefinition> all() {
		return REGISTRY.values();
	}

	public static int size() {
		return REGISTRY.size();
	}

	/** Translation key for a stored scp id; unknown ids fall back to a generic key. */
	public static String nameKey(String id) {
		ScpDefinition definition = get(id);
		return definition == null ? "scp.scpmod.unknown" : definition.nameKey();
	}

	public static Component displayName(String id) {
		return Component.translatable(nameKey(id));
	}
}
