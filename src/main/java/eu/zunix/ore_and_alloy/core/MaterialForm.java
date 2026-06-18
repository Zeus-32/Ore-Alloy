package eu.zunix.ore_and_alloy.core;


public enum MaterialForm {
    INGOT(false),
    NUGGET(false),
    DUST(false),
    PLATE(false),
    ROD(true),
    GEAR(false),
    CRUSHED(false),
    ORE(false),
    RAW(false),
    SILICON(false);

    private final boolean handheld;

    MaterialForm(boolean handheld) {
        this.handheld = handheld;
    }

    public boolean isHandheld() {
        return handheld;
    }
}
