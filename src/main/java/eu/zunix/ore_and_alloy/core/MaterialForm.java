package eu.zunix.ore_and_alloy.core;


public enum MaterialForm {
    INGOT(false),
    NUGGET(false),
    DUST(false),
    DIRTY_DUST(false),
    PURIFIED_DUST(false),
    GEM(false),
    PLATE(false),
    SHEET(false),
    ROD(true),
    LONG_ROD(true),
    GEAR(false),
    BOLT(false),
    SCREW(false),
    CLUMP(false),
    SHARD(false),
    CRYSTAL(false),
    RING(false),
    SPRING(false),
    CRUSHED(false),
    ORE(false),
    RAW(false);

    private final boolean handheld;

    MaterialForm(boolean handheld) {
        this.handheld = handheld;
    }

    public boolean isHandheld() {
        return handheld;
    }
}
