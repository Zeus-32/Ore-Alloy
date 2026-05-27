package eu.zunix.ore_and_alloy.config;

final class OAConfigOverrides {
    private volatile Boolean customVeinWorldgen;
    private volatile Boolean periodicTooltips;
    private volatile Boolean unificationAudit;
    private volatile Boolean unificationStrictMode;
    private volatile Boolean unificationStrictModeFailFast;
    private volatile Boolean unificationSnapshotExport;

    Boolean customVeinWorldgen() {
        return customVeinWorldgen;
    }

    Boolean periodicTooltips() {
        return periodicTooltips;
    }

    Boolean unificationAudit() {
        return unificationAudit;
    }

    Boolean unificationStrictMode() {
        return unificationStrictMode;
    }

    Boolean unificationStrictModeFailFast() {
        return unificationStrictModeFailFast;
    }

    Boolean unificationSnapshotExport() {
        return unificationSnapshotExport;
    }

    void clearCustomVeinWorldgen() {
        customVeinWorldgen = null;
    }

    void clearPeriodicTooltips() {
        periodicTooltips = null;
    }

    void clearUnificationAudit() {
        unificationAudit = null;
    }

    void clearUnificationStrictMode() {
        unificationStrictMode = null;
    }

    void clearUnificationStrictModeFailFast() {
        unificationStrictModeFailFast = null;
    }

    void clearUnificationSnapshotExport() {
        unificationSnapshotExport = null;
    }

    void setCustomVeinWorldgen(boolean enabled) {
        customVeinWorldgen = enabled;
    }

    void setPeriodicTooltips(boolean enabled) {
        periodicTooltips = enabled;
    }

    void setUnificationAudit(boolean enabled) {
        unificationAudit = enabled;
    }

    void setUnificationStrictMode(boolean enabled) {
        unificationStrictMode = enabled;
    }

    void setUnificationStrictModeFailFast(boolean enabled) {
        unificationStrictModeFailFast = enabled;
    }

    void setUnificationSnapshotExport(boolean enabled) {
        unificationSnapshotExport = enabled;
    }
}
