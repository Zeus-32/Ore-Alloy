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
            Map.entry("steel", "C₂Fe"),
            Map.entry("stainless_steel", "CrFeNi"),
            Map.entry("brass", "Cu₃Zn₂"),
            Map.entry("bronze", "Cu₃Sn"),
            Map.entry("cupronickel", "CuNi"),
            Map.entry("electrum", "AgAu"),
            Map.entry("invar", "Fe₂Ni"),
            Map.entry("constantan", "Cu₅₅Ni₄₅"),
            Map.entry("wrought_iron", "Fe"),
            Map.entry("enderium", "Pb₃Pt"),
            Map.entry("lithium", "Li"),
            Map.entry("lumium", "Ag₄Sn"),
            Map.entry("signalum", "AgCu₃Rs₁₀"),
            Map.entry("rose_gold", "AuCu"),
            Map.entry("naquadah", "Nq"),
            Map.entry("pure_netherite", "Nr"),
            Map.entry("red_alloy", "FeRs₄"),
            Map.entry("soul_infused_alloy", "Fe₂NiO₆Si₃"),
            Map.entry("tungsten", "W"),
            Map.entry("silicon", "Si"),
            Map.entry("diamond", "C"),
            Map.entry("ruby", "Al₂CrO₃"),
            Map.entry("sapphire", "Al₂O₃"),
            Map.entry("emerald", "Al₂Be₃O₁₈Si₆"),
            Map.entry("topaz", "Al₂F₂O₄Si"),
            Map.entry("apatite", "Ca₅FO₁₂P₃"),
            Map.entry("certus_quartz", "O₂Si")
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
        assertEquals("O₆Si₃", PeriodicMaterialSymbolResolver.resolve("soulsand_dust").orElseThrow());
        assertEquals("O₆Si₃", PeriodicMaterialSymbolResolver.resolve("soul_sand_dust").orElseThrow());
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
            case "silicon", "diamond", "ruby", "sapphire", "emerald", "topaz", "apatite", "certus_quartz" -> materialName;
            default -> materialName + "_ingot";
        };
    }
}
