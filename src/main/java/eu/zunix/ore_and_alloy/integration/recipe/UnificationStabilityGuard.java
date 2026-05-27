package eu.zunix.ore_and_alloy.integration.recipe;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.config.OAConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class UnificationStabilityGuard {
    private static final int ISSUE_PREVIEW_LIMIT = 12;

    private UnificationStabilityGuard() {}

    public static void enforce(RecipeAliasBuildResult result, String reason) {
        if (!OAConfig.unificationStrictModeEnabled()) {
            return;
        }

        List<String> issues = new ArrayList<>();
        issues.addAll(unresolvedCanonicalIssues(result));
        issues.addAll(aliasConflictIssues(result));
        issues.addAll(vanillaExpectationIssues(result));
        if (issues.isEmpty()) {
            OreAndAlloy.LOGGER.info("[{}][Strict:{}] strict checks passed.", OreAndAlloy.MODID, reason);
            return;
        }

        String summary = "[%s][Strict:%s] %d stability issue(s) detected. Samples: %s"
                .formatted(OreAndAlloy.MODID, reason, issues.size(), preview(issues));
        if (OAConfig.unificationStrictModeFailFastEnabled()) {
            throw new IllegalStateException(summary);
        }

        OreAndAlloy.LOGGER.error(summary);
    }

    private static List<String> unresolvedCanonicalIssues(RecipeAliasBuildResult result) {
        List<String> issues = new ArrayList<>();
        List<Map.Entry<MaterialAliasKey, Item>> entries = result.canonicalByGroup().entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().material() + ":" + entry.getKey().form()))
                .toList();

        for (Map.Entry<MaterialAliasKey, Item> entry : entries) {
            ResourceLocation canonicalId = itemId(entry.getValue());
            if (OreAndAlloy.MODID.equals(canonicalId.getNamespace())) continue;

            MaterialAliasKey key = entry.getKey();
            issues.add("unresolved " + key.material() + ":" + key.form() + " -> " + canonicalId);
        }

        return issues;
    }

    private static List<String> aliasConflictIssues(RecipeAliasBuildResult result) {
        List<String> issues = new ArrayList<>();
        List<Map.Entry<Item, List<Item>>> entries = result.aliasCanonicalCandidates().entrySet().stream()
                .sorted(Comparator.comparing(entry -> itemId(entry.getKey()).toString()))
                .toList();

        for (Map.Entry<Item, List<Item>> entry : entries) {
            LinkedHashSet<String> distinct = new LinkedHashSet<>();
            for (Item candidate : entry.getValue()) {
                distinct.add(itemId(candidate).toString());
            }
            if (distinct.size() < 2) continue;
            issues.add("conflict " + itemId(entry.getKey()) + " -> " + distinct);
        }

        return issues;
    }

    private static List<String> vanillaExpectationIssues(RecipeAliasBuildResult result) {
        List<String> issues = new ArrayList<>();
        for (String issue : UnificationVanillaExpectations.evaluate(result.aliasToCanonical())) {
            issues.add("vanilla " + issue);
        }
        return issues;
    }

    private static String preview(List<String> issues) {
        int end = Math.min(ISSUE_PREVIEW_LIMIT, issues.size());
        return issues.subList(0, end).toString();
    }

    private static ResourceLocation itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }
}
