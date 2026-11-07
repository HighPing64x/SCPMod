package com.scpmod.structure;

import com.scpmod.block.entity.ContainmentUnitBlockEntity;
import com.scpmod.block.entity.TerminalBlockEntity;
import com.scpmod.ScpMod;
import com.scpmod.content.ModStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** Generates one containment site from {@link StationBlueprint} during world generation. */
public class ScpStationPiece extends StructurePiece {
	private static final String KEY_SEED = "layout_seed";

	private final long layoutSeed;

	public ScpStationPiece(BlockPos origin, long layoutSeed) {
		super(ModStructures.SCP_STATION_PIECE, 0,
				BoundingBox.fromCorners(origin, origin.offset(StationBlueprint.WIDTH - 1, StationBlueprint.HEIGHT - 1, StationBlueprint.DEPTH - 1)));
		this.layoutSeed = layoutSeed;
		// MC 26.x: getWorldPos() only maps local coords to world coords when the piece has an
		// orientation (vanilla pieces set one; without it every coordinate stays local).
		this.setOrientation(Direction.NORTH);
	}

	public ScpStationPiece(CompoundTag tag) {
		super(ModStructures.SCP_STATION_PIECE, tag);
		this.layoutSeed = tag.getLongOr(KEY_SEED, 0L);
		this.setOrientation(Direction.NORTH);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		tag.putLong(KEY_SEED, this.layoutSeed);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
			BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
		StationBlueprint.Site site = StationBlueprint.generate(RandomSource.create(this.layoutSeed));
		boolean debug = Boolean.getBoolean("scpmod.selftest");
		if (debug) {
			ScpMod.LOGGER.info("[scpmod][debug] postProcess chunk={} pieceBox=[{},{},{},{},{},{}] chunkBox=[{},{},{},{},{},{}] ops={}",
					chunkPos,
					this.boundingBox.minX(), this.boundingBox.maxX(), this.boundingBox.minY(), this.boundingBox.maxY(), this.boundingBox.minZ(), this.boundingBox.maxZ(),
					chunkBox.minX(), chunkBox.maxX(), chunkBox.minY(), chunkBox.maxY(), chunkBox.minZ(), chunkBox.maxZ(),
					site.ops().size());
		}

		for (StationBlueprint.Op op : site.ops()) {
			this.placeBlock(level, op.state(), op.x(), op.y(), op.z(), chunkBox);
		}

		for (StationBlueprint.Unit unit : site.units()) {
			this.placeBlock(level, StationBlueprint.containmentUnit(), unit.x(), unit.y(), unit.z(), chunkBox);
			BlockPos pos = this.getWorldPos(unit.x(), unit.y(), unit.z());
			if (chunkBox.isInside(pos) && level.getBlockEntity(pos) instanceof ContainmentUnitBlockEntity be) {
				be.setContainedScp(unit.scpId());
			}
		}

		if (debug && !site.ops().isEmpty()) {
			StationBlueprint.Op first = site.ops().get(0);
			BlockPos checkPos = this.getWorldPos(first.x(), first.y(), first.z());
			ScpMod.LOGGER.info("[scpmod][debug] first op worldPos={} insideChunkBox={} stateAfter={}",
					checkPos, chunkBox.isInside(checkPos), level.getBlockState(checkPos));
		}

		for (StationBlueprint.Terminal terminal : site.terminals()) {
			this.placeBlock(level, StationBlueprint.terminal(), terminal.x(), terminal.y(), terminal.z(), chunkBox);
			BlockPos pos = this.getWorldPos(terminal.x(), terminal.y(), terminal.z());
			if (chunkBox.isInside(pos) && level.getBlockEntity(pos) instanceof TerminalBlockEntity be) {
				be.toggleArmed();
			}
		}
	}
}
