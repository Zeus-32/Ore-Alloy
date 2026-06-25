package eu.zunix.ore_and_alloy.registry.discovery;

import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class MaterialItemIdUtil {
    private static final List<String> FORM_SUFFIXES = MaterialFormCatalog.FORM_SUFFIX_PARSE_ORDER;

    private MaterialItemIdUtil() {}

    static ParsedId parseItemId(String itemId) {
        String lowered = itemId.toLowerCase(Locale.ROOT);
        if (isRemovedLegacyFormId(lowered)) {
            return null;
        }
        for (String form : MaterialFormCatalog.PREFIX_FORMS) {
            String prefix = form + "_";
            if (!lowered.startsWith(prefix)) continue;
            if (lowered.length() <= prefix.length()) return null;
            String material = lowered.substring(prefix.length());
            if (material.isBlank()) return null;
            return new ParsedId(normalizeMaterialForForm(material, form), form);
        }

        for (String form : FORM_SUFFIXES) {
            String suffix = "_" + form;
            if (!lowered.endsWith(suffix)) continue;
            String material = lowered.substring(0, lowered.length() - suffix.length());
            if (material.isBlank()) return null;
            return new ParsedId(normalizeMaterialForForm(material, form), form);
        }
        var bareForm = MaterialItemOrder.bareItemForm(lowered);
        if (bareForm.isPresent()) {
            return new ParsedId(MaterialItemOrder.canonicalMaterialToken(lowered), bareForm.get());
        }
        return null;
    }

    static String itemIdFor(String material, String form) {
        String normalizedMaterial = material == null ? "" : material.toLowerCase(Locale.ROOT);
        if (normalizedMaterial.isBlank()) return normalizedMaterial;
        if (form == null || form.isBlank()) return normalizedMaterial;
        String canonicalMaterial = MaterialItemOrder.canonicalMaterialToken(normalizedMaterial);
        String preferredMaterial = MaterialItemOrder.preferredItemMaterialToken(canonicalMaterial);
        if (MaterialItemOrder.bareItemForm(canonicalMaterial).map(form::equals).orElse(false)) {
            return canonicalMaterial;
        }
        if ("raw".equals(form)) {
            return RawMaterialMappings.primaryRawVariantForMaterial(canonicalMaterial)
                    .map(variant -> "raw_" + variant)
                    .orElse("raw_" + preferredMaterial);
        }
        if ("crushed".equals(form)) {
            return RawMaterialMappings.primaryCrushedVariantForMaterial(canonicalMaterial)
                    .map(variant -> "crushed_" + variant)
                    .orElse("crushed_" + preferredMaterial);
        }
        if ("ore".equals(form)) {
            return RawMaterialMappings.primaryRawVariantForMaterial(canonicalMaterial)
                    .map(variant -> variant + "_ore")
                    .orElse(preferredMaterial + "_ore");
        }
        return preferredMaterial + "_" + form;
    }

    static List<String> textureCandidates(String itemId, String material, String form) {
        List<String> out = new ArrayList<>();
        String textureFolder = MaterialFormCatalog.TAG_BUCKET_BY_FORM.getOrDefault(form, form);
        if (MaterialItemOrder.bareItemForm(itemId).map(form::equals).orElse(false)) {
            out.add("item/" + itemId);
            out.add("item/" + textureFolder + "/" + itemId);
            out.add("item/" + form + "/" + itemId);
            return out;
        }
        out.add("item/" + textureFolder + "/" + itemId);
        out.add("item/" + textureFolder + "/" + material + "_" + form);
        out.add("item/" + textureFolder + "/" + form + "_" + material);
        out.add("item/" + form + "/" + itemId);
        out.add("item/" + form + "/" + material + "_" + form);
        out.add("item/" + form + "/" + form + "_" + material);

        if ("raw".equals(form)) {
            out.add("item/raw_materials/" + itemId);
            out.add("item/raw_materials/raw_" + material);
            out.add("item/raw_materials/" + material + "_raw");
        }

        if ("crushed".equals(form)) {
            out.add("item/crushed_raw_materials/" + itemId);
            out.add("item/crushed_raw_materials/crushed_" + material);
            out.add("item/crushed_raw_materials/crushed_raw_" + material);
            out.add("item/crushed_raw_materials/" + material + "_crushed");
            out.add("item/crushed/" + itemId);
            out.add("item/crushed/crushed_" + material);
            out.add("item/crushed/crushed_raw_" + material);
            out.add("item/crushed/" + material + "_crushed");
        }

        return out;
    }

    private static String normalizeMaterialForForm(String material, String form) {
        if (material == null) return "";
        String lowered = material.toLowerCase(Locale.ROOT);
        if ("raw".equals(form)) {
            return RawMaterialMappings.materialForRawVariant(lowered).orElse(lowered);
        }
        if ("crushed".equals(form)) {
            if (lowered.startsWith("raw_") && lowered.length() > "raw_".length()) {
                lowered = lowered.substring("raw_".length());
            }
            if (lowered.endsWith("_raw") && lowered.length() > "_raw".length()) {
                lowered = lowered.substring(0, lowered.length() - "_raw".length());
            }
            lowered = RawMaterialMappings.materialForRawVariant(lowered).orElse(lowered);
        }
        if ("ore".equals(form)) {
            lowered = RawVariantCatalog.stripOreMaterialPrefix(lowered);
            lowered = RawMaterialMappings.materialForRawVariant(lowered).orElse(lowered);
        }
        return lowered;
    }

    private static boolean isRemovedLegacyFormId(String itemId) {
        return itemId.endsWith("_dirty_dust")
                || itemId.endsWith("_purified_dust");
    }

    record ParsedId(String material, String form) {}
}
