package eu.zunix.ore_and_alloy.datagen.material;

import eu.zunix.ore_and_alloy.core.StorageBlockCatalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MaterialRecipeWriter {
    private final Path outRoot;
    private final String namespace;

    public MaterialRecipeWriter(Path outRoot, String namespace) {
        this.outRoot = outRoot;
        this.namespace = namespace;
    }

    public void writeCompactingRecipes(List<String> materialItems) throws IOException {
        Path recipesRoot = outRoot.resolve(Path.of("data", namespace, "recipe"));
        Files.createDirectories(recipesRoot);

        Set<String> itemSet = Set.copyOf(materialItems);
        Set<String> materials = new LinkedHashSet<>();
        for (String itemName : materialItems) {
            MaterialId parsed = MaterialIdParser.parseItemId(itemName);
            if ("ingot".equals(parsed.form())) {
                materials.add(parsed.material());
            }
        }

        for (String material : materials) {
            if (!itemSet.contains(material + "_nugget")) continue;
            String nugget = namespace + ":" + material + "_nugget";
            String ingot = namespace + ":" + material + "_ingot";

            Path n2i = recipesRoot.resolve(material + "_nuggets_to_ingot.json");
            StringBuilder sb1 = new StringBuilder();
            sb1.append("{\n");
            sb1.append("  \"neoforge:conditions\": [\n");
            sb1.append("    { \"type\": \"neoforge:item_exists\", \"item\": \"").append(nugget).append("\" },\n");
            sb1.append("    { \"type\": \"neoforge:item_exists\", \"item\": \"").append(ingot).append("\" }\n");
            sb1.append("  ],\n");
            sb1.append("  \"type\": \"minecraft:crafting_shapeless\",\n");
            sb1.append("  \"ingredients\": [\n");
            for (int i = 0; i < 9; i++) {
                sb1.append("    { \"item\": \"").append(nugget).append("\" }");
                if (i + 1 < 9) sb1.append(",\n");
                else sb1.append('\n');
            }
            sb1.append("  ],\n  \"result\": { \"id\": \"").append(ingot).append("\", \"count\": 1 }\n}");
            DatagenFiles.writeText(n2i, sb1.toString());

            Path i2n = recipesRoot.resolve(material + "_ingot_to_nuggets.json");
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

    public void writeStorageBlockRecipes(Map<String, String> storageBlockBaseForms) throws IOException {
        Path recipesRoot = outRoot.resolve(Path.of("data", namespace, "recipe"));
        Files.createDirectories(recipesRoot);

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

            Path i2b = recipesRoot.resolve(blockItemPath + "_from_" + baseForm + ".json");
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

            Path b2i = recipesRoot.resolve(blockItemPath + "_to_" + baseForm + ".json");
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
}
