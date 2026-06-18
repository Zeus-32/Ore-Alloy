package eu.zunix.ore_and_alloy.datagen.lang;

import java.util.Map;

public final class IntegrationLangEntries {
    private IntegrationLangEntries() {}

    public static void append(Map<String, String> entries, String namespace) {
        entries.put(namespace + ".jei.aliases.title", "Aliases (JEI)");
    }
}
