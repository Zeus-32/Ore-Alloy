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
        assertEquals("wrought_iron", MetalMaterial.WROUGHT_IRON.materialName());
        assertEquals("antimony", MetalMaterial.ANTIMONY.materialName());
        assertEquals("stainless_steel", MetalMaterial.STAINLESS_STEEL.materialName());
        assertEquals("rose_gold", MetalMaterial.ROSE_GOLD.materialName());
        assertEquals("pure_netherite", MetalMaterial.PURE_NETHERITE.materialName());
        assertEquals("silicon", MetalMaterial.SILICON.materialName());
        assertEquals("diamond", MetalMaterial.DIAMOND.materialName());
    }

    @Test
    void bareFormsAreNotUsedForMetalMaterials() {
        assertTrue(MaterialItemOrder.bareItemForm("iron").isEmpty());
        assertTrue(MaterialItemOrder.bareItemForm("copper").isEmpty());
        assertEquals("silicon", MaterialItemOrder.bareItemForm("silicon").orElseThrow());
        assertEquals("gem", MaterialItemOrder.bareItemForm("diamond").orElseThrow());
        assertEquals("gem", MaterialItemOrder.bareItemForm("ruby").orElseThrow());
        assertEquals("gem", MaterialItemOrder.bareItemForm("certus_quartz").orElseThrow());
    }

    @Test
    void materialAndFormParsingUsesVariantNormalization() {
        assertEquals("aluminum", MaterialItemOrder.materialPart("raw_bauxite"));
        assertEquals("raw", MaterialItemOrder.formToken("raw_bauxite").orElseThrow());

        assertEquals("iron", MaterialItemOrder.materialPart("deepslate_magnetite_ore"));
        assertEquals("ore", MaterialItemOrder.formToken("deepslate_magnetite_ore").orElseThrow());

        assertEquals("zinc", MaterialItemOrder.materialPart("raw_zinc"));
        assertEquals("zinc", MaterialItemOrder.materialPart("raw_sphalerite"));
        assertEquals("zinc", MaterialItemOrder.materialPart("crushed_sphalerite"));
        assertEquals("zinc", MaterialItemOrder.materialPart("deepslate_sphalerite_ore"));
    }

    @Test
    void formOrderKeepsOreBeforeRawBeforeCrushed() {
        int ore = MaterialItemOrder.formTokenRank("ore");
        int raw = MaterialItemOrder.formTokenRank("raw");
        int crushed = MaterialItemOrder.formTokenRank("crushed");

        assertTrue(ore < raw);
        assertTrue(raw < crushed);
    }

    @Test
    void gemBareFormsSortWithIngots() {
        assertEquals(
                MaterialItemOrder.formTokenRank("ingot"),
                MaterialItemOrder.formTokenRank("gem")
        );
    }
}
