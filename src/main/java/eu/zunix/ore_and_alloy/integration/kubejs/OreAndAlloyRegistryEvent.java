package eu.zunix.ore_and_alloy.integration.kubejs;

import eu.zunix.ore_and_alloy.integration.MaterialActivationRequests;

public final class OreAndAlloyRegistryEvent {
    public boolean reg(Object material) {
        return MaterialActivationRequests.request(material);
    }
    public boolean register(Object material) {
        return MaterialActivationRequests.request(material);
    }
}
