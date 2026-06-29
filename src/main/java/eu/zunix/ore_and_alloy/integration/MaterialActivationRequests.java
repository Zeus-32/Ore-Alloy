package eu.zunix.ore_and_alloy.integration;

import eu.zunix.ore_and_alloy.core.MetalMaterial;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class MaterialActivationRequests {
    private static final Set<MetalMaterial> REQUESTED = EnumSet.noneOf(MetalMaterial.class);
    private static boolean frozen;

    private MaterialActivationRequests() {}

    public static synchronized boolean request(Object materialToken) {
        if (frozen) {
            throw new IllegalStateException("Ore & Alloy materials can only be requested during mod startup");
        }

        String token = materialToken == null ? "" : materialToken.toString();
        MetalMaterial material = MetalMaterial.fromToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Ore & Alloy material: " + token));
        return REQUESTED.add(material);
    }

    public static synchronized boolean isRequested(MetalMaterial material) {
        return REQUESTED.contains(material);
    }

    public static synchronized List<String> requestedMaterials() {
        return REQUESTED.stream()
                .map(MetalMaterial::materialName)
                .sorted()
                .toList();
    }

    public static synchronized void freeze() {
        frozen = true;
    }

    static synchronized void resetForTests() {
        REQUESTED.clear();
        frozen = false;
    }
}
