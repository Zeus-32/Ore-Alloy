package eu.zunix.ore_and_alloy.core;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
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

    ANTIMONY(processedMetalForms()),
    STEEL(processedMetalForms()),
    STAINLESS_STEEL(processedMetalForms()),
    BRASS(processedMetalForms()),
    BRONZE(processedMetalForms()),
    CUPRONICKEL(processedMetalForms()),
    ELECTRUM(processedMetalForms()),
    INVAR(processedMetalForms()),
    CONSTANTAN(processedMetalForms()),
    WROUGHT_IRON(processedMetalForms()),
    ENDERIUM(processedMetalForms()),
    LITHIUM(processedMetalForms()),
    LUMIUM(processedMetalForms()),
    NAQUADAH(processedMetalForms()),
    RED_ALLOY(processedMetalForms()),
    SOUL_INFUSED(processedMetalForms()),
    TUNGSTEN(processedMetalForms()),
    SILICON(EnumSet.of(MaterialForm.SILICON));

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
        return MaterialItemOrder.canonicalMaterialToken(name().toLowerCase(Locale.ROOT));
    }

    public static Optional<MetalMaterial> fromToken(String token) {
        String canonical = MaterialItemOrder.canonicalMaterialToken(token);
        for (MetalMaterial material : values()) {
            if (material.materialName().equals(canonical)) {
                return Optional.of(material);
            }
        }
        return Optional.empty();
    }

    private static Set<MaterialForm> processedMetalForms() {
        return EnumSet.of(
                MaterialForm.INGOT,
                MaterialForm.PLATE,
                MaterialForm.NUGGET,
                MaterialForm.ROD,
                MaterialForm.GEAR,
                MaterialForm.DUST
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
