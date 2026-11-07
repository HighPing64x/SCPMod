package com.scpmod.content;

import com.scpmod.ScpMod;
import com.scpmod.item.Scp173SpawnerItem;
import com.scpmod.item.Scp500Item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

/** Registers the block items and debug items. */
public final class ModItems {
	private ModItems() {
	}

	public static final Item CONTAINMENT_UNIT = registerBlockItem("containment_unit", ModBlocks.CONTAINMENT_UNIT);
	public static final Item TERMINAL = registerBlockItem("terminal", ModBlocks.TERMINAL);
	public static final Item SCP_500 = registerItem("scp_500",
			properties -> new Scp500Item(properties.stacksTo(59)));
	public static final Item SCP_173_SPAWNER = registerItem("scp_173_spawner",
			properties -> new Scp173SpawnerItem(properties.stacksTo(1)));

	/** Forces class initialisation (items must be created after blocks are registered). */
	public static void register() {
		ScpMod.LOGGER.debug("[scpmod] registered 4 items");
	}

	private static Item registerBlockItem(String name, Block block) {
		Identifier id = ScpMod.id(name);
		Item item = new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
		return Registry.register(BuiltInRegistries.ITEM, id, item);
	}

	private static Item registerItem(String name, Function<Item.Properties, Item> factory) {
		Identifier id = ScpMod.id(name);
		Item item = factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
		return Registry.register(BuiltInRegistries.ITEM, id, item);
	}
}
