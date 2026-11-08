package com.scpmod.structure;

import com.scpmod.content.ModBlocks;
import com.scpmod.content.ModStructures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * SCP-006 不老泉：作为结构注册（因此可被 /locate structure 搜索），
 * 生成时直接调用原版岩浆池使用的 {@link LakeFeature} 算法（液体池 + 石壁）。
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
			int y = getLowestY(context, 16, 16);
			builder.addPiece(new Scp006LakePiece(new BlockPos(chunkPos.getMinBlockX() + 8, y, chunkPos.getMinBlockZ() + 8),
					context.seed()));
		});
	}

	@Override
	public StructureType<?> type() {
		return ModStructures.SCP_006_LAKE;
	}

	/** 构造与原版 lake_lava 等价的配置（SCP-006 液体 + 石头围壁）。 */
	static LakeFeature.Configuration lakeConfiguration() {
		BlockState fluid = ModBlocks.SCP_006.defaultBlockState().setValue(LiquidBlock.LEVEL, 0);
		return new LakeFeature.Configuration(
				BlockStateProvider.simple(fluid),
				BlockStateProvider.simple(Blocks.STONE),
				BlockPredicate.allOf(),
				BlockPredicate.allOf(),
				BlockPredicate.allOf());
	}
}
