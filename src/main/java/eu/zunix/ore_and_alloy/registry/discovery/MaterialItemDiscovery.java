package eu.zunix.ore_and_alloy.registry.discovery;

import eu.zunix.ore_and_alloy.OreAndAlloy;

import java.util.List;

public final class MaterialItemDiscovery {
    private static final String MATERIAL_ITEMS_MANIFEST = "assets/" + OreAndAlloy.MODID + "/material_items.json";

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
            return new DiscoveryResult(guaranteedBuilder.withGuaranteedForms(fromTextures), "enum_texture_fallback+guaranteed");
        }
        return new DiscoveryResult(guaranteedBuilder.withGuaranteedForms(List.of()), "guaranteed_only");
    }

    public record DiscoveryResult(List<String> itemIds, String source) {}
}
