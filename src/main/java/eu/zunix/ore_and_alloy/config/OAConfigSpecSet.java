package eu.zunix.ore_and_alloy.config;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import net.neoforged.neoforge.common.ModConfigSpec;

final class OAConfigSpecSet {
    final ModConfigSpec.BooleanValue customVeinWorldgenEnabled;
    final ModConfigSpec.BooleanValue periodicTooltipsEnabled;
    final ModConfigSpec startupSpec;
    final ModConfigSpec commonSpec;

    private OAConfigSpecSet(
            ModConfigSpec.BooleanValue customVeinWorldgenEnabled,
            ModConfigSpec.BooleanValue periodicTooltipsEnabled,
            ModConfigSpec startupSpec,
            ModConfigSpec commonSpec
    ) {
        this.customVeinWorldgenEnabled = customVeinWorldgenEnabled;
        this.periodicTooltipsEnabled = periodicTooltipsEnabled;
        this.startupSpec = startupSpec;
        this.commonSpec = commonSpec;
    }

    static OAConfigSpecSet create(
            boolean defaultCustomVeinWorldgen,
            boolean defaultPeriodicTooltips
    ) {
        ModConfigSpec.Builder startupBuilder = new ModConfigSpec.Builder();
        startupBuilder.push("worldgen");
        ModConfigSpec.BooleanValue customVeinWorldgenEnabled = startupBuilder
                .comment("Enable Ore & Alloy custom vein world generation.")
                .translation(OreAndAlloy.MODID + ".config.worldgen.custom_vein_worldgen_enabled")
                .gameRestart()
                .define("custom_vein_worldgen_enabled", defaultCustomVeinWorldgen);
        startupBuilder.pop();
        ModConfigSpec startupSpec = startupBuilder.build();

        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();
        commonBuilder.push("tooltips");
        ModConfigSpec.BooleanValue periodicTooltipsEnabled = commonBuilder
                .comment("Show periodic table values on Ore & Alloy item tooltips.")
                .translation(OreAndAlloy.MODID + ".config.tooltips.periodic_enabled")
                .define("periodic_enabled", defaultPeriodicTooltips);
        commonBuilder.pop();
        ModConfigSpec commonSpec = commonBuilder.build();

        return new OAConfigSpecSet(
                customVeinWorldgenEnabled,
                periodicTooltipsEnabled,
                startupSpec,
                commonSpec
        );
    }
}
