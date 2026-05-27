package eu.zunix.ore_and_alloy.integration.kubejs;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.config.OAConfig;
import eu.zunix.ore_and_alloy.core.GemMaterial;
import eu.zunix.ore_and_alloy.core.MaterialForm;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.MetalMaterial;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.integration.recipe.UnificationPriorityRules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;


public final class OreAndAlloyKubeJS {
    private OreAndAlloyKubeJS() {}

    public static String modId() {
        return OreAndAlloy.MODID;
    }

    public static List<String> metals() {
        List<String> out = new ArrayList<>(MetalMaterial.values().length);
        for (MetalMaterial metal : MetalMaterial.values()) {
            out.add(metal.materialName());
        }
        return List.copyOf(out);
    }

    public static List<String> gems() {
        List<String> out = new ArrayList<>(GemMaterial.values().length);
        for (GemMaterial gem : GemMaterial.values()) {
            out.add(gem.name().toLowerCase(Locale.ROOT));
        }
        return List.copyOf(out);
    }

    public static List<String> forms(String material) {
        String normalized = normalizeMaterial(material);
        Optional<MetalMaterial> metal = findMetal(normalized);
        if (metal.isPresent()) {
            return metal.get().getForms().stream()
                    .sorted(Comparator.comparingInt(form ->
                            MaterialItemOrder.formRank(normalized + "_" + form.name().toLowerCase(Locale.ROOT))))
                    .map(form -> form.name().toLowerCase(Locale.ROOT))
                    .toList();
        }

        Optional<GemMaterial> gem = findGem(normalized);
        if (gem.isPresent()) {
            return gem.get().getForms().stream()
                    .sorted(Comparator.comparingInt(form ->
                            MaterialItemOrder.formRank(normalized + "_" + form.name().toLowerCase(Locale.ROOT))))
                    .map(form -> form.name().toLowerCase(Locale.ROOT))
                    .toList();
        }

        return List.of();
    }

    public static boolean hasForm(String material, String form) {
        String normalizedMaterial = normalizeMaterial(material);
        Optional<MaterialForm> normalizedForm = findForm(form);
        if (normalizedForm.isEmpty()) return false;

        return findMetal(normalizedMaterial)
                .map(metal -> metal.getForms().contains(normalizedForm.get()))
                .orElseGet(() -> findGem(normalizedMaterial)
                        .map(gem -> gem.getForms().contains(normalizedForm.get()))
                        .orElse(false));
    }

    public static String itemId(String material, String form) {
        String normalizedMaterial = normalizeMaterial(material);
        Optional<MaterialForm> normalizedForm = findForm(form);
        if (normalizedMaterial.isBlank() || normalizedForm.isEmpty()) return "";
        if (findMetal(normalizedMaterial).isEmpty() && findGem(normalizedMaterial).isEmpty()) return "";
        if (!hasForm(normalizedMaterial, normalizedForm.get().name().toLowerCase(Locale.ROOT))) return "";

        String formToken = normalizedForm.get().name().toLowerCase(Locale.ROOT);
        String canonical = MaterialItemOrder.canonicalMaterialToken(normalizedMaterial);
        String preferredMaterial = MaterialItemOrder.preferredItemMaterialToken(canonical);

        String path;
        if (MaterialItemOrder.bareItemForm(canonical).map(formToken::equals).orElse(false)) {
            path = canonical;
        } else if ("raw".equals(formToken)) {
            path = "raw_" + RawMaterialMappings.primaryRawVariantForMaterial(canonical).orElse(preferredMaterial);
        } else if ("crushed".equals(formToken)) {
            path = "crushed_" + RawMaterialMappings.primaryCrushedVariantForMaterial(canonical).orElse(preferredMaterial);
        } else {
            path = preferredMaterial + "_" + formToken;
        }

        return OreAndAlloy.MODID + ":" + path;
    }

    public static String moltenFluid(String material) {
        return findMetal(normalizeMaterial(material))
                .map(metal -> OreAndAlloy.MODID + ":" + metal.moltenFluidPath())
                .orElse("");
    }

    public static List<String> moltenFluids() {
        List<String> out = new ArrayList<>(MetalMaterial.values().length);
        for (MetalMaterial metal : MetalMaterial.values()) {
            out.add(OreAndAlloy.MODID + ":" + metal.moltenFluidPath());
        }
        return List.copyOf(out);
    }

    public static boolean customVeinWorldgenEnabled() {
        return OAConfig.customVeinWorldgenEnabled();
    }

    public static void setCustomVeinWorldgenEnabled(boolean enabled) {
        OAConfig.setCustomVeinWorldgenEnabledFromKubeJS(enabled);
    }

    public static boolean periodicTooltipsEnabled() {
        return OAConfig.periodicTooltipsEnabled();
    }

    public static void setPeriodicTooltipsEnabled(boolean enabled) {
        OAConfig.setPeriodicTooltipsEnabledFromKubeJS(enabled);
    }

    public static boolean unificationAuditEnabled() {
        return OAConfig.unificationAuditEnabled();
    }

    public static void setUnificationAuditEnabled(boolean enabled) {
        OAConfig.setUnificationAuditEnabledFromKubeJS(enabled);
    }

    public static boolean unificationStrictModeEnabled() {
        return OAConfig.unificationStrictModeEnabled();
    }

    public static void setUnificationStrictModeEnabled(boolean enabled) {
        OAConfig.setUnificationStrictModeEnabledFromKubeJS(enabled);
    }

    public static boolean unificationStrictModeFailFastEnabled() {
        return OAConfig.unificationStrictModeFailFastEnabled();
    }

    public static void setUnificationStrictModeFailFastEnabled(boolean enabled) {
        OAConfig.setUnificationStrictModeFailFastEnabledFromKubeJS(enabled);
    }

    public static boolean unificationSnapshotExportEnabled() {
        return OAConfig.unificationSnapshotExportEnabled();
    }

    public static void setUnificationSnapshotExportEnabled(boolean enabled) {
        OAConfig.setUnificationSnapshotExportEnabledFromKubeJS(enabled);
    }

    public static void resetUnificationPriorityOverrides() {
        UnificationPriorityRules.resetRuntimeOverrides();
    }

    public static void setUnificationGlobalNamespacePriority(String... namespaces) {
        if (namespaces == null) {
            UnificationPriorityRules.setGlobalNamespacePriority(List.of());
            return;
        }
        UnificationPriorityRules.setGlobalNamespacePriority(List.of(namespaces));
    }

    public static void setUnificationModPriority(String modId, int priority) {
        UnificationPriorityRules.setModPriority(modId, priority);
    }

    public static void clearUnificationModPriority(String modId) {
        UnificationPriorityRules.clearModPriority(modId);
    }

    public static void setUnificationMaterialPreferredNamespace(String material, String modId) {
        UnificationPriorityRules.setMaterialPreferredNamespace(material, modId);
    }

    public static void clearUnificationMaterialPreferredNamespace(String material) {
        UnificationPriorityRules.clearMaterialPreferredNamespace(material);
    }

    public static void setUnificationCanonicalItem(String form, String material, String itemId) {
        UnificationPriorityRules.setCanonicalItemOverride(form, material, itemId);
    }

    public static void clearUnificationCanonicalItem(String form, String material) {
        UnificationPriorityRules.clearCanonicalItemOverride(form, material);
    }

    public static Map<String, Object> unificationPrioritySnapshot() {
        return UnificationPriorityRules.snapshot();
    }

    private static Optional<MetalMaterial> findMetal(String material) {
        String canonical = MaterialItemOrder.canonicalMaterialToken(material);
        for (MetalMaterial metal : MetalMaterial.values()) {
            if (metal.materialName().equals(canonical)) return Optional.of(metal);
        }
        return Optional.empty();
    }

    private static Optional<GemMaterial> findGem(String material) {
        String canonical = MaterialItemOrder.canonicalMaterialToken(material);
        for (GemMaterial gem : GemMaterial.values()) {
            if (gem.name().equalsIgnoreCase(canonical)) return Optional.of(gem);
        }
        return Optional.empty();
    }

    private static Optional<MaterialForm> findForm(String form) {
        String normalized = normalize(form);
        if (normalized.isBlank()) return Optional.empty();

        for (MaterialForm value : MaterialForm.values()) {
            if (value.name().equalsIgnoreCase(normalized)) return Optional.of(value);
        }
        return Optional.empty();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_').trim();
    }

    private static String normalizeMaterial(String value) {
        return MaterialItemOrder.canonicalMaterialToken(normalize(value));
    }
}
