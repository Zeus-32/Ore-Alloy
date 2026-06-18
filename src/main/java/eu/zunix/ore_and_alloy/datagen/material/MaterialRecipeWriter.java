package eu.zunix.ore_and_alloy.datagen.material;

import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.core.StorageBlockCatalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MaterialRecipeWriter {
    private static final Map<String, VanillaStorageRecipeIds> VANILLA_STORAGE_RECIPES = Map.ofEntries(
            Map.entry("iron", new VanillaStorageRecipeIds("ingot", "iron_block", "iron_ingot_from_iron_block", "minecraft:iron_ingot", "minecraft:iron_block")),
            Map.entry("gold", new VanillaStorageRecipeIds("ingot", "gold_block", "gold_ingot_from_gold_block", "minecraft:gold_ingot", "minecraft:gold_block")),
            Map.entry("copper", new VanillaStorageRecipeIds("ingot", "copper_block", "copper_ingot_from_copper_block", "minecraft:copper_ingot", "minecraft:copper_block"))
    );
    private static final Map<String, String> VANILLA_INGOT_FROM_NUGGET_RECIPE_IDS = Map.of(
            "iron", "iron_ingot_from_nuggets",
            "gold", "gold_ingot_from_nuggets"
    );

    private final Path outRoot;
    private final String namespace;

    public MaterialRecipeWriter(Path outRoot, String namespace) {
        this.outRoot = outRoot;
        this.namespace = namespace;
    }

    public void writeCompactingRecipes(List<String> materialItems) throws IOException {
        Path craftingRoot = outRoot.resolve(Path.of("data", namespace, "recipe", "crafting"));
        Path minecraftRecipesRoot = outRoot.resolve(Path.of("data", "minecraft", "recipe"));
        Files.createDirectories(craftingRoot);
        Files.createDirectories(minecraftRecipesRoot);

        Set<String> itemSet = Set.copyOf(materialItems);
        Set<String> materials = new LinkedHashSet<>();
        for (String itemName : materialItems) {
            MaterialId parsed = MaterialIdParser.parseItemId(itemName);
            if ("ingot".equals(parsed.form())) {
                materials.add(parsed.material());
            }
        }

        for (String material : materials) {
            String nuggetPath = MaterialIdParser.itemIdFor(material, "nugget");
            String ingotPath = MaterialIdParser.itemIdFor(material, "ingot");
            if (!itemSet.contains(nuggetPath) || !itemSet.contains(ingotPath)) continue;

            String nugget = namespace + ":" + nuggetPath;
            String ingot = namespace + ":" + ingotPath;

            Path n2i = craftingRoot.resolve(Path.of(ingotPath, "from_nuggets.json"));
            DatagenFiles.writeText(n2i, nuggetsToIngotRecipeJson(nugget, null, ingot));

            String vanillaN2I = VANILLA_INGOT_FROM_NUGGET_RECIPE_IDS.get(material);
            if (vanillaN2I != null) {
                String vanillaNugget = "minecraft:" + material + "_nugget";
                Path overridePath = minecraftRecipesRoot.resolve(vanillaN2I + ".json");
                DatagenFiles.writeText(overridePath, nuggetsToIngotRecipeJson(nugget, vanillaNugget, ingot));
            }

            Path i2n = craftingRoot.resolve(Path.of(nuggetPath, "from_ingot.json"));
            String json2 = "{\n"
                    + "  \"neoforge:conditions\": [\n"
                    + "    { \"type\": \"neoforge:item_exists\", \"item\": \"" + nugget + "\" },\n"
                    + "    { \"type\": \"neoforge:item_exists\", \"item\": \"" + ingot + "\" }\n"
                    + "  ],\n"
                    + "  \"type\": \"minecraft:crafting_shapeless\",\n"
                    + "  \"ingredients\": [ { \"item\": \"" + ingot + "\" } ],\n"
                    + "  \"result\": { \"id\": \"" + nugget + "\", \"count\": 9 }\n"
                    + "}";
            DatagenFiles.writeText(i2n, json2);
        }
    }

    private static String nuggetsToIngotRecipeJson(String canonicalNugget, String fallbackVanillaNugget, String resultIngot) {
        String ingredient = fallbackVanillaNugget == null
                ? "{ \"item\": \"" + canonicalNugget + "\" }"
                : "[ { \"item\": \"" + canonicalNugget + "\" }, { \"item\": \"" + fallbackVanillaNugget + "\" } ]";

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"neoforge:conditions\": [\n");
        sb.append("    { \"type\": \"neoforge:item_exists\", \"item\": \"").append(canonicalNugget).append("\" },\n");
        sb.append("    { \"type\": \"neoforge:item_exists\", \"item\": \"").append(resultIngot).append("\" }\n");
        sb.append("  ],\n");
        sb.append("  \"type\": \"minecraft:crafting_shapeless\",\n");
        sb.append("  \"ingredients\": [\n");
        for (int i = 0; i < 9; i++) {
            sb.append("    ").append(ingredient);
            if (i + 1 < 9) sb.append(",\n");
            else sb.append('\n');
        }
        sb.append("  ],\n");
        sb.append("  \"result\": { \"id\": \"").append(resultIngot).append("\", \"count\": 1 }\n");
        sb.append("}");
        return sb.toString();
    }

    public void writeStorageBlockRecipes(Map<String, String> storageBlockBaseForms) throws IOException {
        Path craftingRoot = outRoot.resolve(Path.of("data", namespace, "recipe", "crafting"));
        Path minecraftRecipesRoot = outRoot.resolve(Path.of("data", "minecraft", "recipe"));
        Files.createDirectories(craftingRoot);
        Files.createDirectories(minecraftRecipesRoot);

        for (Map.Entry<String, String> entry : storageBlockBaseForms.entrySet()) {
            String material = entry.getKey();
            String baseForm = entry.getValue();
            String baseItemPath = StorageBlockCatalog.baseItemIdForMaterial(material, baseForm);
            String blockItemPath = StorageBlockCatalog.blockIdForMaterial(material);
            int craftCount = StorageBlockCatalog.storageBlockCraftCountForMaterial(material);
            String[] pattern = craftCount == 4
                    ? new String[] { "##", "##" }
                    : new String[] { "###", "###", "###" };

            String baseItem = namespace + ":" + baseItemPath;
            String storageBlock = namespace + ":" + blockItemPath;

            VanillaStorageRecipeIds vanillaOverride = VANILLA_STORAGE_RECIPES.get(material);
            if (vanillaOverride != null && vanillaOverride.baseForm().equals(baseForm)) {
                writeVanillaStorageOverrideRecipes(minecraftRecipesRoot, vanillaOverride, baseItem, storageBlock);
            }

            Path i2b = craftingRoot.resolve(Path.of(blockItemPath, "from_" + baseForm + ".json"));
            String storageFromItems = "{\n"
                    + "  \"neoforge:conditions\": [\n"
                    + "    { \"type\": \"neoforge:item_exists\", \"item\": \"" + baseItem + "\" },\n"
                    + "    { \"type\": \"neoforge:item_exists\", \"item\": \"" + storageBlock + "\" }\n"
                    + "  ],\n"
                    + "  \"type\": \"minecraft:crafting_shaped\",\n"
                    + "  \"pattern\": [\n"
                    + "    \"" + pattern[0] + "\",\n"
                    + "    \"" + pattern[1] + "\""
                    + (pattern.length == 3 ? ",\n    \"" + pattern[2] + "\"\n" : "\n")
                    + "  ],\n"
                    + "  \"key\": {\n"
                    + "    \"#\": { \"item\": \"" + baseItem + "\" }\n"
                    + "  },\n"
                    + "  \"result\": { \"id\": \"" + storageBlock + "\", \"count\": 1 }\n"
                    + "}";
            DatagenFiles.writeText(i2b, storageFromItems);

            Path b2i = craftingRoot.resolve(Path.of(baseItemPath, "from_block.json"));
            String itemsFromStorage = "{\n"
                    + "  \"neoforge:conditions\": [\n"
                    + "    { \"type\": \"neoforge:item_exists\", \"item\": \"" + baseItem + "\" },\n"
                    + "    { \"type\": \"neoforge:item_exists\", \"item\": \"" + storageBlock + "\" }\n"
                    + "  ],\n"
                    + "  \"type\": \"minecraft:crafting_shapeless\",\n"
                    + "  \"ingredients\": [ { \"item\": \"" + storageBlock + "\" } ],\n"
                    + "  \"result\": { \"id\": \"" + baseItem + "\", \"count\": " + craftCount + " }\n"
                    + "}";
            DatagenFiles.writeText(b2i, itemsFromStorage);
        }
    }

    private static void writeVanillaStorageOverrideRecipes(
            Path minecraftRecipesRoot,
            VanillaStorageRecipeIds override,
            String canonicalBaseItem,
            String canonicalBlockItem
    ) throws IOException {
        String canonicalBaseIngredient = ingredientChoiceJson(canonicalBaseItem, override.vanillaBaseItem());
        String canonicalBlockIngredient = ingredientChoiceJson(canonicalBlockItem, override.vanillaBlockItem());

        String compression = "{\n"
                + "  \"neoforge:conditions\": [\n"
                + "    { \"type\": \"neoforge:item_exists\", \"item\": \"" + canonicalBaseItem + "\" },\n"
                + "    { \"type\": \"neoforge:item_exists\", \"item\": \"" + canonicalBlockItem + "\" }\n"
                + "  ],\n"
                + "  \"type\": \"minecraft:crafting_shaped\",\n"
                + "  \"pattern\": [\n"
                + "    \"###\",\n"
                + "    \"###\",\n"
                + "    \"###\"\n"
                + "  ],\n"
                + "  \"key\": {\n"
                + "    \"#\": " + canonicalBaseIngredient + "\n"
                + "  },\n"
                + "  \"result\": { \"id\": \"" + canonicalBlockItem + "\", \"count\": 1 }\n"
                + "}";
        DatagenFiles.writeText(minecraftRecipesRoot.resolve(override.compressionRecipeId() + ".json"), compression);

        String decompression = "{\n"
                + "  \"neoforge:conditions\": [\n"
                + "    { \"type\": \"neoforge:item_exists\", \"item\": \"" + canonicalBaseItem + "\" },\n"
                + "    { \"type\": \"neoforge:item_exists\", \"item\": \"" + canonicalBlockItem + "\" }\n"
                + "  ],\n"
                + "  \"type\": \"minecraft:crafting_shapeless\",\n"
                + "  \"ingredients\": [ " + canonicalBlockIngredient + " ],\n"
                + "  \"result\": { \"id\": \"" + canonicalBaseItem + "\", \"count\": 9 }\n"
                + "}";
        DatagenFiles.writeText(minecraftRecipesRoot.resolve(override.decompressionRecipeId() + ".json"), decompression);
    }

    private static String ingredientChoiceJson(String canonicalItem, String vanillaItem) {
        return "[ { \"item\": \"" + canonicalItem + "\" }, { \"item\": \"" + vanillaItem + "\" } ]";
    }

    public void writeCanonicalSmeltingRecipes(List<String> materialItems) throws IOException {
        Path smeltingRoot = outRoot.resolve(Path.of("data", namespace, "recipe", "smelting"));
        Path blastingRoot = outRoot.resolve(Path.of("data", namespace, "recipe", "blasting"));
        Path minecraftRecipesRoot = outRoot.resolve(Path.of("data", "minecraft", "recipe"));
        Files.createDirectories(smeltingRoot);
        Files.createDirectories(blastingRoot);
        Files.createDirectories(minecraftRecipesRoot);

        Set<String> itemSet = Set.copyOf(materialItems);
        Set<String> materialsWithIngot = new LinkedHashSet<>();
        for (String itemName : materialItems) {
            MaterialId parsed = MaterialIdParser.parseItemId(itemName);
            if ("ingot".equals(parsed.form())) {
                materialsWithIngot.add(parsed.material());
            }
        }

        Set<String> materialsWithRawVariants = new LinkedHashSet<>();
        for (String itemId : itemSet) {
            if (!itemId.startsWith("raw_") || itemId.length() <= "raw_".length()) continue;
            String variant = itemId.substring("raw_".length());
            String mappedMaterial = RawMaterialMappings.materialForRawVariant(variant).orElse(variant);
            materialsWithRawVariants.add(mappedMaterial);
        }

        Set<String> materialsWithDust = new LinkedHashSet<>();
        for (String itemName : materialItems) {
            MaterialId parsed = MaterialIdParser.parseItemId(itemName);
            if ("dust".equals(parsed.form())) {
                materialsWithDust.add(parsed.material());
            }
        }

        for (String material : materialsWithIngot) {
            String outputIngotId = MaterialIdParser.itemIdFor(material, "ingot");
            String outputIngot = namespace + ":" + outputIngotId;
            String group = outputIngotId;
            double experience = smeltingExperience(material);

            if (materialsWithRawVariants.contains(material)) {
                String rawTag = "c:raw_materials/" + material;
                String crushedTag = "c:crushed_raw_materials/" + material;

                Path smeltingRawPath = smeltingRoot.resolve(Path.of(outputIngotId, "from_raw_materials.json"));
                DatagenFiles.writeText(
                        smeltingRawPath,
                        cookingTagRecipeJson("minecraft:smelting", rawTag, outputIngot, group, experience, 200)
                );

                Path blastingRawPath = blastingRoot.resolve(Path.of(outputIngotId, "from_raw_materials.json"));
                DatagenFiles.writeText(
                        blastingRawPath,
                        cookingTagRecipeJson("minecraft:blasting", rawTag, outputIngot, group, experience, 100)
                );

                Path smeltingCrushedPath = smeltingRoot.resolve(Path.of(outputIngotId, "from_crushed_raw_materials.json"));
                DatagenFiles.writeText(
                        smeltingCrushedPath,
                        cookingTagRecipeJson("minecraft:smelting", crushedTag, outputIngot, group, experience, 200)
                );

                Path blastingCrushedPath = blastingRoot.resolve(Path.of(outputIngotId, "from_crushed_raw_materials.json"));
                DatagenFiles.writeText(
                        blastingCrushedPath,
                        cookingTagRecipeJson("minecraft:blasting", crushedTag, outputIngot, group, experience, 100)
                );

                Optional<String> vanillaSmeltingOverride = vanillaRawSmeltingRecipeName(material);
                if (vanillaSmeltingOverride.isPresent()) {
                    Path overridePath = minecraftRecipesRoot.resolve(vanillaSmeltingOverride.get() + ".json");
                    DatagenFiles.writeText(
                            overridePath,
                            cookingTagRecipeJson("minecraft:smelting", rawTag, outputIngot, group, experience, 200)
                    );
                }

                Optional<String> vanillaBlastingOverride = vanillaRawBlastingRecipeName(material);
                if (vanillaBlastingOverride.isPresent()) {
                    Path overridePath = minecraftRecipesRoot.resolve(vanillaBlastingOverride.get() + ".json");
                    DatagenFiles.writeText(
                            overridePath,
                            cookingTagRecipeJson("minecraft:blasting", rawTag, outputIngot, group, experience, 100)
                    );
                }
            }

            if (materialsWithDust.contains(material)) {
                String dustTag = "c:dusts/" + material;

                Path smeltingDustPath = smeltingRoot.resolve(Path.of(outputIngotId, "from_dusts.json"));
                DatagenFiles.writeText(
                        smeltingDustPath,
                        cookingTagRecipeJson("minecraft:smelting", dustTag, outputIngot, group, experience, 200)
                );

                Path blastingDustPath = blastingRoot.resolve(Path.of(outputIngotId, "from_dusts.json"));
                DatagenFiles.writeText(
                        blastingDustPath,
                        cookingTagRecipeJson("minecraft:blasting", dustTag, outputIngot, group, experience, 100)
                );
            }
        }

        writeDisabledVanillaOreCookingRecipes(minecraftRecipesRoot);
        writeAe2SiliconOverrides(materialItems);
    }

    private void writeAe2SiliconOverrides(List<String> materialItems) throws IOException {
        if (!materialItems.contains("silicon")) return;

        Path ae2RecipeRoot = outRoot.resolve(Path.of("data", "ae2", "recipe"));
        String silicon = namespace + ":silicon";
        String conditions = "  \"neoforge:conditions\": [\n"
                + "    { \"type\": \"neoforge:mod_loaded\", \"modid\": \"ae2\" },\n"
                + "    { \"type\": \"neoforge:item_exists\", \"item\": \"" + silicon + "\" }\n"
                + "  ],\n";

        String smelting = "{\n"
                + conditions
                + "  \"type\": \"minecraft:smelting\",\n"
                + "  \"category\": \"misc\",\n"
                + "  \"cookingtime\": 200,\n"
                + "  \"experience\": 0.35,\n"
                + "  \"ingredient\": { \"tag\": \"c:dusts/certus_quartz\" },\n"
                + "  \"result\": { \"id\": \"" + silicon + "\", \"count\": 1 }\n"
                + "}";
        DatagenFiles.writeText(
                ae2RecipeRoot.resolve(Path.of("smelting", "silicon_from_certus_quartz_dust.json")),
                smelting
        );

        String blasting = "{\n"
                + conditions
                + "  \"type\": \"minecraft:blasting\",\n"
                + "  \"category\": \"misc\",\n"
                + "  \"cookingtime\": 100,\n"
                + "  \"experience\": 0.35,\n"
                + "  \"ingredient\": { \"tag\": \"c:dusts/certus_quartz\" },\n"
                + "  \"result\": { \"id\": \"" + silicon + "\", \"count\": 1 }\n"
                + "}";
        DatagenFiles.writeText(
                ae2RecipeRoot.resolve(Path.of("blasting", "silicon_from_certus_quartz_dust.json")),
                blasting
        );

        String inscriber = "{\n"
                + conditions
                + "  \"type\": \"ae2:inscriber\",\n"
                + "  \"ingredients\": {\n"
                + "    \"middle\": { \"item\": \"" + silicon + "\" },\n"
                + "    \"top\": { \"item\": \"ae2:silicon_press\" }\n"
                + "  },\n"
                + "  \"mode\": \"inscribe\",\n"
                + "  \"result\": { \"id\": \"ae2:printed_silicon\", \"count\": 1 }\n"
                + "}";
        DatagenFiles.writeText(
                ae2RecipeRoot.resolve(Path.of("inscriber", "silicon_print.json")),
                inscriber
        );
    }

    private static double smeltingExperience(String material) {
        if ("gold".equals(material)) {
            return 1.0;
        }
        return 0.7;
    }

    private static Optional<String> vanillaRawSmeltingRecipeName(String material) {
        return switch (material) {
            case "iron" -> Optional.of("iron_ingot_from_smelting_raw_iron");
            case "gold" -> Optional.of("gold_ingot_from_smelting_raw_gold");
            case "copper" -> Optional.of("copper_ingot_from_smelting_raw_copper");
            default -> Optional.empty();
        };
    }

    private static Optional<String> vanillaRawBlastingRecipeName(String material) {
        return switch (material) {
            case "iron" -> Optional.of("iron_ingot_from_blasting_raw_iron");
            case "gold" -> Optional.of("gold_ingot_from_blasting_raw_gold");
            case "copper" -> Optional.of("copper_ingot_from_blasting_raw_copper");
            default -> Optional.empty();
        };
    }

    private static void writeDisabledVanillaOreCookingRecipes(Path minecraftRecipesRoot) throws IOException {
        writeDisabledRecipe(minecraftRecipesRoot, "iron_ingot_from_smelting_iron_ore", "minecraft:smelting", 200);
        writeDisabledRecipe(minecraftRecipesRoot, "iron_ingot_from_smelting_deepslate_iron_ore", "minecraft:smelting", 200);
        writeDisabledRecipe(minecraftRecipesRoot, "gold_ingot_from_smelting_gold_ore", "minecraft:smelting", 200);
        writeDisabledRecipe(minecraftRecipesRoot, "gold_ingot_from_smelting_deepslate_gold_ore", "minecraft:smelting", 200);
        writeDisabledRecipe(minecraftRecipesRoot, "copper_ingot_from_smelting_copper_ore", "minecraft:smelting", 200);
        writeDisabledRecipe(minecraftRecipesRoot, "copper_ingot_from_smelting_deepslate_copper_ore", "minecraft:smelting", 200);

        writeDisabledRecipe(minecraftRecipesRoot, "iron_ingot_from_blasting_iron_ore", "minecraft:blasting", 100);
        writeDisabledRecipe(minecraftRecipesRoot, "iron_ingot_from_blasting_deepslate_iron_ore", "minecraft:blasting", 100);
        writeDisabledRecipe(minecraftRecipesRoot, "gold_ingot_from_blasting_gold_ore", "minecraft:blasting", 100);
        writeDisabledRecipe(minecraftRecipesRoot, "gold_ingot_from_blasting_deepslate_gold_ore", "minecraft:blasting", 100);
        writeDisabledRecipe(minecraftRecipesRoot, "copper_ingot_from_blasting_copper_ore", "minecraft:blasting", 100);
        writeDisabledRecipe(minecraftRecipesRoot, "copper_ingot_from_blasting_deepslate_copper_ore", "minecraft:blasting", 100);
    }

    private static void writeDisabledRecipe(Path root, String id, String type, int cookTime) throws IOException {
        DatagenFiles.writeText(root.resolve(id + ".json"), disabledCookingRecipeJson(type, cookTime));
    }

    private static String disabledCookingRecipeJson(String type, int cookTime) {
        return "{\n"
                + "  \"neoforge:conditions\": [\n"
                + "    { \"type\": \"neoforge:never\" }\n"
                + "  ],\n"
                + "  \"type\": \"" + type + "\",\n"
                + "  \"category\": \"misc\",\n"
                + "  \"ingredient\": { \"item\": \"minecraft:air\" },\n"
                + "  \"result\": { \"id\": \"minecraft:air\" },\n"
                + "  \"experience\": 0.0,\n"
                + "  \"cookingtime\": " + cookTime + "\n"
                + "}";
    }

    private static String cookingTagRecipeJson(
            String recipeType,
            String ingredientTag,
            String resultItem,
            String group,
            double experience,
            int cookingTime
    ) {
        return "{\n"
                + "  \"neoforge:conditions\": [\n"
                + "    { \"type\": \"neoforge:item_exists\", \"item\": \"" + resultItem + "\" },\n"
                + "    {\n"
                + "      \"type\": \"neoforge:not\",\n"
                + "      \"value\": { \"type\": \"neoforge:tag_empty\", \"tag\": \"" + ingredientTag + "\" }\n"
                + "    }\n"
                + "  ],\n"
                + "  \"type\": \"" + recipeType + "\",\n"
                + "  \"category\": \"misc\",\n"
                + "  \"group\": \"" + group + "\",\n"
                + "  \"ingredient\": { \"tag\": \"" + ingredientTag + "\" },\n"
                + "  \"result\": { \"id\": \"" + resultItem + "\" },\n"
                + "  \"experience\": " + experience + ",\n"
                + "  \"cookingtime\": " + cookingTime + "\n"
                + "}";
    }

    private record VanillaStorageRecipeIds(
            String baseForm,
            String compressionRecipeId,
            String decompressionRecipeId,
            String vanillaBaseItem,
            String vanillaBlockItem
    ) {}
}
