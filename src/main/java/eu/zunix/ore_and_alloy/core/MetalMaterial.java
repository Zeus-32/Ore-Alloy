package eu.zunix.ore_and_alloy.core;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;


public enum MetalMaterial {
    IRON(oreMetalForms()),
    GOLD(oreMetalForms()),
    COPPER(oreMetalForms()),
    TIN(oreMetalForms()),
    LEAD(oreMetalForms()),
    SILVER(oreMetalForms()),
    NICKEL(oreMetalForms()),
    ZINC(oreMetalForms()),
    ALUMINUM(oreMetalForms()),
    OSMIUM(oreMetalForms()),
    URANIUM(oreMetalForms()),
    COBALT(oreMetalForms()),
    TITANIUM(oreMetalForms()),
    CHROME(oreMetalForms()),
    PLATINUM(oreMetalForms()),
    IRIDIUM(oreMetalForms()),

    STEEL(processedMetalForms()),
    BRASS(processedMetalForms()),
    BRONZE(processedMetalForms()),
    ELECTRUM(processedMetalForms()),
    INVAR(processedMetalForms()),
    CONSTANTAN(processedMetalForms());

    private final Set<MaterialForm> forms;

    MetalMaterial(Set<MaterialForm> forms) {
        this.forms = Set.copyOf(forms);
    }

    public Set<MaterialForm> getForms() {
        return forms;
    }

    public String texturePath(MaterialForm form) {
        return String.format("item/%s/%s", name().toLowerCase(Locale.ROOT), form.name().toLowerCase(Locale.ROOT));
    }

    public String materialName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String moltenFluidPath() {
        return "molten_" + materialName();
    }

    private static Set<MaterialForm> processedMetalForms() {
        return EnumSet.of(
                MaterialForm.INGOT,
                MaterialForm.PLATE,
                MaterialForm.NUGGET,
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
    }

    private static Set<MaterialForm> oreMetalForms() {
        EnumSet<MaterialForm> forms = EnumSet.copyOf(processedMetalForms());
        forms.add(MaterialForm.CRUSHED);
        forms.add(MaterialForm.ORE);
        forms.add(MaterialForm.RAW);
        return forms;
    }

}
