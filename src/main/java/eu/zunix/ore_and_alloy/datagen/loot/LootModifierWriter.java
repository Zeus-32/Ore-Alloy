package eu.zunix.ore_and_alloy.datagen.loot;

import eu.zunix.ore_and_alloy.datagen.material.DatagenFiles;

import java.io.IOException;
import java.nio.file.Path;

public final class LootModifierWriter {
    private final Path outRoot;
    private final String namespace;
    private final String redstoneModifierId;

    public LootModifierWriter(Path outRoot, String namespace, String redstoneModifierId) {
        this.outRoot = outRoot;
        this.namespace = namespace;
        this.redstoneModifierId = redstoneModifierId;
    }

    public void writeGlobalLootModifiers() throws IOException {
        Path globalList = outRoot.resolve(Path.of("data", "neoforge", "loot_modifiers", "global_loot_modifiers.json"));
        String globalJson = "{\n"
                + "  \"replace\": false,\n"
                + "  \"entries\": [\n"
                + "    \"" + namespace + ":" + redstoneModifierId + "\"\n"
                + "  ]\n"
                + "}";
        DatagenFiles.writeText(globalList, globalJson);

        Path modifier = outRoot.resolve(Path.of("data", namespace, "loot_modifiers", redstoneModifierId + ".json"));
        String modifierJson = "{\n"
                + "  \"type\": \"" + namespace + ":" + redstoneModifierId + "\",\n"
                + "  \"conditions\": []\n"
                + "}";
        DatagenFiles.writeText(modifier, modifierJson);
    }
}
