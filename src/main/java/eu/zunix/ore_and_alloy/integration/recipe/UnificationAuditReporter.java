package eu.zunix.ore_and_alloy.integration.recipe;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;
import eu.zunix.ore_and_alloy.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class UnificationAuditReporter {
    private static final int MAX_DETAIL_LINES = 80;

    private UnificationAuditReporter() {}

    public static void report(RecipeAliasBuildResult result, String reason) {
        OreAndAlloy.LOGGER.warn("[{}][Audit:{}] Unification audit started.", OreAndAlloy.MODID, reason);
        logPriorityRules();
        logDuplicateGroups(result);
        logUnresolvedCanonicalGroups(result);
        logAliasConflicts(result);
        logVanillaExpectations(result);
        logMissingTags();
        logMissingTextures();
        OreAndAlloy.LOGGER.warn("[{}][Audit:{}] Unification audit finished.", OreAndAlloy.MODID, reason);
    }

    private static void logPriorityRules() {
        Map<String, Object> snapshot = UnificationPriorityRules.snapshot();
        OreAndAlloy.LOGGER.warn("[{}][Audit] Priority rules: {}", OreAndAlloy.MODID, snapshot);
    }

    private static void logDuplicateGroups(RecipeAliasBuildResult result) {
        List<Map.Entry<MaterialAliasKey, List<Item>>> duplicates = result.duplicateGroups().entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().material() + ":" + entry.getKey().form()))
                .toList();

        OreAndAlloy.LOGGER.warn("[{}][Audit] Duplicate groups: {}", OreAndAlloy.MODID, duplicates.size());
        int printed = 0;
        for (Map.Entry<MaterialAliasKey, List<Item>> entry : duplicates) {
            if (printed >= MAX_DETAIL_LINES) break;
            MaterialAliasKey key = entry.getKey();
            String items = formatItemIds(entry.getValue());
            OreAndAlloy.LOGGER.warn("[{}][Audit] duplicate {}:{} -> {}", OreAndAlloy.MODID, key.material(), key.form(), items);
            printed++;
        }
        if (duplicates.size() > printed) {
            OreAndAlloy.LOGGER.warn("[{}][Audit] ... {} more duplicate groups omitted.", OreAndAlloy.MODID, duplicates.size() - printed);
        }
    }

    private static void logUnresolvedCanonicalGroups(RecipeAliasBuildResult result) {
        List<String> unresolved = new ArrayList<>();
        for (Map.Entry<MaterialAliasKey, Item> entry : result.canonicalByGroup().entrySet()) {
            ResourceLocation canonicalId = itemId(entry.getValue());
            if (OreAndAlloy.MODID.equals(canonicalId.getNamespace())) continue;
            MaterialAliasKey key = entry.getKey();
            unresolved.add(key.material() + ":" + key.form() + " -> " + canonicalId);
        }
        unresolved.sort(String::compareTo);

        OreAndAlloy.LOGGER.warn("[{}][Audit] Groups without O&A canonical target: {}", OreAndAlloy.MODID, unresolved.size());
        int printed = 0;
        for (String line : unresolved) {
            if (printed >= MAX_DETAIL_LINES) break;
            OreAndAlloy.LOGGER.warn("[{}][Audit] unresolved {}", OreAndAlloy.MODID, line);
            printed++;
        }
        if (unresolved.size() > printed) {
            OreAndAlloy.LOGGER.warn("[{}][Audit] ... {} more unresolved groups omitted.", OreAndAlloy.MODID, unresolved.size() - printed);
        }
    }

    private static void logAliasConflicts(RecipeAliasBuildResult result) {
        List<String> conflicts = new ArrayList<>();
        for (Map.Entry<Item, List<Item>> entry : result.aliasCanonicalCandidates().entrySet()) {
            LinkedHashSet<String> distinct = new LinkedHashSet<>();
            for (Item candidate : entry.getValue()) {
                distinct.add(itemId(candidate).toString());
            }
            if (distinct.size() < 2) continue;
            conflicts.add(itemId(entry.getKey()) + " -> " + distinct);
        }
        conflicts.sort(String::compareTo);

        OreAndAlloy.LOGGER.warn("[{}][Audit] Aliases with multiple canonical candidates: {}", OreAndAlloy.MODID, conflicts.size());
        int printed = 0;
        for (String conflict : conflicts) {
            if (printed >= MAX_DETAIL_LINES) break;
            OreAndAlloy.LOGGER.warn("[{}][Audit] conflict {}", OreAndAlloy.MODID, conflict);
            printed++;
        }
        if (conflicts.size() > printed) {
            OreAndAlloy.LOGGER.warn("[{}][Audit] ... {} more alias conflicts omitted.", OreAndAlloy.MODID, conflicts.size() - printed);
        }
    }

    private static void logVanillaExpectations(RecipeAliasBuildResult result) {
        List<String> issues = new ArrayList<>(UnificationVanillaExpectations.evaluate(result.aliasToCanonical()));
        issues.sort(String::compareTo);

        OreAndAlloy.LOGGER.warn("[{}][Audit] Vanilla canonical expectation mismatches: {}", OreAndAlloy.MODID, issues.size());
        int printed = 0;
        for (String issue : issues) {
            if (printed >= MAX_DETAIL_LINES) break;
            OreAndAlloy.LOGGER.warn("[{}][Audit] vanilla {}", OreAndAlloy.MODID, issue);
            printed++;
        }
        if (issues.size() > printed) {
            OreAndAlloy.LOGGER.warn("[{}][Audit] ... {} more vanilla expectation mismatches omitted.", OreAndAlloy.MODID, issues.size() - printed);
        }
    }

    private static void logMissingTags() {
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, DeferredItem<Item>> entry : ModItems.materialItems().entrySet()) {
            String path = entry.getKey();
            Optional<MaterialAliasKey> parsed = RecipeAliasMapBuilder.materialKeyFromItemId(itemId(path));
            if (parsed.isEmpty()) continue;

            String bucket = MaterialFormCatalog.TAG_BUCKET_BY_FORM.get(parsed.get().form());
            if (bucket == null) continue;

            Item item = entry.getValue().value();
            ItemStack stack = item.getDefaultInstance();
            if (stack.isEmpty()) continue;

            Set<ResourceLocation> tagIds = new LinkedHashSet<>();
            stack.getTags().map(TagKey::location).forEach(tagIds::add);

            ResourceLocation bucketTag = ResourceLocation.fromNamespaceAndPath("c", bucket);
            ResourceLocation materialTag = ResourceLocation.fromNamespaceAndPath("c", bucket + "/" + parsed.get().material());
            if (!tagIds.contains(bucketTag)) {
                missing.add(path + " missing c:" + bucket);
            }
            if (!tagIds.contains(materialTag)) {
                missing.add(path + " missing c:" + bucket + "/" + parsed.get().material());
            }
        }

        missing.sort(String::compareTo);
        OreAndAlloy.LOGGER.warn("[{}][Audit] Missing tag bindings: {}", OreAndAlloy.MODID, missing.size());
        int printed = 0;
        for (String line : missing) {
            if (printed >= MAX_DETAIL_LINES) break;
            OreAndAlloy.LOGGER.warn("[{}][Audit] tag {}", OreAndAlloy.MODID, line);
            printed++;
        }
        if (missing.size() > printed) {
            OreAndAlloy.LOGGER.warn("[{}][Audit] ... {} more missing tag entries omitted.", OreAndAlloy.MODID, missing.size() - printed);
        }
    }

    private static void logMissingTextures() {
        ClassLoader loader = UnificationAuditReporter.class.getClassLoader();
        List<String> missing = new ArrayList<>();

        for (String path : ModItems.materialItems().keySet()) {
            Optional<MaterialAliasKey> parsed = RecipeAliasMapBuilder.materialKeyFromItemId(itemId(path));
            if (parsed.isEmpty()) continue;

            List<String> candidates = textureCandidates(path, parsed.get().material(), parsed.get().form());
            boolean found = false;
            for (String candidate : candidates) {
                if (loader.getResource(candidate) != null) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missing.add(path + " -> " + candidates);
            }
        }

        missing.sort(String::compareTo);
        OreAndAlloy.LOGGER.warn("[{}][Audit] Missing item textures: {}", OreAndAlloy.MODID, missing.size());
        int printed = 0;
        for (String line : missing) {
            if (printed >= MAX_DETAIL_LINES) break;
            OreAndAlloy.LOGGER.warn("[{}][Audit] texture {}", OreAndAlloy.MODID, line);
            printed++;
        }
        if (missing.size() > printed) {
            OreAndAlloy.LOGGER.warn("[{}][Audit] ... {} more missing texture entries omitted.", OreAndAlloy.MODID, missing.size() - printed);
        }
    }

    private static List<String> textureCandidates(String itemPath, String material, String form) {
        List<String> out = new ArrayList<>();
        addTexture(out, form, itemPath);
        addTexture(out, form, material + "_" + form);
        addTexture(out, form, form + "_" + material);

        if ("gem".equals(form)) {
            addTexture(out, "gem", material);
        }
        if ("dirty_dust".equals(form)) {
            addTexture(out, "dirty_dust", "dirty_" + material + "_dust");
            addTexture(out, "dirty_dust", material + "_dust");
        }
        if ("purified_dust".equals(form)) {
            addTexture(out, "purified_dust", "purified_" + material + "_dust");
            addTexture(out, "purified_dust", material + "_dust");
        }
        if ("raw".equals(form)) {
            addTexture(out, "raw", itemPath);
            addTexture(out, "raw", "raw_" + material);
            addTexture(out, "raw", material + "_raw");
            addTexture(out, "raw_materials", itemPath);
            addTexture(out, "raw_materials", "raw_" + material);
            addTexture(out, "raw_materials", material + "_raw");
        }
        if ("crushed".equals(form)) {
            addTexture(out, "crushed", itemPath);
            addTexture(out, "crushed", "crushed_" + material);
            addTexture(out, "crushed", "crushed_raw_" + material);
            addTexture(out, "crushed", material + "_crushed");
        }

        return out.stream().distinct().toList();
    }

    private static void addTexture(List<String> out, String folder, String id) {
        if (folder == null || folder.isBlank() || id == null || id.isBlank()) return;
        out.add("assets/" + OreAndAlloy.MODID + "/textures/item/" + folder + "/" + id + ".png");
    }

    private static ResourceLocation itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    private static ResourceLocation itemId(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, normalizePath(path));
    }

    private static String normalizePath(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.ROOT).trim();
    }

    private static String formatItemIds(List<Item> items) {
        List<String> ids = new ArrayList<>(items.size());
        for (Item item : items) {
            ids.add(itemId(item).toString());
        }
        ids.sort(String::compareTo);
        return ids.toString();
    }
}
