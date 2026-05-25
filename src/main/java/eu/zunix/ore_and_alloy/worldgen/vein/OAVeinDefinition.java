package eu.zunix.ore_and_alloy.worldgen.vein;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Locale;

public record OAVeinDefinition(
        String id,
        String materialToken,
        List<String> rawVariants,
        List<ResourceLocation> oreTags,
        List<ResourceLocation> fallbackBlocks,
        ResourceLocation dimensionId,
        int minY,
        int maxY,
        int size,
        int attemptsPerChunk,
        int chanceDenominator
) {
    public OAVeinDefinition {
        id = normalizeToken(id);
        materialToken = normalizeToken(materialToken);
        rawVariants = List.copyOf(rawVariants);
        oreTags = List.copyOf(oreTags);
        fallbackBlocks = List.copyOf(fallbackBlocks);
        dimensionId = dimensionId == null ? Level.OVERWORLD.location() : dimensionId;
        if (chanceDenominator < 1) {
            chanceDenominator = 1;
        }
        if (attemptsPerChunk < 1) {
            attemptsPerChunk = 1;
        }
        if (size < 1) {
            size = 1;
        }
    }

    public String displayName() {
        return OAVeinText.toDisplayName(materialToken) + " Vein";
    }

    public String dimensionDisplayName() {
        String path = dimensionId.getPath();
        if ("overworld".equals(path)) return "Overworld";
        if ("the_nether".equals(path)) return "Nether";
        if ("the_end".equals(path)) return "The End";
        return OAVeinText.toDisplayName(path);
    }

    private static String normalizeToken(String token) {
        if (token == null) return "";
        return token.toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }
}

