package eu.zunix.ore_and_alloy.config;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class OAConfig {
    private static final boolean DEFAULT_CUSTOM_VEIN_WORLDGEN = true;
    private static final boolean DEFAULT_PERIODIC_TOOLTIPS = true;
    private static final boolean DEFAULT_UNIFICATION_AUDIT = false;
    private static final boolean DEFAULT_UNIFICATION_STRICT_MODE = false;
    private static final boolean DEFAULT_UNIFICATION_STRICT_FAIL_FAST = false;
    private static final boolean DEFAULT_UNIFICATION_SNAPSHOT_EXPORT = false;

    private static final OAConfigSpecSet SPECS = OAConfigSpecSet.create(
            DEFAULT_CUSTOM_VEIN_WORLDGEN,
            DEFAULT_PERIODIC_TOOLTIPS,
            DEFAULT_UNIFICATION_AUDIT,
            DEFAULT_UNIFICATION_STRICT_MODE,
            DEFAULT_UNIFICATION_STRICT_FAIL_FAST,
            DEFAULT_UNIFICATION_SNAPSHOT_EXPORT
    );
    private static final OAConfigOverrides OVERRIDES = new OAConfigOverrides();

    public static final ModConfigSpec STARTUP_SPEC = SPECS.startupSpec;
    public static final ModConfigSpec COMMON_SPEC = SPECS.commonSpec;

    private OAConfig() {}

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, STARTUP_SPEC);
        container.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    public static boolean customVeinWorldgenEnabled() {
        Boolean override = OVERRIDES.customVeinWorldgen();
        return override != null ? override : readBoolean(SPECS.customVeinWorldgenEnabled, DEFAULT_CUSTOM_VEIN_WORLDGEN);
    }

    public static boolean prospectorEnabled() {
        return customVeinWorldgenEnabled();
    }

    public static boolean periodicTooltipsEnabled() {
        Boolean override = OVERRIDES.periodicTooltips();
        return override != null ? override : readBoolean(SPECS.periodicTooltipsEnabled, DEFAULT_PERIODIC_TOOLTIPS);
    }

    public static boolean unificationAuditEnabled() {
        Boolean override = OVERRIDES.unificationAudit();
        return override != null ? override : readBoolean(SPECS.unificationAuditEnabled, DEFAULT_UNIFICATION_AUDIT);
    }

    public static boolean unificationStrictModeEnabled() {
        Boolean override = OVERRIDES.unificationStrictMode();
        return override != null ? override : readBoolean(SPECS.unificationStrictModeEnabled, DEFAULT_UNIFICATION_STRICT_MODE);
    }

    public static boolean unificationStrictModeFailFastEnabled() {
        Boolean override = OVERRIDES.unificationStrictModeFailFast();
        return override != null ? override : readBoolean(SPECS.unificationStrictModeFailFastEnabled, DEFAULT_UNIFICATION_STRICT_FAIL_FAST);
    }

    public static boolean unificationSnapshotExportEnabled() {
        Boolean override = OVERRIDES.unificationSnapshotExport();
        return override != null ? override : readBoolean(SPECS.unificationSnapshotExportEnabled, DEFAULT_UNIFICATION_SNAPSHOT_EXPORT);
    }

    public static boolean customVeinWorldgenConfiguredValue() {
        return readBoolean(SPECS.customVeinWorldgenEnabled, DEFAULT_CUSTOM_VEIN_WORLDGEN);
    }

    public static boolean periodicTooltipsConfiguredValue() {
        return readBoolean(SPECS.periodicTooltipsEnabled, DEFAULT_PERIODIC_TOOLTIPS);
    }

    public static boolean unificationAuditConfiguredValue() {
        return readBoolean(SPECS.unificationAuditEnabled, DEFAULT_UNIFICATION_AUDIT);
    }

    public static boolean unificationStrictModeConfiguredValue() {
        return readBoolean(SPECS.unificationStrictModeEnabled, DEFAULT_UNIFICATION_STRICT_MODE);
    }

    public static boolean unificationStrictModeFailFastConfiguredValue() {
        return readBoolean(SPECS.unificationStrictModeFailFastEnabled, DEFAULT_UNIFICATION_STRICT_FAIL_FAST);
    }

    public static boolean unificationSnapshotExportConfiguredValue() {
        return readBoolean(SPECS.unificationSnapshotExportEnabled, DEFAULT_UNIFICATION_SNAPSHOT_EXPORT);
    }

    public static void setCustomVeinWorldgenEnabledFromScreen(boolean enabled) {
        OVERRIDES.clearCustomVeinWorldgen();
        SPECS.customVeinWorldgenEnabled.set(enabled);
        STARTUP_SPEC.save();
    }

    public static void setPeriodicTooltipsEnabledFromScreen(boolean enabled) {
        OVERRIDES.clearPeriodicTooltips();
        SPECS.periodicTooltipsEnabled.set(enabled);
        COMMON_SPEC.save();
    }

    public static void setUnificationAuditEnabledFromScreen(boolean enabled) {
        OVERRIDES.clearUnificationAudit();
        SPECS.unificationAuditEnabled.set(enabled);
        COMMON_SPEC.save();
    }

    public static void setUnificationStrictModeEnabledFromScreen(boolean enabled) {
        OVERRIDES.clearUnificationStrictMode();
        SPECS.unificationStrictModeEnabled.set(enabled);
        COMMON_SPEC.save();
    }

    public static void setUnificationStrictModeFailFastEnabledFromScreen(boolean enabled) {
        OVERRIDES.clearUnificationStrictModeFailFast();
        SPECS.unificationStrictModeFailFastEnabled.set(enabled);
        COMMON_SPEC.save();
    }

    public static void setUnificationSnapshotExportEnabledFromScreen(boolean enabled) {
        OVERRIDES.clearUnificationSnapshotExport();
        SPECS.unificationSnapshotExportEnabled.set(enabled);
        COMMON_SPEC.save();
    }

    public static boolean hasKubeCustomVeinWorldgenOverride() {
        return OVERRIDES.customVeinWorldgen() != null;
    }

    public static boolean hasKubePeriodicTooltipsOverride() {
        return OVERRIDES.periodicTooltips() != null;
    }

    public static boolean hasKubeUnificationAuditOverride() {
        return OVERRIDES.unificationAudit() != null;
    }

    public static boolean hasKubeUnificationStrictModeOverride() {
        return OVERRIDES.unificationStrictMode() != null;
    }

    public static boolean hasKubeUnificationStrictModeFailFastOverride() {
        return OVERRIDES.unificationStrictModeFailFast() != null;
    }

    public static boolean hasKubeUnificationSnapshotExportOverride() {
        return OVERRIDES.unificationSnapshotExport() != null;
    }

    public static void setCustomVeinWorldgenEnabledFromKubeJS(boolean enabled) {
        OVERRIDES.setCustomVeinWorldgen(enabled);
        OreAndAlloy.LOGGER.info("[{}] KubeJS override: worldgen.custom_vein_worldgen_enabled={}",
                OreAndAlloy.MODID, enabled);
    }

    public static void setPeriodicTooltipsEnabledFromKubeJS(boolean enabled) {
        OVERRIDES.setPeriodicTooltips(enabled);
        OreAndAlloy.LOGGER.info("[{}] KubeJS override: tooltips.periodic_enabled={}",
                OreAndAlloy.MODID, enabled);
    }

    public static void setUnificationAuditEnabledFromKubeJS(boolean enabled) {
        OVERRIDES.setUnificationAudit(enabled);
        OreAndAlloy.LOGGER.info("[{}] KubeJS override: unification.audit_report_enabled={}",
                OreAndAlloy.MODID, enabled);
    }

    public static void setUnificationStrictModeEnabledFromKubeJS(boolean enabled) {
        OVERRIDES.setUnificationStrictMode(enabled);
        OreAndAlloy.LOGGER.info("[{}] KubeJS override: unification.strict_mode_enabled={}",
                OreAndAlloy.MODID, enabled);
    }

    public static void setUnificationStrictModeFailFastEnabledFromKubeJS(boolean enabled) {
        OVERRIDES.setUnificationStrictModeFailFast(enabled);
        OreAndAlloy.LOGGER.info("[{}] KubeJS override: unification.strict_mode_fail_fast_enabled={}",
                OreAndAlloy.MODID, enabled);
    }

    public static void setUnificationSnapshotExportEnabledFromKubeJS(boolean enabled) {
        OVERRIDES.setUnificationSnapshotExport(enabled);
        OreAndAlloy.LOGGER.info("[{}] KubeJS override: unification.snapshot_export_enabled={}",
                OreAndAlloy.MODID, enabled);
    }

    private static boolean readBoolean(ModConfigSpec.BooleanValue value, boolean fallback) {
        try {
            return value.get();
        } catch (IllegalStateException ex) {
            return fallback;
        }
    }
}
