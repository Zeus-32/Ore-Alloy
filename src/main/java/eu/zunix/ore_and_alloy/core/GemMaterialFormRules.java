package eu.zunix.ore_and_alloy.core;

public final class GemMaterialFormRules {
    private GemMaterialFormRules() {}

    public static boolean isAllowed(String materialToken, String formToken) {
        if (!isRawOrCrushed(formToken)) return true;
        if (!GemMaterial.isGemMaterialToken(materialToken)) return true;
        return GemRawCrushedMaterials.supportsRawCrushed(materialToken);
    }

    private static boolean isRawOrCrushed(String formToken) {
        return "raw".equals(formToken) || "crushed".equals(formToken);
    }
}
