# Ore & Alloy

![NeoForge](https://img.shields.io/badge/NeoForge-1.21.1-orange?style=for-the-badge)
![License](https://img.shields.io/badge/License-Custom_ARR_/_MIT-blue?style=for-the-badge)
![Website](https://img.shields.io/badge/Website-zunix.eu-green?style=for-the-badge)

Ore & Alloy is a unification framework for modern tech modpacks.

Its core idea is simple: **one material language for the entire pack**.  
If ten mods add ten incompatible versions of the same resource, progression becomes noise. Ore & Alloy turns that noise into a single, predictable production grammar.

## Why It Exists

Most large tech packs eventually hit the same structural problems:
- Duplicated materials.
- Recipe chains that do not agree on processing forms.
- Machine outputs that diverge depending on the source mod.
- Viewer clutter (JEI/EMI) that hides actual progression.

Ore & Alloy is built as an infrastructure layer to eliminate this fragmentation, ensuring that modpack design decisions matter more than workaround recipes.

## GregTech Inspiration

This project is inspired by GregTech mainly in these areas:
- **Multi-ore resource model:** one material can exist through multiple ore/raw source variants.
- **High crafting/processing complexity:** deeper machine and crafting chains instead of one-step outputs.
- **Form-driven progression:** material forms are treated as real production stages, not cosmetic duplicates.
- **Worldgen as progression axis:** ore vein layout matters for planning extraction and logistics.

Ore & Alloy is not a GregTech clone. The goal is to bring this design direction to NeoForge packs with practical compatibility and maintainable tooling.

O&A custom vein worldgen is optional and can be toggled in config (including KubeJS startup config), so packs can switch between custom and default generation.

## Conceptual Model

Ore & Alloy treats materials as **systems**, not isolated items.

Each material exposes a controlled set of forms (ore variants, raw/crushed, dust chains, ingot lines, mechanical components, and fluids). Recipes are unified strictly toward canonical outputs.  
As a result, automation planning remains coherent whether the processing block comes from Vanilla, Create, or any other tech mod.

## Core Features

- **Runtime Unification:** Forcing recipes, inputs, and outputs toward canonical O&A materials.
- **Data-Driven Registration:** Easy material and form registration.
- **Extended Form Coverage:** Comprehensive handling of items, components, ore variants, and molten forms.
- **Custom Worldgen:** Optional vein generation with JEI integration.
- **Viewer Cleanup:** Strategic JEI/EMI duplicate-hiding while preserving usable recipes.
- **Vanilla Hooks:** Compatibility bridges for hardcoded edge-cases (brewing, trades, alias compatibility).
- **Extensive Configuration:** Adjustable via in-game menus or KubeJS startup scripts.
- **Datagen Pipeline:** Automated synchronization of assets and generated data.

## Mod Integrations

The following integrations are actively implemented as runtime features:

- **JEI** (Optional Client): Item hiding, alias search terms, and a custom vein info category.
- **EMI** (Optional Client): Item hiding and alias search terms.
- **KubeJS** (Optional): Startup bindings for config, material, and form access.
- **Vanilla Minecraft**: Behavior unification hooks (interactions, brewing, trades).
- **Create**: Recipe unification that rewrites compatible outputs (e.g., `create:iron_sheet` defaults to the canonical O&A iron plate).

*Other mod-specific recipe integrations will be added progressively and documented here.*

## Texture Preview

### Ore Hosts & Overlays
|                                     Stone Host                                      |                                       Deepslate Host                                        | Bauxite Overlay |
|:-----------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------:| :---: |
| ![Stone Host](src/main/resources/assets/ore_and_alloy/textures/block/ore/stone.png) | ![Deepslate Host](src/main/resources/assets/ore_and_alloy/textures/block/ore/deepslate.png) | ![Bauxite Layer](src/main/resources/assets/ore_and_alloy/textures/block/ore/bauxite.png) |
|             *Standard overworld generation.* |                                  *Deepslate ore variants.*                                  | *Overlay for composing variants.* |

### Processed Forms
| Iron Ingot | Iron Plate | Copper Dust |
| :---: | :---: | :---: |
| ![Iron Ingot](src/main/resources/assets/ore_and_alloy/textures/item/ingot/iron_ingot.png) | ![Iron Plate](src/main/resources/assets/ore_and_alloy/textures/item/plate/iron_plate.png) | ![Copper Dust](src/main/resources/assets/ore_and_alloy/textures/item/dust/copper_dust.png) |
| *Canonical base metal form.* | *Processed mechanical form.* | *Pulverized processing stage.* |

## Configuration

### In-game
Settings are readily available through the native NeoForge mod configuration screen.

### KubeJS (Startup)
Exposed startup bindings allow for deep script control:
- `OreAndAlloy`
- `OAMetals`
- `OAForms`
- `OAGems`

**Example:**
```javascript
// kubejs/startup_scripts/ore_and_alloy.js
OreAndAlloy.setCustomVeinWorldgenEnabled(true);
OreAndAlloy.setPeriodicTooltipsEnabled(true);
```

## For Modpack Developers

Ore & Alloy should be treated as a pack infrastructure layer.  
If you maintain custom material definitions/assets, regenerate material data with:

```bash
./gradlew generateMaterialData
```

## Installation

1. Install [NeoForge 1.21.1](https://neoforged.net/).
2. Download the latest Ore & Alloy release.
3. Place the `.jar` into your `mods` folder.

## Licensing Note

Code and assets use a split licensing model (MIT for code, ARR for art/assets).
