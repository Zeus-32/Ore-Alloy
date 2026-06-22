package eu.zunix.ore_and_alloy.datagen.material;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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

    @Test
    void gemCompactingRecipesUseBareGemInsteadOfDust() throws Exception {
        MaterialRecipeWriter writer = new MaterialRecipeWriter(tempDir, "ore_and_alloy");
        writer.writeCompactingRecipes(List.of(
                "diamond",
                "diamond_dust",
                "diamond_nugget"
        ));

        String fromNuggets = Files.readString(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/diamond/from_nuggets.json"));
        String toNuggets = Files.readString(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/diamond_nugget/from_diamond.json"));

        assertTrue(fromNuggets.contains("\"id\": \"ore_and_alloy:diamond\""));
        assertTrue(toNuggets.contains("\"item\": \"ore_and_alloy:diamond\""));
        assertFalse(Files.exists(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/diamond_dust/from_nuggets.json")));
        assertFalse(Files.exists(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/diamond_nugget/from_dust.json")));
    }

    @Test
    void gemStorageBlockRecipesUseBareGemInsteadOfDust() throws Exception {
        MaterialRecipeWriter writer = new MaterialRecipeWriter(tempDir, "ore_and_alloy");
        writer.writeStorageBlockRecipes(Map.of("diamond", "diamond"));

        String fromGem = Files.readString(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/diamond_block/from_diamond.json"));
        String fromBlock = Files.readString(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/diamond/from_block.json"));

        assertTrue(fromGem.contains("\"item\": \"ore_and_alloy:diamond\""));
        assertTrue(fromBlock.contains("\"id\": \"ore_and_alloy:diamond\""));
        assertFalse(Files.exists(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/diamond_block/from_dust.json")));
        assertFalse(Files.exists(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/diamond_dust/from_block.json")));
    }
}
