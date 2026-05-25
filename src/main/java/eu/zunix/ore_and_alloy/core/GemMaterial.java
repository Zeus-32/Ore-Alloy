package eu.zunix.ore_and_alloy.core;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;


public enum GemMaterial {
    COAL(gemFormsFor("coal")),
    DIAMOND(gemFormsFor("diamond")),
    EMERALD(gemFormsFor("emerald")),
    LAPIS(gemFormsFor("lapis")),
    QUARTZ(gemFormsFor("quartz")),
    AMETHYST(gemFormsFor("amethyst")),
    SAPPHIRE(gemFormsFor("sapphire")),
    RUBY(gemFormsFor("ruby")),
    REDSTONE(gemFormsFor("redstone", false));

    private final Set<MaterialForm> forms;

    GemMaterial(Set<MaterialForm> forms) {
        this.forms = Set.copyOf(forms);
    }

    public Set<MaterialForm> getForms() {
        return forms;
    }

    public String texturePath(MaterialForm form) {
        return String.format("item/%s/%s", name().toLowerCase(Locale.ROOT), form.name().toLowerCase(Locale.ROOT));
    }

    public static boolean isGemMaterialToken(String materialToken) {
        String canonical = MaterialItemOrder.canonicalMaterialToken(materialToken);
        for (GemMaterial gem : values()) {
            if (gem.name().equalsIgnoreCase(canonical)) return true;
        }
        return false;
    }

    private static Set<MaterialForm> gemFormsFor(String materialToken) {
        return gemFormsFor(materialToken, true);
    }

    private static Set<MaterialForm> gemFormsFor(String materialToken, boolean includeGem) {
        EnumSet<MaterialForm> forms = EnumSet.of(
                MaterialForm.GEM,
                MaterialForm.PLATE,
                MaterialForm.ROD,
                MaterialForm.LONG_ROD,
                MaterialForm.GEAR,
                MaterialForm.BOLT,
                MaterialForm.SCREW,
                MaterialForm.RING,
                MaterialForm.SPRING,
                MaterialForm.DUST,
                MaterialForm.DIRTY_DUST,
                MaterialForm.PURIFIED_DUST,
                MaterialForm.CLUMP,
                MaterialForm.SHARD,
                MaterialForm.CRYSTAL
        );

        if (!includeGem) {
            forms.remove(MaterialForm.GEM);
        }
        if (GemRawCrushedMaterials.supportsRawCrushed(materialToken)) {
            forms.add(MaterialForm.RAW);
            forms.add(MaterialForm.CRUSHED);
        }
        return forms;
    }
}
