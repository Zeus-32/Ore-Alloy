# Ore & Alloy

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.229+-E68A2E?style=flat-square)](https://neoforged.net/)
[![Version](https://img.shields.io/badge/version-1.0.11-4C8BF5?style=flat-square)](CHANGELOG.md)
[![License](https://img.shields.io/badge/license-mixed-blue?style=flat-square)](LICENCE.md)

Ore & Alloy is a focused material registry and enforced unification foundation for NeoForge modpacks.

![Ore & Alloy ingots](o%26a_ingots.png)

It provides canonical ores, raw and crushed materials, ingots, nuggets, dusts, plates, rods, gears, bolts, screws, and storage blocks. The core mod keeps material identity and recipes consistent without requiring scripts or configuration.

## Core behavior

- Iron, gold, copper, and diamond are available by default.
- Additional materials activate only when a compatible material source is installed.
- Ore, raw, and crushed variants are defined centrally by `RawMaterialMappings`.
- Shared `c:` tags keep equivalent materials interoperable.
- Recipe inputs and outputs are rewritten to the canonical Ore & Alloy representation.
- Conflicting foreign recipes are removed when an equivalent canonical recipe exists.
- Duplicate external entries are hidden in JEI and EMI.
- Periodic material symbols are shown directly in item tooltips.

Material activation is automatic from compatible material sources. KubeJS startup scripts can explicitly activate additional supported materials.

## Supported Materials

The texture catalog is the authoritative supported-material list. Iron, gold, copper, and diamond are available by default. External-source materials activate when a compatible material mod is installed. Custom materials, such as Pure Netherite, stay separate from vanilla Minecraft netherite and can be activated by an explicit request or a dedicated external source.

### Ore-Bearing Materials

| Material | Variants used by O&A | Availability |
|---|---|---|
| Iron | `iron`, `hematite`, `magnetite`, `limonite` | Built in |
| Gold | `gold`, `sylvanite` | Built in |
| Copper | `chalcopyrite`, `malachite`, `bornite`, `copper` | Built in |
| Tin | `cassiterite`, `tin` | External source |
| Lead | `galena`, `lead` | External source |
| Silver | `silver` | External source |
| Nickel | `pentlandite`, `garnierite` | External source |
| Zinc | `sphalerite`, `hemimorphite` | External source |
| Aluminum | `bauxite`, `cryolite` | External source |
| Osmium | `osmium` | External source |
| Uranium | `uranium`, `uraninite` | External source |
| Cobalt | `cobaltite` | External source |
| Titanium | `titanium` | External source |
| Chromium | `chromite` | External source |
| Platinum | `platinum`, `sperrylite` | External source |
| Iridium | `iridium` | External source |
| Antimony | `antimony` | External source |
| Lithium | `lithium` | External source |
| Tungsten | `tungsten` | External source |

### Gems

| Material | Type | Notes | Availability |
|---|---|---|---|
| Diamond | Gem | Bare item `ore_and_alloy:diamond`; gem-based material forms | Built in |

### Special Materials

| Material | Type | Notes | Availability |
|---|---|---|---|
| Silicon | Material | Bare item `ore_and_alloy:silicon`; no ingot or storage block | External source |
| Pure Netherite | Alloy | Fully custom material, separate from vanilla Minecraft netherite | Dedicated external source or explicit request |

### Alloys

| Material | Forms | Availability |
|---|---|---|
| Steel | Standard alloy forms | External source |
| Stainless Steel | Standard alloy forms | External source |
| Brass | Standard alloy forms | External source |
| Bronze | Standard alloy forms | External source |
| Cupronickel | Standard alloy forms | External source |
| Electrum | Standard alloy forms | External source |
| Invar | Standard alloy forms | External source |
| Constantan | Standard alloy forms | External source |
| Wrought Iron | Standard alloy forms | External source |
| Enderium | Standard alloy forms | External source |
| Lumium | Standard alloy forms | External source |
| Signalum | Standard alloy forms | External source |
| Rose Gold | Standard alloy forms | External source |
| Naquadah | Hot alloy forms | External source |
| Red Alloy | Standard alloy forms | External source |
| Soul Infused | Standard alloy forms | External source |

The catalog contains 38 material names. Ore-bearing metals provide ore, raw, crushed, ingot, nugget, dust, plate, rod, gear, bolt, screw, and storage-block forms.

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

## Gradle Dependency

Ore & Alloy publishes as a normal Maven artifact:

```text
eu.zunix.ore_and_alloy:ore_and_alloy:<version>
```
To add Ore & Alloy as a dependency, include the Maven repository and dependency in your `build.gradle` file:

```groovy
repositories {
    maven {
        name = "Ore & Alloy Maven"
        url = uri("https://maven.zunix.eu/repo")
        content {
            includeGroup "eu.zunix.ore_and_alloy"
        }
    }
}
```

Then add the dependency to your project. The following examples show how to include Ore & Alloy in different types of projects:

```groovy
dependencies {
    // NeoForge
    implementation "eu.zunix.ore_and_alloy:ore_and_alloy:1.0.11"

    // ForgeGradle-style projects
    implementation fg.deobf("eu.zunix.ore_and_alloy:ore_and_alloy:1.0.11")

    // Architectury Loom-style projects
    modImplementation "eu.zunix.ore_and_alloy:ore_and_alloy:1.0.11"
}
```


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
