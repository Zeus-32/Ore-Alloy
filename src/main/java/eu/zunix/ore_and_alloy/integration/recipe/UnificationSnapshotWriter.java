package eu.zunix.ore_and_alloy.integration.recipe;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UnificationSnapshotWriter {
    private static final Path SNAPSHOT_RELATIVE_PATH = Path.of("ore_and_alloy", "unification_snapshot.json");

    private UnificationSnapshotWriter() {}

    public static void write(RecipeAliasBuildResult result, String reason) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("generated_at", Instant.now().toString());
        root.put("reason", reason);
        root.put("priority", UnificationPriorityRules.snapshot());
        root.put("statistics", statistics(result));
        root.put("aliases", aliases(result.aliasToCanonical()));
        root.put("unresolved_groups", unresolvedGroups(result));
        root.put("alias_conflicts", aliasConflicts(result));
        root.put("vanilla_expectation_mismatches", vanillaExpectationMismatches(result));

        String json = toJson(root, 0);
        Path out = FMLPaths.GAMEDIR.get().resolve(SNAPSHOT_RELATIVE_PATH);
        try {
            Files.createDirectories(out.getParent());
            Files.writeString(out, json + System.lineSeparator(), StandardCharsets.UTF_8);
            OreAndAlloy.LOGGER.info("[{}] Wrote unification snapshot: {}", OreAndAlloy.MODID, out.toAbsolutePath());
        } catch (IOException ex) {
            OreAndAlloy.LOGGER.error("[{}] Failed to write unification snapshot: {}", OreAndAlloy.MODID, out.toAbsolutePath(), ex);
        }
    }

    private static Map<String, Object> statistics(RecipeAliasBuildResult result) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("alias_count", result.aliasToCanonical().size());
        out.put("duplicate_group_count", result.duplicateGroups().size());
        out.put("unresolved_group_count", unresolvedGroups(result).size());
        out.put("alias_conflict_count", aliasConflicts(result).size());
        out.put("vanilla_expectation_mismatch_count", vanillaExpectationMismatches(result).size());
        return out;
    }

    private static List<Map<String, String>> aliases(Map<Item, Item> aliasToCanonical) {
        List<Map.Entry<Item, Item>> entries = aliasToCanonical.entrySet().stream()
                .sorted(Comparator.comparing(entry -> itemId(entry.getKey()).toString()))
                .toList();

        List<Map<String, String>> out = new ArrayList<>(entries.size());
        for (Map.Entry<Item, Item> entry : entries) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("alias", itemId(entry.getKey()).toString());
            row.put("canonical", itemId(entry.getValue()).toString());
            out.add(row);
        }
        return out;
    }

    private static List<String> unresolvedGroups(RecipeAliasBuildResult result) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<MaterialAliasKey, Item> entry : result.canonicalByGroup().entrySet()) {
            ResourceLocation canonicalId = itemId(entry.getValue());
            if (OreAndAlloy.MODID.equals(canonicalId.getNamespace())) continue;

            MaterialAliasKey key = entry.getKey();
            out.add(key.material() + ":" + key.form() + " -> " + canonicalId);
        }
        out.sort(String::compareTo);
        return out;
    }

    private static List<Map<String, Object>> aliasConflicts(RecipeAliasBuildResult result) {
        List<Map<String, Object>> out = new ArrayList<>();
        List<Map.Entry<Item, List<Item>>> entries = result.aliasCanonicalCandidates().entrySet().stream()
                .sorted(Comparator.comparing(entry -> itemId(entry.getKey()).toString()))
                .toList();

        for (Map.Entry<Item, List<Item>> entry : entries) {
            Set<String> candidates = new LinkedHashSet<>();
            for (Item item : entry.getValue()) {
                candidates.add(itemId(item).toString());
            }
            if (candidates.size() < 2) continue;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("alias", itemId(entry.getKey()).toString());
            row.put("candidates", new ArrayList<>(candidates));
            out.add(row);
        }
        return out;
    }

    private static List<String> vanillaExpectationMismatches(RecipeAliasBuildResult result) {
        List<String> out = new ArrayList<>(UnificationVanillaExpectations.evaluate(result.aliasToCanonical()));
        out.sort(String::compareTo);
        return out;
    }

    private static ResourceLocation itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    private static String toJson(Object value, int indent) {
        if (value == null) return "null";
        if (value instanceof String string) return "\"" + escape(string) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();

        if (value instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            if (!map.isEmpty()) sb.append("\n");

            int index = 0;
            int size = map.size();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sb.append("  ".repeat(indent + 1))
                        .append("\"").append(escape(String.valueOf(entry.getKey()))).append("\": ")
                        .append(toJson(entry.getValue(), indent + 1));
                if (++index < size) sb.append(",");
                sb.append("\n");
            }
            if (!map.isEmpty()) sb.append("  ".repeat(indent));
            sb.append("}");
            return sb.toString();
        }

        if (value instanceof Iterable<?> iterable) {
            List<Object> list = new ArrayList<>();
            for (Object element : iterable) list.add(element);

            StringBuilder sb = new StringBuilder();
            sb.append("[");
            if (!list.isEmpty()) sb.append("\n");

            for (int i = 0; i < list.size(); i++) {
                sb.append("  ".repeat(indent + 1)).append(toJson(list.get(i), indent + 1));
                if (i + 1 < list.size()) sb.append(",");
                sb.append("\n");
            }
            if (!list.isEmpty()) sb.append("  ".repeat(indent));
            sb.append("]");
            return sb.toString();
        }

        return "\"" + escape(String.valueOf(value)) + "\"";
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
