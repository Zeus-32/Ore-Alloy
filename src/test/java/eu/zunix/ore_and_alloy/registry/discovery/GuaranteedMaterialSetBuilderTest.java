package eu.zunix.ore_and_alloy.registry.discovery;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuaranteedMaterialSetBuilderTest {
    @Test
    void mappedRawAndCrushedVariantsRemainDistinct() {
        List<String> items = new GuaranteedMaterialSetBuilder().withGuaranteedForms(List.of(
                "copper_ingot",
                "raw_chalcopyrite",
                "raw_copper",
                "raw_legacy_copper",
                "crushed_chalcopyrite",
                "crushed_copper",
                "crushed_legacy_copper"
        ));

        assertTrue(items.contains("raw_chalcopyrite"));
        assertTrue(items.contains("raw_copper"));
        assertTrue(items.contains("crushed_chalcopyrite"));
        assertTrue(items.contains("crushed_copper"));
        assertFalse(items.contains("raw_legacy_copper"));
        assertFalse(items.contains("crushed_legacy_copper"));
    }

    @Test
    void singleFormMaterialsDoNotGainMissingTextureForms() {
        List<String> items = new GuaranteedMaterialSetBuilder().withGuaranteedForms(List.of("silicon"));

        assertTrue(items.contains("silicon"));
        assertFalse(items.contains("silicon_ingot"));
        assertFalse(items.contains("silicon_plate"));
        assertFalse(items.contains("silicon_dust"));
        assertFalse(items.contains("silicon_gear"));
    }

    @Test
    void diamondUsesBareGemForm() {
        List<String> items = new GuaranteedMaterialSetBuilder().withGuaranteedForms(List.of("diamond"));

        assertTrue(items.contains("diamond"));
        assertTrue(items.contains("raw_diamond"));
        assertTrue(items.contains("crushed_diamond"));
        assertTrue(items.contains("diamond_dust"));
        assertTrue(items.contains("diamond_nugget"));
        assertTrue(items.contains("diamond_plate"));
        assertTrue(items.contains("diamond_rod"));
        assertTrue(items.contains("diamond_gear"));
        assertTrue(items.contains("diamond_bolt"));
        assertTrue(items.contains("diamond_screw"));
        assertFalse(items.contains("diamond_ingot"));
    }
}
