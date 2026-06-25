package eu.zunix.ore_and_alloy.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RawMaterialMappingsTest {
    @Test
    void variantToMaterialLookupWorks() {
        assertEquals("aluminum", RawMaterialMappings.materialForRawVariant("bauxite").orElseThrow());
        assertEquals("iron", RawMaterialMappings.materialForRawVariant("magnetite").orElseThrow());
        assertEquals("copper", RawMaterialMappings.materialForRawVariant("chalcopyrite").orElseThrow());
        assertEquals("copper", RawMaterialMappings.materialForRawVariant("copper").orElseThrow());
        assertEquals("zinc", RawMaterialMappings.materialForRawVariant("sphalerite").orElseThrow());
        assertEquals("zinc", RawMaterialMappings.materialForRawVariant("zinc").orElseThrow());
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
        List<String> ids = RawMaterialMappings.rawItemIdsForMaterial("aluminum");
        assertEquals(List.of("raw_bauxite", "raw_cryolite"), ids);
    }

    @Test
    void primaryCrushedVariantUsesFirstConfiguredEntry() {
        assertEquals("bauxite", RawMaterialMappings.primaryCrushedVariantForMaterial("aluminum").orElseThrow());
        assertEquals("unknown_material", RawMaterialMappings.primaryCrushedVariantForMaterial("unknown_material").orElseThrow());
    }

    @Test
    void zincKeepsMineralVariantsAndDirectRawZincCompatibility() {
        assertEquals(List.of("raw_sphalerite", "raw_hemimorphite"), RawMaterialMappings.rawItemIdsForMaterial("zinc"));
        assertEquals(List.of("crushed_sphalerite", "crushed_hemimorphite"), RawMaterialMappings.crushedItemIdsForMaterial("zinc"));
    }

    @Test
    void configuredOreMaterialsAlsoAcceptDirectMaterialRawNamesForUnificationOnly() {
        assertEquals("aluminum", RawMaterialMappings.materialForRawVariant("aluminum").orElseThrow());
        assertEquals("chromium", RawMaterialMappings.materialForRawVariant("chromium").orElseThrow());
        assertEquals("cobalt", RawMaterialMappings.materialForRawVariant("cobalt").orElseThrow());
        assertEquals("nickel", RawMaterialMappings.materialForRawVariant("nickel").orElseThrow());

        assertTrue(RawMaterialMappings.isConfiguredRawVariant("bauxite"));
        assertTrue(RawMaterialMappings.isConfiguredRawVariant("sphalerite"));
        assertFalse(RawMaterialMappings.isConfiguredRawVariant("aluminum"));
        assertFalse(RawMaterialMappings.isConfiguredRawVariant("zinc"));
    }
}
