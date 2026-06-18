# Ore & Alloy

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.229+-E68A2E?style=flat-square)](https://neoforged.net/)
[![Version](https://img.shields.io/badge/version-1.0.4-4C8BF5?style=flat-square)](CHANGELOG.md)
[![License](https://img.shields.io/badge/code-MIT-blue?style=flat-square)](LICENCE.md)

Ore & Alloy is a focused material registry and enforced unification foundation for NeoForge modpacks.

It provides canonical ores, raw and crushed materials, ingots, nuggets, dusts, plates, rods, gears, and storage blocks. The core mod keeps material identity and recipes consistent without requiring scripts or configuration.

## Core behavior

- Iron, gold, and copper are always available.
- Additional materials activate only when a known provider mod is installed.
- Ore, raw, and crushed variants are defined centrally by `RawMaterialMappings`.
- Shared `c:` tags keep equivalent materials interoperable.
- Recipe inputs and outputs are rewritten to the canonical Ore & Alloy representation.
- Conflicting foreign recipes are removed when an equivalent canonical recipe exists.
- Duplicate external entries are hidden in JEI and EMI.
- Periodic material symbols are shown directly in item tooltips.

Material activation is automatic from installed provider mods. KubeJS startup scripts can explicitly activate additional supported materials.

## Supported materials

The texture catalog is the authoritative supported-material list. Iron, gold, and copper are always active. Every other material is activated when at least one listed provider mod is installed.

| Material | Type | Raw/ore variants | Provider mods |
|---|---|---|---|
| Iron | Metal | `iron`, `hematite`, `magnetite`, `limonite` | Minecraft |
| Gold | Metal | `gold`, `sylvanite` | Minecraft |
| Copper | Metal | `chalcopyrite`, `malachite`, `bornite`, `copper` | Minecraft |
| Tin | Metal | `cassiterite`, `tin` | Mekanism, Thermal, AllTheOres, Tech Reborn, GTCEu |
| Lead | Metal | `galena`, `lead` | Mekanism, Thermal, AllTheOres, Tech Reborn, GTCEu |
| Silver | Metal | `silver` | Thermal, AllTheOres, Tech Reborn, GTCEu |
| Nickel | Metal | `pentlandite`, `garnierite` | Thermal, AllTheOres, Tech Reborn, GTCEu |
| Zinc | Metal | `sphalerite`, `hemimorphite` | Create, AllTheOres, Thermal, Tech Reborn, GTCEu |
| Aluminium | Metal | `bauxite`, `cryolite` | Immersive Engineering, AllTheOres, Tech Reborn, GTCEu |
| Osmium | Metal | `osmium` | Mekanism |
| Uranium | Metal | `uranium`, `uraninite` | Mekanism, Immersive Engineering, Powah, Big Reactors, Bigger Reactors |
| Cobalt | Metal | `cobaltite` | Tinkers' Construct, AllTheOres, GTCEu |
| Titanium | Metal | `titanium` | Ad Astra, Tech Reborn, GTCEu, Modern Industrialization |
| Chromium | Metal | `chromite` | Tech Reborn, GTCEu, Modern Industrialization |
| Platinum | Metal | `platinum`, `sperrylite` | Thermal, AllTheOres, Tech Reborn, GTCEu |
| Iridium | Metal | `iridium` | Tech Reborn, GTCEu, Modern Industrialization |
| Antimony | Metal | — | GTCEu, Modern Industrialization |
| Lithium | Metal | — | Mekanism, Tech Reborn, GTCEu, Modern Industrialization |
| Tungsten | Metal | — | Tech Reborn, GTCEu, Modern Industrialization |
| Silicon | Material | — | Mekanism, Applied Energistics 2, Ender IO, GTCEu |
| Steel | Alloy | — | Immersive Engineering, Mekanism, Ad Astra, PneumaticCraft, Create Crafts & Additions |
| Stainless Steel | Alloy | — | GTCEu, Modern Industrialization, Tech Reborn |
| Brass | Alloy | — | Create, Thermal, AllTheOres |
| Bronze | Alloy | — | Mekanism, Thermal, AllTheOres, Tech Reborn, GTCEu |
| Cupronickel | Alloy | — | Immersive Engineering, GTCEu, Modern Industrialization |
| Electrum | Alloy | — | Thermal, Immersive Engineering, AllTheOres, Tech Reborn, GTCEu |
| Invar | Alloy | — | Thermal, AllTheOres, Tech Reborn, GTCEu |
| Constantan | Alloy | — | Immersive Engineering, AllTheOres, Tech Reborn |
| Wrought Iron | Alloy | — | GTCEu, Modern Industrialization |
| Enderium | Alloy | — | Thermal |
| Lumium | Alloy | — | Thermal |
| Naquadah | Alloy | — | GTCEu |
| Red Alloy | Alloy | — | ProjectRed, GTCEu |
| Soul Infused | Alloy | — | Thermal |

The catalog contains 34 material names. Ore-bearing metals provide ore, raw, crushed, ingot, nugget, dust, plate, rod, gear, and storage-block forms. Silicon is registered as the bare item `ore_and_alloy:silicon` and unified through `c:silicon`; it is not an ingot and has no storage block.

## KubeJS registration

KubeJS is optional. To activate materials manually, place a script in `kubejs/startup_scripts/`.

```js
OreAndAlloy.registry(event => {
    event.reg(aluminium)
})
```

Multiple materials can be requested in the same callback:

```js
OreAndAlloy.registry(event => {
    event.reg(aluminium)
    event.reg(antimony)
    event.reg(wrought_iron)
})
```

Every material name from the supported-material table is available as a global startup-script constant. Quoted names such as `event.reg('aluminium')` are also accepted.

Registration runs only during startup. Changes require a full game restart; `/reload` cannot modify item or block registries. Unknown material names produce a startup-script error.

## Optional integrations

- [JEI](https://www.curseforge.com/minecraft/mc-mods/jei): duplicate hiding and material aliases
- [EMI](https://www.curseforge.com/minecraft/mc-mods/emi): duplicate hiding and material aliases
- [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs): optional startup material activation

Neither viewer is required to run Ore & Alloy.

## License

- Source code: MIT
- Art and other assets: All Rights Reserved

See [LICENCE.md](LICENCE.md).
