package eu.zunix.ore_and_alloy.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaterialItemOrderTest {
    @Test
    void canonicalMaterialAliasesAreNormalized() {
        assertEquals("aluminum", MaterialItemOrder.canonicalMaterialToken("aluminium"));
        assertEquals("cupronickel", MaterialItemOrder.canonicalMaterialToken("cuppronickel"));
        assertEquals("chromium", MaterialItemOrder.canonicalMaterialToken("chrome"));
    }

    @Test
    void bareFormsResolveForVanillaLikeIds() {
        assertEquals("gem", MaterialItemOrder.bareItemForm("diamond").orElseThrow());
        assertEquals("dust", MaterialItemOrder.bareItemForm("redstone").orElseThrow());
    }

    @Test
    void materialAndFormParsingUsesVariantNormalization() {
        assertEquals("aluminum", MaterialItemOrder.materialPart("raw_bauxite"));
        assertEquals("raw", MaterialItemOrder.formToken("raw_bauxite").orElseThrow());

        assertEquals("iron", MaterialItemOrder.materialPart("deepslate_magnetite_ore"));
        assertEquals("ore", MaterialItemOrder.formToken("deepslate_magnetite_ore").orElseThrow());
    }

    @Test
    void formOrderKeepsOreBeforeRawBeforeCrushed() {
        int ore = MaterialItemOrder.formTokenRank("ore");
        int raw = MaterialItemOrder.formTokenRank("raw");
        int crushed = MaterialItemOrder.formTokenRank("crushed");

        assertTrue(ore < raw);
        assertTrue(raw < crushed);
    }
}
