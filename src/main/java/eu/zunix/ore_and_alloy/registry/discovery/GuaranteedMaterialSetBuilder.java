package eu.zunix.ore_and_alloy.registry.discovery;

import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;
import eu.zunix.ore_and_alloy.core.MaterialForm;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.MetalMaterial;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

final class GuaranteedMaterialSetBuilder {
    List<String> withGuaranteedForms(List<String> base) {
        Map<String, String> preferredTokens = preferredMaterialTokens(base);
        Map<String, Set<String>> discoveredFormsByMaterial = discoveredFormsByMaterial(base);
        Set<String> out = new TreeSet<>();

        for (String id : base) {
            MaterialItemIdUtil.ParsedId parsed = MaterialItemIdUtil.parseItemId(id);
            if (parsed == null) {
                continue;
            }
            if ("raw".equals(parsed.form()) || "crushed".equals(parsed.form())) {
                if (RawMaterialMappings.isConfiguredRawVariant(variantToken(id, parsed.form()))) {
                    out.add(id);
                }
                continue;
            }
            String material = preferredMaterialToken(preferredTokens, parsed.material());
            out.add(MaterialItemIdUtil.itemIdFor(material, parsed.form()));
        }

        for (Map.Entry<String, Set<String>> entry : discoveredFormsByMaterial.entrySet()) {
            String material = preferredMaterialToken(preferredTokens, entry.getKey());
            Set<String> forms = entry.getValue();

            if (forms.contains("ingot")) {
                MetalMaterial.fromToken(entry.getKey()).ifPresent(metal -> {
                    for (MaterialForm form : metal.getForms()) {
                        String formToken = form.name().toLowerCase(java.util.Locale.ROOT);
                        if ("ore".equals(formToken) || "raw".equals(formToken) || "crushed".equals(formToken)) continue;
                        out.add(MaterialItemIdUtil.itemIdFor(material, formToken));
                    }
                });
            }
            if (forms.contains("raw") && !RawMaterialMappings.rawVariantsForMaterial(material).isEmpty()) {
                out.addAll(RawMaterialMappings.rawItemIdsForMaterial(material));
            }
            if (forms.contains("crushed") && !RawMaterialMappings.rawVariantsForMaterial(material).isEmpty()) {
                out.addAll(RawMaterialMappings.crushedItemIdsForMaterial(material));
            }
        }

        return MaterialItemOrder.sorted(out);
    }

    private static Map<String, Set<String>> discoveredFormsByMaterial(List<String> itemIds) {
        Map<String, Set<String>> out = new LinkedHashMap<>();
        for (String id : itemIds) {
            MaterialItemIdUtil.ParsedId parsed = MaterialItemIdUtil.parseItemId(id);
            if (parsed == null) continue;
            String material = MaterialItemOrder.canonicalMaterialToken(parsed.material());
            out.computeIfAbsent(material, ignored -> new LinkedHashSet<>()).add(parsed.form());
        }
        return out;
    }

    private static Map<String, String> preferredMaterialTokens(List<String> itemIds) {
        Map<String, String> out = new LinkedHashMap<>();

        for (String id : itemIds) {
            MaterialItemIdUtil.ParsedId parsed = MaterialItemIdUtil.parseItemId(id);
            if (parsed == null) continue;
            String token = parsed.material();
            String canonical = MaterialItemOrder.canonicalMaterialToken(token);
            String previous = out.get(canonical);
            if (previous == null || shouldPreferToken(token, previous, canonical)) {
                out.put(canonical, token);
            }
        }

        return out;
    }

    private static boolean shouldPreferToken(String candidate, String existing, String canonical) {
        return canonical.equals(candidate) && !canonical.equals(existing);
    }

    private static String preferredMaterialToken(Map<String, String> preferredTokens, String material) {
        String canonical = MaterialItemOrder.canonicalMaterialToken(material);
        return preferredTokens.getOrDefault(canonical, canonical);
    }

    private static String variantToken(String itemId, String form) {
        String prefix = form + "_";
        return itemId.startsWith(prefix) ? itemId.substring(prefix.length()) : "";
    }
}
