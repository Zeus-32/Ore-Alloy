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

            "dirty_dust",
            "dust",
            "purified_dust",

            "crystal",
            "clump",
            "shard",

            "gem",
            "ingot",
            "nugget",

            "plate",
            "sheet",
            "rod",
            "long_rod",

            "ring",
            "gear",
            "spring",
            "bolt",
            "screw"
    );

    public static final List<String> PREFIX_FORMS = List.of("crushed", "raw");

    public static final List<String> FORM_SUFFIX_PARSE_ORDER = FORM_ORDER.stream()
            .sorted(Comparator.comparingInt(String::length).reversed())
            .toList();

    public static final Map<String, String> TAG_BUCKET_BY_FORM = Map.ofEntries(
            Map.entry("ingot", "ingots"),
            Map.entry("nugget", "nuggets"),
            Map.entry("dust", "dusts"),
            Map.entry("dirty_dust", "dirty_dusts"),
            Map.entry("purified_dust", "purified_dusts"),
            Map.entry("gem", "gems"),
            Map.entry("plate", "plates"),
            Map.entry("sheet", "sheets"),
            Map.entry("rod", "rods"),
            Map.entry("long_rod", "long_rods"),
            Map.entry("gear", "gears"),
            Map.entry("crystal", "crystals"),
            Map.entry("clump", "clumps"),
            Map.entry("shard", "shards"),
            Map.entry("ring", "rings"),
            Map.entry("spring", "springs"),
            Map.entry("bolt", "bolts"),
            Map.entry("screw", "screws"),
            Map.entry("crushed", "crushed_raw_materials"),
            Map.entry("ore", "ores"),
            Map.entry("raw", "raw_materials"),
            Map.entry("block", "storage_blocks")
    );

    public static final Map<String, String> FORM_BY_TAG_BUCKET = Map.ofEntries(
            Map.entry("ingots", "ingot"),
            Map.entry("nuggets", "nugget"),
            Map.entry("dusts", "dust"),
            Map.entry("dirty_dusts", "dirty_dust"),
            Map.entry("purified_dusts", "purified_dust"),
            Map.entry("gems", "gem"),
            Map.entry("plates", "plate"),
            Map.entry("sheets", "plate"),
            Map.entry("crystals", "crystal"),
            Map.entry("clumps", "clump"),
            Map.entry("shards", "shard"),
            Map.entry("rods", "rod"),
            Map.entry("long_rods", "long_rod"),
            Map.entry("gears", "gear"),
            Map.entry("rings", "ring"),
            Map.entry("springs", "spring"),
            Map.entry("bolts", "bolt"),
            Map.entry("screws", "screw"),
            Map.entry("crushed_raw_materials", "crushed"),
            Map.entry("crushed_materials", "crushed"),
            Map.entry("crusheds", "crushed"),
            Map.entry("ores", "ore"),
            Map.entry("raw_materials", "raw"),
            Map.entry("raws", "raw"),
            Map.entry("storage_blocks", "block")
    );

    public static final Set<String> HANDHELD_FORMS = Set.of("rod", "long_rod");

    public static final Set<String> METAL_SET_FORMS = Set.of(
            "ingot",
            "nugget",
            "plate",
            "rod",
            "long_rod",
            "crystal",
            "shard",
            "clump",
            "dirty_dust",
            "dust",
            "purified_dust",
            "gear",
            "bolt",
            "screw",
            "ring",
            "spring"
    );

    public static final Set<String> GEM_SET_FORMS = Set.of(
            "gem",
            "plate",
            "rod",
            "long_rod",
            "crystal",
            "shard",
            "clump",
            "dirty_dust",
            "dust",
            "purified_dust",
            "gear",
            "bolt",
            "screw",
            "ring",
            "spring"
    );

    private MaterialFormCatalog() {}
}
