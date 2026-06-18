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
    void parseOreHostVariantsAndRejectBareIds() {
        MaterialId ore = MaterialIdParser.parseItemId("deepslate_magnetite_ore");
        assertEquals("iron", ore.material());
        assertEquals("ore", ore.form());

        assertThrows(IllegalArgumentException.class, () -> MaterialIdParser.parseItemId("iron"));
        MaterialId silicon = MaterialIdParser.parseItemId("silicon");
        assertEquals("silicon", silicon.material());
        assertEquals("silicon", silicon.form());
    }

    @Test
    void itemIdForRespectsPreferredRawAndMetalForms() {
        assertEquals("raw_bauxite", MaterialIdParser.itemIdFor("aluminum", "raw"));
        assertEquals("crushed_bauxite", MaterialIdParser.itemIdFor("aluminum", "crushed"));
        assertEquals("iron_dust", MaterialIdParser.itemIdFor("iron", "dust"));
        assertEquals("copper_plate", MaterialIdParser.itemIdFor("copper", "plate"));
        assertEquals("silicon", MaterialIdParser.itemIdFor("silicon", "silicon"));
    }

    @Test
    void deriveFromTextureFolderNames() {
        assertEquals("raw_bauxite", MaterialIdParser.deriveItemIdFromTextureFile("raw", "bauxite"));
        assertEquals("raw_bauxite", MaterialIdParser.deriveItemIdFromTextureFile("raw_materials", "raw_bauxite"));
        assertEquals("crushed_bauxite", MaterialIdParser.deriveItemIdFromTextureFile("crushed", "crushed_raw_bauxite"));
        assertEquals("iron_rod", MaterialIdParser.deriveItemIdFromTextureFile("rod", "rod_iron"));
    }

    @Test
    void invalidIdsThrow() {
        assertThrows(IllegalArgumentException.class, () -> MaterialIdParser.parseItemId("invalid_material_id_without_form"));
        assertThrows(IllegalArgumentException.class, () -> MaterialIdParser.parseItemId("iron_dirty_dust"));
    }
}
