package eu.zunix.ore_and_alloy.datagen.material;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class DatagenFiles {
    private DatagenFiles() {}

    public static void writeText(Path path, String text) throws IOException {
        Files.createDirectories(path.getParent());
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
    }

    public static void cleanupOutputs(Path outRoot, String namespace) throws IOException {
        deleteTreeIfExists(outRoot.resolve(Path.of("assets", namespace, "models")));
        deleteTreeIfExists(outRoot.resolve(Path.of("assets", namespace, "blockstates")));
        deleteTreeIfExists(outRoot.resolve(Path.of("assets", namespace, "textures")));
        deleteTreeIfExists(outRoot.resolve(Path.of("assets", namespace, "lang")));
        deleteTreeIfExists(outRoot.resolve(Path.of("assets", namespace, "material_items.txt")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", "c", "tags", "item")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", "c", "tags", "fluid")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", "c", "tags", "block")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", "c", "tags", "items")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", "c", "tags", "fluids")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", "minecraft", "tags", "block")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", "minecraft", "tags", "item")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", namespace, "recipe")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", namespace, "recipes")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", namespace, "loot_table", "blocks")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", namespace, "loot_modifiers")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", namespace, "trim_material")));
        deleteTreeIfExists(outRoot.resolve(Path.of("data", "neoforge", "loot_modifiers")));
    }

    public static void deleteTreeIfExists(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var stream = Files.walk(root)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
        }
    }
}
