package eu.zunix.ore_and_alloy.core;

import java.util.Set;
import java.util.Locale;

public final class GemRawCrushedMaterials {
    private static final Set<String> SUPPORTED = Set.of("coal", "diamond", "emerald");

    private GemRawCrushedMaterials() {}

    public static boolean supportsRawCrushed(String materialToken) {
        String canonical = MaterialItemOrder.canonicalMaterialToken(materialToken);
        return SUPPORTED.contains(canonical);
    }

    public static boolean supportsRawCrushed(GemMaterial material) {
        return supportsRawCrushed(material.name().toLowerCase(Locale.ROOT));
    }
}
