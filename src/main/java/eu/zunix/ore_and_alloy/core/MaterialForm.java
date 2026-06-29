package eu.zunix.ore_and_alloy.core;


public enum MaterialForm {
    INGOT(false),
    HOT_INGOT(false),
    NUGGET(false),
    DUST(false),
    DUST_PILE(false),
    TINY_DUST_PILE(false),
    PLATE(false),
    DOUBLE_PLATE(false),
    FOIL(false),
    ROD(true),
    LONG_ROD(true),
    WIRE(true),
    GEAR(false),
    SMALL_GEAR(false),
    BOLT(false),
    SCREW(false),
    CRUSHED(false),
    ORE(false),
    RAW(false),

    SILICON(false),
    GEM(false),
    GEODE(false),
    GLASS(false);

    private final boolean handheld;

    MaterialForm(boolean handheld) {
        this.handheld = handheld;
    }

    public boolean isHandheld() {
        return handheld;
    }
}
