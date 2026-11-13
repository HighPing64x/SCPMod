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
 * SCP-006 不老泉：注册成结构，因此可以用 /locate structure 定位。
 * 具体形态由 {@link Scp006LakePiece} 按原版湖泊（岩浆池）算法生成。
 */
public class Scp006LakeStructure extends Structure {
	public static final MapCodec<Scp006LakeStructure> CODEC = simpleCodec(Scp006LakeStructure::new);

	public Scp006LakeStructure(StructureSettings settings) {
		super(settings);
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder -> {
			ChunkPos chunkPos = context.chunkPos();
			// 用锚点所在列的地表高度作为原点，池子就会开在地表（露天可见）。
			int y = context.chunkGenerator().getFirstOccupiedHeight(chunkPos.getMiddleBlockX(),
					chunkPos.getMiddleBlockZ(), Heightmap.Types.WORLD_SURFACE_WG,
					context.heightAccessor(), context.randomState());
			builder.addPiece(new Scp006LakePiece(
					new BlockPos(chunkPos.getMiddleBlockX(), y, chunkPos.getMiddleBlockZ()), context.seed()));
		});
	}

	@Override
	public StructureType<?> type() {
		return ModStructures.SCP_006_LAKE;
	}
}
