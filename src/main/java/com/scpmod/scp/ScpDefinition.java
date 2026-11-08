package com.scpmod.scp;

import net.minecraft.resources.Identifier;

/**
 * Static definition of one anomaly.
 *
 * @param id             registry id, e.g. "scpmod:scp_173"
 * @param nameKey        translation key for the display name
 * @param descriptionKey translation key for the codex description
 * @param threat         direction-level threat tier (1 = low, 5 = extreme)
 * @param classification containment class (Safe / Euclid / Keter / Thaumiel)
 */
public record ScpDefinition(Identifier id, String nameKey, String descriptionKey, int threat, String classification) {
}
