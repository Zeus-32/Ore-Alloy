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
            MetalMaterial.COPPER
    );

    static final Map<MetalMaterial, Set<String>> METAL_PROVIDER_MODS = buildMetalProviders();

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

        map.put(MetalMaterial.ANTIMONY, set("gtceu", "modern_industrialization"));
        map.put(MetalMaterial.STEEL, set("immersiveengineering", "mekanism", "ad_astra", "pneumaticcraft", "createaddition"));
        map.put(MetalMaterial.STAINLESS_STEEL, set("gtceu", "modern_industrialization", "techreborn"));
        map.put(MetalMaterial.BRASS, set("create", "thermal", "alltheores"));
        map.put(MetalMaterial.BRONZE, set("mekanism", "thermal", "alltheores", "techreborn", "gtceu"));
        map.put(MetalMaterial.CUPRONICKEL, set("immersiveengineering", "gtceu", "modern_industrialization"));
        map.put(MetalMaterial.ELECTRUM, set("thermal", "immersiveengineering", "alltheores", "techreborn", "gtceu"));
        map.put(MetalMaterial.INVAR, set("thermal", "alltheores", "techreborn", "gtceu"));
        map.put(MetalMaterial.CONSTANTAN, set("immersiveengineering", "alltheores", "techreborn"));
        map.put(MetalMaterial.WROUGHT_IRON, set("gtceu", "modern_industrialization"));
        map.put(MetalMaterial.ENDERIUM, set("thermal"));
        map.put(MetalMaterial.LITHIUM, set("mekanism", "techreborn", "gtceu", "modern_industrialization"));
        map.put(MetalMaterial.LUMIUM, set("thermal"));
        map.put(MetalMaterial.NAQUADAH, set("gtceu"));
        map.put(MetalMaterial.RED_ALLOY, set("projectred_core", "gtceu"));
        map.put(MetalMaterial.SOUL_INFUSED, set("thermal"));
        map.put(MetalMaterial.TUNGSTEN, set("techreborn", "gtceu", "modern_industrialization"));
        map.put(MetalMaterial.SILICON, set("mekanism", "ae2", "enderio", "gtceu"));

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
