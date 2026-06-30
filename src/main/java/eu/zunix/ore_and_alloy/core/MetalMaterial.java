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
    URANIUM(oreMetalForms()),

    ALUMINUM(hotOreMetalForms()),
    OSMIUM(hotOreMetalForms()),
    COBALT(hotOreMetalForms()),
    TITANIUM(hotOreMetalForms()),
    CHROME(hotOreMetalForms()),
    PLATINUM(hotOreMetalForms()),
    IRIDIUM(hotOreMetalForms()),
    TUNGSTEN(hotOreMetalForms()),

    ANTIMONY(oreMetalForms()),
    LITHIUM(oreMetalForms()),

    STEEL(alloyForms()),
    STAINLESS_STEEL(alloyForms()),
    BRASS(alloyForms()),
    BRONZE(alloyForms()),
    CUPRONICKEL(alloyForms()),
    ELECTRUM(alloyForms()),
    INVAR(alloyForms()),
    CONSTANTAN(alloyForms()),
    WROUGHT_IRON(alloyForms()),

    ENDERIUM(alloyForms()),
    LUMIUM(alloyForms()),
    SIGNALUM(alloyForms()),
    ROSE_GOLD(alloyForms()),
    RED_ALLOY(alloyForms()),
    SOUL_INFUSED_ALLOY(alloyForms()),

    NAQUADAH(hotAlloyForms()),
    PURE_NETHERITE(hotAlloyForms()),

    SILICON(EnumSet.of(MaterialForm.SILICON)),

    DIAMOND(engineeringGemForms()),
    RUBY(engineeringGemForms()),
    SAPPHIRE(engineeringGemForms()),
    EMERALD(engineeringGemForms()),
    TOPAZ(engineeringGemForms()),
    APATITE(engineeringGemForms()),
    CERTUS_QUARTZ(engineeringGemForms());

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

    private static Set<MaterialForm> metalForms() {
        return EnumSet.of(
                MaterialForm.INGOT,
                MaterialForm.PLATE,
                MaterialForm.DOUBLE_PLATE,
                MaterialForm.FOIL,
                MaterialForm.NUGGET,
                MaterialForm.ROD,
                MaterialForm.LONG_ROD,
                MaterialForm.WIRE,
                MaterialForm.GEAR,
                MaterialForm.SMALL_GEAR,
                MaterialForm.BOLT,
                MaterialForm.SCREW,
                MaterialForm.DUST,
                MaterialForm.TINY_DUST_PILE,
                MaterialForm.DUST_PILE
        );
    }

    private static Set<MaterialForm> hotMetalForms() {
        EnumSet<MaterialForm> forms = EnumSet.copyOf(metalForms());
        forms.add(MaterialForm.HOT_INGOT);
        return forms;
    }

    private static Set<MaterialForm> oreMetalForms() {
        EnumSet<MaterialForm> forms = EnumSet.copyOf(metalForms());
        forms.add(MaterialForm.CRUSHED);
        forms.add(MaterialForm.ORE);
        forms.add(MaterialForm.RAW);
        return forms;
    }

    private static Set<MaterialForm> hotOreMetalForms() {
        EnumSet<MaterialForm> forms = EnumSet.copyOf(hotMetalForms());
        forms.add(MaterialForm.CRUSHED);
        forms.add(MaterialForm.ORE);
        forms.add(MaterialForm.RAW);
        return forms;
    }

    private static Set<MaterialForm> alloyForms() {
        return metalForms();
    }

    private static Set<MaterialForm> hotAlloyForms() {
        return hotMetalForms();
    }

    private static Set<MaterialForm> gemForms() {
        return EnumSet.of(
                MaterialForm.GEM,
                MaterialForm.RAW,
                MaterialForm.CRUSHED,
                MaterialForm.ORE,
                MaterialForm.DUST,
                MaterialForm.TINY_DUST_PILE,
                MaterialForm.DUST_PILE,
                MaterialForm.GEODE,
                MaterialForm.PLATE,
                MaterialForm.ROD,
                MaterialForm.LONG_ROD
        );
    }

    private static Set<MaterialForm> engineeringGemForms() {
        EnumSet<MaterialForm> forms = EnumSet.copyOf(gemForms());
        forms.add(MaterialForm.GEAR);
        forms.add(MaterialForm.SMALL_GEAR);
        forms.add(MaterialForm.BOLT);
        forms.add(MaterialForm.SCREW);
        return forms;
    }

}
