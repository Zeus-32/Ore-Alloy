package eu.zunix.ore_and_alloy.registry.discovery;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import eu.zunix.ore_and_alloy.core.MaterialForm;
import eu.zunix.ore_and_alloy.core.MaterialItemOrder;
import eu.zunix.ore_and_alloy.core.MetalMaterial;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

final class TextureBackedItemDiscovery {
    List<String> discoverFromEnumTextures() {
        Set<String> ids = new TreeSet<>();

        for (MetalMaterial metal : MetalMaterial.values()) {
            for (MaterialForm form : metal.getForms()) {
                String name = String.format("%s_%s",
                        metal.name().toLowerCase(Locale.ROOT),
                        form.name().toLowerCase(Locale.ROOT));
                if (hasBackedTexture(name)) {
                    ids.add(name);
                }
            }
        }

        return MaterialItemOrder.sorted(ids);
    }

    private static boolean hasBackedTexture(String itemId) {
        MaterialItemIdUtil.ParsedId parsed = MaterialItemIdUtil.parseItemId(itemId);
        if (parsed == null) return false;

        ClassLoader loader = MaterialItemDiscovery.class.getClassLoader();
        for (String relativeTexturePath : MaterialItemIdUtil.textureCandidates(itemId, parsed.material(), parsed.form())) {
            String fullPath = "assets/" + OreAndAlloy.MODID + "/textures/" + relativeTexturePath + ".png";
            if (loader.getResource(fullPath) != null) return true;
        }

        String legacy = "assets/" + OreAndAlloy.MODID + "/" + parsed.form() + "/" + parsed.material() + "_" + parsed.form() + ".png";
        return loader.getResource(legacy) != null;
    }
}
