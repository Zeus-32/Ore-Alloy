package eu.zunix.ore_and_alloy.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RawMaterialMappingsTest {
    @Test
    void variantToMaterialLookupWorks() {
        assertEquals("aluminum", RawMaterialMappings.materialForRawVariant("bauxite").orElseThrow());
        assertEquals("aluminum", RawMaterialMappings.materialForRawVariant("cryolite").orElseThrow());
        assertEquals("iron", RawMaterialMappings.materialForRawVariant("magnetite").orElseThrow());
    }

    @Test
    void rawAndCrushedItemIdsFollowConfiguredVariants() {
        List<String> aluminumRaw = RawMaterialMappings.rawItemIdsForMaterial("aluminum");
        List<String> aluminumCrushed = RawMaterialMappings.crushedItemIdsForMaterial("aluminum");

        assertEquals(List.of("raw_bauxite", "raw_cryolite"), aluminumRaw);
        assertEquals(List.of("crushed_bauxite", "crushed_cryolite"), aluminumCrushed);
    }

    @Test
    void canonicalAliasInputIsAccepted() {
        List<String> ids = RawMaterialMappings.rawItemIdsForMaterial("aluminium");
        assertEquals(List.of("raw_bauxite", "raw_cryolite"), ids);
    }

    @Test
    void primaryCrushedVariantUsesFirstConfiguredEntry() {
        assertEquals("bauxite", RawMaterialMappings.primaryCrushedVariantForMaterial("aluminum").orElseThrow());
        assertEquals("unknown_material", RawMaterialMappings.primaryCrushedVariantForMaterial("unknown_material").orElseThrow());
    }
}
