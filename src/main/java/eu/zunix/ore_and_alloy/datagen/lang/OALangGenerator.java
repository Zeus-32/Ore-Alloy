package eu.zunix.ore_and_alloy.datagen.lang;

import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.OreHostVariantCatalog;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;
import eu.zunix.ore_and_alloy.core.StorageBlockCatalog;
import eu.zunix.ore_and_alloy.datagen.material.MaterialId;
import eu.zunix.ore_and_alloy.datagen.material.MaterialIdParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class OALangGenerator {
    private OALangGenerator() {}

    public static void write(
            Path out,
            String namespace,
            List<String> materialItems,
            List<String> rawVariants,
            Map<String, String> storageBlockBaseForms
    ) throws IOException {
        Map<String, String> entries = new LinkedHashMap<>();

        entries.put("itemGroup." + namespace + ".materials", "Ore & Alloy: Materials");
        IntegrationLangEntries.append(entries, namespace);

        for (String name : materialItems) {
            entries.put("item." + namespace + "." + name, displayNameFromId(name));
        }
        for (String rawVariant : rawVariants) {
            String rawName = capitalizeWords(rawVariant.replace('_', ' '));
            for (OreHostVariantCatalog.HostVariant host : OreHostVariantCatalog.hostVariants()) {
                String blockId = host.blockId(rawVariant);
                String blockName = host.displayPrefix() + rawName + " Ore";
                entries.put("block." + namespace + "." + blockId, blockName);
            }
        }
        for (String material : storageBlockBaseForms.keySet()) {
            String blockId = StorageBlockCatalog.blockIdForMaterial(material);
            String materialName = capitalizeWords(material.replace('_', ' '));
            entries.put("block." + namespace + "." + blockId, materialName + " Block");
        }

        addTagLangEntries(entries, materialItems);
        addOreTagLangEntries(entries, rawVariants);
        addStorageBlockTagLangEntries(entries, storageBlockBaseForms);
        writeJson(out, entries);
    }

    private static String displayNameFromId(String id) {
        MaterialId parsed = MaterialIdParser.parseItemId(id);
        String material = capitalizeWords(parsed.material().replace('_', ' '));
        if ("raw".equals(parsed.form())) {
            String rawToken = tokenAfterPrefix(id, "raw_");
            String rawName = rawToken.isBlank() ? material : capitalizeWords(rawToken.replace('_', ' '));
            return "Raw " + rawName;
        }
        if ("crushed".equals(parsed.form())) {
            String crushedToken = tokenAfterPrefix(id, "crushed_");
            String crushedName = crushedToken.isBlank() ? material : capitalizeWords(crushedToken.replace('_', ' '));
            return "Crushed " + crushedName;
        }
        String formNice = switch (parsed.form()) {
            case "ingot" -> "Ingot";
            case "nugget" -> "Nugget";
            case "dust" -> "Dust";
            case "plate" -> "Plate";
            case "rod" -> "Rod";
            case "gear" -> "Gear";
            case "bolt" -> "Bolt";
            case "screw" -> "Screw";
            case "crushed" -> "Crushed";
            case "ore" -> "Ore";
            case "raw" -> "Raw";
            case "silicon" -> "";
            default -> capitalizeWords(parsed.form().replace('_', ' '));
        };
        if (formNice.isBlank()) return material;
        return material + " " + formNice;
    }

    private static String tokenAfterPrefix(String id, String prefix) {
        if (id == null || prefix == null) return "";
        String loweredId = id.toLowerCase(Locale.ROOT);
        String loweredPrefix = prefix.toLowerCase(Locale.ROOT);
        if (!loweredId.startsWith(loweredPrefix) || loweredId.length() <= loweredPrefix.length()) return "";
        return loweredId.substring(loweredPrefix.length());
    }

    private static void addTagLangEntries(Map<String, String> entries, List<String> materialItems) {
        Map<String, Set<String>> materialsByBucket = new LinkedHashMap<>();
        for (String itemName : materialItems) {
            MaterialId parsed = MaterialIdParser.parseItemId(itemName);
            String bucket = MaterialFormCatalog.TAG_BUCKET_BY_FORM.get(parsed.form());
            if (bucket == null) continue;
            if (MaterialItemOrder.bareItemForm(itemName).isPresent()) {
                materialsByBucket.computeIfAbsent(bucket, ignored -> new LinkedHashSet<>());
                continue;
            }
            materialsByBucket.computeIfAbsent(bucket, ignored -> new LinkedHashSet<>()).add(parsed.material());
        }

        List<String> sortedBuckets = new ArrayList<>(materialsByBucket.keySet());
        sortedBuckets.sort(String::compareTo);
        for (String bucket : sortedBuckets) {
            String bucketLabel = tagPathDisplayName(bucket);
            entries.put(tagLangKey("item", "c", bucket), bucketLabel);

            List<String> materials = new ArrayList<>(materialsByBucket.get(bucket));
            materials.sort(String::compareTo);
            for (String material : materials) {
                String materialLabel = capitalizeWords(material.replace('_', ' '));
                entries.put(tagLangKey("item", "c", bucket + "/" + material), materialLabel + " " + bucketLabel);
            }
        }
    }

    private static void addOreTagLangEntries(Map<String, String> entries, List<String> rawVariants) {
        entries.put(tagLangKey("block", "c", "ores"), "Ores");
        entries.put(tagLangKey("item", "c", "ores"), "Ores");

        Set<String> materials = new LinkedHashSet<>();
        for (String rawVariant : rawVariants) {
            String label = capitalizeWords(rawVariant.replace('_', ' '));
            entries.put(tagLangKey("block", "c", "ores/" + rawVariant), label + " Ores");
            entries.put(tagLangKey("item", "c", "ores/" + rawVariant), label + " Ores");

            String material = RawMaterialMappings.materialForRawVariant(rawVariant).orElse(rawVariant);
            material = MaterialItemOrder.canonicalMaterialToken(material);
            materials.add(material);
        }

        List<String> sortedMaterials = new ArrayList<>(materials);
        sortedMaterials.sort(String::compareTo);
        for (String material : sortedMaterials) {
            String label = capitalizeWords(material.replace('_', ' '));
            entries.put(tagLangKey("block", "c", "ores/" + material), label + " Ores");
            entries.put(tagLangKey("item", "c", "ores/" + material), label + " Ores");
        }
    }

    private static void addStorageBlockTagLangEntries(Map<String, String> entries, Map<String, String> storageBlockBaseForms) {
        if (storageBlockBaseForms.isEmpty()) return;

        entries.put(tagLangKey("block", "c", "storage_blocks"), "Storage Blocks");
        entries.put(tagLangKey("item", "c", "storage_blocks"), "Storage Blocks");

        for (String material : storageBlockBaseForms.keySet()) {
            String materialLabel = capitalizeWords(material.replace('_', ' '));
            entries.put(tagLangKey("block", "c", "storage_blocks/" + material), materialLabel + " Storage Blocks");
            entries.put(tagLangKey("item", "c", "storage_blocks/" + material), materialLabel + " Storage Blocks");
        }
    }

    private static String tagLangKey(String registryType, String namespace, String tagPath) {
        return "tag." + registryType + "." + namespace + "." + tagPath.replace('/', '.');
    }

    private static String tagPathDisplayName(String tagPath) {
        return capitalizeWords(tagPath.replace('_', ' '));
    }

    private static String capitalizeWords(String value) {
        if (value.isBlank()) return value;
        String[] words = value.split(" ");
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (w.isEmpty()) continue;
            words[i] = w.substring(0, 1).toUpperCase(Locale.ROOT) + w.substring(1).toLowerCase(Locale.ROOT);
        }
        return String.join(" ", words);
    }

    private static void writeJson(Path path, Map<String, String> entries) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        int i = 0;
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            sb.append("  \"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
            if (++i < entries.size()) sb.append(",\n");
            else sb.append('\n');
        }
        sb.append('}');

        Files.createDirectories(path.getParent());
        Files.write(path, sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
