package eu.zunix.ore_and_alloy.worldgen.vein;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.RawVariantCatalog;
import eu.zunix.ore_and_alloy.datagen.material.MaterialId;
import eu.zunix.ore_and_alloy.datagen.material.MaterialIdParser;
import eu.zunix.ore_and_alloy.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class OAVeinCatalog {
    private static volatile List<OAVeinDefinition> cached;

    private OAVeinCatalog() {}

    public static List<OAVeinDefinition> definitions() {
        List<OAVeinDefinition> out = cached;
        if (out != null) return out;

        synchronized (OAVeinCatalog.class) {
            if (cached != null) return cached;
            cached = List.copyOf(buildDefinitions());
            return cached;
        }
    }

    public static void invalidate() {
        cached = null;
    }

    private static List<OAVeinDefinition> buildDefinitions() {
        Map<String, LinkedHashSet<String>> rawVariantsByMaterial = collectRawVariantsByMaterial();
        List<OAVeinDefinition> out = new ArrayList<>();

        for (Map.Entry<String, LinkedHashSet<String>> entry : rawVariantsByMaterial.entrySet()) {
            String material = entry.getKey();
            List<String> rawVariants = List.copyOf(entry.getValue());
            VeinProfile profile = profileFor(material);

            List<ResourceLocation> oreTags = new ArrayList<>();
            LinkedHashSet<String> tagTokens = new LinkedHashSet<>();
            tagTokens.add(material);
            tagTokens.add(MaterialItemOrder.preferredItemMaterialToken(material));
            if ("aluminum".equals(material)) {
                tagTokens.add("aluminium");
            }
            if ("chromium".equals(material)) {
                tagTokens.add("chrome");
            }
            for (String variant : rawVariants) {
                tagTokens.add(variant);
            }
            for (String token : tagTokens) {
                if (token.isBlank()) continue;
                oreTags.add(ResourceLocation.fromNamespaceAndPath("c", "ores/" + token));
            }

            List<ResourceLocation> fallbackBlocks = fallbackBlocksForRawVariants(rawVariants);
            out.add(new OAVeinDefinition(
                    material + "_vein",
                    material,
                    rawVariants,
                    oreTags,
                    fallbackBlocks,
                    profile.dimension(),
                    profile.minY(),
                    profile.maxY(),
                    profile.size(),
                    profile.attemptsPerChunk(),
                    profile.chanceDenominator()
            ));
        }

        out.sort(Comparator.comparing(OAVeinDefinition::materialToken));
        return out;
    }

    private static Map<String, LinkedHashSet<String>> collectRawVariantsByMaterial() {
        Map<String, LinkedHashSet<String>> out = new TreeMap<>();
        for (String itemId : ModItems.materialItems().keySet()) {
            if (!itemId.startsWith("raw_") || itemId.length() <= "raw_".length()) continue;
            try {
                MaterialId parsed = MaterialIdParser.parseItemId(itemId);
                if (!"raw".equals(parsed.form())) continue;

                String variantToken = normalize(itemId.substring("raw_".length()));
                if (variantToken.isBlank()) continue;

                String material = MaterialItemOrder.canonicalMaterialToken(parsed.material());
                out.computeIfAbsent(material, ignored -> new LinkedHashSet<>()).add(variantToken);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return out;
    }

    private static VeinProfile profileFor(String material) {
        return switch (material) {
            case "coal" -> new VeinProfile(0, 192, 28, 1, 8, Level.OVERWORLD.location());
            case "iron" -> new VeinProfile(-40, 96, 30, 2, 7, Level.OVERWORLD.location());
            case "copper" -> new VeinProfile(-16, 112, 30, 2, 7, Level.OVERWORLD.location());
            case "gold" -> new VeinProfile(-64, 48, 20, 1, 9, Level.OVERWORLD.location());
            case "diamond" -> new VeinProfile(-64, 20, 10, 1, 16, Level.OVERWORLD.location());
            case "emerald" -> new VeinProfile(-16, 180, 9, 1, 20, Level.OVERWORLD.location());
            case "lapis" -> new VeinProfile(-48, 64, 16, 1, 14, Level.OVERWORLD.location());
            case "redstone" -> new VeinProfile(-64, 24, 16, 1, 12, Level.OVERWORLD.location());
            case "quartz" -> new VeinProfile(10, 120, 18, 1, 8, Level.NETHER.location());
            case "uranium" -> new VeinProfile(-48, 42, 14, 1, 14, Level.OVERWORLD.location());
            case "tin", "lead", "silver", "nickel", "zinc", "aluminum", "chromium", "cobalt", "osmium", "platinum", "iridium", "titanium" ->
                    new VeinProfile(-48, 64, 16, 1, 15, Level.OVERWORLD.location());
            default -> new VeinProfile(-32, 64, 14, 1, 18, Level.OVERWORLD.location());
        };
    }

    private static List<ResourceLocation> fallbackBlocksForRawVariants(List<String> rawVariants) {
        List<ResourceLocation> out = new ArrayList<>(rawVariants.size() * 8);
        for (String rawVariant : rawVariants) {
            for (String blockId : RawVariantCatalog.oreBlockIds(rawVariant)) {
                out.add(ResourceLocation.fromNamespaceAndPath(OreAndAlloy.MODID, blockId));
            }
        }
        return List.copyOf(out);
    }

    private static String normalize(String token) {
        return token == null
                ? ""
                : token.toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_').trim();
    }

    private record VeinProfile(
            int minY,
            int maxY,
            int size,
            int attemptsPerChunk,
            int chanceDenominator,
            ResourceLocation dimension
    ) {}
}
