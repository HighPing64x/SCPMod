package com.scpmod.content;

import com.scpmod.ScpMod;
import com.scpmod.structure.ScpStationPiece;
import com.scpmod.structure.Scp006LakePiece;
import com.scpmod.structure.Scp006LakeStructure;
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
	public static StructureType<Scp006LakeStructure> SCP_006_LAKE;
	public static StructurePieceType SCP_006_LAKE_PIECE;
	public static StructurePieceType SCP_STATION_PIECE;

	public static void register() {
		SCP_STATION = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, ScpMod.id("scp_station"),
				() -> ScpStationStructure.CODEC);
		SCP_STATION_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE, ScpMod.id("scp_station"),
				(context, tag) -> new ScpStationPiece(tag));
		SCP_006_LAKE = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, ScpMod.id("scp_006_lake"),
				() -> Scp006LakeStructure.CODEC);
		SCP_006_LAKE_PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE, ScpMod.id("scp_006_lake"),
				(context, tag) -> new Scp006LakePiece(tag));
	}
}
