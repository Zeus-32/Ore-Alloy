package eu.zunix.ore_and_alloy.datagen.material;

import eu.zunix.ore_and_alloy.core.StorageBlockCatalog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaterialStorageBlockWriter {
    private static final Pattern TAG_ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");

    private final Path outRoot;
    private final String namespace;

    public MaterialStorageBlockWriter(Path outRoot, String namespace) {
        this.outRoot = outRoot;
        this.namespace = namespace;
    }

    public Map<String, String> collectStorageBlockBaseForms(List<String> materialItems) {
        return StorageBlockCatalog.collectStorageBlockBaseForms(materialItems);
    }

    public void writeBlockstatesAndModels(Map<String, String> storageBaseFormsByMaterial) throws IOException {
        Path blockstatesRoot = outRoot.resolve(Path.of("assets", namespace, "blockstates"));
        Path blockModelsRoot = outRoot.resolve(Path.of("assets", namespace, "models", "block"));
        Path itemModelsRoot = outRoot.resolve(Path.of("assets", namespace, "models", "item"));

        Files.createDirectories(blockstatesRoot);
        Files.createDirectories(blockModelsRoot);
        Files.createDirectories(itemModelsRoot);

        for (String material : storageBaseFormsByMaterial.keySet()) {
            String blockId = StorageBlockCatalog.blockIdForMaterial(material);

            String blockstateJson = "{\n"
                    + "  \"variants\": {\n"
                    + "    \"\": { \"model\": \"" + namespace + ":block/" + blockId + "\" }\n"
                    + "  }\n"
                    + "}";
            DatagenFiles.writeText(blockstatesRoot.resolve(blockId + ".json"), blockstateJson);

            String blockModelJson = "{\n"
                    + "  \"parent\": \"minecraft:block/cube_all\",\n"
                    + "  \"textures\": {\n"
                    + "    \"all\": \"" + namespace + ":block/storage/" + blockId + "\"\n"
                    + "  }\n"
                    + "}";
            DatagenFiles.writeText(blockModelsRoot.resolve(blockId + ".json"), blockModelJson);

            String itemModelJson = "{\n"
                    + "  \"parent\": \"" + namespace + ":block/" + blockId + "\"\n"
                    + "}";
            DatagenFiles.writeText(itemModelsRoot.resolve(blockId + ".json"), itemModelJson);
        }
    }

    public void writeLootTables(Map<String, String> storageBaseFormsByMaterial) throws IOException {
        Path lootRoot = outRoot.resolve(Path.of("data", namespace, "loot_table", "blocks"));
        Files.createDirectories(lootRoot);

        for (String material : storageBaseFormsByMaterial.keySet()) {
            String blockId = StorageBlockCatalog.blockIdForMaterial(material);

            String json = "{\n"
                    + "  \"type\": \"minecraft:block\",\n"
                    + "  \"pools\": [\n"
                    + "    {\n"
                    + "      \"rolls\": 1,\n"
                    + "      \"entries\": [\n"
                    + "        {\n"
                    + "          \"type\": \"minecraft:item\",\n"
                    + "          \"name\": \"" + namespace + ":" + blockId + "\"\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"conditions\": [\n"
                    + "        {\n"
                    + "          \"condition\": \"minecraft:survives_explosion\"\n"
                    + "        }\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";
            DatagenFiles.writeText(lootRoot.resolve(blockId + ".json"), json);
        }
    }

    public void writeBlockTags(Map<String, String> storageBaseFormsByMaterial) throws IOException {
        Path commonBlockTagsRoot = outRoot.resolve(Path.of("data", "c", "tags", "block"));
        Path commonItemTagsRoot = outRoot.resolve(Path.of("data", "c", "tags", "item"));
        Path minecraftBlockTagsRoot = outRoot.resolve(Path.of("data", "minecraft", "tags", "block"));
        Files.createDirectories(commonBlockTagsRoot);
        Files.createDirectories(commonItemTagsRoot);
        Files.createDirectories(minecraftBlockTagsRoot);

        List<String> storageBlockIds = new ArrayList<>(storageBaseFormsByMaterial.size());
        List<String> beaconBaseBlockIds = new ArrayList<>();

        for (String material : storageBaseFormsByMaterial.keySet()) {
            String blockId = StorageBlockCatalog.blockIdForMaterial(material);
            String fullId = namespace + ":" + blockId;
            storageBlockIds.add(fullId);

            writeTag(commonBlockTagsRoot.resolve(Path.of("storage_blocks", material + ".json")), List.of(fullId));
            writeTag(commonItemTagsRoot.resolve(Path.of("storage_blocks", material + ".json")), List.of(fullId));

            if (isBeaconBaseMaterial(material)) {
                beaconBaseBlockIds.add(fullId);
            }
        }

        storageBlockIds = new ArrayList<>(new LinkedHashSet<>(storageBlockIds));
        storageBlockIds.sort(String::compareTo);
        beaconBaseBlockIds = new ArrayList<>(new LinkedHashSet<>(beaconBaseBlockIds));
        beaconBaseBlockIds.sort(String::compareTo);

        writeTag(commonBlockTagsRoot.resolve("storage_blocks.json"), storageBlockIds);
        writeTag(commonItemTagsRoot.resolve("storage_blocks.json"), storageBlockIds);

        writeMergedTag(minecraftBlockTagsRoot.resolve(Path.of("mineable", "pickaxe.json")), storageBlockIds);
        writeMergedTag(minecraftBlockTagsRoot.resolve("needs_stone_tool.json"), storageBlockIds);
        if (!beaconBaseBlockIds.isEmpty()) {
            writeMergedTag(minecraftBlockTagsRoot.resolve("beacon_base_blocks.json"), beaconBaseBlockIds);
        }
    }

    private static boolean isBeaconBaseMaterial(String material) {
        return "iron".equals(material)
                || "gold".equals(material)
                || "diamond".equals(material)
                || "emerald".equals(material);
    }

    private static void writeTag(Path path, List<String> values) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"replace\": false,\n  \"values\": [\n");
        for (int i = 0; i < values.size(); i++) {
            sb.append("    { \"id\": \"").append(values.get(i)).append("\", \"required\": false }");
            if (i + 1 < values.size()) sb.append(",\n");
            else sb.append('\n');
        }
        sb.append("  ]\n}");
        DatagenFiles.writeText(path, sb.toString());
    }

    private static void writeMergedTag(Path path, List<String> valuesToAdd) throws IOException {
        LinkedHashSet<String> values = new LinkedHashSet<>(valuesToAdd);
        if (Files.isRegularFile(path)) {
            String existing = Files.readString(path, StandardCharsets.UTF_8);
            Matcher matcher = TAG_ID_PATTERN.matcher(existing);
            while (matcher.find()) {
                String id = matcher.group(1);
                if (id != null && !id.isBlank()) {
                    values.add(id);
                }
            }
        }
        List<String> merged = new ArrayList<>(values);
        merged.sort(String::compareTo);
        writeTag(path, merged);
    }
}
