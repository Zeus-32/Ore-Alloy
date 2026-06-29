package eu.zunix.ore_and_alloy.integration;

import eu.zunix.ore_and_alloy.api.OreAndAlloyApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OreAndAlloyApiTest {
    @AfterEach
    void reset() {
        MaterialActivationRequests.resetForTests();
    }

    @Test
    void javaModsCanRequestMaterialsThroughPublicApi() {
        assertTrue(OreAndAlloyApi.registerMaterial("pure_netherite"));
        assertFalse(OreAndAlloyApi.reg("pure_netherite"));
        assertTrue(OreAndAlloyApi.requestedMaterials().contains("pure_netherite"));
    }

    @Test
    void publicApiRequestsMustRunBeforeMaterialRegistrationFreezes() {
        MaterialActivationRequests.freeze();
        assertThrows(IllegalStateException.class, () -> OreAndAlloyApi.registerMaterial("aluminum"));
    }
}
