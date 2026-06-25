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
            "tiny_dust_pile",
            "dust_pile",
            "dust",
            "hot_ingot",
            "ingot",
            "nugget",
            "plate",
            "double_plate",
            "rod",
            "long_rod",
            "small_gear",
            "gear",
            "bolt",
            "screw",
            "silicon",
            "gem",
            "diamond"
    );

    public static final List<String> PREFIX_FORMS = List.of("crushed", "raw");

    public static final List<String> FORM_SUFFIX_PARSE_ORDER = FORM_ORDER.stream()
            .sorted(Comparator.comparingInt(String::length).reversed())
            .toList();

    public static final Map<String, String> TAG_BUCKET_BY_FORM = Map.ofEntries(
            Map.entry("ingot", "ingots"),
            Map.entry("hot_ingot", "hot_ingots"),
            Map.entry("nugget", "nuggets"),
            Map.entry("dust", "dusts"),
            Map.entry("tiny_dust_pile", "tiny_dust_piles"),
            Map.entry("dust_pile", "dust_piles"),
            Map.entry("plate", "plates"),
            Map.entry("double_plate", "double_plates"),
            Map.entry("rod", "rods"),
            Map.entry("long_rod", "long_rods"),
            Map.entry("gear", "gears"),
            Map.entry("small_gear", "small_gears"),
            Map.entry("bolt", "bolts"),
            Map.entry("screw", "screws"),
            Map.entry("crushed", "crushed_raw_materials"),
            Map.entry("ore", "ores"),
            Map.entry("raw", "raw_materials"),
            Map.entry("block", "storage_blocks"),
            Map.entry("silicon", "silicon"),
            Map.entry("gem", "gems"),
            Map.entry("diamond", "gems")
    );

    public static final Map<String, String> FORM_BY_TAG_BUCKET = Map.ofEntries(
            Map.entry("ingots", "ingot"),
            Map.entry("hot_ingots", "hot_ingot"),
            Map.entry("nuggets", "nugget"),
            Map.entry("dusts", "dust"),
            Map.entry("tiny_dust_piles", "tiny_dust_pile"),
            Map.entry("dust_piles", "dust_pile"),
            Map.entry("plates", "plate"),
            Map.entry("sheets", "plate"),
            Map.entry("double_plates", "double_plate"),
            Map.entry("rods", "rod"),
            Map.entry("long_rods", "long_rod"),
            Map.entry("gears", "gear"),
            Map.entry("small_gears", "small_gear"),
            Map.entry("bolts", "bolt"),
            Map.entry("screws", "screw"),
            Map.entry("crushed_raw_materials", "crushed"),
            Map.entry("crushed_materials", "crushed"),
            Map.entry("crusheds", "crushed"),
            Map.entry("ores", "ore"),
            Map.entry("raw_materials", "raw"),
            Map.entry("raws", "raw"),
            Map.entry("storage_blocks", "block"),
            Map.entry("silicon", "silicon"),
            Map.entry("gems", "gem")
    );

    public static final Set<String> HANDHELD_FORMS = Set.of("rod", "long_rod");

    public static final Set<String> METAL_SET_FORMS = Set.of(
            "ingot",
            "hot_ingot",
            "nugget",
            "plate",
            "double_plate",
            "rod",
            "long_rod",
            "dust",
            "tiny_dust_pile",
            "dust_pile",
            "gear",
            "small_gear",
            "bolt",
            "screw"
    );

    private MaterialFormCatalog() {}
}
