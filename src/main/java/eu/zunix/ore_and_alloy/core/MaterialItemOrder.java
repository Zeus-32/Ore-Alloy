package eu.zunix.ore_and_alloy.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MaterialItemOrder {
    private static final Map<String, String> MATERIAL_ALIASES = Map.ofEntries(
            Map.entry("aluminium", "aluminum"),
            Map.entry("chrome", "chromium"),
            Map.entry("cuppronickel", "cupronickel")
    );
    private static final Map<String, String> PREFERRED_ITEM_MATERIAL_TOKENS = Map.of();
    private static final Map<String, String> BARE_ITEM_FORMS = Map.ofEntries(
            Map.entry("silicon", "silicon"),
            Map.entry("diamond", "gem"),
            Map.entry("ruby", "gem"),
            Map.entry("sapphire", "gem"),
            Map.entry("emerald", "gem"),
            Map.entry("topaz", "gem"),
            Map.entry("apatite", "gem"),
            Map.entry("certus_quartz", "gem")
    );
    private static final Set<String> INGOT_EQUIVALENT_FORMS = buildIngotEquivalentForms();

    private static final Map<String, Integer> FORM_RANK = buildFormRank();
    private static final List<String> PARSE_SUFFIXES = MaterialFormCatalog.FORM_SUFFIX_PARSE_ORDER;

    private static final Comparator<String> ID_COMPARATOR = Comparator
            .comparing(MaterialItemOrder::materialPart)
            .thenComparingInt(MaterialItemOrder::formRank)
            .thenComparing(Comparator.naturalOrder());

    private MaterialItemOrder() {}

    public static Comparator<String> comparator() {
        return ID_COMPARATOR;
    }

    public static List<String> sorted(Collection<String> itemIds) {
        List<String> out = new ArrayList<>(itemIds);
        out.sort(ID_COMPARATOR);
        return out;
    }

    public static String materialPart(String itemId) {
        if (StandaloneMaterialItems.byId(itemId).isPresent()) return itemId;
        ParsedId parsed = parse(itemId);
        return parsed == null ? canonicalMaterialToken(itemId) : canonicalMaterialToken(parsed.material());
    }

    public static int formRank(String itemId) {
        if (StandaloneMaterialItems.byId(itemId).isPresent()) return Integer.MAX_VALUE - 1;
        ParsedId parsed = parse(itemId);
        if (parsed == null) return Integer.MAX_VALUE;
        return formTokenRank(parsed.form());
    }

    public static int formTokenRank(String formToken) {
        if (formToken == null) return Integer.MAX_VALUE;
        String normalized = normalize(formToken);
        if (INGOT_EQUIVALENT_FORMS.contains(normalized)) {
            normalized = "ingot";
        }
        return FORM_RANK.getOrDefault(normalized, Integer.MAX_VALUE);
    }

    public static String canonicalMaterialToken(String material) {
        String normalized = normalize(material);
        return MATERIAL_ALIASES.getOrDefault(normalized, normalized);
    }

    private static ParsedId parse(String itemId) {
        String lowered = normalize(itemId);
        for (String form : MaterialFormCatalog.PREFIX_FORMS) {
            String prefix = form + "_";
            if (!lowered.startsWith(prefix)) continue;
            if (lowered.length() <= prefix.length()) return null;
            String material = lowered.substring(prefix.length());
            if (material.isBlank()) return null;
            return new ParsedId(normalizeMaterialForForm(material, form), form);
        }

        for (String form : PARSE_SUFFIXES) {
            String suffix = "_" + form;
            if (!lowered.endsWith(suffix)) continue;
            String material = lowered.substring(0, lowered.length() - suffix.length());
            if (material.isBlank()) return null;
            return new ParsedId(normalizeMaterialForForm(material, form), form);
        }

        Optional<String> bareForm = bareItemForm(lowered);
        if (bareForm.isPresent()) {
            return new ParsedId(canonicalMaterialToken(lowered), bareForm.get());
        }
        return null;
    }

    public static Optional<String> bareItemForm(String itemId) {
        String normalized = canonicalMaterialToken(normalize(itemId));
        return Optional.ofNullable(BARE_ITEM_FORMS.get(normalized));
    }

    public static Optional<String> formToken(String itemId) {
        ParsedId parsed = parse(itemId);
        return parsed == null ? Optional.empty() : Optional.of(parsed.form());
    }

    public static String preferredItemMaterialToken(String materialToken) {
        String canonical = canonicalMaterialToken(materialToken);
        return PREFERRED_ITEM_MATERIAL_TOKENS.getOrDefault(canonical, canonical);
    }

    private static Map<String, Integer> buildFormRank() {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (int i = 0; i < MaterialFormCatalog.FORM_ORDER.size(); i++) {
            out.put(MaterialFormCatalog.FORM_ORDER.get(i), i);
        }
        return Map.copyOf(out);
    }

    private static Set<String> buildIngotEquivalentForms() {
        return MaterialFormCatalog.TAG_BUCKET_BY_FORM.entrySet().stream()
                .filter(entry -> "gems".equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static String normalizeMaterialForForm(String material, String form) {
        String out = normalize(material);
        if ("raw".equals(form)) {
            out = RawMaterialMappings.materialForRawVariant(out).orElse(out);
        }
        if ("crushed".equals(form)) {
            if (out.startsWith("raw_") && out.length() > "raw_".length()) {
                out = out.substring("raw_".length());
            }
            if (out.endsWith("_raw") && out.length() > "_raw".length()) {
                out = out.substring(0, out.length() - "_raw".length());
            }
            out = RawMaterialMappings.materialForRawVariant(out).orElse(out);
        }
        if ("ore".equals(form)) {
            out = normalizeOreMaterialToken(out);
        }
        return out;
    }

    private static String normalizeOreMaterialToken(String materialToken) {
        String out = RawVariantCatalog.stripOreMaterialPrefix(materialToken);
        out = RawMaterialMappings.materialForRawVariant(out).orElse(out);
        return out;
    }

    private record ParsedId(String material, String form) {}
}
