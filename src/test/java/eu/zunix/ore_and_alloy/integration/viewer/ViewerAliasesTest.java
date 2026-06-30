package eu.zunix.ore_and_alloy.integration.viewer;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewerAliasesTest {
    @Test
    void aliasesCoverExtendedMaterialForms() {
        assertAliases(
                "aluminum_double_plate",
                Set.of("Aluminum", "double plate", "Aluminum double plate")
        );
        assertAliases(
                "aluminum_hot_ingot",
                Set.of("Aluminum", "hot ingot", "hot Aluminum ingot")
        );
        assertAliases(
                "aluminum_wire",
                Set.of("Aluminum", "wire", "Aluminum wire")
        );
    }

    @Test
    void rawStorageBlocksUseRawBlockAliases() {
        assertAliases(
                "raw_iron_block",
                Set.of("Raw Iron", "raw block", "Raw Iron block", "Raw Iron storage block")
        );
    }

    @Test
    void rawStorageBlockVariantAliasesUseCanonicalMaterialLikeRawItems() {
        Set<String> aliases = aliases("raw_magnetite_block");

        assertTrue(aliases.contains("Raw Iron"), "Aliases were " + aliases);
        assertTrue(aliases.contains("Raw Iron block"), "Aliases were " + aliases);
        assertTrue(aliases.contains("Raw Iron storage block"), "Aliases were " + aliases);
        assertFalse(aliases.contains("Raw Magnetite"), "Aliases were " + aliases);
        assertFalse(aliases.contains("Raw Magnetite block"), "Aliases were " + aliases);
    }

    @Test
    void aliasesDoNotContainDuplicates() {
        Set<String> aliases = aliases("aluminum_plate");
        assertEquals(aliases.size(), Set.copyOf(aliases).size());
    }

    private static void assertAliases(String itemPath, Set<String> expected) {
        Set<String> aliases = aliases(itemPath);
        assertTrue(aliases.containsAll(expected), "Aliases for " + itemPath + " were " + aliases);
    }

    private static Set<String> aliases(String itemPath) {
        return ViewerAliases.aliasesForItemId(ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, itemPath));
    }
}
