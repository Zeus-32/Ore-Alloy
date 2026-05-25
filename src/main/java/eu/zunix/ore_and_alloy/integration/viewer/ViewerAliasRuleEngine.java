package eu.zunix.ore_and_alloy.integration.viewer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class ViewerAliasRuleEngine {
    private static final List<String> ITEM_ALIAS_RULES = List.of(
            "prospector -> ore scanner",
            "prospector -> prospecting",
            "prospector -> vein scanner"
    );

    private static final List<String> MOLTEN_BUCKET_ALIAS_RULES = List.of(
            "{material}",
            "molten {material}",
            "liquid {material}",
            "{material} fluid"
    );

    private static final List<String> FORM_ALIAS_RULES = List.of(
            "* -> {material}",

            "ingot -> ingot",
            "ingot -> {material} ingot",

            "nugget -> nugget",
            "nugget -> {material} nugget",

            "plate -> plate",
            "plate -> {material} plate",
            "plate -> sheet",
            "plate -> {material} sheet",

            "sheet -> sheet",
            "sheet -> {material} sheet",
            "sheet -> plate",
            "sheet -> {material} plate",

            "rod -> rod",
            "rod -> {material} rod",

            "long_rod -> long rod",
            "long_rod -> {material} long rod",

            "gem -> gem",
            "gem -> {material} gem",

            "crystal -> crystal",
            "crystal -> {material} crystal",

            "dust -> dust",
            "dust -> {material} dust",

            "dirty_dust -> dirty dust",
            "dirty_dust -> {material} dirty dust",
            "dirty_dust -> {material} impure dust",

            "purified_dust -> purified dust",
            "purified_dust -> {material} purified dust",
            "purified_dust -> {material} clean dust",

            "clump -> clump",
            "clump -> {material} clump",

            "shard -> shard",
            "shard -> {material} shard",

            "gear -> gear",
            "gear -> {material} gear",

            "bolt -> bolt",
            "bolt -> {material} bolt",

            "screw -> screw",
            "screw -> {material} screw",

            "crushed -> crushed",
            "crushed -> crushed {material}",
            "crushed -> {material} crushed",
            "crushed -> crushed raw {material}",

            "ring -> ring",
            "ring -> {material} ring",

            "spring -> spring",
            "spring -> {material} spring",

            "ore -> ore",
            "ore -> {material} ore",

            "raw -> raw",
            "raw -> raw {material}",
            "raw -> {material} raw"
    );

    private static final Map<String, String> MATERIAL_SYNONYMS = Map.ofEntries(
            Map.entry("aluminium", "aluminum"),
            Map.entry("aluminum", "aluminium")
    );

    private ViewerAliasRuleEngine() {}

    static void addItemAliases(String itemPath, Set<String> aliases) {
        for (String ruleLine : ITEM_ALIAS_RULES) {
            Optional<AliasRule> rule = parseRule(ruleLine);
            if (rule.isEmpty()) continue;
            if (!rule.get().selector().equals(itemPath)) continue;
            aliases.add(resolveTemplate(rule.get().template(), "", ""));
        }
    }

    static void addMoltenBucketAliases(String materialToken, Set<String> aliases) {
        String materialLabel = toDisplayName(materialToken);
        for (String template : MOLTEN_BUCKET_ALIAS_RULES) {
            aliases.add(resolveTemplate(template, materialLabel, ""));
        }

        synonymFor(materialToken).ifPresent(synonym -> {
            String synonymLabel = toDisplayName(synonym);
            for (String template : MOLTEN_BUCKET_ALIAS_RULES) {
                aliases.add(resolveTemplate(template, synonymLabel, ""));
            }
        });
    }

    static void addFormAliases(String formToken, String materialToken, Set<String> aliases) {
        String normalizedForm = normalizeToken(formToken);
        String materialDisplay = toDisplayName(materialToken);
        String formDisplay = toDisplayName(normalizedForm);

        for (String ruleLine : FORM_ALIAS_RULES) {
            Optional<AliasRule> rule = parseRule(ruleLine);
            if (rule.isEmpty()) continue;

            String selector = rule.get().selector();
            if (!"*".equals(selector) && !selector.equals(normalizedForm)) continue;
            aliases.add(resolveTemplate(rule.get().template(), materialDisplay, formDisplay));
        }

        synonymFor(materialToken).ifPresent(synonym -> {
            String synonymDisplay = toDisplayName(synonym);
            for (String ruleLine : FORM_ALIAS_RULES) {
                Optional<AliasRule> rule = parseRule(ruleLine);
                if (rule.isEmpty()) continue;

                String selector = rule.get().selector();
                if (!"*".equals(selector) && !selector.equals(normalizedForm)) continue;
                aliases.add(resolveTemplate(rule.get().template(), synonymDisplay, formDisplay));
            }
        });
    }

    static String normalizeToken(String token) {
        return token.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private static Optional<AliasRule> parseRule(String line) {
        if (line == null) return Optional.empty();
        String trimmed = line.trim();
        if (trimmed.isEmpty()) return Optional.empty();

        int split = trimmed.indexOf("->");
        if (split <= 0 || split >= trimmed.length() - 2) return Optional.empty();

        String selector = normalizeToken(trimmed.substring(0, split).trim());
        String template = trimmed.substring(split + 2).trim();
        if (selector.isBlank() || template.isBlank()) return Optional.empty();
        return Optional.of(new AliasRule(selector, template));
    }

    private static String resolveTemplate(String template, String materialDisplayName, String formDisplayName) {
        String resolved = template
                .replace("{material}", materialDisplayName)
                .replace("{form}", formDisplayName)
                .trim();
        if (resolved.isBlank()) return resolved;
        return String.join(" ", resolved.split("\\s+"));
    }

    private static String toDisplayName(String token) {
        String normalized = normalizeToken(token);
        if (normalized.isBlank()) return normalized;

        String[] words = normalized.split("_");
        List<String> capitalized = new ArrayList<>(words.length);
        for (String word : words) {
            if (word.isBlank()) continue;
            capitalized.add(word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1));
        }
        return String.join(" ", capitalized);
    }

    private static Optional<String> synonymFor(String materialToken) {
        String normalized = normalizeToken(materialToken);
        return Optional.ofNullable(MATERIAL_SYNONYMS.get(normalized));
    }

    private record AliasRule(String selector, String template) {}
}
