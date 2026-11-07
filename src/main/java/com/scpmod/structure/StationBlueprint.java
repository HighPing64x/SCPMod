package com.scpmod.structure;

import com.scpmod.content.ModBlocks;
import com.scpmod.scp.ScpRegistry;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure layout description of a containment site.
 *
 * <p>Deliberately independent of world access so the same blueprint can be applied by
 * (a) a structure piece during world generation and (b) a debug command at a player's position.
 *
 * <p>M1 base layer: a code-generated site (rooms, walls, containment units, terminals).
 * The Jigsaw/template-pool variant is a later step and will reuse the same room vocabulary.
 */
public final class StationBlueprint {
	public static final int WIDTH = 17;
	public static final int HEIGHT = 8;
	public static final int DEPTH = 17;

	/** One block placement in site-local coordinates. */
	public record Op(int x, int y, int z, BlockState state) {
	}

	/** A containment unit plus the anomaly stored inside it ('' = vacant). */
	public record Unit(int x, int y, int z, String scpId) {
	}

	/** An operation terminal. */
	public record Terminal(int x, int y, int z) {
	}

	/** Full site description. */
	public record Site(List<Op> ops, List<Unit> units, List<Terminal> terminals) {
	}

	private StationBlueprint() {
	}

	public static Site generate(RandomSource random) {
		List<Op> ops = new ArrayList<>(1024);
		List<Unit> units = new ArrayList<>(2);
		List<Terminal> terminals = new ArrayList<>(1);

		BlockState floor = Blocks.SMOOTH_STONE.defaultBlockState();
		BlockState trim = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
		BlockState wall = Blocks.STONE_BRICKS.defaultBlockState();
		BlockState ceiling = Blocks.STONE_BRICKS.defaultBlockState();
		BlockState light = Blocks.SEA_LANTERN.defaultBlockState();
		BlockState cellWall = Blocks.IRON_BARS.defaultBlockState();
		BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
		BlockState machine = Blocks.IRON_BLOCK.defaultBlockState();

		// ---- floor (y = 0) ----
		for (int x = 0; x < WIDTH; x++) {
			for (int z = 0; z < DEPTH; z++) {
				boolean edge = x == 0 || z == 0 || x == WIDTH - 1 || z == DEPTH - 1;
				ops.add(new Op(x, 0, z, edge ? trim : floor));
			}
		}

		// ---- perimeter walls (y = 1..4) with a 2-wide entrance on the -Z side ----
		for (int y = 1; y <= 4; y++) {
			for (int x = 0; x < WIDTH; x++) {
				for (int z = 0; z < DEPTH; z++) {
					boolean perimeter = x == 0 || z == 0 || x == WIDTH - 1 || z == DEPTH - 1;
					if (!perimeter) {
						continue;
					}
					boolean entrance = z == 0 && (x == WIDTH / 2 || x == WIDTH / 2 + 1) && y <= 2;
					if (!entrance) {
						ops.add(new Op(x, y, z, wall));
					}
				}
			}
		}

		// ---- interior partitions with doorways ----
		int midX = WIDTH / 2;
		int midZ = DEPTH / 2;
		for (int y = 1; y <= 4; y++) {
			for (int z = 1; z < DEPTH - 1; z++) {
				boolean doorway = (z >= 4 && z <= 5) || (z >= 11 && z <= 12);
				if (!doorway) {
					ops.add(new Op(midX, y, z, wall));
				}
			}
			for (int x = 1; x < WIDTH - 1; x++) {
				boolean doorway = (x >= 3 && x <= 4) || (x >= 12 && x <= 13);
				if (!doorway) {
					ops.add(new Op(x, y, midZ, wall));
				}
			}
		}

		// ---- ceiling (y = 5) with lights ----
		for (int x = 0; x < WIDTH; x++) {
			for (int z = 0; z < DEPTH; z++) {
				boolean lit = (x == 4 || x == 8 || x == 12) && (z == 4 || z == 8 || z == 12);
				ops.add(new Op(x, 5, z, lit ? light : ceiling));
			}
		}

		// ---- room A: containment unit on an obsidian pedestal ----
		ops.add(new Op(3, 1, 3, obsidian));
		units.add(new Unit(3, 2, 3, randomScp(random)));

		// ---- room B: second containment unit ----
		ops.add(new Op(13, 1, 3, obsidian));
		units.add(new Unit(13, 2, 3, randomScp(random)));

		// ---- room C: cell block + terminal ----
		for (int x = 5; x <= 7; x++) {
			for (int z = 9; z <= 11; z++) {
				boolean fence = x == 5 || x == 7 || z == 9 || z == 11;
				ops.add(new Op(x, 1, z, fence ? cellWall : floor));
			}
		}
		ops.add(new Op(3, 1, 12, machine));
		terminals.add(new Terminal(3, 2, 12));

		// ---- room D: machine room with terminal ----
		for (int x = 11; x <= 13; x++) {
			ops.add(new Op(x, 1, 12, machine));
		}
		terminals.add(new Terminal(13, 2, 12));

		// ---- corridor markers ----
		ops.add(new Op(8, 1, 8, light));

		return new Site(List.copyOf(ops), List.copyOf(units), List.copyOf(terminals));
	}

	private static String randomScp(RandomSource random) {
		if (ScpRegistry.size() == 0) {
			return "";
		}
		int index = random.nextInt(ScpRegistry.size());
		return ScpRegistry.all().stream().skip(index).findFirst().map(d -> d.id().toString()).orElse("");
	}

	/** Convenience for command-side placement: containment unit block state. */
	public static BlockState containmentUnit() {
		return ModBlocks.CONTAINMENT_UNIT.defaultBlockState();
	}

	/** Convenience for command-side placement: terminal block state. */
	public static BlockState terminal() {
		return ModBlocks.TERMINAL.defaultBlockState();
	}
}
