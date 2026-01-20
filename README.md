# Greedy Gold

A Fabric mod for Minecraft 1.20.1 that makes golden tools and armor regenerate durability and upgrade through use.

## Features

Golden tools and armor gain two types of upgrades as they are used:

**Enchantment Upgrades** - Tools and armor gradually gain levels in their primary enchantment (Fortune for pickaxes/hoes, Sharpness for swords/axes, Efficiency for shovels, Protection for armor).

**Durability Upgrades** - Each upgrade adds +1 to maximum durability. For tools, reaching certain durability thresholds unlocks higher mining levels:
- Stone level by default (can mine iron ore, lapis, copper)
- Iron level at 200 durability level (can mine diamond ore, gold ore, redstone)
- Diamond level at 550 durability level (can mine obsidian, ancient debris)

**Durability Regeneration** - All golden items slowly regenerate durability while in your inventory.

## Configuration

The mod is highly configurable through Mod Menu or by editing `config/greedy-gold.json`.

### Regeneration Settings

```json
{
  "enabled": true,
  "regenIntervalSeconds": 30,
  "usePercentage": true,
  "regenPercentage": 0.01,
  "regenAmount": 1
}
```

- `enabled` - Enable/disable durability regeneration
- `regenIntervalSeconds` - Seconds between regeneration ticks
- `usePercentage` - Use percentage of max durability (true) or fixed amount (false)
- `regenPercentage` - Percentage to restore per tick (0.01 = 1%)
- `regenAmount` - Fixed amount to restore if not using percentage

### Upgrade System

```json
{
  "upgradesEnabled": true,
  "showUpgradeTooltip": true,
  "useRandomAffinity": true,
  "minAffinity": 0.8,
  "maxAffinity": 1.2
}
```

- `upgradesEnabled` - Enable/disable the upgrade system
- `showUpgradeTooltip` - Show uses remaining until next upgrade in tooltip
- `useRandomAffinity` - Each item gets random upgrade speed (makes armor pieces upgrade at different rates)
- `minAffinity` / `maxAffinity` - Range for random speed multiplier (0.8 = 20% slower, 1.2 = 20% faster)

### Enchantment Upgrades

```json
{
  "enchantUpgradeBaseUses": 150,
  "enchantUpgradeModifier": 1.8,
  "maxEnchantLevel": 6
}
```

- `enchantUpgradeBaseUses` - Uses required for first enchantment level
- `enchantUpgradeModifier` - Multiplier for each subsequent level (exponential growth)
- `maxEnchantLevel` - Maximum enchantment level

Formula: `usesRequired = baseUses * modifier^(level-1)`

Example with defaults:
- Level 1: 150 uses
- Level 2: 270 uses
- Level 3: 486 uses
- Level 4: 875 uses

### Durability Upgrades

```json
{
  "durabilityUpgradeBaseUses": 25,
  "durabilityUpgradeModifier": 1.002,
  "maxDurabilityLevel": 1200,
  "miningLevelIronThreshold": 200,
  "miningLevelDiamondThreshold": 550
}
```

- `durabilityUpgradeBaseUses` - Uses required for first durability upgrade
- `durabilityUpgradeModifier` - Multiplier for each subsequent upgrade
- `maxDurabilityLevel` - Maximum number of durability upgrades
- `miningLevelIronThreshold` - Durability level needed for iron-tier mining
- `miningLevelDiamondThreshold` - Durability level needed for diamond-tier mining

## Installation

Requires Fabric Loader and Fabric API. Optional: Mod Menu and Cloth Config for in-game configuration.

1. Download from Modrinth or CurseForge
2. Place in `mods` folder
3. Launch game

## Compatibility

Server-side mod. Clients do not need it installed to join servers running this mod.

## License

MIT License
