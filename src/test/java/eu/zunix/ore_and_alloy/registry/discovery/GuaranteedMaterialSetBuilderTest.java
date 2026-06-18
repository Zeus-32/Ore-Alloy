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
}
