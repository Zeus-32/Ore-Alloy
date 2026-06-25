package eu.zunix.ore_and_alloy.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaterialActivationRequestsTest {
    @AfterEach
    void reset() {
        MaterialActivationRequests.resetForTests();
    }

    @Test
    void aliasesAndDuplicatesAreHandled() {
        assertTrue(MaterialActivationRequests.request("aluminum"));
        assertFalse(MaterialActivationRequests.request("aluminium"));
        assertTrue(MaterialActivationRequests.requestedMaterials().contains("aluminum"));
    }

    @Test
    void unknownMaterialsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> MaterialActivationRequests.request("unobtainium"));
    }

    @Test
    void requestsAreRejectedAfterRegistryDiscoveryStarts() {
        MaterialActivationRequests.freeze();
        assertThrows(IllegalStateException.class, () -> MaterialActivationRequests.request("antimony"));
    }
}
