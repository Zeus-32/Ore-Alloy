package eu.zunix.ore_and_alloy.integration.kubejs;

import eu.zunix.ore_and_alloy.api.OreAndAlloyApi;

public final class OreAndAlloyRegistryEvent {
    public boolean reg(Object material) {
        return OreAndAlloyApi.registerMaterial(material);
    }
    public boolean register(Object material) {
        return OreAndAlloyApi.registerMaterial(material);
    }
}
