package eu.zunix.ore_and_alloy.datagen.material;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaterialIdParserTest {
    @Test
    void parseSuffixAndPrefixForms() {
        MaterialId ingot = MaterialIdParser.parseItemId("iron_ingot");
        assertEquals("iron", ingot.material());
        assertEquals("ingot", ingot.form());

        MaterialId raw = MaterialIdParser.parseItemId("raw_bauxite");
        assertEquals("aluminum", raw.material());
        assertEquals("raw", raw.form());

        MaterialId crushed = MaterialIdParser.parseItemId("crushed_raw_bauxite");
        assertEquals("aluminum", crushed.material());
        assertEquals("crushed", crushed.form());
    }

    @Test
    void parseOreHostVariantsAndBareForms() {
        MaterialId ore = MaterialIdParser.parseItemId("deepslate_magnetite_ore");
        assertEquals("iron", ore.material());
        assertEquals("ore", ore.form());

        MaterialId gem = MaterialIdParser.parseItemId("diamond");
        assertEquals("diamond", gem.material());
        assertEquals("gem", gem.form());

        MaterialId dust = MaterialIdParser.parseItemId("redstone");
        assertEquals("redstone", dust.material());
        assertEquals("dust", dust.form());
    }

    @Test
    void itemIdForRespectsPreferredRawAndBareForms() {
        assertEquals("raw_bauxite", MaterialIdParser.itemIdFor("aluminum", "raw"));
        assertEquals("crushed_bauxite", MaterialIdParser.itemIdFor("aluminum", "crushed"));
        assertEquals("redstone", MaterialIdParser.itemIdFor("redstone", "dust"));
        assertEquals("diamond", MaterialIdParser.itemIdFor("diamond", "gem"));
    }

    @Test
    void deriveFromTextureFolderNames() {
        assertEquals("raw_bauxite", MaterialIdParser.deriveItemIdFromTextureFile("raw", "bauxite"));
        assertEquals("raw_bauxite", MaterialIdParser.deriveItemIdFromTextureFile("raw_materials", "raw_bauxite"));
        assertEquals("crushed_bauxite", MaterialIdParser.deriveItemIdFromTextureFile("crushed", "crushed_raw_bauxite"));
        assertEquals("iron_dirty_dust", MaterialIdParser.deriveItemIdFromTextureFile("dirty_dust", "dirty_iron_dust"));
    }

    @Test
    void invalidIdsThrow() {
        assertThrows(IllegalArgumentException.class, () -> MaterialIdParser.parseItemId("invalid_material_id_without_form"));
    }
}
