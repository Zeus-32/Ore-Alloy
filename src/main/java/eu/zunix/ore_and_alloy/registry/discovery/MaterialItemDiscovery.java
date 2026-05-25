package eu.zunix.ore_and_alloy.registry.discovery;

import eu.zunix.ore_and_alloy.OreAndAlloy;

import java.util.List;

public final class MaterialItemDiscovery {
    private static final String MATERIAL_ITEMS_MANIFEST = "assets/" + OreAndAlloy.MODID + "/material_items.txt";

    private MaterialItemDiscovery() {}

    public static DiscoveryResult discoverMaterialItems() {
        GuaranteedMaterialSetBuilder guaranteedBuilder = new GuaranteedMaterialSetBuilder();
        MaterialManifestReader manifestReader = new MaterialManifestReader(MATERIAL_ITEMS_MANIFEST);

        List<String> fromManifest = manifestReader.read();
        if (!fromManifest.isEmpty()) {
            return new DiscoveryResult(guaranteedBuilder.withGuaranteedForms(fromManifest), "manifest+guaranteed");
        }

        List<String> fromTextures = new TextureBackedItemDiscovery().discoverFromEnumTextures();
        if (!fromTextures.isEmpty()) {
            OreAndAlloy.LOGGER.warn("[{}] Material manifest {} missing or empty, using enum/texture fallback.",
                    OreAndAlloy.MODID, MATERIAL_ITEMS_MANIFEST);
            return new DiscoveryResult(guaranteedBuilder.withGuaranteedForms(fromTextures), "enum_texture_fallback+guaranteed");
        }

        OreAndAlloy.LOGGER.warn("[{}] No material items discovered from manifest or fallback.", OreAndAlloy.MODID);
        return new DiscoveryResult(guaranteedBuilder.withGuaranteedForms(List.of()), "guaranteed_only");
    }

    public record DiscoveryResult(List<String> itemIds, String source) {}
}
