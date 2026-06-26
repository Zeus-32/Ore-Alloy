package eu.zunix.ore_and_alloy.integration;

import eu.zunix.ore_and_alloy.core.MetalMaterial;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class IntegrationMaterialProviders {
    static final Set<MetalMaterial> VANILLA_METALS = EnumSet.of(
            MetalMaterial.IRON,
            MetalMaterial.GOLD,
            MetalMaterial.COPPER,
            MetalMaterial.DIAMOND
    );

    private static final Set<String> COMMON_PROVIDER_MODS = set(
            "mekanism",
            "thermal",
            "alltheores",
            "techreborn",
            "create",
            "immersiveengineering",
            "ad_astra",
            "pneumaticcraft",
            "createaddition",
            "tconstruct",
            "powah",
            "bigreactors",
            "biggerreactors",
            "projectred_core",
            "ae2",
            "enderio",
            "modern_industrialization",
            "ftbmaterials"
    );

    private static final Set<String> PURE_NETHERITE_PROVIDER_MODS = set(
            "tgecore",
            "tge_core"
    );

    static final Map<MetalMaterial, Set<String>> METAL_PROVIDER_MODS = buildMetalProviders();

    private IntegrationMaterialProviders() {}

    private static Map<MetalMaterial, Set<String>> buildMetalProviders() {
        Map<MetalMaterial, Set<String>> map = new EnumMap<>(MetalMaterial.class);

        for (MetalMaterial material : MetalMaterial.values()) {
            if (material == MetalMaterial.PURE_NETHERITE) {
                map.put(material, PURE_NETHERITE_PROVIDER_MODS);
                continue;
            }
            if (!VANILLA_METALS.contains(material)) {
                map.put(material, COMMON_PROVIDER_MODS);
            }
        }

        return Collections.unmodifiableMap(map);
    }

    private static Set<String> set(String... values) {
        Set<String> out = new LinkedHashSet<>(values.length);
        for (String value : values) {
            if (value == null) continue;
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty()) out.add(normalized);
        }
        return Set.copyOf(out);
    }
}
