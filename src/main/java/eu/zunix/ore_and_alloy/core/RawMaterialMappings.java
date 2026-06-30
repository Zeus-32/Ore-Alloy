package eu.zunix.ore_and_alloy.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class RawMaterialMappings {
    private static final Map<String, List<String>> RAW_VARIANTS_BY_MATERIAL = buildRawVariantsByMaterial();
    private static final Map<String, String> MATERIAL_BY_RAW_VARIANT = buildMaterialByRawVariant();

    private RawMaterialMappings() {}

    public static List<String> rawVariantsForMaterial(String materialToken) {
        String canonical = MaterialItemOrder.canonicalMaterialToken(normalize(materialToken));
        return RAW_VARIANTS_BY_MATERIAL.getOrDefault(canonical, List.of());
    }

    public static Optional<String> primaryRawVariantForMaterial(String materialToken) {
        List<String> variants = rawVariantsForMaterial(materialToken);
        if (variants.isEmpty()) return Optional.empty();
        return Optional.of(variants.getFirst());
    }

    public static Optional<String> materialForRawVariant(String rawVariantToken) {
        String normalized = normalize(rawVariantToken);
        return Optional.ofNullable(MATERIAL_BY_RAW_VARIANT.get(normalized));
    }

    public static boolean isConfiguredRawVariant(String rawVariantToken) {
        String normalized = normalize(rawVariantToken);
        for (List<String> variants : RAW_VARIANTS_BY_MATERIAL.values()) {
            if (variants.contains(normalized)) return true;
        }
        return false;
    }

    public static List<String> rawItemIdsForMaterial(String materialToken) {
        String normalizedMaterial = normalize(materialToken);
        List<String> variants = rawVariantsForMaterial(normalizedMaterial);
        if (variants.isEmpty()) {
            return List.of("raw_" + normalizedMaterial);
        }
        List<String> out = new ArrayList<>(variants.size());
        for (String variant : variants) {
            out.add("raw_" + variant);
        }
        return List.copyOf(out);
    }

    public static List<String> crushedItemIdsForMaterial(String materialToken) {
        String normalizedMaterial = normalize(materialToken);
        List<String> variants = rawVariantsForMaterial(normalizedMaterial);
        if (variants.isEmpty()) {
            return List.of("crushed_" + normalizedMaterial);
        }
        List<String> out = new ArrayList<>(variants.size());
        for (String variant : variants) {
            out.add("crushed_" + variant);
        }
        return List.copyOf(out);
    }

    public static Optional<String> primaryCrushedVariantForMaterial(String materialToken) {
        List<String> variants = crushedItemIdsForMaterial(materialToken);
        if (variants.isEmpty()) return Optional.empty();
        String first = variants.getFirst();
        if (!first.startsWith("crushed_") || first.length() <= "crushed_".length()) {
            return Optional.empty();
        }
        return Optional.of(first.substring("crushed_".length()));
    }

    private static Map<String, List<String>> buildRawVariantsByMaterial() {
        Map<String, List<String>> out = new LinkedHashMap<>();

        put(out, "aluminum", "bauxite", "cryolite");
        put(out, "antimony", "antimony");
        put(out, "chromium", "chromite");
        put(out, "cobalt", "cobaltite");
        put(out, "copper", "chalcopyrite", "malachite", "bornite", "copper");
        put(out, "certus_quartz", "certus_quartz");
        put(out, "diamond", "diamond");
        put(out, "emerald", "emerald");
        put(out, "gold", "gold", "sylvanite");
        put(out, "iridium", "iridium");
        put(out, "iron", "iron", "hematite", "magnetite", "limonite");
        put(out, "lead", "galena", "lead");
        put(out, "lithium", "lithium");
        put(out, "nickel", "pentlandite", "garnierite");
        put(out, "osmium", "osmium");
        put(out, "platinum", "platinum", "sperrylite");
        put(out, "ruby", "ruby");
        put(out, "sapphire", "sapphire");
        put(out, "silver", "silver");
        put(out, "tin", "cassiterite", "tin");
        put(out, "titanium", "titanium");
        put(out, "topaz", "topaz");
        put(out, "apatite", "apatite");
        put(out, "uranium", "uranium", "uraninite");
        put(out, "zinc", "sphalerite", "hemimorphite");

        return Map.copyOf(out);
    }

    private static Map<String, String> buildMaterialByRawVariant() {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : RAW_VARIANTS_BY_MATERIAL.entrySet()) {
            for (String variant : entry.getValue()) {
                out.putIfAbsent(variant, entry.getKey());
            }
            out.putIfAbsent(entry.getKey(), entry.getKey());
        }
        return Map.copyOf(out);
    }

    private static void put(Map<String, List<String>> out, String material, String... variants) {
        String canonicalMaterial = MaterialItemOrder.canonicalMaterialToken(normalize(material));
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String variant : variants) {
            String token = normalize(variant);
            if (!token.isBlank()) normalized.add(token);
        }
        out.put(canonicalMaterial, List.copyOf(normalized));
    }

    private static String normalize(String token) {
        return token == null
                ? ""
                : token.toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }
}
