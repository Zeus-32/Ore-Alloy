package eu.zunix.ore_and_alloy.datagen.material;

import eu.zunix.ore_and_alloy.core.MetalMaterial;
import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class MaterialTagWriter {
    private final Path outRoot;
    private final String namespace;

    public MaterialTagWriter(Path outRoot, String namespace) {
        this.outRoot = outRoot;
        this.namespace = namespace;
    }

    public void writeItemTags(List<String> materialItems) throws IOException {
        Path tagsRoot = outRoot.resolve(Path.of("data", "c", "tags", "item"));
        Map<String, List<String>> topLevel = new LinkedHashMap<>();
        Map<String, List<String>> byBucketAndMaterial = new LinkedHashMap<>();

        for (String itemName : materialItems) {
            MaterialId parsed = MaterialIdParser.parseItemId(itemName);
            String bucket = MaterialFormCatalog.TAG_BUCKET_BY_FORM.get(parsed.form());
            if (bucket == null) continue;

            String itemId = namespace + ":" + itemName;
            topLevel.computeIfAbsent(bucket, ignored -> new ArrayList<>()).add(itemId);
            String canonicalMaterial = MaterialItemOrder.canonicalMaterialToken(parsed.material());
            String key = bucket + "/" + canonicalMaterial;
            byBucketAndMaterial.computeIfAbsent(key, ignored -> new ArrayList<>()).add(itemId);
        }

        for (Map.Entry<String, List<String>> entry : byBucketAndMaterial.entrySet()) {
            List<String> unique = new ArrayList<>(new LinkedHashSet<>(entry.getValue()));
            writeTagFile(tagsRoot.resolve(entry.getKey() + ".json"), unique);
        }

        for (Map.Entry<String, List<String>> entry : topLevel.entrySet()) {
            List<String> unique = new ArrayList<>(new LinkedHashSet<>(entry.getValue()));
            writeTagFile(tagsRoot.resolve(entry.getKey() + ".json"), unique);
        }

        writeVanillaCompatibilityItemTags(materialItems);
    }

    public void writeMoltenFluidTags() throws IOException {
        Path tagsRoot = outRoot.resolve(Path.of("data", "c", "tags", "fluid"));
        List<String> moltenFluids = new ArrayList<>();

        for (MetalMaterial metal : MetalMaterial.values()) {
            String fluidId = namespace + ":" + metal.moltenFluidPath();
            moltenFluids.add(fluidId);

            String material = metal.materialName();
            writeTagFile(tagsRoot.resolve(Path.of("molten", material + ".json")), List.of(fluidId));
            writeTagFile(tagsRoot.resolve(Path.of("moltens", material + ".json")), List.of(fluidId));
        }

        writeTagFile(tagsRoot.resolve("molten.json"), moltenFluids);
        writeTagFile(tagsRoot.resolve("moltens.json"), moltenFluids);
    }

    private static void writeTagFile(Path path, List<String> values) throws IOException {
        Files.createDirectories(path.getParent());
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

    private void writeVanillaCompatibilityItemTags(List<String> materialItems) throws IOException {
        Path minecraftItemTagsRoot = outRoot.resolve(Path.of("data", "minecraft", "tags", "item"));

        if (materialItems.contains("coal")) {
            writeTagFile(minecraftItemTagsRoot.resolve("coals.json"), List.of(namespace + ":coal"));
        }

        List<String> beaconPayments = new ArrayList<>();
        addIfPresent(beaconPayments, materialItems, "iron_ingot");
        addIfPresent(beaconPayments, materialItems, "gold_ingot");
        addIfPresent(beaconPayments, materialItems, "diamond");
        addIfPresent(beaconPayments, materialItems, "emerald");
        if (!beaconPayments.isEmpty()) {
            writeTagFile(minecraftItemTagsRoot.resolve("beacon_payment_items.json"), beaconPayments);
        }

        List<String> piglinLoved = new ArrayList<>();
        addIfPresent(piglinLoved, materialItems, "gold_ingot");
        addIfPresent(piglinLoved, materialItems, "gold_nugget");
        if (!piglinLoved.isEmpty()) {
            writeTagFile(minecraftItemTagsRoot.resolve("piglin_loved.json"), piglinLoved);
        }
    }

    private void addIfPresent(List<String> out, List<String> materialItems, String itemPath) {
        if (materialItems.contains(itemPath)) {
            out.add(namespace + ":" + itemPath);
        }
    }
}
