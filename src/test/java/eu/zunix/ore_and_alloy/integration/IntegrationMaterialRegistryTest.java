package eu.zunix.ore_and_alloy.integration;

import eu.zunix.ore_and_alloy.core.MetalMaterial;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationMaterialRegistryTest {
    @AfterEach
    void clearProperties() {
        System.clearProperty("ore_and_alloy.register_all");
        System.clearProperty("ore_and_alloy.datagen.include_all");
        MaterialActivationRequests.resetForTests();
    }

    @Test
    void vanillaEnvironmentDoesNotActivateExternalMaterials() {
        assertTrue(IntegrationMaterialRegistry.isMetalEnabled(MetalMaterial.IRON));
        assertTrue(IntegrationMaterialRegistry.isMetalEnabled(MetalMaterial.GOLD));
        assertTrue(IntegrationMaterialRegistry.isMetalEnabled(MetalMaterial.COPPER));
        assertTrue(IntegrationMaterialRegistry.isMetalEnabled(MetalMaterial.PURE_NETHERITE));
        assertFalse(IntegrationMaterialRegistry.isMetalEnabled(MetalMaterial.ALUMINUM));
        assertFalse(IntegrationMaterialRegistry.isMetalEnabled(MetalMaterial.OSMIUM));
    }

    @Test
    void datagenCanStillGenerateAssetsForTheWholeCatalog() {
        System.setProperty("ore_and_alloy.datagen.include_all", "true");
        assertTrue(IntegrationMaterialRegistry.isMetalEnabled(MetalMaterial.ALUMINUM));
    }

    @Test
    void startupRequestActivatesKnownMaterialWithoutProviderMod() {
        assertTrue(MaterialActivationRequests.request("aluminum"));
        assertTrue(IntegrationMaterialRegistry.isMetalEnabled(MetalMaterial.ALUMINUM));
        assertTrue(MaterialActivationRequests.requestedMaterials().contains("aluminum"));
    }

    @Test
    void providerListsAreSharedAndDoNotIncludeGregTech() {
        Set<String> expected = null;
        for (MetalMaterial material : MetalMaterial.values()) {
            Set<String> providers = IntegrationMaterialProviders.METAL_PROVIDER_MODS.get(material);
            if (IntegrationMaterialProviders.VANILLA_METALS.contains(material)) {
                assertTrue(providers == null || providers.isEmpty());
                continue;
            }

            if (expected == null) {
                expected = providers;
            } else {
                assertEquals(expected, providers, "Provider list mismatch for " + material.materialName());
            }
            assertFalse(providers.contains("gtceu"));
            assertFalse(providers.contains("gregtech"));
        }
    }
}
