package com.scpmod;

import com.scpmod.command.ScpCommand;
import com.scpmod.config.AnomalyConfig;
import com.scpmod.config.EventConfig;
import com.scpmod.config.ScpModConfig;
import com.scpmod.content.ModBlockEntities;
import com.scpmod.content.ModBlocks;
import com.scpmod.content.ModCreativeTabs;
import com.scpmod.content.ModEntities;
import com.scpmod.content.ModItems;
import com.scpmod.content.ModSounds;
import com.scpmod.content.ModStructures;
import com.scpmod.event.AnomalyController;
import com.scpmod.event.BreachEventManager;
import com.scpmod.event.ServerTickHandler;
import com.scpmod.scp.ScpRegistry;
import com.scpmod.structure.StructureDiagnostics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point of the SCP containment framework (M1: foundation layer).
 *
 * <p>Registration order matters: blocks -> block entity types -> items -> SCP registry -> commands.
 */
public class ScpMod implements ModInitializer {
	public static final String MOD_ID = "scpmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	/** Custom damage type used by SCP-173 (drives the death message, see data/scpmod/damage_type/neck_snap.json). */
	public static final ResourceKey<DamageType> NECK_SNAP = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "neck_snap"));

	@Override
	public void onInitialize() {
		ScpModConfig.load();
		EventConfig.load();
		AnomalyConfig.load();

		ModBlocks.register();
		ModBlockEntities.register();
		ModItems.register();
		ModSounds.register();
		ModCreativeTabs.register();
		ModEntities.register();
		ModStructures.register();
		ScpRegistry.bootstrap();

		ScpCommand.register();
		ServerTickHandler.register();
		StructureDiagnostics.register();
		BreachEventManager.init();
		// 玩家重生时重置异常的目标状态（保证重生保护从重生瞬间开始计时）。
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> AnomalyController.noteRespawn(newPlayer));

		LOGGER.info("[scpmod] framework initialised: blocks={}, scps={}", ModBlocks.count(), ScpRegistry.size());
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
