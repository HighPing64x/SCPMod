package com.scpmod.content;

import com.scpmod.ScpMod;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Three custom creative tabs: entities, blocks & misc, equipment. */
public final class ModCreativeTabs {
	public static final ResourceKey<CreativeModeTab> ENTITIES = key("entities");
	public static final ResourceKey<CreativeModeTab> BLOCKS = key("blocks");
	public static final ResourceKey<CreativeModeTab> EQUIPMENT = key("equipment");

	private ModCreativeTabs() {
	}

	public static void register() {
		register(ENTITIES, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.scpmod.entities"))
				.icon(() -> new ItemStack(ModItems.SCP_173_SPAWNER))
				.displayItems((context, output) -> output.accept(ModItems.SCP_173_SPAWNER))
				.build());

		register(BLOCKS, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.scpmod.blocks"))
				.icon(() -> new ItemStack(ModBlocks.CONTAINMENT_UNIT))
				.displayItems((context, output) -> {
					output.accept(ModItems.CONTAINMENT_UNIT);
					output.accept(ModItems.TERMINAL);
					output.accept(ModItems.SCP_500);
					output.accept(ModItems.SCP_005);
					output.accept(ModItems.SCP_DOCUMENT);
				})
				.build());

		// Reserved for SCP gear/equipment (empty until that content exists).
		register(EQUIPMENT, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.scpmod.equipment"))
				.icon(() -> new ItemStack(Items.IRON_CHESTPLATE))
				.displayItems((context, output) -> {
				})
				.build());
	}

	private static void register(ResourceKey<CreativeModeTab> key, CreativeModeTab tab) {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab);
	}

	private static ResourceKey<CreativeModeTab> key(String path) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, ScpMod.id(path));
	}
}
