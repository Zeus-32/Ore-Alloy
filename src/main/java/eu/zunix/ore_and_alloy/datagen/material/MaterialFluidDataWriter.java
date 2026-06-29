package eu.zunix.ore_and_alloy.datagen.material;

import eu.zunix.ore_and_alloy.core.MaterialFluidCatalog;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class MaterialFluidDataWriter {
    private final Path outRoot;
    private final Path mainResources;
    private final String namespace;

    public MaterialFluidDataWriter(Path outRoot, Path mainResources, String namespace) {
        this.outRoot = outRoot;
        this.mainResources = mainResources;
        this.namespace = namespace;
    }

    public void writeFluidData() throws IOException {
        List<String> allFluids = new ArrayList<>();
        List<String> allMolten = new ArrayList<>();
        for (MaterialFluidCatalog.Entry fluid : MaterialFluidCatalog.entries()) {
            List<ColorStop> palette = paletteForFluid(fluid);
            writeBucketModel(fluid, !palette.isEmpty());
            writeBucketOverlayTexture(fluid, palette);
            writeFluidTags(fluid);
            writeFluidTextures(fluid, palette);
            allFluids.add(namespace + ":" + fluid.id());
            allFluids.add(namespace + ":flowing_" + fluid.id());
            if (fluid.metalLike()) {
                allMolten.add(namespace + ":" + fluid.id());
                allMolten.add(namespace + ":flowing_" + fluid.id());
            }
        }
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluid", "fluids.json")), allFluids);
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluids", "fluids.json")), allFluids);
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluid", "molten.json")), allMolten);
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluids", "molten.json")), allMolten);
    }

    private void writeBucketModel(MaterialFluidCatalog.Entry fluid, boolean hasOverlay) throws IOException {
        Path model = outRoot.resolve(Path.of("assets", namespace, "models", "item", fluid.bucketItemId() + ".json"));
        StringBuilder json = new StringBuilder();
        json.append("{")
                .append("\n  \"parent\": \"minecraft:item/generated\",")
                .append("\n  \"textures\": {")
                .append("\n    \"layer0\": \"minecraft:item/bucket\"");
        if (hasOverlay) {
            json.append(",")
                    .append("\n    \"layer1\": \"").append(namespace).append(":item/bucket/")
                    .append(fluid.bucketItemId()).append("_fluid\"");
        }
        json.append("\n  }")
                .append("\n}");
        DatagenFiles.writeText(model, json.toString());
    }

    private void writeFluidTags(MaterialFluidCatalog.Entry fluid) throws IOException {
        List<String> values = List.of(namespace + ":" + fluid.id(), namespace + ":flowing_" + fluid.id());
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluid", fluid.id() + ".json")), values);
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluids", fluid.id() + ".json")), values);

        if (!fluid.metalLike()) return;

        String material = fluid.material();
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluid", material + ".json")), values);
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluids", material + ".json")), values);
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluid", "molten", material + ".json")), values);
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluids", "molten", material + ".json")), values);
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluid", "molten_" + material + ".json")), values);
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluids", "molten_" + material + ".json")), values);
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluid", "liquid", material + ".json")), values);
        writeTagFile(outRoot.resolve(Path.of("data", "c", "tags", "fluids", "liquid", material + ".json")), values);
    }

    private static void writeTagFile(Path path, List<String> values) throws IOException {
        List<String> unique = new ArrayList<>(new java.util.LinkedHashSet<>(values));
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"replace\": false,\n  \"values\": [\n");
        for (int i = 0; i < unique.size(); i++) {
            sb.append("    { \"id\": \"").append(unique.get(i)).append("\", \"required\": false }");
            sb.append(i + 1 < unique.size() ? ",\n" : "\n");
        }
        sb.append("  ]\n}");
        DatagenFiles.writeText(path, sb.toString());
    }

    private void writeFluidTextures(MaterialFluidCatalog.Entry fluid, List<ColorStop> palette) throws IOException {
        if (!fluid.metalLike()) return;
        if (palette.isEmpty()) return;

        Path templateRoot = mainResources.resolve(Path.of("assets", namespace, "textures", "block"));
        writeColorizedTemplate(
                templateRoot.resolve("fluid_still.png"),
                outRoot.resolve(Path.of("assets", namespace, "textures", "block", "fluid", fluid.id() + "_still.png")),
                palette
        );
        writeColorizedTemplate(
                templateRoot.resolve("fluid_flow.png"),
                outRoot.resolve(Path.of("assets", namespace, "textures", "block", "fluid", fluid.id() + "_flow.png")),
                palette
        );
    }

    private void writeBucketOverlayTexture(MaterialFluidCatalog.Entry fluid, List<ColorStop> palette) throws IOException {
        if (palette.isEmpty()) return;

        BufferedImage out = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (FluidPixel pixel : LAVA_BUCKET_FLUID_PIXELS) {
            double normalized = (pixel.luminance() - LAVA_BUCKET_MIN_LUMINANCE)
                    / (LAVA_BUCKET_MAX_LUMINANCE - LAVA_BUCKET_MIN_LUMINANCE);
            Color color = colorAt(palette, clamp01(normalized));
            out.setRGB(pixel.x(), pixel.y(), new Color(color.getRed(), color.getGreen(), color.getBlue(), 255).getRGB());
        }

        Path texture = outRoot.resolve(Path.of(
                "assets", namespace, "textures", "item", "bucket", fluid.bucketItemId() + "_fluid.png"
        ));
        Files.createDirectories(texture.getParent());
        ImageIO.write(out, "PNG", texture.toFile());
    }

    private List<ColorStop> paletteForFluid(MaterialFluidCatalog.Entry fluid) throws IOException {
        if (!fluid.metalLike()) {
            return switch (fluid.id()) {
                case "creosote" -> expandedLiquidPalette(List.of(
                        stop(35, 22, 9),
                        stop(78, 44, 18),
                        stop(126, 76, 28),
                        stop(182, 125, 48)
                ));
                case "steam" -> expandedLiquidPalette(List.of(
                        stop(164, 170, 172),
                        stop(195, 199, 200),
                        stop(224, 226, 225),
                        stop(248, 248, 244)
                ));
                default -> List.of();
            };
        }

        Path ingotTexture = mainResources.resolve(Path.of(
                "assets", namespace, "textures", "item", "ingot", fluid.material() + "_ingot.png"
        ));
        if (!Files.exists(ingotTexture)) return List.of();

        return ingotInteriorPalette(ImageIO.read(ingotTexture.toFile()));
    }

    private static ColorStop stop(int red, int green, int blue) {
        Color color = new Color(red, green, blue);
        return new ColorStop(color, luminance(color));
    }

    private static List<ColorStop> ingotInteriorPalette(BufferedImage ingot) {
        int width = ingot.getWidth();
        int height = ingot.getHeight();
        boolean[][] opaque = new boolean[width][height];
        Set<Integer> outlineColors = new HashSet<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                opaque[x][y] = alpha(ingot.getRGB(x, y)) > 0;
            }
        }

        int[][] edgeDistance = edgeDistances(opaque);
        int maxDistance = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!opaque[x][y]) continue;
                maxDistance = Math.max(maxDistance, edgeDistance[x][y]);
                if (edgeDistance[x][y] == 1) {
                    outlineColors.add(rgb(ingot.getRGB(x, y)));
                }
            }
        }

        int interiorDistance = maxDistance >= 3 ? 3 : Math.max(2, maxDistance);
        List<ColorStop> colors = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!opaque[x][y] || edgeDistance[x][y] < interiorDistance) continue;

                int rgb = rgb(ingot.getRGB(x, y));
                if (outlineColors.contains(rgb)) continue;
                Color color = new Color(rgb);
                colors.add(new ColorStop(color, luminance(color)));
            }
        }

        if (colors.size() < 2) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (!opaque[x][y] || edgeDistance[x][y] <= 1) continue;
                    Color color = new Color(rgb(ingot.getRGB(x, y)));
                    colors.add(new ColorStop(color, luminance(color)));
                }
            }
        }

        List<ColorStop> rawPalette = colors.stream()
                .collect(java.util.stream.Collectors.groupingBy(stop -> stop.color().getRGB()))
                .values()
                .stream()
                .map(group -> group.getFirst())
                .sorted(Comparator.comparingDouble(ColorStop::luminance))
                .toList();
        List<ColorStop> liquidPaletteSource = rawPalette.size() > 1
                ? rawPalette.subList(0, rawPalette.size() - 1)
                : rawPalette;
        return expandedLiquidPalette(liquidPaletteSource);
    }

    private static void writeColorizedTemplate(Path templatePath, Path outPath, List<ColorStop> palette) throws IOException {
        BufferedImage template = ImageIO.read(templatePath.toFile());
        BufferedImage out = new BufferedImage(template.getWidth(), template.getHeight(), BufferedImage.TYPE_INT_ARGB);
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (int y = 0; y < template.getHeight(); y++) {
            for (int x = 0; x < template.getWidth(); x++) {
                int argb = template.getRGB(x, y);
                if (alpha(argb) == 0) continue;
                double luma = luminance(new Color(rgb(argb)));
                min = Math.min(min, luma);
                max = Math.max(max, luma);
            }
        }

        double range = Math.max(1.0, max - min);
        for (int y = 0; y < template.getHeight(); y++) {
            for (int x = 0; x < template.getWidth(); x++) {
                int argb = template.getRGB(x, y);
                int alpha = alpha(argb);
                if (alpha == 0) {
                    out.setRGB(x, y, 0);
                    continue;
                }

                double normalized = (luminance(new Color(rgb(argb))) - min) / range;
                normalized = clamp01(((normalized - 0.5) * 0.94) + 0.5);
                Color mapped = colorAt(palette, normalized);
                out.setRGB(x, y, new Color(mapped.getRed(), mapped.getGreen(), mapped.getBlue(), alpha).getRGB());
            }
        }

        Files.createDirectories(outPath.getParent());
        ImageIO.write(out, "PNG", outPath.toFile());
        writeAnimationMetadata(outPath);
    }

    private static void writeAnimationMetadata(Path texturePath) throws IOException {
        Path metadataPath = texturePath.resolveSibling(texturePath.getFileName() + ".mcmeta");
        String fileName = texturePath.getFileName().toString();
        if (fileName.contains("_flow") || fileName.contains("fluid_flow")) {
            writeDefaultAnimationMetadata(metadataPath);
            return;
        }

        BufferedImage texture = ImageIO.read(texturePath.toFile());
        int frameCount = texture.getHeight() / texture.getWidth();
        if (frameCount <= 1) {
            writeDefaultAnimationMetadata(metadataPath);
            return;
        }

        StringBuilder frames = new StringBuilder();
        for (int i = 0; i < frameCount; i++) {
            appendFrame(frames, i);
        }
        for (int i = frameCount - 2; i > 0; i--) {
            appendFrame(frames, i);
        }

        DatagenFiles.writeText(metadataPath, "{"
                + "\n  \"animation\": {"
                + "\n    \"frametime\": 2,"
                + "\n    \"interpolate\": true,"
                + "\n    \"frames\": ["
                + frames
                + "\n    ]"
                + "\n  }"
                + "\n}");
    }

    private static void writeDefaultAnimationMetadata(Path metadataPath) throws IOException {
        DatagenFiles.writeText(metadataPath, "{"
                + "\n  \"animation\": {"
                + "\n    \"frametime\": 2,"
                + "\n    \"interpolate\": true"
                + "\n  }"
                + "\n}");
    }

    private static void appendFrame(StringBuilder frames, int frame) {
        if (!frames.isEmpty()) {
            frames.append(",");
        }
        frames.append("\n      ").append(frame);
    }

    private static Color colorAt(List<ColorStop> palette, double normalized) {
        if (palette.size() == 1) return palette.getFirst().color();
        double scaled = clamp01(normalized) * (palette.size() - 1);
        int lower = (int) Math.floor(scaled);
        int upper = Math.min(palette.size() - 1, lower + 1);
        double t = scaled - lower;
        return lerp(palette.get(lower).color(), palette.get(upper).color(), t);
    }

    private static List<ColorStop> expandedLiquidPalette(List<ColorStop> rawPalette) {
        if (rawPalette.isEmpty()) return List.of();

        double min = rawPalette.getFirst().luminance();
        double max = rawPalette.getLast().luminance();
        double average = rawPalette.stream()
                .mapToDouble(ColorStop::luminance)
                .average()
                .orElse((min + max) / 2.0);

        double rawSpan = Math.max(0.0, max - min);
        double targetSpan = Math.max(rawSpan * 0.96, minimumLiquidSpan(average));
        double targetMin = average - (targetSpan * 0.42);
        double targetMax = average + (targetSpan * 0.46);

        targetMin = Math.max(minimumLiquidMin(average), targetMin);
        targetMax = Math.min(maximumLiquidMax(average), Math.max(targetMax, targetMin + targetSpan));
        if (targetMax > maximumLiquidMax(average)) {
            targetMin = Math.max(minimumLiquidMin(average), targetMin - (targetMax - maximumLiquidMax(average)));
            targetMax = maximumLiquidMax(average);
        }
        if (targetMax - targetMin < targetSpan * 0.72) {
            targetMax = Math.min(maximumLiquidMax(average), targetMin + (targetSpan * 0.72));
        }

        int stopCount = Math.max(9, Math.min(14, rawPalette.size() + 4));
        List<ColorStop> expanded = new ArrayList<>(stopCount);
        for (int i = 0; i < stopCount; i++) {
            double position = stopCount == 1 ? 0.0 : (double) i / (double) (stopCount - 1);
            Color base = colorAt(rawPalette, position);
            double targetLuminance = targetMin + ((targetMax - targetMin) * position);
            Color adjusted = adjustLuminance(base, targetLuminance);
            adjusted = naturalizeLiquidColor(adjusted, average);
            adjusted = adjustLuminance(adjusted, targetLuminance);
            adjusted = naturalizeLiquidColor(adjusted, average);
            expanded.add(new ColorStop(adjusted, luminance(adjusted)));
        }
        return expanded;
    }

    private static double minimumLiquidSpan(double averageLuminance) {
        if (averageLuminance > 195.0) return 58.0;
        if (averageLuminance > 145.0) return 72.0;
        if (averageLuminance < 55.0) return 48.0;
        if (averageLuminance < 95.0) return 60.0;
        return 68.0;
    }

    private static double minimumLiquidMin(double averageLuminance) {
        if (averageLuminance > 195.0) return 150.0;
        if (averageLuminance > 145.0) return 112.0;
        if (averageLuminance > 105.0) return 74.0;
        if (averageLuminance > 70.0) return 48.0;
        if (averageLuminance > 45.0) return 26.0;
        return 8.0;
    }

    private static double maximumLiquidMax(double averageLuminance) {
        if (averageLuminance > 195.0) return 232.0;
        if (averageLuminance > 145.0) return 222.0;
        if (averageLuminance > 105.0) return 208.0;
        if (averageLuminance > 70.0) return 188.0;
        if (averageLuminance > 45.0) return 166.0;
        return 138.0;
    }

    private static Color naturalizeLiquidColor(Color color, double averageLuminance) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float maxSaturation = averageLuminance > 185.0 ? 0.42F : 0.70F;
        if (hsb[0] > 0.24F && hsb[0] < 0.48F) {
            maxSaturation = Math.min(maxSaturation, 0.58F);
        }
        if (hsb[1] <= maxSaturation) return color;

        int rgb = Color.HSBtoRGB(hsb[0], maxSaturation, hsb[2]);
        return new Color(rgb & 0xFFFFFF);
    }

    private static Color adjustLuminance(Color base, double targetLuminance) {
        double current = Math.max(0.001, luminance(base));
        double target = Math.max(0.0, Math.min(255.0, targetLuminance));
        if (target < current) {
            double factor = target / current;
            return new Color(
                    clamp((int) Math.round(base.getRed() * factor)),
                    clamp((int) Math.round(base.getGreen() * factor)),
                    clamp((int) Math.round(base.getBlue() * factor))
            );
        }

        double mix = (target - current) / Math.max(1.0, 255.0 - current);
        return new Color(
                clamp((int) Math.round(base.getRed() + ((255 - base.getRed()) * mix))),
                clamp((int) Math.round(base.getGreen() + ((255 - base.getGreen()) * mix))),
                clamp((int) Math.round(base.getBlue() + ((255 - base.getBlue()) * mix)))
        );
    }

    private static Color lerp(Color a, Color b, double t) {
        int r = (int) Math.round(a.getRed() + ((b.getRed() - a.getRed()) * t));
        int g = (int) Math.round(a.getGreen() + ((b.getGreen() - a.getGreen()) * t));
        int blue = (int) Math.round(a.getBlue() + ((b.getBlue() - a.getBlue()) * t));
        return new Color(clamp(r), clamp(g), clamp(blue));
    }

    private static boolean touchesTransparent(boolean[][] opaque, int x, int y) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (nx < 0 || ny < 0 || nx >= opaque.length || ny >= opaque[0].length) return true;
                if (!opaque[nx][ny]) return true;
            }
        }
        return false;
    }

    private static int[][] edgeDistances(boolean[][] opaque) {
        int width = opaque.length;
        int height = opaque[0].length;
        int[][] distance = new int[width][height];
        Queue<int[]> queue = new ArrayDeque<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!opaque[x][y]) continue;
                if (touchesTransparent(opaque, x, y)) {
                    distance[x][y] = 1;
                    queue.add(new int[]{x, y});
                }
            }
        }

        while (!queue.isEmpty()) {
            int[] point = queue.remove();
            int nextDistance = distance[point[0]][point[1]] + 1;
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = point[0] + dx;
                    int ny = point[1] + dy;
                    if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                    if (!opaque[nx][ny] || distance[nx][ny] != 0) continue;
                    distance[nx][ny] = nextDistance;
                    queue.add(new int[]{nx, ny});
                }
            }
        }

        return distance;
    }

    private static int alpha(int argb) {
        return (argb >>> 24) & 0xFF;
    }

    private static int rgb(int argb) {
        return argb & 0xFFFFFF;
    }

    private static double luminance(Color color) {
        return (0.2126 * color.getRed()) + (0.7152 * color.getGreen()) + (0.0722 * color.getBlue());
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static final double LAVA_BUCKET_MIN_LUMINANCE = 74.5;
    private static final double LAVA_BUCKET_MAX_LUMINANCE = 205.3;
    private static final FluidPixel[] LAVA_BUCKET_FLUID_PIXELS = {
            new FluidPixel(5, 2, 152.9),
            new FluidPixel(6, 2, 133.3),
            new FluidPixel(7, 2, 147.7),
            new FluidPixel(8, 2, 147.7),
            new FluidPixel(9, 2, 96.3),
            new FluidPixel(10, 2, 96.3),
            new FluidPixel(3, 3, 133.3),
            new FluidPixel(4, 3, 96.3),
            new FluidPixel(5, 3, 205.3),
            new FluidPixel(6, 3, 205.3),
            new FluidPixel(7, 3, 152.9),
            new FluidPixel(8, 3, 152.9),
            new FluidPixel(9, 3, 205.3),
            new FluidPixel(10, 3, 152.9),
            new FluidPixel(11, 3, 96.3),
            new FluidPixel(12, 3, 147.7),
            new FluidPixel(3, 4, 96.3),
            new FluidPixel(4, 4, 152.9),
            new FluidPixel(5, 4, 205.3),
            new FluidPixel(6, 4, 152.9),
            new FluidPixel(7, 4, 205.3),
            new FluidPixel(8, 4, 205.3),
            new FluidPixel(9, 4, 152.9),
            new FluidPixel(10, 4, 205.3),
            new FluidPixel(11, 4, 152.9),
            new FluidPixel(12, 4, 96.3),
            new FluidPixel(3, 5, 168.0),
            new FluidPixel(4, 5, 74.5),
            new FluidPixel(5, 5, 96.3),
            new FluidPixel(6, 5, 152.9),
            new FluidPixel(7, 5, 205.3),
            new FluidPixel(8, 5, 96.3),
            new FluidPixel(9, 5, 205.3),
            new FluidPixel(10, 5, 205.3),
            new FluidPixel(11, 5, 96.3),
            new FluidPixel(12, 5, 150.0),
            new FluidPixel(5, 6, 74.5),
            new FluidPixel(6, 6, 74.5),
            new FluidPixel(7, 6, 74.5),
            new FluidPixel(8, 6, 74.5),
            new FluidPixel(9, 6, 96.3),
            new FluidPixel(10, 6, 152.9),
            new FluidPixel(10, 7, 96.3),
            new FluidPixel(12, 7, 168.0),
            new FluidPixel(10, 9, 96.3)
    };

    private record FluidPixel(int x, int y, double luminance) {}

    private record ColorStop(Color color, double luminance) {}
}
