package eu.zunix.ore_and_alloy.core;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RawVariantCatalog {
    private RawVariantCatalog() {}

    public static List<String> collectRawVariants(Collection<String> itemIds) {
        LinkedHashSet<String> variants = new LinkedHashSet<>();
        for (String id : itemIds) {
            if (id == null) continue;
            String normalized = normalize(id);
            if (!normalized.startsWith("raw_") || normalized.length() <= "raw_".length()) continue;
            variants.add(normalized.substring("raw_".length()));
        }
        return variants.stream().sorted().toList();
    }

    public static Set<String> collectRawVariantsSet(Collection<String> itemIds) {
        return Set.copyOf(collectRawVariants(itemIds));
    }

    public static String oreBlockId(String rawVariantToken) {
        return OreHostVariantCatalog.stoneHost().blockId(rawVariantToken);
    }

    public static String deepslateOreBlockId(String rawVariantToken) {
        return oreBlockId(rawVariantToken, "deepslate");
    }

    public static String oreBlockId(String rawVariantToken, String hostToken) {
        String token = normalize(rawVariantToken);
        if (token.isBlank()) return token;
        return OreHostVariantCatalog.hostByToken(hostToken).blockId(token);
    }

    public static List<String> oreBlockIds(String rawVariantToken) {
        String token = normalize(rawVariantToken);
        if (token.isBlank()) return List.of();
        return OreHostVariantCatalog.hostVariants().stream()
                .map(host -> host.blockId(token))
                .toList();
    }

    public static List<String> oreMaterialPrefixesForParsing() {
        return OreHostVariantCatalog.oreParsePrefixes();
    }

    public static String stripOreMaterialPrefix(String materialToken) {
        String out = normalize(materialToken);
        for (String prefix : oreMaterialPrefixesForParsing()) {
            if (out.startsWith(prefix) && out.length() > prefix.length()) {
                return out.substring(prefix.length());
            }
        }
        return out;
    }

    public static String hostTokenForOreBlockId(String oreBlockId) {
        return OreHostVariantCatalog.hostFromOreBlockId(oreBlockId).token();
    }

    public static String normalize(String token) {
        return token == null
                ? ""
                : token.toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_').trim();
    }
}
