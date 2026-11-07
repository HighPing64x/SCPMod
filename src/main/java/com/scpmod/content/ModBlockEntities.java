package com.scpmod.content;

import com.scpmod.ScpMod;
import com.scpmod.block.entity.ContainmentUnitBlockEntity;
import com.scpmod.block.entity.TerminalBlockEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

/** Registers block entity types (M1). */
public final class ModBlockEntities {
	private ModBlockEntities() {
	}

	public static BlockEntityType<ContainmentUnitBlockEntity> CONTAINMENT_UNIT;
	public static BlockEntityType<TerminalBlockEntity> TERMINAL;

	public static void register() {
		CONTAINMENT_UNIT = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ScpMod.id("containment_unit"),
				new BlockEntityType<>(ContainmentUnitBlockEntity::new, Set.of(ModBlocks.CONTAINMENT_UNIT)));
		TERMINAL = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ScpMod.id("terminal"),
				new BlockEntityType<>(TerminalBlockEntity::new, Set.of(ModBlocks.TERMINAL)));
	}
}
