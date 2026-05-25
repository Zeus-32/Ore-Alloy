package eu.zunix.ore_and_alloy.datagen.material;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
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
}
