package eu.zunix.ore_and_alloy.core;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MaterialFormCatalog {
    public static final List<String> FORM_ORDER = List.of(
            "ore",
            "raw",
            "crushed",
            "dust",
            "ingot",
            "nugget",
            "plate",
            "rod",
            "gear"
    );

    public static final List<String> PREFIX_FORMS = List.of("crushed", "raw");

    public static final List<String> FORM_SUFFIX_PARSE_ORDER = FORM_ORDER.stream()
            .sorted(Comparator.comparingInt(String::length).reversed())
            .toList();

    public static final Map<String, String> TAG_BUCKET_BY_FORM = Map.ofEntries(
            Map.entry("ingot", "ingots"),
            Map.entry("nugget", "nuggets"),
            Map.entry("dust", "dusts"),
            Map.entry("plate", "plates"),
            Map.entry("rod", "rods"),
            Map.entry("gear", "gears"),
            Map.entry("crushed", "crushed_raw_materials"),
            Map.entry("ore", "ores"),
            Map.entry("raw", "raw_materials"),
            Map.entry("block", "storage_blocks")
    );

    public static final Map<String, String> FORM_BY_TAG_BUCKET = Map.ofEntries(
            Map.entry("ingots", "ingot"),
            Map.entry("nuggets", "nugget"),
            Map.entry("dusts", "dust"),
            Map.entry("plates", "plate"),
            Map.entry("sheets", "plate"),
            Map.entry("rods", "rod"),
            Map.entry("gears", "gear"),
            Map.entry("crushed_raw_materials", "crushed"),
            Map.entry("crushed_materials", "crushed"),
            Map.entry("crusheds", "crushed"),
            Map.entry("ores", "ore"),
            Map.entry("raw_materials", "raw"),
            Map.entry("raws", "raw"),
            Map.entry("storage_blocks", "block")
    );

    public static final Set<String> HANDHELD_FORMS = Set.of("rod");

    public static final Set<String> METAL_SET_FORMS = Set.of(
            "ingot",
            "nugget",
            "plate",
            "rod",
            "dust",
            "gear"
    );

    private MaterialFormCatalog() {}
}
