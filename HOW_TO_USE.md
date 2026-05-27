# Ore & Alloy – Complete How-To-Use

This document is the practical usage guide for players and modpack developers.

## 1) What the mod does

Ore & Alloy unifies duplicated materials from vanilla/mods into one canonical material set, then rewrites recipe inputs/outputs and viewer behavior around that canonical set.

Main goals:
- less duplicate items in JEI/EMI
- predictable crafting results
- one consistent material progression chain

## 2) Installation

1. Use NeoForge `1.21.1`.
2. Put `ore_and_alloy-<version>.jar` into `mods/`.
3. (Optional) Add JEI / EMI / KubeJS for integrations.

## 3) First run workflow

1. Start game once and let registries/datapacks load.
2. Check logs for unification summary lines (`Recipe unification ...`).
3. If you tune pack behavior, enable audit/snapshot modes (see section 5).

## 4) In-game behavior you should expect

- Duplicate materials from other mods are aliased to O&A canonical items.
- JEI/EMI hide duplicated alias items, but recipes should keep working.
- O&A forms (ore/raw/crushed/dust/ingot/plate/etc.) are used as canonical outputs where aliases exist.
- If custom worldgen is enabled, O&A vein generation is used.

## 5) Config options (important)

Config groups:
- `startup`:
  - `worldgen.custom_vein_worldgen_enabled`
- `common`:
  - `tooltips.periodic_enabled`
  - `unification.audit_report_enabled`
  - `unification.strict_mode_enabled`
  - `unification.strict_mode_fail_fast_enabled`
  - `unification.snapshot_export_enabled`

Recommended tuning flow:
1. `audit_report_enabled = true`
2. `snapshot_export_enabled = true`
3. `strict_mode_enabled = false`
4. fix priority overrides if needed
5. `strict_mode_enabled = true`
6. optional hard enforcement: `strict_mode_fail_fast_enabled = true`

## 6) Unification debug outputs

If enabled:
- Audit report logs detailed conflicts/missing data.
- Snapshot JSON is written to:
  - `<game_dir>/ore_and_alloy/unification_snapshot.json`

Use those two outputs to validate pack-level unification.

## 7) Worldgen and prospector

- `custom_vein_worldgen_enabled = true`:
  - O&A custom veins are active.
  - prospector item is available.
- `custom_vein_worldgen_enabled = false`:
  - custom veins are disabled.
  - prospector is not registered.

## 8) Datagen workflow (dev / pack maintenance)

When you change textures/material forms/tags, run:

```bash
./gradlew generateMaterialData
```

Then run tests:

```bash
./gradlew test
```

## 9) Common troubleshooting

### A) "Wrong canonical item in recipes"
Actions:
1. enable audit + snapshot
2. inspect snapshot conflicts
3. add KubeJS unification priority overrides

### B) Missing translation / missing texture
Actions:
1. run `generateMaterialData`
2. verify expected texture path exists in `assets/ore_and_alloy/textures/...`
3. check generated lang/tags in `src/generated/resources`

### C) JEI/EMI hides too much or too little
Actions:
1. verify alias map in snapshot
2. verify canonical target really resolves to O&A item
3. re-check unification priorities

## 10) Related docs

- KubeJS API guide: [HOW_TO_USE_KUBEJS.md](HOW_TO_USE_KUBEJS.md)
- Project overview: [README.md](README.md)
- Changes by version: [CHANGELOG.md](CHANGELOG.md)
