package eu.zunix.ore_and_alloy.item.tooltip;

import eu.zunix.ore_and_alloy.core.MetalMaterial;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodicMaterialSymbolResolverTest {
    private static final Map<String, String> EXPECTED_FORMULAS = Map.ofEntries(
            Map.entry("iron", "Fe"),
            Map.entry("gold", "Au"),
            Map.entry("copper", "Cu"),
            Map.entry("tin", "Sn"),
            Map.entry("lead", "Pb"),
            Map.entry("silver", "Ag"),
            Map.entry("nickel", "Ni"),
            Map.entry("zinc", "Zn"),
            Map.entry("aluminum", "Al"),
            Map.entry("osmium", "Os"),
            Map.entry("uranium", "U"),
            Map.entry("cobalt", "Co"),
            Map.entry("titanium", "Ti"),
            Map.entry("chromium", "Cr"),
            Map.entry("platinum", "Pt"),
            Map.entry("iridium", "Ir"),
            Map.entry("antimony", "Sb"),
            Map.entry("steel", "FeC2"),
            Map.entry("stainless_steel", "FeCrNi"),
            Map.entry("brass", "Cu3Zn2"),
            Map.entry("bronze", "Cu3Sn"),
            Map.entry("cupronickel", "CuNi"),
            Map.entry("electrum", "AuAg"),
            Map.entry("invar", "Fe2Ni"),
            Map.entry("constantan", "Cu55Ni45"),
            Map.entry("wrought_iron", "Fe"),
            Map.entry("enderium", "Pb3Pt"),
            Map.entry("lithium", "Li"),
            Map.entry("lumium", "SnAg4"),
            Map.entry("signalum", "Cu3AgRs10"),
            Map.entry("rose_gold", "AuCu"),
            Map.entry("naquadah", "Nq"),
            Map.entry("pure_netherite", "Nr"),
            Map.entry("red_alloy", "FeRs4"),
            Map.entry("soul_infused", "Fe2NiSi3O6"),
            Map.entry("tungsten", "W"),
            Map.entry("silicon", "Si"),
            Map.entry("diamond", "C")
    );

    @Test
    void allMaterialsHaveExplicitTooltipFormulas() {
        for (MetalMaterial material : MetalMaterial.values()) {
            String materialName = material.materialName();
            assertTrue(EXPECTED_FORMULAS.containsKey(materialName), "Missing expected formula for " + materialName);
            assertEquals(
                    EXPECTED_FORMULAS.get(materialName),
                    PeriodicMaterialSymbolResolver.resolve(itemIdFor(materialName)).orElseThrow(),
                    "Formula mismatch for " + materialName
            );
        }
    }

    @Test
    void rawVariantsResolveToMappedMaterialFormula() {
        assertEquals("Al", PeriodicMaterialSymbolResolver.resolve("raw_bauxite").orElseThrow());
        assertEquals("Cr", PeriodicMaterialSymbolResolver.resolve("crushed_chromite").orElseThrow());
        assertEquals("C", PeriodicMaterialSymbolResolver.resolve("diamond").orElseThrow());
    }

    @Test
    void foreignMaterialItemsResolveFromPathConventions() {
        assertEquals("3SiO2", PeriodicMaterialSymbolResolver.resolve("soulsand_dust").orElseThrow());
        assertEquals("3SiO2", PeriodicMaterialSymbolResolver.resolve("soul_sand_dust").orElseThrow());
        assertEquals("Sn", PeriodicMaterialSymbolResolver.resolve("tin_dust").orElseThrow());
    }

    @Test
    void pureNetheriteIsDistinctFromVanillaNetherite() {
        assertEquals("AuNr", PeriodicMaterialSymbolResolver.resolve("netherite_ingot").orElseThrow());
        assertEquals("Nr", PeriodicMaterialSymbolResolver.resolve("pure_netherite_ingot").orElseThrow());
    }

    @Test
    void unknownMaterialsDoNotGetGeneratedFormulas() {
        assertTrue(PeriodicMaterialSymbolResolver.resolve("unknownium_dust").isEmpty());
    }

    private static String itemIdFor(String materialName) {
        return switch (materialName) {
            case "silicon", "diamond" -> materialName;
            default -> materialName + "_ingot";
        };
    }
}
