package eu.zunix.ore_and_alloy.datagen.lang;

import java.util.Map;

public final class IntegrationLangEntries {
    private IntegrationLangEntries() {}

    public static void append(Map<String, String> entries, String namespace) {
        entries.put(namespace + ".message.prospector_ping", "Prospector ping");
        entries.put(namespace + ".jei.info.prospector", "Scans nearby ore density and helps with vein prospecting.");
        entries.put(namespace + ".jei.info.molten.line1", "Molten metals from Ore & Alloy.");
        entries.put(namespace + ".jei.info.molten.line2", "Use these fluids in machines and fluid-based recipe chains.");
        entries.put(namespace + ".jei.aliases.title", "Aliases (JEI)");
        entries.put(namespace + ".jei.vein.title", "Vein Information");
        entries.put(namespace + ".jei.vein.height", "Height: Y %s to %s");
        entries.put(namespace + ".jei.vein.size", "Size: %s");
        entries.put(namespace + ".jei.vein.chance", "Chance: 1/%s per chunk");
        entries.put(namespace + ".jei.vein.dimension", "Dimension: %s");
        entries.put(namespace + ".jei.vein.raw_variants", "Raw Variants: %s");
    }
}
