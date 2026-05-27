# Changelog

All notable changes to this project are documented in this file.

## [0.1.4-alpha]

### Unification
- Deterministic canonical selection now uses layered priority rules:
  1. global namespace order
  2. per-mod priority override
  3. per-material preferred namespace
  4. explicit canonical item override (`material + form`)
- Added unification audit mode:
  - config key: `unification.audit_report_enabled`
  - reports duplicate groups, unresolved canonical groups, alias conflicts, missing tags, missing textures, and vanilla expectation mismatches
- Added strict unification stability mode:
  - `unification.strict_mode_enabled`
  - `unification.strict_mode_fail_fast_enabled`
- Added structured snapshot export:
  - `unification.snapshot_export_enabled`
  - output: `<game_dir>/ore_and_alloy/unification_snapshot.json`
- Added built-in vanilla canonical expectation checks for core items/blocks (ingots, nuggets, gems, dusts, storage blocks).

### KubeJS API
- Added runtime unification control methods:
  - `resetUnificationPriorityOverrides()`
  - `setUnificationGlobalNamespacePriority(...)`
  - `setUnificationModPriority(modId, priority)`
  - `clearUnificationModPriority(modId)`
  - `setUnificationMaterialPreferredNamespace(material, modId)`
  - `clearUnificationMaterialPreferredNamespace(material)`
  - `setUnificationCanonicalItem(form, material, itemId)`
  - `clearUnificationCanonicalItem(form, material)`
  - `unificationPrioritySnapshot()`
- Added unification toggles:
  - `setUnificationAuditEnabled(enabled)`
  - `setUnificationStrictModeEnabled(enabled)`
  - `setUnificationStrictModeFailFastEnabled(enabled)`
  - `setUnificationSnapshotExportEnabled(enabled)`

### Storage Blocks
- Added dynamic storage block registration (`<material>_block`) for discovered materials.
- Added storage block datagen:
  - blockstates
  - block/item models
  - loot tables
  - `c:storage_blocks` tags (global + per material)
  - `minecraft:beacon_base_blocks` compatibility
- Added storage block recipes:
  - `9x base form -> 1x block`
  - `1x block -> 9x base form`
- Excluded `ore_and_alloy:amethyst_block` and `ore_and_alloy:quartz_block` from storage block generation.
- Storage compression remains 3x3 only.
- `ore_and_alloy:redstone_block` now uses powered-block behavior equivalent to vanilla redstone block signal output.

### Datagen / Tags / Parsing
- Alias and unification parsing now recognizes `_block` form.
- Datagen for `minecraft:mineable/pickaxe` and `minecraft:needs_stone_tool` now merges storage block entries instead of overwriting ore entries.
- Added language entries for new unification config keys.

### Creative / UX
- Creative tab includes storage blocks in material-sorted order.
- Config screen now detects KubeJS overrides for unification toggles and shows override warning accordingly.

### Tooling / QA
- Added automated regression tests:
  - `MaterialItemOrderTest`
  - `RawMaterialMappingsTest`
  - `MaterialIdParserTest`
- Test runtime now includes JUnit Platform launcher support in Gradle.

### Notes
- Missing texture files intentionally render as missing texture (no fallback placeholder).

## [0.1.3-alpha] - 2026-05-26

### Added
- First public release of Ore & Alloy (initial public version).
- Initial dynamic material unification and form registration.
- JEI/EMI/KubeJS integration baseline.
- Optional custom vein worldgen and supporting datagen pipeline.

### Known Issues
- Some textures/lang entries are still incomplete.
- Edge-case recipe unification behavior is still being tuned.
