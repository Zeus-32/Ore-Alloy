package eu.zunix.ore_and_alloy.config;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class OAConfig {
    private static final boolean DEFAULT_CUSTOM_VEIN_WORLDGEN = true;
    private static final boolean DEFAULT_PERIODIC_TOOLTIPS = true;

    private static final OAConfigSpecSet SPECS = OAConfigSpecSet.create(
            DEFAULT_CUSTOM_VEIN_WORLDGEN,
            DEFAULT_PERIODIC_TOOLTIPS
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

    public static boolean customVeinWorldgenConfiguredValue() {
        return readBoolean(SPECS.customVeinWorldgenEnabled, DEFAULT_CUSTOM_VEIN_WORLDGEN);
    }

    public static boolean periodicTooltipsConfiguredValue() {
        return readBoolean(SPECS.periodicTooltipsEnabled, DEFAULT_PERIODIC_TOOLTIPS);
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

    public static boolean hasKubeCustomVeinWorldgenOverride() {
        return OVERRIDES.customVeinWorldgen() != null;
    }

    public static boolean hasKubePeriodicTooltipsOverride() {
        return OVERRIDES.periodicTooltips() != null;
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

    private static boolean readBoolean(ModConfigSpec.BooleanValue value, boolean fallback) {
        try {
            return value.get();
        } catch (IllegalStateException ex) {
            return fallback;
        }
    }
}
