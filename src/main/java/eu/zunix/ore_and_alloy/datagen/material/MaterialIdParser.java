package eu.zunix.ore_and_alloy.datagen.material;

import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;
import java.util.Locale;

public final class MaterialIdParser {
    private static final String PREFIX_RAW = "raw";
    private static final String PREFIX_CRUSHED = "crushed";

    private MaterialIdParser() {}

    public static MaterialId parseItemId(String id) {
        String lowered = id.toLowerCase(Locale.ROOT);
        if (isRemovedLegacyFormId(lowered)) {
            throw new IllegalArgumentException("Removed material form id: " + id);
        }
        for (String prefixForm : MaterialFormCatalog.PREFIX_FORMS) {
            String prefix = prefixForm + "_";
            if (!lowered.startsWith(prefix) || lowered.length() <= prefix.length()) continue;
            String material = lowered.substring(prefix.length());
            if (!material.isBlank()) {
                return new MaterialId(normalizeMaterialForForm(material, prefixForm), prefixForm);
            }
        }

        for (String formToken : MaterialFormCatalog.FORM_SUFFIX_PARSE_ORDER) {
            String suffix = "_" + formToken;
            if (!lowered.endsWith(suffix)) continue;
            String material = lowered.substring(0, lowered.length() - suffix.length());
            if (!material.isBlank()) {
                return new MaterialId(normalizeMaterialForForm(material, formToken), formToken);
            }
        }
        var bareForm = MaterialItemOrder.bareItemForm(lowered);
        if (bareForm.isPresent()) {
            return new MaterialId(MaterialItemOrder.canonicalMaterialToken(lowered), bareForm.get());
        }
        throw new IllegalArgumentException("Cannot parse material form from id: " + id);
    }

    public static String itemIdFor(String material, String form) {
        String normalizedMaterial = material == null ? "" : material.toLowerCase(Locale.ROOT);
        if (normalizedMaterial.isBlank()) return normalizedMaterial;
        if (form == null || form.isBlank()) return normalizedMaterial;
        String canonicalMaterial = MaterialItemOrder.canonicalMaterialToken(normalizedMaterial);
        String preferredMaterial = MaterialItemOrder.preferredItemMaterialToken(canonicalMaterial);
        if (MaterialItemOrder.bareItemForm(canonicalMaterial).map(form::equals).orElse(false)) {
            return canonicalMaterial;
        }
        if (PREFIX_RAW.equals(form)) {
            return RawMaterialMappings.primaryRawVariantForMaterial(canonicalMaterial)
                    .map(variant -> "raw_" + variant)
                    .orElse("raw_" + preferredMaterial);
        }
        if (PREFIX_CRUSHED.equals(form)) {
            return RawMaterialMappings.primaryCrushedVariantForMaterial(canonicalMaterial)
                    .map(variant -> "crushed_" + variant)
                    .orElse("crushed_" + preferredMaterial);
        }
        return preferredMaterial + "_" + form;
    }

    public static String deriveItemIdFromTextureFile(String form, String textureId) {
        String normalizedForm = normalizeFormFolder(form);

        if (MaterialItemOrder.bareItemForm(textureId).map(normalizedForm::equals).orElse(false)) {
            return textureId;
        }
        if ("single".equals(normalizedForm)) {
            return textureId;
        }
        if (PREFIX_RAW.equals(normalizedForm)) {
            if (textureId.startsWith("raw_") && textureId.length() > "raw_".length()) {
                return "raw_" + textureId.substring("raw_".length());
            }
            if (textureId.endsWith("_raw") && textureId.length() > "_raw".length()) {
                return "raw_" + textureId.substring(0, textureId.length() - "_raw".length());
            }
            return textureId.isBlank() ? "" : "raw_" + textureId;
        }

        if (PREFIX_CRUSHED.equals(normalizedForm)) {
            if (textureId.startsWith("crushed_raw_") && textureId.length() > "crushed_raw_".length()) {
                return "crushed_" + textureId.substring("crushed_raw_".length());
            }
            if (textureId.startsWith("crushed_") && textureId.length() > "crushed_".length()) {
                return "crushed_" + normalizeMaterialForForm(textureId.substring("crushed_".length()), PREFIX_CRUSHED);
            }
            if (textureId.endsWith("_crushed") && textureId.length() > "_crushed".length()) {
                return "crushed_" + normalizeMaterialForForm(textureId.substring(0, textureId.length() - "_crushed".length()), PREFIX_CRUSHED);
            }
            return textureId.isBlank() ? "" : "crushed_" + normalizeMaterialForForm(textureId, PREFIX_CRUSHED);
        }

        if (textureId.endsWith("_" + normalizedForm) && textureId.length() > normalizedForm.length() + 1) {
            return textureId;
        }
        if (textureId.startsWith(normalizedForm + "_") && textureId.length() > normalizedForm.length() + 1) {
            String material = textureId.substring(normalizedForm.length() + 1);
            return material.isBlank() ? "" : material + "_" + normalizedForm;
        }
        return "";
    }

    private static String normalizeFormFolder(String form) {
        if (form == null) return "";
        String lowered = form.toLowerCase(Locale.ROOT);
        if ("raw_material".equals(lowered) || "raw_materials".equals(lowered)) return PREFIX_RAW;
        return lowered;
    }

    private static String normalizeMaterialForForm(String material, String form) {
        if (material == null) return "";
        String lowered = material.toLowerCase(Locale.ROOT);
        if (PREFIX_RAW.equals(form)) {
            return RawMaterialMappings.materialForRawVariant(lowered).orElse(lowered);
        }
        if (PREFIX_CRUSHED.equals(form)) {
            if (lowered.startsWith("raw_") && lowered.length() > "raw_".length()) {
                lowered = lowered.substring("raw_".length());
            }
            if (lowered.endsWith("_raw") && lowered.length() > "_raw".length()) {
                lowered = lowered.substring(0, lowered.length() - "_raw".length());
            }
            lowered = RawMaterialMappings.materialForRawVariant(lowered).orElse(lowered);
        }
        if ("ore".equals(form)) {
            lowered = normalizeOreMaterialToken(lowered);
        }
        return MaterialItemOrder.canonicalMaterialToken(lowered);
    }

    private static String normalizeOreMaterialToken(String materialToken) {
        String out = RawVariantCatalog.stripOreMaterialPrefix(materialToken);
        return RawMaterialMappings.materialForRawVariant(out).orElse(out);
    }

    private static boolean isRemovedLegacyFormId(String itemId) {
        return itemId.endsWith("_dirty_dust")
                || itemId.endsWith("_purified_dust")
                || itemId.endsWith("_long_rod");
    }
}
