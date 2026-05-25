package eu.zunix.ore_and_alloy.registry.discovery;

import eu.zunix.ore_and_alloy.core.GemMaterial;
import eu.zunix.ore_and_alloy.core.GemMaterialFormRules;
import eu.zunix.ore_and_alloy.core.MaterialForm;
import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.MetalMaterial;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.registry.ModStandaloneItems;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

final class GuaranteedMaterialSetBuilder {
    List<String> withGuaranteedForms(List<String> base) {
        Map<String, String> preferredTokens = preferredMaterialTokens(base);
        Map<String, Set<String>> discoveredFormsByMaterial = discoveredFormsByMaterial(base);
        Set<String> out = new TreeSet<>();

        for (String id : base) {
            if (ModStandaloneItems.isStandaloneItemId(id) || MaterialItemIdUtil.isStandaloneMaterialItemId(id)) {
                continue;
            }
            MaterialItemIdUtil.ParsedId parsed = MaterialItemIdUtil.parseItemId(id);
            if (parsed == null) {
                out.add(id.toLowerCase(Locale.ROOT));
                continue;
            }
            if (!GemMaterialFormRules.isAllowed(parsed.material(), parsed.form())) continue;

            String material = preferredMaterialToken(preferredTokens, parsed.material());
            out.add(MaterialItemIdUtil.itemIdFor(material, parsed.form()));
        }

        for (Map.Entry<String, Set<String>> entry : discoveredFormsByMaterial.entrySet()) {
            String material = preferredMaterialToken(preferredTokens, entry.getKey());
            Set<String> forms = entry.getValue();

            if (forms.contains("ingot")) {
                for (String form : MaterialFormCatalog.METAL_SET_FORMS) {
                    out.add(MaterialItemIdUtil.itemIdFor(material, form));
                }
            }

            if (forms.contains("gem")) {
                for (String form : MaterialFormCatalog.GEM_SET_FORMS) {
                    out.add(MaterialItemIdUtil.itemIdFor(material, form));
                }
            }

            out.add(MaterialItemIdUtil.itemIdFor(material, "shard"));
        }

        for (MetalMaterial metal : MetalMaterial.values()) {
            String material = preferredMaterialToken(preferredTokens, metal.materialName());
            out.add(MaterialItemIdUtil.itemIdFor(material, "dirty_dust"));
            out.add(MaterialItemIdUtil.itemIdFor(material, "clump"));
            out.add(MaterialItemIdUtil.itemIdFor(material, "shard"));
            out.add(MaterialItemIdUtil.itemIdFor(material, "ring"));
            out.add(MaterialItemIdUtil.itemIdFor(material, "spring"));
            if (metal.getForms().contains(MaterialForm.RAW)) {
                out.addAll(RawMaterialMappings.rawItemIdsForMaterial(material));
            }
            if (metal.getForms().contains(MaterialForm.CRUSHED)) {
                out.addAll(RawMaterialMappings.crushedItemIdsForMaterial(material));
            }
        }
        for (GemMaterial gem : GemMaterial.values()) {
            String material = preferredMaterialToken(preferredTokens, gem.name().toLowerCase(Locale.ROOT));
            for (MaterialForm form : gem.getForms()) {
                String formName = form.name().toLowerCase(Locale.ROOT);
                if ("raw".equals(formName)) {
                    out.addAll(RawMaterialMappings.rawItemIdsForMaterial(material));
                    continue;
                }
                if ("crushed".equals(formName)) {
                    out.addAll(RawMaterialMappings.crushedItemIdsForMaterial(material));
                    continue;
                }
                out.add(MaterialItemIdUtil.itemIdFor(material, formName));
            }
            out.add(MaterialItemIdUtil.itemIdFor(material, "dirty_dust"));
            out.add(MaterialItemIdUtil.itemIdFor(material, "clump"));
            out.add(MaterialItemIdUtil.itemIdFor(material, "shard"));
            out.add(MaterialItemIdUtil.itemIdFor(material, "ring"));
            out.add(MaterialItemIdUtil.itemIdFor(material, "spring"));
        }

        return MaterialItemOrder.sorted(out);
    }

    private static Map<String, Set<String>> discoveredFormsByMaterial(List<String> itemIds) {
        Map<String, Set<String>> out = new LinkedHashMap<>();
        for (String id : itemIds) {
            MaterialItemIdUtil.ParsedId parsed = MaterialItemIdUtil.parseItemId(id);
            if (parsed == null) continue;
            if (ModStandaloneItems.isStandaloneMaterialToken(parsed.material())) continue;
            if (!GemMaterialFormRules.isAllowed(parsed.material(), parsed.form())) continue;

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
            if (ModStandaloneItems.isStandaloneMaterialToken(parsed.material())) continue;

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
}
