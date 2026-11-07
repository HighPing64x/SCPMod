package com.scpmod.structure;

import com.scpmod.ScpMod;
import com.scpmod.config.EventConfig;
import com.scpmod.content.ModBlocks;
import com.scpmod.event.AnomalyController;
import com.scpmod.event.BreachEventManager;
import com.scpmod.event.ScpModStats;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import com.scpmod.scp.ScpRegistry;
import net.minecraft.core.BlockPos;
import com.scpmod.ScpMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Map;

/**
 * Optional self-test for the structure layer.
 *
 * <p>Enabled with {@code -Dscpmod.selftest=true}. On server start it asks the world for the
 * nearest generated containment site and inspects the structure start + blocks around it, so the
 * whole worldgen JSON -> structure -> piece -> blocks pipeline can be verified headlessly.
 */
public final class StructureDiagnostics {
	private static int eventTestTicks;
	private static boolean eventTestRunning;
	private static boolean sawBreached;
	private static ServerLevel eventLevel;

	private StructureDiagnostics() {
	}

	public static void register() {
		if (!Boolean.getBoolean("scpmod.selftest")) {
			return;
		}
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			try {
				runSelfTest(server.overworld());
			} catch (Exception e) {
				ScpMod.LOGGER.error("[scpmod] structure self-test failed", e);
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> tickEventTest());
	}

	private static void runSelfTest(ServerLevel level) {
		blueprintSelfTest(level);
		TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, ScpMod.id("scp_stations"));
		BlockPos center = new BlockPos(0, 64, 0);
		BlockPos found = level.findNearestMapStructure(tag, center, 64, false);
		if (found == null) {
			ScpMod.LOGGER.warn("[scpmod][selftest] no containment site found within 64 chunks of {}", center);
			return;
		}
		ScpMod.LOGGER.info("[scpmod][selftest] spawn={} nearest site reported at {}", level.getRespawnData().pos(), found);
		startEventTest(level);

		ChunkAccess chunk = level.getChunk(found.getX() >> 4, found.getZ() >> 4);
		Map<Structure, StructureStart> starts = chunk.getAllStarts();
		ScpMod.LOGGER.info("[scpmod][selftest] chunk {},{} has {} structure start(s)", found.getX() >> 4, found.getZ() >> 4, starts.size());
		for (Map.Entry<Structure, StructureStart> entry : starts.entrySet()) {
			BoundingBox box = entry.getValue().getBoundingBox();
			ScpMod.LOGGER.info("[scpmod][selftest]   start type={} valid={} box=[{},{} , {},{} , {},{}]",
					entry.getKey().type(), entry.getValue().isValid(),
					box.minX(), box.maxX(), box.minY(), box.maxY(), box.minZ(), box.maxZ());
			inspectBox(level, box);
		}
	}

	/** Validates the blueprint + block-entity path directly on a live level (no worldgen involved). */
	private static void blueprintSelfTest(ServerLevel level) {
		BlockPos origin = level.getRespawnData().pos().offset(24, 2, 24);
		StationBlueprint.Site site = StationBlueprint.generate(level.getRandom());
		for (StationBlueprint.Op op : site.ops()) {
			level.setBlock(origin.offset(op.x(), op.y(), op.z()), op.state(), 3);
		}
		int units = 0;
		int terminals = 0;
		for (StationBlueprint.Unit unit : site.units()) {
			BlockPos pos = origin.offset(unit.x(), unit.y(), unit.z());
			level.setBlock(pos, StationBlueprint.containmentUnit(), 3);
			if (level.getBlockEntity(pos) instanceof com.scpmod.block.entity.ContainmentUnitBlockEntity be) {
				be.setContainedScp(unit.scpId());
				units++;
			}
		}
		for (StationBlueprint.Terminal terminal : site.terminals()) {
			BlockPos pos = origin.offset(terminal.x(), terminal.y(), terminal.z());
			level.setBlock(pos, StationBlueprint.terminal(), 3);
			if (level.getBlockEntity(pos) instanceof com.scpmod.block.entity.TerminalBlockEntity be) {
				be.toggleArmed();
				terminals++;
			}
		}
		ScpMod.LOGGER.info("[scpmod][selftest] blueprint build at {} -> ops={} units={} terminals={}",
				origin, site.ops().size(), units, terminals);
	}

	private static void inspectBox(ServerLevel level, BoundingBox box) {
		int units = 0;
		int terminals = 0;
		int bricks = 0;
		int scanned = 0;
		for (int x = box.minX(); x <= box.maxX(); x++) {
			for (int y = box.minY(); y <= box.maxY(); y++) {
				for (int z = box.minZ(); z <= box.maxZ(); z++) {
					scanned++;
					var state = level.getBlockState(new BlockPos(x, y, z));
					if (state.is(ModBlocks.CONTAINMENT_UNIT)) {
						units++;
					} else if (state.is(ModBlocks.TERMINAL)) {
						terminals++;
					} else if (state.is(net.minecraft.world.level.block.Blocks.STONE_BRICKS)) {
						bricks++;
					}
				}
			}
		}
		ScpMod.LOGGER.info("[scpmod][selftest]   scanned={} stone_bricks={} containment_units={} terminals={}", scanned, bricks, units, terminals);
	}

	/** Kicks off the breach-event self-test: 3s warning, then breach, then re-contain. */
	private static void startEventTest(ServerLevel level) {
		EventConfig.get().breachWarningSeconds = 3;
		eventLevel = level;
		String scpId = ScpRegistry.all().stream().findFirst().map(d -> d.id().toString()).orElse("");
		boolean started = BreachEventManager.start(level, level.getRespawnData().pos(), scpId);
		eventTestRunning = started;
		eventTestTicks = 0;
		sawBreached = false;
		ScpMod.LOGGER.info("[scpmod][selftest] event test started={} scp={} neckSnapDamageTypeLoaded={}", started, scpId,
				level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(ScpMod.NECK_SNAP).isPresent());
	}

	private static void tickEventTest() {
		if (!eventTestRunning) {
			return;
		}
		eventTestTicks++;
		BreachEventManager.Phase phase = BreachEventManager.phase();
		if (phase == BreachEventManager.Phase.WARNING && eventTestTicks == 10) {
			ScpMod.LOGGER.info("[scpmod][selftest] event phase=WARNING remainingTicks={}", BreachEventManager.ticksRemaining());
		}
		if (phase == BreachEventManager.Phase.BREACHED && !sawBreached) {
			sawBreached = true;
			ScpMod.LOGGER.info("[scpmod][selftest] event phase=BREACHED after {} ticks, anomalyBodyActive={}", eventTestTicks, AnomalyController.isActive());
		}
		if (phase == BreachEventManager.Phase.BREACHED && eventTestTicks > 100) {
			boolean ok = BreachEventManager.tryRecontain(eventLevel);
			ScpMod.LOGGER.info("[scpmod][selftest] recontain attempted ok={} -> phase={}", ok, BreachEventManager.phase());
		}
		if (phase == BreachEventManager.Phase.CONTAINED) {
			ScpMod.LOGGER.info("[scpmod][selftest] event phase=CONTAINED, anomalyBodyActive={}, stats: {}", AnomalyController.isActive(), ScpModStats.summary());
			eventTestRunning = false;
		}
		if (eventTestTicks > 600) {
			ScpMod.LOGGER.warn("[scpmod][selftest] event test timeout, phase={}", phase);
			eventTestRunning = false;
		}
	}
}
