package com.scpmod.structure;

import com.scpmod.content.ModStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.Optional;

/** 执行原版 LakeFeature 生成的不老泉池。 */
public class Scp006LakePiece extends StructurePiece {
	private static final String KEY_SEED = "layout_seed";
	private final long layoutSeed;
	private static final LakeFeature LAKE = new LakeFeature(LakeFeature.Configuration.CODEC);

	public Scp006LakePiece(BlockPos origin, long layoutSeed) {
		super(ModStructures.SCP_006_LAKE_PIECE, 0,
				BoundingBox.fromCorners(origin.offset(-20, -20, -20), origin.offset(20, 20, 20)));
		this.layoutSeed = layoutSeed;
		this.setOrientation(Direction.NORTH);
	}

	public Scp006LakePiece(CompoundTag tag) {
		super(ModStructures.SCP_006_LAKE_PIECE, tag);
		this.layoutSeed = tag.getLongOr(KEY_SEED, 0L);
		this.setOrientation(Direction.NORTH);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		tag.putLong(KEY_SEED, this.layoutSeed);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
			RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
		// 盒是以 origin 为中心建的（±20），因此原点在盒中心；直接 getWorldPos(0,0,0) 会偏移 20。
		BlockPos origin = new BlockPos(this.boundingBox.minX() + 20, this.boundingBox.minY() + 20,
				this.boundingBox.minZ() + 20);
		LakeFeature.Configuration config = Scp006LakeStructure.lakeConfiguration();
		FeaturePlaceContext<LakeFeature.Configuration> placeContext = new FeaturePlaceContext<>(
				Optional.empty(), level, generator, RandomSource.create(this.layoutSeed), origin, config);
		boolean placed = LAKE.place(placeContext);
		com.scpmod.ScpMod.LOGGER.info("[scpmod] scp006 lake at {} -> {}", origin, (placed ? "placed" : "FAILED"));
	}
}
