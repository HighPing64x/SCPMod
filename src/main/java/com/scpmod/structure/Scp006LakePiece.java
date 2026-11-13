package com.scpmod.structure;

import com.scpmod.content.ModBlocks;
import com.scpmod.content.ModStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * 原版 {@code minecraft:lake}（岩浆池用的算法）的等价移植：同样的随机 blob 形状、
 * 同样的地形合法性检查、同样的「内部填液体 / 上方清空 / 边缘补石头」放置规则，
 * 只把液体换成 SCP-006、围壁换成石头。
 */
public class Scp006LakePiece extends StructurePiece {
	private static final String KEY_SEED = "layout_seed";
	private static final int BOX_HALF = 20;
	private final long layoutSeed;

	public Scp006LakePiece(BlockPos origin, long layoutSeed) {
		super(ModStructures.SCP_006_LAKE_PIECE, 0,
				BoundingBox.fromCorners(origin.offset(-BOX_HALF, -BOX_HALF, -BOX_HALF),
						origin.offset(BOX_HALF, BOX_HALF, BOX_HALF)));
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
		BlockPos origin = new BlockPos(this.boundingBox.minX() + BOX_HALF, this.boundingBox.minY() + BOX_HALF,
				this.boundingBox.minZ() + BOX_HALF);
		if (origin.getY() <= level.getMinY() + 4) {
			return;
		}
		RandomSource rnd = RandomSource.create(this.layoutSeed);
		BlockState liquid = ModBlocks.SCP_006.defaultBlockState().setValue(LiquidBlock.LEVEL, 0);
		BlockState stone = Blocks.STONE.defaultBlockState();
		BlockPos base = origin.offset(-8, -4, -8);
		boolean[] map = new boolean[16 * 16 * 8];

		// 1) 随机生成 4~7 个椭球 blob，组成不规则的池形（与原版一致）
		int blobs = rnd.nextInt(4) + 4;
		for (int i = 0; i < blobs; i++) {
			double sizeX = rnd.nextDouble() * 6.0D + 3.0D;
			double sizeY = rnd.nextDouble() * 4.0D + 2.0D;
			double sizeZ = rnd.nextDouble() * 6.0D + 3.0D;
			double centerX = rnd.nextDouble() * (16.0D - sizeX - 2.0D) + 1.0D + sizeX / 2.0D;
			double centerY = rnd.nextDouble() * (8.0D - sizeY - 4.0D) + 2.0D + sizeY / 2.0D;
			double centerZ = rnd.nextDouble() * (16.0D - sizeZ - 2.0D) + 1.0D + sizeZ / 2.0D;
			for (int x = 1; x < 15; x++) {
				for (int z = 1; z < 15; z++) {
					for (int y = 1; y < 7; y++) {
						double nx = ((double) x - centerX) / (sizeX / 2.0D);
						double ny = ((double) y - centerY) / (sizeY / 2.0D);
						double nz = ((double) z - centerZ) / (sizeZ / 2.0D);
						if (nx * nx + ny * ny + nz * nz < 1.0D) {
							map[(x * 16 + z) * 8 + y] = true;
						}
					}
				}
			}
		}

		// 3) 放置：池内填液体、液面以上清空、紧邻的一圈补石头（与原版一致）
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < 8; y++) {
					BlockPos world = base.offset(x, y, z);
					int localX = BOX_HALF - 8 + x;
					int localY = BOX_HALF - 4 + y;
					int localZ = BOX_HALF - 8 + z;
					if (map[(x * 16 + z) * 8 + y]) {
						this.placeBlock(level, y >= 4 ? Blocks.AIR.defaultBlockState() : liquid,
								localX, localY, localZ, chunkBox);
					} else {
						boolean neighbour = (x < 15 && map[((x + 1) * 16 + z) * 8 + y]
								|| x > 0 && map[((x - 1) * 16 + z) * 8 + y]
								|| z < 15 && map[(x * 16 + z + 1) * 8 + y]
								|| z > 0 && map[(x * 16 + z - 1) * 8 + y]
								|| y < 7 && map[(x * 16 + z) * 8 + y + 1]
								|| y > 0 && map[(x * 16 + z) * 8 + y - 1]);
						if (!neighbour) {
							continue;
						}
						// 池沿处理：
						// - 液面以下（y < 4）：一律铺石头，池底/池壁全石质，避免泥土草地透出；
						// - 液面以上：高于原地表的（露天）不放石头；低于原地表（凹陷）时，
						//   若原方块非实心则补石头当岸壁。
						if (y < 4) {
							this.placeBlock(level, stone, localX, localY, localZ, chunkBox);
						} else {
							int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, world.getX(), world.getZ());
							if (world.getY() >= surfaceY) {
								continue;
							}
							if (!level.getBlockState(world).isSolid()) {
								this.placeBlock(level, stone, localX, localY, localZ, chunkBox);
							}
						}
					}
				}
			}
		}
		com.scpmod.ScpMod.LOGGER.info("[scpmod] scp006 lake at {} -> placed", origin);
	}
}
