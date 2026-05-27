# Ore & Alloy – Complete KubeJS How-To-Use

This file documents all exposed KubeJS API methods for Ore & Alloy.

## 1) Where scripts go

Create startup script:

`kubejs/startup_scripts/ore_and_alloy.js`

Use startup scripts for config/priority overrides so they are applied before normal gameplay logic.

## 2) Exposed bindings

Available bindings:
- `OreAndAlloy`
- `OAMetals`
- `OAForms`
- `OAGems`

## 3) Full API reference (`OreAndAlloy`)

### A) General info
- `OreAndAlloy.modId() -> string`

### B) Material/form queries
- `OreAndAlloy.metals() -> string[]`
- `OreAndAlloy.gems() -> string[]`
- `OreAndAlloy.forms(material) -> string[]`
- `OreAndAlloy.hasForm(material, form) -> boolean`
- `OreAndAlloy.itemId(material, form) -> string`
- `OreAndAlloy.moltenFluid(material) -> string`
- `OreAndAlloy.moltenFluids() -> string[]`

### C) Worldgen + tooltip toggles
- `OreAndAlloy.customVeinWorldgenEnabled() -> boolean`
- `OreAndAlloy.setCustomVeinWorldgenEnabled(enabled)`
- `OreAndAlloy.periodicTooltipsEnabled() -> boolean`
- `OreAndAlloy.setPeriodicTooltipsEnabled(enabled)`

### D) Unification debug/stability toggles
- `OreAndAlloy.unificationAuditEnabled() -> boolean`
- `OreAndAlloy.setUnificationAuditEnabled(enabled)`
- `OreAndAlloy.unificationStrictModeEnabled() -> boolean`
- `OreAndAlloy.setUnificationStrictModeEnabled(enabled)`
- `OreAndAlloy.unificationStrictModeFailFastEnabled() -> boolean`
- `OreAndAlloy.setUnificationStrictModeFailFastEnabled(enabled)`
- `OreAndAlloy.unificationSnapshotExportEnabled() -> boolean`
- `OreAndAlloy.setUnificationSnapshotExportEnabled(enabled)`

### E) Unification priority control
- `OreAndAlloy.resetUnificationPriorityOverrides()`
- `OreAndAlloy.setUnificationGlobalNamespacePriority(...namespaces)`
- `OreAndAlloy.setUnificationModPriority(modId, priority)`
- `OreAndAlloy.clearUnificationModPriority(modId)`
- `OreAndAlloy.setUnificationMaterialPreferredNamespace(material, modId)`
- `OreAndAlloy.clearUnificationMaterialPreferredNamespace(material)`
- `OreAndAlloy.setUnificationCanonicalItem(form, material, itemId)`
- `OreAndAlloy.clearUnificationCanonicalItem(form, material)`
- `OreAndAlloy.unificationPrioritySnapshot() -> object`

## 4) Recommended script template

```javascript
// kubejs/startup_scripts/ore_and_alloy.js

// 1) Core toggles
OreAndAlloy.setCustomVeinWorldgenEnabled(true);
OreAndAlloy.setPeriodicTooltipsEnabled(true);

// 2) Debug mode while tuning pack
OreAndAlloy.setUnificationAuditEnabled(true);
OreAndAlloy.setUnificationSnapshotExportEnabled(true);
OreAndAlloy.setUnificationStrictModeEnabled(false);
OreAndAlloy.setUnificationStrictModeFailFastEnabled(false);

// 3) Reset and rebuild priority strategy
OreAndAlloy.resetUnificationPriorityOverrides();

// Global order: lower index = stronger preference
OreAndAlloy.setUnificationGlobalNamespacePriority(
  "ore_and_alloy",
  "minecraft",
  "create"
);

// Explicit mod priorities (lower number = stronger)
OreAndAlloy.setUnificationModPriority("ore_and_alloy", 0);
OreAndAlloy.setUnificationModPriority("minecraft", 10);
OreAndAlloy.setUnificationModPriority("create", 20);

// Per material namespace preference
OreAndAlloy.setUnificationMaterialPreferredNamespace("steel", "ore_and_alloy");

// Per material+form explicit canonical mapping
OreAndAlloy.setUnificationCanonicalItem("plate", "iron", "ore_and_alloy:iron_plate");
OreAndAlloy.setUnificationCanonicalItem("block", "iron", "ore_and_alloy:iron_block");

// Optional snapshot in log
console.info("O&A priority snapshot:", OreAndAlloy.unificationPrioritySnapshot());
```

## 5) Move from debug to production

When tuning is done:
1. keep `snapshot_export` on for one final validation run
2. enable strict mode:
   - `setUnificationStrictModeEnabled(true)`
3. if stable, enable fail-fast:
   - `setUnificationStrictModeFailFastEnabled(true)`
4. optional: disable audit spam:
   - `setUnificationAuditEnabled(false)`

## 6) Notes and pitfalls

- These overrides are runtime overrides from KubeJS startup; keep your script under version control.
- `setUnificationCanonicalItem` expects full item ID (`namespace:path`) or valid O&A path.
- Always reset overrides first (`resetUnificationPriorityOverrides`) if script evolves over time.
- Use snapshot JSON to verify the final alias map:
  - `<game_dir>/ore_and_alloy/unification_snapshot.json`

## 7) Quick checks

Useful checks inside script:

```javascript
console.info("O&A modId:", OreAndAlloy.modId());
console.info("Known metals:", OreAndAlloy.metals());
console.info("Known gems:", OreAndAlloy.gems());
console.info("Forms for iron:", OreAndAlloy.forms("iron"));
console.info("Canonical iron plate:", OreAndAlloy.itemId("iron", "plate"));
```

## 8) Related docs

- General usage guide: [HOW_TO_USE.md](HOW_TO_USE.md)
- Project overview: [README.md](README.md)
- Changelog: [CHANGELOG.md](CHANGELOG.md)
