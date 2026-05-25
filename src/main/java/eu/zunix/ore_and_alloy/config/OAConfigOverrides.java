package eu.zunix.ore_and_alloy.config;

final class OAConfigOverrides {
    private volatile Boolean customVeinWorldgen;
    private volatile Boolean periodicTooltips;

    Boolean customVeinWorldgen() {
        return customVeinWorldgen;
    }

    Boolean periodicTooltips() {
        return periodicTooltips;
    }

    void clearCustomVeinWorldgen() {
        customVeinWorldgen = null;
    }

    void clearPeriodicTooltips() {
        periodicTooltips = null;
    }

    void setCustomVeinWorldgen(boolean enabled) {
        customVeinWorldgen = enabled;
    }

    void setPeriodicTooltips(boolean enabled) {
        periodicTooltips = enabled;
    }
}
