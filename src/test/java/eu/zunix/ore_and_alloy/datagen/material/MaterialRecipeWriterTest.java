package eu.zunix.ore_and_alloy.datagen.material;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
