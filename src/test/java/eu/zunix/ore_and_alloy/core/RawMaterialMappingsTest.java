package eu.zunix.ore_and_alloy.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RawMaterialMappingsTest {
    @Test
    void variantToMaterialLookupWorks() {
        assertEquals("aluminum", RawMaterialMappings.materialForRawVariant("bauxite").orElseThrow());
        assertEquals("iron", RawMaterialMappings.materialForRawVariant("magnetite").orElseThrow());
        assertEquals("copper", RawMaterialMappings.materialForRawVariant("chalcopyrite").orElseThrow());
        assertEquals("copper", RawMaterialMappings.materialForRawVariant("copper").orElseThrow());
    }

    @Test
    void rawAndCrushedItemIdsFollowConfiguredVariants() {
        List<String> copperRaw = RawMaterialMappings.rawItemIdsForMaterial("copper");
        List<String> copperCrushed = RawMaterialMappings.crushedItemIdsForMaterial("copper");

        assertEquals(List.of("raw_chalcopyrite", "raw_malachite", "raw_bornite", "raw_copper"), copperRaw);
        assertEquals(List.of("crushed_chalcopyrite", "crushed_malachite", "crushed_bornite", "crushed_copper"), copperCrushed);
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
