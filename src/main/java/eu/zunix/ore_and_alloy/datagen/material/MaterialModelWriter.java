package eu.zunix.ore_and_alloy.datagen.material;

import eu.zunix.ore_and_alloy.core.MaterialFormCatalog;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class MaterialModelWriter {
    private final Path outRoot;
    private final String namespace;

    public MaterialModelWriter(Path outRoot, String namespace) {
        this.outRoot = outRoot;
        this.namespace = namespace;
    }

    public void writeMaterialItemModels(List<String> materialItemIds) throws IOException {
        for (String itemName : materialItemIds) {
            writeMaterialItemModel(itemName);
        }
    }

    public void writeHandheldItemModel(String itemId, String texture) throws IOException {
        Path model = outRoot.resolve(Path.of("assets", namespace, "models", "item", itemId + ".json"));
        DatagenFiles.writeText(model, "{"
                + "\n  \"parent\": \"item/handheld\","
                + "\n  \"textures\": {\n    \"layer0\": \"" + texture + "\"\n  }\n}");
    }

    public void writeGeneratedItemModel(String itemId, String texture) throws IOException {
        Path model = outRoot.resolve(Path.of("assets", namespace, "models", "item", itemId + ".json"));
        DatagenFiles.writeText(model, "{"
                + "\n  \"parent\": \"item/generated\","
                + "\n  \"textures\": {\n    \"layer0\": \"" + texture + "\"\n  }\n}");
    }

    private void writeMaterialItemModel(String itemName) throws IOException {
        MaterialId parsed = MaterialIdParser.parseItemId(itemName);
        boolean handheld = MaterialFormCatalog.HANDHELD_FORMS.contains(parsed.form());
        String parent = handheld ? "item/handheld" : "item/generated";
        boolean bareItem = eu.zunix.ore_and_alloy.core.MaterialItemOrder.bareItemForm(itemName).isPresent();
        String textureFolder = textureFolder(parsed.form());
        String texture = bareItem && !"gem".equals(parsed.form())
                ? namespace + ":item/" + itemName
                : namespace + ":item/" + textureFolder + "/" + itemName;

        Path out = outRoot.resolve(Path.of("assets", namespace, "models", "item", itemName + ".json"));
        DatagenFiles.writeText(out, "{"
                + "\n  \"parent\": \"" + parent + "\","
                + "\n  \"textures\": {\n    \"layer0\": \"" + texture + "\"\n  }\n}");
    }

    private static String textureFolder(String form) {
        return switch (form) {
            case "raw" -> "raw_materials";
            case "gem", "diamond" -> "gems";
            default -> form;
        };
    }

}
