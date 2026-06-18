package eu.zunix.ore_and_alloy.integration;

import eu.zunix.ore_and_alloy.core.MetalMaterial;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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
        assertTrue(MaterialActivationRequests.request("aluminium"));
        assertTrue(IntegrationMaterialRegistry.isMetalEnabled(MetalMaterial.ALUMINUM));
        assertTrue(MaterialActivationRequests.requestedMaterials().contains("aluminum"));
    }
}
