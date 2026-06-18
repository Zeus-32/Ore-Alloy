package eu.zunix.ore_and_alloy.integration;

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

    public static boolean isMetalEnabled(MetalMaterial material) {
        if (material == null) return false;
        if (Boolean.getBoolean(FORCE_ALL_PROPERTY)) return true;
        if (MaterialActivationRequests.isRequested(material)) return true;
        if (IntegrationMaterialProviders.VANILLA_METALS.contains(material)) return true;
        if (!MOD_LIST.available()) return Boolean.getBoolean(DATAGEN_INCLUDE_ALL_PROPERTY);

        Set<String> providers = IntegrationMaterialProviders.METAL_PROVIDER_MODS.getOrDefault(material, Set.of());
        for (String modId : providers) {
            if (MOD_LIST.isLoaded(modId)) return true;
        }
        return false;
    }

    public static boolean isMaterialEnabled(String materialToken) {
        return MetalMaterial.fromToken(materialToken)
                .map(IntegrationMaterialRegistry::isMetalEnabled)
                .orElse(false);
    }

}
