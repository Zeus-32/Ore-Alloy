package eu.zunix.ore_and_alloy.integration;

import eu.zunix.ore_and_alloy.core.GemMaterial;
import eu.zunix.ore_and_alloy.core.MaterialForm;
import eu.zunix.ore_and_alloy.core.MetalMaterial;

import java.util.Set;

public final class IntegrationMaterialRegistry {
    private static final String FORCE_ALL_PROPERTY = "ore_and_alloy.register_all";
    private static final String DATAGEN_INCLUDE_ALL_PROPERTY = "ore_and_alloy.datagen.include_all";
    private static final ModListAccessor MOD_LIST = ModListAccessor.resolve();

    private IntegrationMaterialRegistry() {}

    public static boolean shouldRegister(MetalMaterial material, MaterialForm form) {
        return isMetalEnabled(material);
    }

    public static boolean shouldRegister(GemMaterial material, MaterialForm form) {
        return isGemEnabled(material);
    }

    public static boolean isMetalEnabled(MetalMaterial material) {
        if (isForceAllEnabled()) return true;
        if (IntegrationMaterialProviders.VANILLA_METALS.contains(material)) return true;
        if (!MOD_LIST.available()) return isDatagenIncludeAllEnabled();

        Set<String> providers = IntegrationMaterialProviders.METAL_PROVIDER_MODS.getOrDefault(material, Set.of());
        for (String modId : providers) {
            if (MOD_LIST.isLoaded(modId)) return true;
        }
        return false;
    }

    public static boolean isGemEnabled(GemMaterial material) {
        if (isForceAllEnabled()) return true;
        if (IntegrationMaterialProviders.VANILLA_GEMS.contains(material)) return true;
        if (!MOD_LIST.available()) return isDatagenIncludeAllEnabled();

        Set<String> providers = IntegrationMaterialProviders.GEM_PROVIDER_MODS.getOrDefault(material, Set.of());
        for (String modId : providers) {
            if (MOD_LIST.isLoaded(modId)) return true;
        }
        return false;
    }

    private static boolean isForceAllEnabled() {
        return Boolean.parseBoolean(System.getProperty(FORCE_ALL_PROPERTY, "false"));
    }

    private static boolean isDatagenIncludeAllEnabled() {
        return Boolean.parseBoolean(System.getProperty(DATAGEN_INCLUDE_ALL_PROPERTY, "false"));
    }
}
