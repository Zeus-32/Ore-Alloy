# Changelog

All notable changes to this project are documented in this file.

## [1.0.8-a] - 2026-06-22

### Added
- Added Signalum, Rose Gold, and Pure Netherite materials.

### Changed
- Material provider detection now uses one shared known-provider mod list for non-vanilla materials and no longer includes GregTech/GTCEu.

## [1.0.7-a_hotfix] - 2026-06-22

### Fixed
- Fixed a world-creation crash on NeoForge builds that expose immutable villager trade lists.

## [1.0.7-a] - 2026-06-22

### Changed
- Edited ingot and plate textures. More texture updates and new materials will follow in future updates.
- Gems now sort with ingots in the creative tab and shared material ordering.
- Diamond crafting now uses the base gem item instead of diamond dust for nugget and storage-block conversions.

### Fixed
- Fixed duplicate item registration for block-backed material IDs such as ore and storage block items.

## [1.0.6] - 2026-06-22

### Added
- Added `diamond` as a gem-based material type.
- Added generated Diamond Plate, Rod, Gear, Bolt, and Screw forms.
- Added common tags for `c:gems/diamond`, `c:plates/diamond`, `c:rods/diamond`, `c:gears/diamond`, `c:bolts/diamond`, and `c:screws/diamond`.

### Changed
- Diamond uses the bare item `ore_and_alloy:diamond` instead of an ingot form.
- Added an explicit `minecraft:diamond -> ore_and_alloy:diamond` unification alias.
- Diamond generated models currently reuse the vanilla diamond item texture until the 1.0.7a texture pass.

## [1.0.5] - 2026-06-21

### Added
- Added `bolt` and `screw` material forms for all supported metal and alloy materials.
- Added generated item registrations, models, language entries, and common tags for `c:bolts`, `c:bolts/<material>`, `c:screws`, and `c:screws/<material>`.
- Added recipe/viewer alias support for bolt and screw material forms.

### Compatibility
- Prepared Ore & Alloy 1.0.5 as the required dependency target for TGE Core material crafting recipes.

## [1.0.4] - 2026-06-18

### Critical Fix
- Added an explicit `ae2:silicon -> ore_and_alloy:silicon` unification alias independent of common tags.
- Overrode AE2 silicon smelting and blasting outputs to produce canonical Ore & Alloy silicon.
- Overrode the AE2 silicon-print Inscriber recipe to consume canonical Ore & Alloy silicon directly.

## [1.0.3] - 2026-06-18

### Bug Fix
- Replaced `ore_and_alloy:silicon_ingot` with the canonical bare item `ore_and_alloy:silicon`.
- Silicon now uses the common `c:silicon` tag and can replace equivalent silicon items from other mods.
- Removed silicon ingot recipes, ingot tags, and the automatically generated silicon storage block.

## [1.0.2] - 2026-06-18

### Quick Fix
- Fixed single-form materials such as silicon incorrectly gaining untextured metal forms.
- Added Create Crafts & Additions as an Electrum provider so its Electrum recipes unify to Ore & Alloy.

## [1.0.1] - 2026-06-18

### KubeJS
- Added optional startup-script material activation.
- Added the `OreAndAlloy.registry(event => { ... })` DSL.
- Added `event.reg(material)` with validation, alias normalization, and duplicate handling.
- Exposed all supported material names as startup-script constants, including `aluminium`, `antimony`, and `wrought_iron`.
- Material requests are frozen before deferred item and block discovery; registry changes require a full restart.

## [1.0.0] - 2026-06-18

First stable release of the architecture developed during the `0.2.1` cycle.

### Core Rework
- Repositioned Ore & Alloy as a focused ore, metal, and alloy database with code-driven registration.
- Vanilla materials are always registered; additional catalog materials activate only when a known provider mod is installed, without KubeJS or config activation.
- Canonical recipe rewriting, shared tags, and JEI/EMI duplicate hiding are enforced by the mod.
- Limited material forms to ores, raw materials, crushed materials, ingots, nuggets, dusts, plates, rods, gears, and storage blocks.
- Removed gems and all unrelated materials, including amethyst, coal, diamond, emerald, lapis, quartz, redstone, ruby, and sapphire.
- Removed molten fluids, shards, rings, screws, bolts, clumps, crystals, springs, legacy dust variants, and long rods.
- Removed worldgen, prospectors, modular tools, workstations, guide book content, networking, and their assets; these systems are reserved for separate companion mods.
- Rebuilt datagen, language entries, tags, models, recipes, trims, and JEI/EMI APIs around the reduced scope.
- Removed the KubeJS API, runtime material activation requests, unification toggles, audit/snapshot settings, and built-in machine-mod compatibility recipes.
- Removed the config file and config screen; periodic material tooltips are now an always-on core behavior.
- Removed the unused runtime unification-priority override infrastructure.
- Replaced the text-based material manifest with a structured JSON resource.
- Cleaned release metadata and documentation for the stable 1.0.0 export.

## [0.1.5] - 2026-06-01

### Unification
- Raw/Crushed unification now keeps variant-specific materials and prevents collapsing to generic outputs.
- O&A recipes are authoritative: canonical recipes are generated in `ore_and_alloy`, and conflicting foreign outputs are filtered.
- Create crushing/splashing outputs are forced to O&A canonical forms for raw/crushed flows.

### Worldgen
- Custom worldgen now sanitizes foreign ores in processed chunks and replaces them with host stone before placing O&A ores.
- Veins generate as one compact source-chunk cluster (`spacing = 4`) with deterministic assignment and no mixed vein types per source chunk.
- Added built-in known lists for per-material ore variant weights and ore density (code-driven, no config).
- Low-density veins are kept spatially compact (no wide scatter) and use center-first fallback placement to avoid empty chunks.

### Recipes
- Recipes are structured by type/folder (`crafting`, `smelting`, `blasting`, `compat`) for easier maintenance.
- Added canonical raw/crushed/dust cooking flows to ingots, and removed/overrode duplicate vanilla/Create ore-cooking paths.
- Overrode key vanilla compression/decompression recipes (blocks/nuggets) to return canonical O&A outputs.
- Added Create pressing compatibility recipes for ingots (`c:ingots/<material> -> ore_and_alloy:<material>_plate`).
- Added Create milling compatibility recipes for crushed materials (`c:crushed_raw_materials/<material> -> <material>_dirty_dust` with +25% extra output chance).
- Added Create wet-processing chain: splashing `dirty_dust -> dust` (1:1) and mixing `dust + 100mb water -> purified_dust` (1:1).
- Switched ingot finish step to Create splashing from `c:purified_dusts/<material>` (`1x` ingot + `50%` extra) and removed autogenerated O&A smelting/blasting ingot recipes.
- Simplified Create compat recipe file names to output-based IDs (e.g. `iron_ingot.json`, `iron_nugget.json`, `bronze_plate.json`).
- Fixed ingot<->nugget crafting generation for aliased material tokens (notably `aluminium/aluminum`) so both conversion recipes are always emitted when forms exist.
- Extended Create pressing plate generation: ingot materials use `c:ingots/<material>`, gem materials use `c:gems/<material>`, and `lapis`/`redstone` use `c:storage_blocks/<material>` for block->plate behavior.
- Added Create saw/cutting recipes for rods: `c:ingots/<material> -> 2x ore_and_alloy:<material>_rod` (`create:cutting`).

### Gameplay / UX
- Advanced Prospector now opens on right-click and uses map-focused waypoint selection.
- Added `basic_prospector` (chunk vein check) and `chunk_deletor` debug item.

### Performance
- Reduced non-essential logs and optimized worldgen processing/caching.
- Time-sliced custom worldgen chunk processing to reduce TPS spikes while preserving the same generated result.

### Assets
- Added molten fluid textures (`molten_still`, `molten_flow`).

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
