package com.scpmod.content;

import com.scpmod.ScpMod;
import com.scpmod.structure.ScpStationPiece;
import com.scpmod.structure.ScpStationStructure;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/** Registers the structure types and structure piece types (M1 base layer). */
public final class ModStructures {
	private ModStructures() {
	}

	public static StructureType<ScpStationStructure> SCP_STATION;
	public static StructurePieceType SCP_STATION_PIECE;

	public static void register() {
		SCP_STATION = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, ScpMod.id("scp_station"),
				() -> ScpStationStructure.CODEC);
		SCP_STATION_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE, ScpMod.id("scp_station"),
				(context, tag) -> new ScpStationPiece(tag));
	}
}
