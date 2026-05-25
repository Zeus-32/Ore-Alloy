package eu.zunix.ore_and_alloy.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class OreHostVariantCatalog {
    private static final List<HostVariant> HOST_VARIANTS = List.of(
            new HostVariant("stone", "", "stone", "", "stone"),
            new HostVariant("deepslate", "deepslate_", "deepslate", "Deepslate ", "deepslate"),
            new HostVariant("granite", "granite_", "granite", "Granite ", "granite"),
            new HostVariant("diorite", "diorite_", "diorite", "Diorite ", "diorite"),
            new HostVariant("andesite", "andesite_", "andesite", "Andesite ", "andesite"),
            new HostVariant("nether", "nether_", "netherrack", "Nether ", "netherrack"),
            new HostVariant("end", "end_", "end_stone", "End ", "end_stone"),
            new HostVariant("tuff", "tuff_", "tuff", "Tuff ", "tuff")
    );

    private static final Map<String, HostVariant> HOST_BY_TOKEN = buildHostByToken();
    private static final List<String> ORE_PARSE_PREFIXES = List.of(
            "end_stone_",
            "netherrack_",
            "deepslate_",
            "granite_",
            "diorite_",
            "andesite_",
            "nether_",
            "end_",
            "tuff_",
            "stone_",
            "raw_"
    );
    private static final String LEGACY_NETHERRACK_PREFIX = "netherrack_";
    private static final String LEGACY_END_STONE_PREFIX = "end_stone_";

    private OreHostVariantCatalog() {}

    public static List<HostVariant> hostVariants() {
        return HOST_VARIANTS;
    }

    public static HostVariant stoneHost() {
        return HOST_VARIANTS.getFirst();
    }

    public static HostVariant hostByToken(String token) {
        String normalized = normalizeToken(token);
        if ("netherrack".equals(normalized)) {
            normalized = "nether";
        } else if ("end_stone".equals(normalized)) {
            normalized = "end";
        }
        return HOST_BY_TOKEN.getOrDefault(normalized, stoneHost());
    }

    public static List<String> oreParsePrefixes() {
        return ORE_PARSE_PREFIXES;
    }

    public static HostVariant hostFromOreBlockId(String blockId) {
        String normalized = normalizeToken(blockId);
        if (!normalized.endsWith("_ore")) {
            return stoneHost();
        }

        if (normalized.startsWith(LEGACY_NETHERRACK_PREFIX)) {
            return hostByToken("nether");
        }
        if (normalized.startsWith(LEGACY_END_STONE_PREFIX)) {
            return hostByToken("end");
        }

        for (HostVariant variant : HOST_VARIANTS) {
            String prefix = variant.blockPrefix();
            if (prefix.isBlank()) continue;
            if (normalized.startsWith(prefix)) {
                return variant;
            }
        }
        return stoneHost();
    }

    public static String normalizeToken(String token) {
        return token == null
                ? ""
                : token.toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_').trim();
    }

    private static Map<String, HostVariant> buildHostByToken() {
        Map<String, HostVariant> out = new LinkedHashMap<>();
        for (HostVariant variant : HOST_VARIANTS) {
            out.put(variant.token(), variant);
        }
        if (out.containsKey("nether")) {
            out.put("netherrack", out.get("nether"));
        }
        if (out.containsKey("end")) {
            out.put("end_stone", out.get("end"));
        }
        return Map.copyOf(out);
    }

    public record HostVariant(
            String token,
            String blockPrefix,
            String baseTexture,
            String displayPrefix,
            String baseBlockId
    ) {
        public String blockId(String rawVariantToken) {
            String raw = normalizeToken(rawVariantToken);
            if (raw.isBlank()) return raw;
            return blockPrefix + raw + "_ore";
        }
    }
}
