package eu.zunix.ore_and_alloy.datagen.lang;

import java.util.Map;

public final class ConfigScreenLangEntries {
    private ConfigScreenLangEntries() {}

    public static void append(Map<String, String> entries, String namespace) {
        entries.put(namespace + ".config.worldgen.custom_vein_worldgen_enabled", "Enable Custom Vein Worldgen");
        entries.put(namespace + ".config.tooltips.periodic_enabled", "Show Periodic Table Values Tooltip");
        entries.put(namespace + ".config.unification.audit_report_enabled", "Enable Unification Audit Report");
        entries.put(namespace + ".config.unification.strict_mode_enabled", "Enable Strict Unification Mode");
        entries.put(namespace + ".config.unification.strict_mode_fail_fast_enabled", "Fail Fast in Strict Unification");
        entries.put(namespace + ".config.unification.snapshot_export_enabled", "Enable Unification Snapshot Export");

        entries.put(namespace + ".config.screen.title", "Ore & Alloy Configuration");
        entries.put(namespace + ".config.screen.subtitle", "Machine Control");
        entries.put(namespace + ".config.screen.option.worldgen", "Custom Vein Worldgen");
        entries.put(namespace + ".config.screen.option.worldgen.desc", "Uses Ore & Alloy world generation rules.");
        entries.put(namespace + ".config.screen.option.tooltips", "Periodic Symbols");
        entries.put(namespace + ".config.screen.option.tooltips.desc", "Adds material symbols to O&A item tooltips.");
        entries.put(namespace + ".config.screen.worldgen.tooltip", "Controls custom Ore & Alloy vein generation. Requires game restart.");
        entries.put(namespace + ".config.screen.tooltips.tooltip", "Shows periodic symbols (for example Fe, Cu, Ag) on Ore & Alloy items.");

        entries.put(namespace + ".config.screen.buttons.save", "Save");
        entries.put(namespace + ".config.screen.buttons.reset", "Reset");
        entries.put(namespace + ".config.screen.buttons.back", "Back");
        entries.put(namespace + ".config.screen.toggle.on", "ON");
        entries.put(namespace + ".config.screen.toggle.off", "OFF");

        entries.put(namespace + ".config.screen.status.saved", "Config saved.");
        entries.put(namespace + ".config.screen.status.save_failed", "Failed to save config. See log for details.");
        entries.put(namespace + ".config.screen.status.reset", "Values reset in menu.");
        entries.put(namespace + ".config.screen.status.kube_override", "KubeJS override detected. Saving here writes config files; startup scripts can override next launch.");
    }
}
