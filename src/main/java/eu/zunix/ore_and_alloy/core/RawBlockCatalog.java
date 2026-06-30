package eu.zunix.ore_and_alloy.core;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RawBlockCatalog {
    private RawBlockCatalog() {}

    public static Map<String, String> collectRawBlockBaseItems(Collection<String> materialItemIds) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String id : materialItemIds) {
            if (!id.startsWith("raw_") || id.length() <= "raw_".length()) continue;
            String rawVariant = id.substring("raw_".length());
            if (!RawMaterialMappings.isConfiguredRawVariant(rawVariant) && MetalMaterial.fromToken(rawVariant).isEmpty()) continue;
            out.put(rawVariant, id);
        }
        return Map.copyOf(out);
    }

    public static String blockIdForRawVariant(String rawVariant) {
        return "raw_" + rawVariant + "_block";
    }
}
