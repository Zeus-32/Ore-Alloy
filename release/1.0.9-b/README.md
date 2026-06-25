# Ore & Alloy

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.229+-E68A2E?style=flat-square)](https://neoforged.net/)
[![Version](https://img.shields.io/badge/version-1.0.9--b-4C8BF5?style=flat-square)](CHANGELOG.md)
[![License](https://img.shields.io/badge/license-mixed-blue?style=flat-square)](LICENCE.md)

Ore & Alloy is a focused material registry and enforced unification foundation for NeoForge modpacks.

![Ore & Alloy ingots](o%26a_ingots.png)

It provides canonical ores, raw and crushed materials, ingots, nuggets, dusts, plates, rods, gears, bolts, screws, and storage blocks. The core mod keeps material identity and recipes consistent without requiring scripts or configuration.

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

The texture catalog is the authoritative supported-material list. Iron, gold, copper, diamond, and pure netherite are always active. Every other material is activated when at least one known material provider mod is installed.

| Material | Type | Raw/ore variants | Provider mods |
|---|---|---|---|
| Iron | Metal | `iron`, `hematite`, `magnetite`, `limonite` | Minecraft |
| Gold | Metal | `gold`, `sylvanite` | Minecraft |
| Copper | Metal | `chalcopyrite`, `malachite`, `bornite`, `copper` | Minecraft |
| Tin | Metal | `cassiterite`, `tin` | Known provider set |
| Lead | Metal | `galena`, `lead` | Known provider set |
| Silver | Metal | `silver` | Known provider set |
| Nickel | Metal | `pentlandite`, `garnierite` | Known provider set |
| Zinc | Metal | `sphalerite`, `hemimorphite` | Known provider set |
| Aluminum | Metal | `bauxite`, `cryolite` | Known provider set |
| Osmium | Metal | `osmium` | Known provider set |
| Uranium | Metal | `uranium`, `uraninite` | Known provider set |
| Cobalt | Metal | `cobaltite` | Known provider set |
| Titanium | Metal | `titanium` | Known provider set |
| Chromium | Metal | `chromite` | Known provider set |
| Platinum | Metal | `platinum`, `sperrylite` | Known provider set |
| Iridium | Metal | `iridium` | Known provider set |
| Antimony | Metal | — | Known provider set |
| Lithium | Metal | — | Known provider set |
| Tungsten | Metal | — | Known provider set |
| Silicon | Material | — | Known provider set |
| Diamond | Gem | - | Minecraft |
| Steel | Alloy | — | Known provider set |
| Stainless Steel | Alloy | — | Known provider set |
| Brass | Alloy | — | Known provider set |
| Bronze | Alloy | — | Known provider set |
| Cupronickel | Alloy | — | Known provider set |
| Electrum | Alloy | — | Known provider set |
| Invar | Alloy | — | Known provider set |
| Constantan | Alloy | — | Known provider set |
| Wrought Iron | Alloy | — | Known provider set |
| Enderium | Alloy | — | Known provider set |
| Lumium | Alloy | — | Known provider set |
| Signalum | Alloy | - | Known provider set |
| Rose Gold | Alloy | - | Known provider set |
| Naquadah | Alloy | — | Known provider set |
| Pure Netherite | Alloy | - | Minecraft |
| Red Alloy | Alloy | — | Known provider set |
| Soul Infused | Alloy | — | Known provider set |

The catalog contains 38 material names. Ore-bearing metals provide ore, raw, crushed, ingot, nugget, dust, plate, rod, gear, bolt, screw, and storage-block forms. Silicon is registered as the bare item `ore_and_alloy:silicon` and unified through `c:silicon`; it is not an ingot and has no storage block. Diamond is registered as the bare item `ore_and_alloy:diamond` and unified through `c:gems/diamond`; it is not an ingot, but its storage and nugget recipes use the base gem item. Pure Netherite is always active and is crafted from vanilla Minecraft netherite items.

## KubeJS registration

KubeJS is optional. To activate materials manually, place a script in `kubejs/startup_scripts/`.

```js
OreAndAlloy.registry(event => {
    event.reg(aluminum)
})
```

Multiple materials can be requested in the same callback:

```js
OreAndAlloy.registry(event => {
    event.reg(aluminum)
    event.reg(antimony)
    event.reg(wrought_iron)
})
```

Every material name from the supported-material table is available as a global startup-script constant. Quoted names such as `event.reg('aluminum')` are also accepted.

Registration runs only during startup. Changes require a full game restart; `/reload` cannot modify item or block registries. Unknown material names produce a startup-script error.

## Optional integrations

- [JEI](https://www.curseforge.com/minecraft/mc-mods/jei): duplicate hiding and material aliases
- [EMI](https://www.curseforge.com/minecraft/mc-mods/emi): duplicate hiding and material aliases
- [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs): optional startup material activation

Neither viewer is required to run Ore & Alloy.

## Credits

Ore & Alloy is inspired by GregTech-style material processing and unification. The `*_small_gear.png` textures use a wheel shape based on GregTechCEu/GregTech CEu Modern assets; see [LICENCE.md](LICENCE.md) for the LGPL asset notice.

## License

- Source code: MIT
- Art and other assets: All Rights Reserved
- `*_small_gear.png` textures: LGPL-3.0-or-later, because the wheel shape is based on GregTechCEu/GregTech CEu Modern assets

See [LICENCE.md](LICENCE.md).
