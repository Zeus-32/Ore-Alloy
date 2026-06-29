package eu.zunix.ore_and_alloy.api;

import eu.zunix.ore_and_alloy.integration.MaterialActivationRequests;

import java.util.List;

public final class OreAndAlloyApi {
    private OreAndAlloyApi() {}

    public static boolean registerMaterial(Object material) {
        return MaterialActivationRequests.request(material);
    }

    public static boolean reg(Object material) {
        return registerMaterial(material);
    }

    public static List<String> requestedMaterials() {
        return MaterialActivationRequests.requestedMaterials();
    }
}
