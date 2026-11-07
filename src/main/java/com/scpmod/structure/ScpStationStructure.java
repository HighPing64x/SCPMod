package com.scpmod.structure;

import com.scpmod.content.ModStructures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Code-registered containment site structure (M1 base layer).
 *
 * <p>Why code instead of vanilla Jigsaw: Jigsaw needs binary {@code .nbt} room templates. This
 * structure proves the generation pipeline (structure JSON -> structure_set -> biome selection ->
 * piece placement) without that dependency. The Jigsaw/template-pool variant replaces the piece
 * later and reuses the same block vocabulary.
 */
public class ScpStationStructure extends Structure {
	public static final MapCodec<ScpStationStructure> CODEC = simpleCodec(ScpStationStructure::new);

	public ScpStationStructure(StructureSettings settings) {
		super(settings);
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder -> {
			ChunkPos chunkPos = context.chunkPos();
			int x = chunkPos.getMinBlockX() + 8 - StationBlueprint.WIDTH / 2;
			int z = chunkPos.getMinBlockZ() + 8 - StationBlueprint.DEPTH / 2;
			int y = getLowestY(context, StationBlueprint.WIDTH, StationBlueprint.DEPTH);
			BlockPos origin = new BlockPos(x, y, z);
			builder.addPiece(new ScpStationPiece(origin, context.seed()));
		});
	}

	@Override
	public StructureType<?> type() {
		return ModStructures.SCP_STATION;
	}
}
