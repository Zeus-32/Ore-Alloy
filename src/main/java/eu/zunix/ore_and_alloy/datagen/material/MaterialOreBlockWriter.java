package eu.zunix.ore_and_alloy.datagen.material;

import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.OreHostVariantCatalog;
import eu.zunix.ore_and_alloy.core.RawMaterialMappings;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class MaterialOreBlockWriter {
    private final Path outRoot;
    private final Path mainResources;
    private final String namespace;
    private final Map<String, BufferedImage> imageCache = new HashMap<>();

    public MaterialOreBlockWriter(Path outRoot, Path mainResources, String namespace) {
        this.outRoot = outRoot;
        this.mainResources = mainResources;
        this.namespace = namespace;
    }

    public List<String> collectRawVariants(List<String> materialItemIds) {
        return RawVariantCatalog.collectRawVariants(materialItemIds);
    }

    public void writeBlockstatesAndModels(List<String> rawVariants) throws IOException {
        Path blockstatesRoot = outRoot.resolve(Path.of("assets", namespace, "blockstates"));
        Path blockModelsRoot = outRoot.resolve(Path.of("assets", namespace, "models", "block"));
        Path itemModelsRoot = outRoot.resolve(Path.of("assets", namespace, "models", "item"));

        Files.createDirectories(blockstatesRoot);
        Files.createDirectories(blockModelsRoot);
        Files.createDirectories(itemModelsRoot);

        for (String rawVariant : rawVariants) {
            for (String blockId : oreBlockIdsForRawVariant(rawVariant)) {
                String blockstateJson = "{\n"
                        + "  \"variants\": {\n"
                        + "    \"\": { \"model\": \"" + namespace + ":block/" + blockId + "\" }\n"
                        + "  }\n"
                        + "}";
                DatagenFiles.writeText(blockstatesRoot.resolve(blockId + ".json"), blockstateJson);

                String blockModelJson = "{\n"
                        + "  \"parent\": \"minecraft:block/cube_all\",\n"
                        + "  \"textures\": {\n"
                        + "    \"all\": \"" + namespace + ":block/ore/" + blockId + "\"\n"
                        + "  }\n"
                        + "}";
                DatagenFiles.writeText(blockModelsRoot.resolve(blockId + ".json"), blockModelJson);

                String itemModelJson = "{\n"
                        + "  \"parent\": \"" + namespace + ":block/" + blockId + "\"\n"
                        + "}";
                DatagenFiles.writeText(itemModelsRoot.resolve(blockId + ".json"), itemModelJson);
            }
        }

        writeBlockTextures(rawVariants);
    }

    public void writeLootTables(List<String> rawVariants) throws IOException {
        Path lootRoot = outRoot.resolve(Path.of("data", namespace, "loot_table", "blocks"));
        Files.createDirectories(lootRoot);

        for (String rawVariant : rawVariants) {
            String rawItemId = "raw_" + rawVariant;
            for (String blockId : oreBlockIdsForRawVariant(rawVariant)) {
                String json = "{\n"
                        + "  \"type\": \"minecraft:block\",\n"
                        + "  \"pools\": [\n"
                        + "    {\n"
                        + "      \"rolls\": 1,\n"
                        + "      \"entries\": [\n"
                        + "        {\n"
                        + "          \"type\": \"minecraft:item\",\n"
                        + "          \"name\": \"" + namespace + ":" + rawItemId + "\"\n"
                        + "        }\n"
                        + "      ],\n"
                        + "      \"conditions\": [\n"
                        + "        {\n"
                        + "          \"condition\": \"minecraft:survives_explosion\"\n"
                        + "        }\n"
                        + "      ]\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}";
                DatagenFiles.writeText(lootRoot.resolve(blockId + ".json"), json);
            }
        }
    }

    public void writeBlockTags(List<String> rawVariants) throws IOException {
        Path commonBlockTagsRoot = outRoot.resolve(Path.of("data", "c", "tags", "block"));
        Path commonItemTagsRoot = outRoot.resolve(Path.of("data", "c", "tags", "item"));
        Path minecraftBlockTagsRoot = outRoot.resolve(Path.of("data", "minecraft", "tags", "block"));
        Files.createDirectories(commonBlockTagsRoot);
        Files.createDirectories(commonItemTagsRoot);
        Files.createDirectories(minecraftBlockTagsRoot);

        List<String> oreBlockIds = new ArrayList<>(rawVariants.size());
        List<String> oreItemIds = new ArrayList<>(rawVariants.size());
        Map<String, LinkedHashSet<String>> oreIdsByMaterial = new LinkedHashMap<>();
        for (String rawVariant : rawVariants) {
            List<String> variantIds = new ArrayList<>(oreBlockIdsForRawVariant(rawVariant).size());
            for (String blockId : oreBlockIdsForRawVariant(rawVariant)) {
                String fullId = namespace + ":" + blockId;
                oreBlockIds.add(fullId);
                oreItemIds.add(fullId);
                variantIds.add(fullId);
            }
            variantIds.sort(String::compareTo);
            writeTag(commonBlockTagsRoot.resolve(Path.of("ores", rawVariant + ".json")), variantIds);
            writeTag(commonItemTagsRoot.resolve(Path.of("ores", rawVariant + ".json")), variantIds);

            String material = RawMaterialMappings.materialForRawVariant(rawVariant).orElse(rawVariant);
            String canonicalMaterial = MaterialItemOrder.canonicalMaterialToken(material);
            oreIdsByMaterial.computeIfAbsent(canonicalMaterial, ignored -> new LinkedHashSet<>()).addAll(variantIds);
        }
        oreBlockIds = new ArrayList<>(new LinkedHashSet<>(oreBlockIds));
        oreBlockIds.sort(String::compareTo);
        oreItemIds = new ArrayList<>(new LinkedHashSet<>(oreItemIds));
        oreItemIds.sort(String::compareTo);

        for (Map.Entry<String, LinkedHashSet<String>> entry : oreIdsByMaterial.entrySet()) {
            List<String> values = new ArrayList<>(entry.getValue());
            values.sort(String::compareTo);
            writeTag(commonBlockTagsRoot.resolve(Path.of("ores", entry.getKey() + ".json")), values);
            writeTag(commonItemTagsRoot.resolve(Path.of("ores", entry.getKey() + ".json")), values);
        }

        writeTag(commonBlockTagsRoot.resolve("ores.json"), oreBlockIds);
        writeTag(commonItemTagsRoot.resolve("ores.json"), oreItemIds);
        writeTag(minecraftBlockTagsRoot.resolve(Path.of("mineable", "pickaxe.json")), oreBlockIds);
        writeTag(minecraftBlockTagsRoot.resolve("needs_stone_tool.json"), oreBlockIds);
    }

    private static List<String> oreBlockIdsForRawVariant(String rawVariant) {
        return RawVariantCatalog.oreBlockIds(rawVariant);
    }

    private static void writeTag(Path path, List<String> values) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"replace\": false,\n  \"values\": [\n");
        for (int i = 0; i < values.size(); i++) {
            sb.append("    { \"id\": \"").append(values.get(i)).append("\", \"required\": false }");
            if (i + 1 < values.size()) sb.append(",\n");
            else sb.append('\n');
        }
        sb.append("  ]\n}");
        DatagenFiles.writeText(path, sb.toString());
    }

    private void writeBlockTextures(List<String> rawVariants) throws IOException {
        Path texturesRoot = outRoot.resolve(Path.of("assets", namespace, "textures", "block", "ore"));
        Files.createDirectories(texturesRoot);

        for (String rawVariant : rawVariants) {
            String material = RawMaterialMappings.materialForRawVariant(rawVariant)
                    .map(MaterialItemOrder::canonicalMaterialToken)
                    .orElse(rawVariant);

            BufferedImage oreTemplate = loadOreTemplate(rawVariant, material);
            if (oreTemplate == null) continue;

            BufferedImage overlayReferenceBase = loadOverlayReferenceBase(material);

            for (OreHostVariantCatalog.HostVariant host : OreHostVariantCatalog.hostVariants()) {
                BufferedImage hostBaseTemplate = loadHostBaseTemplate(host);
                if (hostBaseTemplate == null) continue;

                BufferedImage composed = composeOreTexture(hostBaseTemplate, oreTemplate, overlayReferenceBase);
                if (composed == null) continue;

                Path outPath = texturesRoot.resolve(host.blockId(rawVariant) + ".png");
                ImageIO.write(composed, "PNG", outPath.toFile());
            }
        }
    }

    private BufferedImage loadHostBaseTemplate(OreHostVariantCatalog.HostVariant host) throws IOException {
        String key = "host-base:" + host.token();
        if (imageCache.containsKey(key)) {
            return copyImage(imageCache.get(key));
        }

        BufferedImage image = readFirstExisting(List.of(
                templatePath("ore", host.token() + ".png"),
                templatePath("ore", host.baseTexture() + ".png")
        ));

        imageCache.put(key, image);
        return copyImage(image);
    }

    private BufferedImage loadStoneTemplate() throws IOException {
        String key = "host-base:stone_template";
        if (imageCache.containsKey(key)) {
            return copyImage(imageCache.get(key));
        }

        BufferedImage image = readFirstExisting(List.of(
                templatePath("ore", "stone.png")
        ));

        imageCache.put(key, image);
        return copyImage(image);
    }

    private BufferedImage loadOreTemplate(String rawVariant, String material) throws IOException {
        String key = "ore:" + rawVariant + ":" + material;
        if (imageCache.containsKey(key)) {
            return copyImage(imageCache.get(key));
        }

        List<Path> localCandidates = List.of(
                templatePath("ore", rawVariant + "_ore.png"),
                templatePath("ore", rawVariant + ".png"),
                templatePath("ore", material + "_ore.png"),
                templatePath("ore", material + ".png"),
                templatePath("ore", "ore.png"),
                templatePath("ore", "ore_template.png")
        );

        BufferedImage image = readFirstExisting(localCandidates);

        imageCache.put(key, image);
        return copyImage(image);
    }

    private BufferedImage loadOverlayReferenceBase(String material) throws IOException {
        if ("quartz".equals(material)) {
            return loadHostBaseTemplate(OreHostVariantCatalog.hostByToken("nether"));
        }
        return loadStoneTemplate();
    }

    private Path templatePath(String... segments) {
        Path[] pathParts = new Path[segments.length + 4];
        pathParts[0] = Path.of("assets");
        pathParts[1] = Path.of(namespace);
        pathParts[2] = Path.of("textures");
        pathParts[3] = Path.of("block");
        for (int i = 0; i < segments.length; i++) {
            pathParts[i + 4] = Path.of(segments[i]);
        }

        Path relative = pathParts[0];
        for (int i = 1; i < pathParts.length; i++) {
            relative = relative.resolve(pathParts[i]);
        }
        return mainResources.resolve(relative);
    }

    private static BufferedImage composeOreTexture(BufferedImage hostBase, BufferedImage oreTemplate, BufferedImage overlayReferenceBase) {
        if (hostBase == null || oreTemplate == null) return null;

        BufferedImage base = ensureSize(hostBase, hostBase.getWidth(), hostBase.getHeight());
        BufferedImage ore = ensureSize(oreTemplate, base.getWidth(), base.getHeight());
        BufferedImage overlayBase = overlayReferenceBase == null
                ? null
                : ensureSize(overlayReferenceBase, base.getWidth(), base.getHeight());

        BufferedImage overlay = hasAnyTransparency(ore) ? ore : extractOverlay(ore, overlayBase);
        if (overlay == null) return copyImage(base);

        BufferedImage out = copyImage(base);
        int width = out.getWidth();
        int height = out.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int dst = out.getRGB(x, y);
                int src = overlay.getRGB(x, y);
                out.setRGB(x, y, alphaComposite(src, dst));
            }
        }

        return out;
    }

    private static BufferedImage extractOverlay(BufferedImage ore, BufferedImage referenceBase) {
        if (ore == null) return null;
        if (referenceBase == null) return ore;

        int width = ore.getWidth();
        int height = ore.getHeight();
        BufferedImage overlay = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int oreRgb = ore.getRGB(x, y);
                int baseRgb = referenceBase.getRGB(x, y);

                int dr = Math.abs(((oreRgb >> 16) & 0xFF) - ((baseRgb >> 16) & 0xFF));
                int dg = Math.abs(((oreRgb >> 8) & 0xFF) - ((baseRgb >> 8) & 0xFF));
                int db = Math.abs((oreRgb & 0xFF) - (baseRgb & 0xFF));
                int diff = dr + dg + db;

                if (diff <= 18) {
                    overlay.setRGB(x, y, 0x00000000);
                } else {
                    int rgb = oreRgb & 0x00FFFFFF;
                    overlay.setRGB(x, y, 0xFF000000 | rgb);
                }
            }
        }

        return overlay;
    }

    private static int alphaComposite(int src, int dst) {
        int srcA = (src >>> 24) & 0xFF;
        if (srcA == 0) return dst;
        if (srcA == 255) return src;

        int dstA = (dst >>> 24) & 0xFF;
        int outA = srcA + ((dstA * (255 - srcA)) / 255);
        if (outA <= 0) return 0;

        int srcR = (src >>> 16) & 0xFF;
        int srcG = (src >>> 8) & 0xFF;
        int srcB = src & 0xFF;

        int dstR = (dst >>> 16) & 0xFF;
        int dstG = (dst >>> 8) & 0xFF;
        int dstB = dst & 0xFF;

        int outR = (srcR * srcA + dstR * dstA * (255 - srcA) / 255) / outA;
        int outG = (srcG * srcA + dstG * dstA * (255 - srcA) / 255) / outA;
        int outB = (srcB * srcA + dstB * dstA * (255 - srcA) / 255) / outA;

        return ((outA & 0xFF) << 24)
                | ((outR & 0xFF) << 16)
                | ((outG & 0xFF) << 8)
                | (outB & 0xFF);
    }

    private static boolean hasAnyTransparency(BufferedImage image) {
        if (image == null) return false;
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (((image.getRGB(x, y) >>> 24) & 0xFF) < 255) {
                    return true;
                }
            }
        }
        return false;
    }

    private static BufferedImage ensureSize(BufferedImage source, int width, int height) {
        if (source == null) return null;
        if (source.getWidth() == width && source.getHeight() == height) {
            return source;
        }

        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return resized;
    }

    private static BufferedImage readFirstExisting(List<Path> candidates) throws IOException {
        for (Path candidate : candidates) {
            BufferedImage image = readImageIfExists(candidate);
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    private static BufferedImage readImageIfExists(Path path) throws IOException {
        if (!Files.isRegularFile(path)) return null;
        try (InputStream stream = Files.newInputStream(path)) {
            return ImageIO.read(stream);
        }
    }

    private static BufferedImage copyImage(BufferedImage source) {
        if (source == null) return null;
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = copy.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return copy;
    }
}
