package com.scpmod.content;

import com.scpmod.ScpMod;
import com.scpmod.block.ContainmentUnitBlock;
import com.scpmod.block.TerminalBlock;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

/**
 * Registers the framework blocks.
 *
 * <p>Note for MC 26.x: {@link BlockBehaviour.Properties} requires the block id ("setId") before a
 * Block is constructed, otherwise {@code effectiveDrops()} throws at runtime. Vanilla does this in
 * its own register helper; we mirror that here.
 */
public final class ModBlocks {
	private ModBlocks() {
	}

	private static int COUNT = 0;

	public static final Block CONTAINMENT_UNIT = register("containment_unit", ContainmentUnitBlock::new,
			BlockBehaviour.Properties.of().strength(6.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());
	public static final Block TERMINAL = register("terminal", TerminalBlock::new,
			BlockBehaviour.Properties.of().strength(4.0F, 1200.0F).sound(SoundType.METAL).noOcclusion());

	/** Forces class initialisation order (blocks must exist before items reference them). */
	public static void register() {
		ScpMod.LOGGER.debug("[scpmod] registered {} blocks", COUNT);
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		Identifier id = ScpMod.id(name);
		Block block = factory.apply(properties.setId(ResourceKey.create(Registries.BLOCK, id)));
		COUNT++;
		return Registry.register(BuiltInRegistries.BLOCK, id, block);
	}

	public static int count() {
		return COUNT;
	}
}
