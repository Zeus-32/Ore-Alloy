package eu.zunix.ore_and_alloy.datagen.material;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MaterialRecipeWriterTest {
    @TempDir
    Path tempDir;

    @Test
    void cookingRecipesUseMaterialLevelRawAndCrushedTags() throws Exception {
        MaterialRecipeWriter writer = new MaterialRecipeWriter(tempDir, "ore_and_alloy");
        writer.writeCanonicalSmeltingRecipes(List.of(
                "copper_ingot",
                "raw_chalcopyrite",
                "raw_copper",
                "crushed_chalcopyrite",
                "crushed_copper"
        ));

        String rawRecipe = Files.readString(tempDir.resolve(
                "data/ore_and_alloy/recipe/smelting/copper_ingot/from_raw_materials.json"));
        String crushedRecipe = Files.readString(tempDir.resolve(
                "data/ore_and_alloy/recipe/smelting/copper_ingot/from_crushed_raw_materials.json"));

        assertTrue(rawRecipe.contains("\"tag\": \"c:raw_materials/copper\""));
        assertTrue(crushedRecipe.contains("\"tag\": \"c:crushed_raw_materials/copper\""));
    }

    @Test
    void ae2SiliconRecipesUseCanonicalBareSilicon() throws Exception {
        MaterialRecipeWriter writer = new MaterialRecipeWriter(tempDir, "ore_and_alloy");
        writer.writeCanonicalSmeltingRecipes(List.of("silicon"));

        String smelting = Files.readString(tempDir.resolve(
                "data/ae2/recipe/smelting/silicon_from_certus_quartz_dust.json"));
        String blasting = Files.readString(tempDir.resolve(
                "data/ae2/recipe/blasting/silicon_from_certus_quartz_dust.json"));
        String inscriber = Files.readString(tempDir.resolve(
                "data/ae2/recipe/inscriber/silicon_print.json"));

        assertTrue(smelting.contains("\"id\": \"ore_and_alloy:silicon\""));
        assertTrue(blasting.contains("\"id\": \"ore_and_alloy:silicon\""));
        assertTrue(inscriber.contains("\"item\": \"ore_and_alloy:silicon\""));
        assertFalse(smelting.contains("ae2:silicon\""));
    }
}
