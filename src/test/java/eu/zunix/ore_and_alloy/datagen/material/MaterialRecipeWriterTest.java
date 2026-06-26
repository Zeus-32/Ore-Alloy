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
                "data/ore_and_alloy/recipe/crafting/diamond_nugget/from_gem.json"));

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
        writer.writeStorageBlockRecipes(Map.of("diamond", "gem"));

        String fromGem = Files.readString(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/diamond_block/from_gem.json"));
        String fromBlock = Files.readString(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/diamond/from_block.json"));

        assertTrue(fromGem.contains("\"item\": \"ore_and_alloy:diamond\""));
        assertTrue(fromBlock.contains("\"id\": \"ore_and_alloy:diamond\""));
        assertFalse(Files.exists(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/diamond_block/from_dust.json")));
        assertFalse(Files.exists(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/diamond_dust/from_block.json")));
    }

    @Test
    void pureNetheriteDoesNotAutoConvertFromMinecraftNetherite() throws Exception {
        MaterialRecipeWriter writer = new MaterialRecipeWriter(tempDir, "ore_and_alloy");
        writer.writeStorageBlockRecipes(Map.of("pure_netherite", "ingot"));

        assertFalse(Files.exists(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/pure_netherite_ingot/from_netherite_ingot.json")));
        assertFalse(Files.exists(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/pure_netherite_block/from_netherite_block.json")));
    }

    @Test
    void dustBreakdownRecipesAreShaped() throws Exception {
        MaterialRecipeWriter writer = new MaterialRecipeWriter(tempDir, "ore_and_alloy");
        writer.writeCompactingRecipes(List.of(
                "iron_dust",
                "iron_dust_pile",
                "iron_tiny_dust_pile"
        ));

        String dustToPiles = Files.readString(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/iron_dust_pile/from_dust.json"));
        String dustToTinyPiles = Files.readString(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/iron_tiny_dust_pile/from_dust.json"));
        String pilesToDust = Files.readString(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/iron_dust/from_dust_piles.json"));
        String tinyPilesToDust = Files.readString(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/iron_dust/from_tiny_dust_piles.json"));

        assertTrue(dustToPiles.contains("\"type\": \"minecraft:crafting_shaped\""));
        assertTrue(dustToPiles.contains("\" # \""));
        assertTrue(dustToPiles.contains("\"id\": \"ore_and_alloy:iron_dust_pile\", \"count\": 4"));
        assertTrue(dustToTinyPiles.contains("\"type\": \"minecraft:crafting_shaped\""));
        assertTrue(dustToTinyPiles.contains("\"#  \""));
        assertTrue(dustToTinyPiles.contains("\"id\": \"ore_and_alloy:iron_tiny_dust_pile\", \"count\": 9"));
        assertTrue(pilesToDust.contains("\"##\""));
        assertTrue(pilesToDust.contains("\"id\": \"ore_and_alloy:iron_dust\", \"count\": 1"));
        assertTrue(tinyPilesToDust.contains("\"###\""));
        assertTrue(tinyPilesToDust.contains("\"id\": \"ore_and_alloy:iron_dust\", \"count\": 1"));
        assertFalse(Files.exists(tempDir.resolve(
                "data/ore_and_alloy/recipe/crafting/iron_tiny_dust_pile/from_dust_pile.json")));
    }
}
