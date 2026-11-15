package com.scpmod.content;

import com.scpmod.ScpMod;
import com.scpmod.entity.Scp131Entity;
import com.scpmod.entity.Scp173Entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** 实体类型注册（SCP-173）。 */
public final class ModEntities {
	public static final EntityType<Scp173Entity> SCP_173 = Registry.register(BuiltInRegistries.ENTITY_TYPE,
			ScpMod.id("scp_173"),
			EntityType.Builder.of(Scp173Entity::new, MobCategory.MONSTER)
					.sized(0.8F, 2.0F)
					.eyeHeight(1.7F)
					.build(ResourceKey.create(Registries.ENTITY_TYPE, ScpMod.id("scp_173"))));

	public static final EntityType<Scp131Entity> SCP_131 = Registry.register(BuiltInRegistries.ENTITY_TYPE,
			ScpMod.id("scp_131"),
			EntityType.Builder.of(Scp131Entity::new, MobCategory.CREATURE)
					.sized(0.5F, 0.6F)
					.eyeHeight(0.5F)
					.build(ResourceKey.create(Registries.ENTITY_TYPE, ScpMod.id("scp_131"))));

	private ModEntities() {
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(SCP_173, Scp173Entity.createAttributes());
		FabricDefaultAttributeRegistry.register(SCP_131, Scp131Entity.createAttributes());
		ScpMod.LOGGER.debug("[scpmod] registered entity types scpmod:scp_173, scpmod:scp_131");
	}
}
