package eu.zunix.ore_and_alloy.datagen.material;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MaterialTrimWriter {
    private static final List<TrimBridgeDefinition> VANILLA_TRIM_BRIDGES = List.of(
            new TrimBridgeDefinition(
                    "iron",
                    List.of("iron_ingot"),
                    0.2F,
                    "#ECECEC",
                    Map.of("minecraft:iron", "iron_darker")
            ),
            new TrimBridgeDefinition(
                    "copper",
                    List.of("copper_ingot"),
                    0.5F,
                    "#B4684D",
                    Map.of()
            ),
            new TrimBridgeDefinition(
                    "gold",
                    List.of("gold_ingot"),
                    0.6F,
                    "#DEB12D",
                    Map.of("minecraft:gold", "gold_darker")
            )
    );

    private final Path outRoot;
    private final String namespace;

    public MaterialTrimWriter(Path outRoot, String namespace) {
        this.outRoot = outRoot;
        this.namespace = namespace;
    }

    public void writeTrimSupport(List<String> materialItems) throws IOException {
        Set<String> availableItems = new LinkedHashSet<>();
        for (String id : materialItems) {
            if (id == null) continue;
            String normalized = normalize(id);
            if (!normalized.isBlank()) availableItems.add(normalized);
        }

        List<ResolvedTrimBridge> resolved = new ArrayList<>();
        for (TrimBridgeDefinition definition : VANILLA_TRIM_BRIDGES) {
            Optional<String> ingredient = resolveIngredient(definition, availableItems);
            if (ingredient.isEmpty()) continue;
            resolved.add(new ResolvedTrimBridge(definition, ingredient.get()));
        }

        if (resolved.isEmpty()) return;

        writeTrimMaterialsTag(resolved);
        writeTrimMaterialEntries(resolved);
    }

    private Optional<String> resolveIngredient(TrimBridgeDefinition definition, Set<String> availableItems) {
        for (String candidate : definition.ingredientCandidates()) {
            String normalized = normalize(candidate);
            if (availableItems.contains(normalized)) {
                return Optional.of(normalized);
            }
        }
        return Optional.empty();
    }

    private void writeTrimMaterialsTag(List<ResolvedTrimBridge> resolved) throws IOException {
        Path path = outRoot.resolve(Path.of("data", "minecraft", "tags", "item", "trim_materials.json"));
        List<String> values = new ArrayList<>(resolved.size());
        for (ResolvedTrimBridge bridge : resolved) {
            values.add(namespace + ":" + bridge.ingredientPath());
        }
        writeTagFile(path, values);
    }

    private void writeTrimMaterialEntries(List<ResolvedTrimBridge> resolved) throws IOException {
        Path root = outRoot.resolve(Path.of("data", namespace, "trim_material"));
        for (ResolvedTrimBridge bridge : resolved) {
            String fileName = bridge.definition().key() + ".json";
            Path path = root.resolve(fileName);
            DatagenFiles.writeText(path, trimMaterialJson(bridge));
        }
    }

    private String trimMaterialJson(ResolvedTrimBridge bridge) {
        TrimBridgeDefinition definition = bridge.definition();
        String ingredientId = namespace + ":" + bridge.ingredientPath();
        StringBuilder out = new StringBuilder();
        out.append("{\n");
        out.append("  \"asset_name\": \"").append(definition.key()).append("\",\n");
        out.append("  \"description\": {\n");
        out.append("    \"color\": \"").append(definition.color()).append("\",\n");
        out.append("    \"translate\": \"trim_material.minecraft.").append(definition.key()).append("\"\n");
        out.append("  },\n");
        out.append("  \"ingredient\": \"").append(ingredientId).append("\",\n");
        out.append("  \"item_model_index\": ").append(Float.toString(definition.itemModelIndex()));

        if (definition.overrideArmorMaterials().isEmpty()) {
            out.append('\n');
        } else {
            out.append(",\n");
            out.append("  \"override_armor_materials\": {\n");
            int index = 0;
            int size = definition.overrideArmorMaterials().size();
            for (Map.Entry<String, String> entry : definition.overrideArmorMaterials().entrySet()) {
                out.append("    \"")
                        .append(entry.getKey())
                        .append("\": \"")
                        .append(entry.getValue())
                        .append("\"");
                if (++index < size) out.append(",\n");
                else out.append('\n');
            }
            out.append("  }\n");
        }

        out.append("}\n");
        return out.toString();
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
        sb.append("  ]\n}\n");
        DatagenFiles.writeText(path, sb.toString());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private record TrimBridgeDefinition(
            String key,
            List<String> ingredientCandidates,
            float itemModelIndex,
            String color,
            Map<String, String> overrideArmorMaterials
    ) {
        private TrimBridgeDefinition {
            ingredientCandidates = List.copyOf(ingredientCandidates);
            Map<String, String> stable = new LinkedHashMap<>(overrideArmorMaterials);
            overrideArmorMaterials = Map.copyOf(stable);
        }
    }

    private record ResolvedTrimBridge(TrimBridgeDefinition definition, String ingredientPath) {}
}
