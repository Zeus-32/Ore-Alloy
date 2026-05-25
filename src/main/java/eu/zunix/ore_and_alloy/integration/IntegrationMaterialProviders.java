package eu.zunix.ore_and_alloy.integration;

import eu.zunix.ore_and_alloy.core.GemMaterial;
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
            MetalMaterial.COPPER
    );

    static final Set<GemMaterial> VANILLA_GEMS = EnumSet.of(
            GemMaterial.COAL,
            GemMaterial.DIAMOND,
            GemMaterial.EMERALD,
            GemMaterial.LAPIS,
            GemMaterial.QUARTZ,
            GemMaterial.AMETHYST,
            GemMaterial.REDSTONE
    );

    static final Map<MetalMaterial, Set<String>> METAL_PROVIDER_MODS = buildMetalProviders();
    static final Map<GemMaterial, Set<String>> GEM_PROVIDER_MODS = buildGemProviders();

    private IntegrationMaterialProviders() {}

    private static Map<MetalMaterial, Set<String>> buildMetalProviders() {
        Map<MetalMaterial, Set<String>> map = new EnumMap<>(MetalMaterial.class);

        map.put(MetalMaterial.TIN, set("mekanism", "thermal", "alltheores", "techreborn", "gtceu"));
        map.put(MetalMaterial.LEAD, set("mekanism", "thermal", "alltheores", "techreborn", "gtceu"));
        map.put(MetalMaterial.SILVER, set("thermal", "alltheores", "techreborn", "gtceu"));
        map.put(MetalMaterial.NICKEL, set("thermal", "alltheores", "techreborn", "gtceu"));
        map.put(MetalMaterial.ZINC, set("create", "alltheores", "thermal", "techreborn", "gtceu"));
        map.put(MetalMaterial.ALUMINUM, set("immersiveengineering", "alltheores", "techreborn", "gtceu"));
        map.put(MetalMaterial.OSMIUM, set("mekanism"));
        map.put(MetalMaterial.URANIUM, set("mekanism", "immersiveengineering", "powah", "bigreactors", "biggerreactors"));
        map.put(MetalMaterial.COBALT, set("tconstruct", "alltheores", "gtceu"));
        map.put(MetalMaterial.TITANIUM, set("ad_astra", "techreborn", "gtceu", "modern_industrialization"));
        map.put(MetalMaterial.CHROME, set("techreborn", "gtceu", "modern_industrialization"));
        map.put(MetalMaterial.PLATINUM, set("thermal", "alltheores", "techreborn", "gtceu"));
        map.put(MetalMaterial.IRIDIUM, set("techreborn", "gtceu", "modern_industrialization"));

        map.put(MetalMaterial.STEEL, set("immersiveengineering", "mekanism", "ad_astra", "pneumaticcraft", "createaddition"));
        map.put(MetalMaterial.BRASS, set("create", "thermal", "alltheores"));
        map.put(MetalMaterial.BRONZE, set("mekanism", "thermal", "alltheores", "techreborn", "gtceu"));
        map.put(MetalMaterial.ELECTRUM, set("thermal", "immersiveengineering", "alltheores", "techreborn", "gtceu"));
        map.put(MetalMaterial.INVAR, set("thermal", "alltheores", "techreborn", "gtceu"));
        map.put(MetalMaterial.CONSTANTAN, set("immersiveengineering", "alltheores", "techreborn"));

        return Collections.unmodifiableMap(map);
    }

    private static Map<GemMaterial, Set<String>> buildGemProviders() {
        Map<GemMaterial, Set<String>> map = new EnumMap<>(GemMaterial.class);
        map.put(GemMaterial.SAPPHIRE, set("projectred_core", "thermal", "alltheores"));
        map.put(GemMaterial.RUBY, set("projectred_core", "thermal", "alltheores"));
        return Collections.unmodifiableMap(map);
    }

    private static Set<String> set(String... values) {
        if (values.length == 0) return Set.of();
        Set<String> out = new LinkedHashSet<>(values.length);
        for (String value : values) {
            if (value == null) continue;
            String trimmed = value.trim().toLowerCase(Locale.ROOT);
            if (!trimmed.isEmpty()) out.add(trimmed);
        }
        return Set.copyOf(out);
    }
}
