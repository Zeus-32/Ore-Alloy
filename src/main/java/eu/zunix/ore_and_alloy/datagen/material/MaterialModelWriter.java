package eu.zunix.ore_and_alloy.datagen.material;

import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;
import eu.zunix.ore_and_alloy.core.MetalMaterial;
import eu.zunix.ore_and_alloy.registry.ModStandaloneItems;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class MaterialModelWriter {
    private final Path outRoot;
    private final String namespace;
    private final String prospectorId;
    private final String prospectorTexture;

    public MaterialModelWriter(Path outRoot, String namespace, String prospectorId, String prospectorTexture) {
        this.outRoot = outRoot;
        this.namespace = namespace;
        this.prospectorId = prospectorId;
        this.prospectorTexture = prospectorTexture;
    }

    public void writeMaterialItemModels(List<String> materialItemIds) throws IOException {
        for (String itemName : materialItemIds) {
            writeMaterialItemModel(itemName);
        }
    }

    public void writeStandaloneItemModels(List<ModStandaloneItems.StandaloneItemDefinition> standaloneItems) throws IOException {
        for (ModStandaloneItems.StandaloneItemDefinition item : standaloneItems) {
            writeStandaloneItemModel(item);
        }
    }

    public void writeProspectorModel() throws IOException {
        Path model = outRoot.resolve(Path.of("assets", namespace, "models", "item", prospectorId + ".json"));
        DatagenFiles.writeText(model, "{"
                + "\n  \"parent\": \"item/handheld\","
                + "\n  \"textures\": {\n    \"layer0\": \"" + namespace + ":" + prospectorTexture + "\"\n  }\n}");
    }

    public void writeMoltenBucketModels() throws IOException {
        Path models = outRoot.resolve(Path.of("assets", namespace, "models", "item"));
        Files.createDirectories(models);

        for (MetalMaterial metal : MetalMaterial.values()) {
            String moltenPath = metal.moltenFluidPath();
            String bucketName = moltenPath + "_bucket";
            String fluidId = namespace + ":" + moltenPath;
            Path out = models.resolve(bucketName + ".json");

            String json = "{"
                    + "\n  \"loader\": \"neoforge:fluid_container\","
                    + "\n  \"parent\": \"neoforge:item/bucket\","
                    + "\n  \"fluid\": \"" + fluidId + "\""
                    + "\n}";

            Files.write(out, json.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void writeMaterialItemModel(String itemName) throws IOException {
        MaterialId parsed = MaterialIdParser.parseItemId(itemName);
        boolean handheld = MaterialFormCatalog.HANDHELD_FORMS.contains(parsed.form());
        String parent = handheld ? "item/handheld" : "item/generated";
        String textureFolder = "raw".equals(parsed.form()) ? "raw_materials" : parsed.form();
        String texture = namespace + ":item/" + textureFolder + "/" + itemName;

        Path out = outRoot.resolve(Path.of("assets", namespace, "models", "item", itemName + ".json"));
        DatagenFiles.writeText(out, "{"
                + "\n  \"parent\": \"" + parent + "\","
                + "\n  \"textures\": {\n    \"layer0\": \"" + texture + "\"\n  }\n}");
    }

    private void writeStandaloneItemModel(ModStandaloneItems.StandaloneItemDefinition item) throws IOException {
        Path out = outRoot.resolve(Path.of("assets", namespace, "models", "item", item.id() + ".json"));
        DatagenFiles.writeText(out, "{"
                + "\n  \"parent\": \"item/generated\","
                + "\n  \"textures\": {\n    \"layer0\": \"" + namespace + ":" + item.texturePath() + "\"\n  }\n}");
    }
}
