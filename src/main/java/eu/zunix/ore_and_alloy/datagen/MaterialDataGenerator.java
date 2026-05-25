package eu.zunix.ore_and_alloy.datagen;

import eu.zunix.ore_and_alloy.datagen.lang.OALangGenerator;
import eu.zunix.ore_and_alloy.datagen.loot.LootModifierWriter;
import eu.zunix.ore_and_alloy.datagen.material.DatagenFiles;
import eu.zunix.ore_and_alloy.datagen.material.MaterialItemCollector;
import eu.zunix.ore_and_alloy.datagen.material.MaterialModelWriter;
import eu.zunix.ore_and_alloy.datagen.material.MaterialOreBlockWriter;
import eu.zunix.ore_and_alloy.datagen.material.MaterialRecipeWriter;
import eu.zunix.ore_and_alloy.datagen.material.MaterialTagWriter;
import eu.zunix.ore_and_alloy.datagen.material.MaterialTrimWriter;
import eu.zunix.ore_and_alloy.registry.ModStandaloneItems;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class MaterialDataGenerator {
    private static final Path OUT = Path.of("src/generated/resources");
    private static final Path MAIN_RES = Path.of("src/main/resources");
    private static final String NAMESPACE = "ore_and_alloy";
    private static final String PROSPECTOR_ID = "prospector";
    private static final String PROSPECTOR_TEXTURE = "item/prospector";
    private static final String REDSTONE_LOOT_MODIFIER_ID = "replace_redstone_drops";

    private MaterialDataGenerator() {}

    public static void main(String[] args) throws IOException {
        System.out.println("Generating material data to " + OUT.toAbsolutePath());
        try {
            Files.createDirectories(OUT);
            DatagenFiles.cleanupOutputs(OUT, NAMESPACE);

            MaterialItemCollector collector = new MaterialItemCollector(NAMESPACE, MAIN_RES);
            List<String> materialItems = collector.collectMaterialItems();
            collector.writeMaterialManifest(MAIN_RES, materialItems);

            List<ModStandaloneItems.StandaloneItemDefinition> standaloneItems = ModStandaloneItems.definitions();

            MaterialModelWriter modelWriter = new MaterialModelWriter(OUT, NAMESPACE, PROSPECTOR_ID, PROSPECTOR_TEXTURE);
            modelWriter.writeMaterialItemModels(materialItems);
            modelWriter.writeStandaloneItemModels(standaloneItems);
            modelWriter.writeProspectorModel();
            modelWriter.writeMoltenBucketModels();

            MaterialOreBlockWriter oreBlockWriter = new MaterialOreBlockWriter(OUT, MAIN_RES, NAMESPACE);
            List<String> rawVariants = oreBlockWriter.collectRawVariants(materialItems);
            oreBlockWriter.writeBlockstatesAndModels(rawVariants);
            oreBlockWriter.writeLootTables(rawVariants);
            oreBlockWriter.writeBlockTags(rawVariants);

            OALangGenerator.write(
                    OUT.resolve(Path.of("assets", NAMESPACE, "lang", "en_us.json")),
                    NAMESPACE,
                    PROSPECTOR_ID,
                    materialItems,
                    rawVariants,
                    standaloneItems
            );

            MaterialTagWriter tagWriter = new MaterialTagWriter(OUT, NAMESPACE);
            tagWriter.writeItemTags(materialItems);
            tagWriter.writeMoltenFluidTags();
            MaterialTrimWriter trimWriter = new MaterialTrimWriter(OUT, NAMESPACE);
            trimWriter.writeTrimSupport(materialItems);

            MaterialRecipeWriter recipeWriter = new MaterialRecipeWriter(OUT, NAMESPACE);
            recipeWriter.writeCompactingRecipes(materialItems);

            LootModifierWriter lootModifierWriter = new LootModifierWriter(OUT, NAMESPACE, REDSTONE_LOOT_MODIFIER_ID);
            lootModifierWriter.writeGlobalLootModifiers();

            System.out.println("Material data generation complete.");
        } catch (Exception ex) {
            System.err.println("Data generation failed:");
            ex.printStackTrace(System.err);
            throw ex instanceof IOException ? (IOException) ex : new IOException(ex);
        }
    }
}
