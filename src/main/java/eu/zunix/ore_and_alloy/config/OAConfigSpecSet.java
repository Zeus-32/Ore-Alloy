package eu.zunix.ore_and_alloy.config;

import eu.zunix.ore_and_alloy.OreAndAlloy;
import net.neoforged.neoforge.common.ModConfigSpec;

final class OAConfigSpecSet {
    final ModConfigSpec.BooleanValue customVeinWorldgenEnabled;
    final ModConfigSpec.BooleanValue periodicTooltipsEnabled;
    final ModConfigSpec.BooleanValue unificationAuditEnabled;
    final ModConfigSpec.BooleanValue unificationStrictModeEnabled;
    final ModConfigSpec.BooleanValue unificationStrictModeFailFastEnabled;
    final ModConfigSpec.BooleanValue unificationSnapshotExportEnabled;
    final ModConfigSpec startupSpec;
    final ModConfigSpec commonSpec;

    private OAConfigSpecSet(
            ModConfigSpec.BooleanValue customVeinWorldgenEnabled,
            ModConfigSpec.BooleanValue periodicTooltipsEnabled,
            ModConfigSpec.BooleanValue unificationAuditEnabled,
            ModConfigSpec.BooleanValue unificationStrictModeEnabled,
            ModConfigSpec.BooleanValue unificationStrictModeFailFastEnabled,
            ModConfigSpec.BooleanValue unificationSnapshotExportEnabled,
            ModConfigSpec startupSpec,
            ModConfigSpec commonSpec
    ) {
        this.customVeinWorldgenEnabled = customVeinWorldgenEnabled;
        this.periodicTooltipsEnabled = periodicTooltipsEnabled;
        this.unificationAuditEnabled = unificationAuditEnabled;
        this.unificationStrictModeEnabled = unificationStrictModeEnabled;
        this.unificationStrictModeFailFastEnabled = unificationStrictModeFailFastEnabled;
        this.unificationSnapshotExportEnabled = unificationSnapshotExportEnabled;
        this.startupSpec = startupSpec;
        this.commonSpec = commonSpec;
    }

    static OAConfigSpecSet create(
            boolean defaultCustomVeinWorldgen,
            boolean defaultPeriodicTooltips,
            boolean defaultUnificationAudit,
            boolean defaultUnificationStrictMode,
            boolean defaultUnificationStrictFailFast,
            boolean defaultUnificationSnapshotExport
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

        commonBuilder.push("unification");
        ModConfigSpec.BooleanValue unificationAuditEnabled = commonBuilder
                .comment("Log detailed unification audit report after recipe remap.")
                .translation(OreAndAlloy.MODID + ".config.unification.audit_report_enabled")
                .define("audit_report_enabled", defaultUnificationAudit);

        ModConfigSpec.BooleanValue unificationStrictModeEnabled = commonBuilder
                .comment("Require strict canonical stability checks during unification.")
                .translation(OreAndAlloy.MODID + ".config.unification.strict_mode_enabled")
                .define("strict_mode_enabled", defaultUnificationStrictMode);

        ModConfigSpec.BooleanValue unificationStrictModeFailFastEnabled = commonBuilder
                .comment("Crash startup/reload when strict mode finds unresolved groups.")
                .translation(OreAndAlloy.MODID + ".config.unification.strict_mode_fail_fast_enabled")
                .define("strict_mode_fail_fast_enabled", defaultUnificationStrictFailFast);

        ModConfigSpec.BooleanValue unificationSnapshotExportEnabled = commonBuilder
                .comment("Write unification snapshot JSON report on each rebuild.")
                .translation(OreAndAlloy.MODID + ".config.unification.snapshot_export_enabled")
                .define("snapshot_export_enabled", defaultUnificationSnapshotExport);
        commonBuilder.pop();
        ModConfigSpec commonSpec = commonBuilder.build();

        return new OAConfigSpecSet(
                customVeinWorldgenEnabled,
                periodicTooltipsEnabled,
                unificationAuditEnabled,
                unificationStrictModeEnabled,
                unificationStrictModeFailFastEnabled,
                unificationSnapshotExportEnabled,
                startupSpec,
                commonSpec
        );
    }
}
