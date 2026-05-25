package eu.zunix.ore_and_alloy.datagen;

import eu.zunix.ore_and_alloy.core.MetalMaterial;
import eu.zunix.ore_and_alloy.core.MaterialForm;


public final class MaterialItemModelProvider {
    private MaterialItemModelProvider() {}

    public static String modelTypeFor(MaterialForm form) {
        return form.isHandheld() ? "item/handheld" : "item/generated";
    }

    public static String texturePathFor(MetalMaterial material, MaterialForm form) {
        return String.format("%s", material.texturePath(form));
    }
}

