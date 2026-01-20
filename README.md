# Greedy Gold

**Purpose:** Transform gold from Minecraft's most disappointing material into a rewarding long-term investment. Instead of being disposable tools that break after mining a handful of blocks, golden equipment becomes a progression system—items that grow stronger the more you use them. While they won't surpass netherite in raw power, fully upgraded golden tools offer unique advantages: blazing-fast mining speed with Efficiency 6, or Sharpness 7 weapons that leverage gold's natural speed. This mod rewards patience and dedication.

---

A Fabric mod for Minecraft 1.20.1 that makes golden tools and armor regenerate durability and upgrade through use.

## Features

Golden tools and armor gain power in three ways:

### 1. Enchantment Upgrades
Tools and armor gradually gain levels in their primary enchantment as you use them:
- **Pickaxes**: Efficiency
- **Swords & Axes**: Sharpness
- **Shovels**: Efficiency
-  **Hoes**: Fortune
- **Armor**: Protection

Maximum levels are configurable per item type:
- Pickaxe: 6 (default)
- Sword: 7 (default)
- Axe: 7 (default)
- Shovel: 5 (default)
- Hoe: 5 (default)
- Armor: 6 (default)

### 2. Durability Upgrades
Each upgrade adds +1 to maximum durability. For pickaxes, reaching certain durability thresholds unlocks higher mining levels:
- **Stone level** by default (can mine iron ore, lapis, copper)
- **Iron level** at 120 durability upgrades (can mine diamond ore, gold ore, redstone)
- **Diamond level** at 500 durability upgrades (can mine obsidian, ancient debris)

All other tools (shovels, axes, hoes, swords) and armor also gain durability.

### 3. Durability Regeneration
All golden items slowly regenerate durability while in your inventory. Tools regenerate faster than weapons and armor (configurable separately).

## Configuration

The mod is highly configurable through Mod Menu or by editing `config/greedy-gold.json`.

### Regeneration Settings

```json
{
  "enabled": true,
  "regenIntervalSeconds": 25,
  "usePercentage": true,
  "regenPercentage": 0.02,
  "regenAmount": 1,
  
  "useSeparateToolRegen": true,
  "toolRegenIntervalSeconds": 4,
  "toolRegenPercentage": 0.02
}
```

- `enabled` - Enable/disable durability regeneration
- `regenIntervalSeconds` - Seconds between regeneration ticks for weapons/armor
- `usePercentage` - Use percentage of max durability (true) or fixed amount (false)
- `regenPercentage` - Percentage to restore per tick for weapons/armor (0.02 = 2%)
- `regenAmount` - Fixed amount to restore if not using percentage
- `useSeparateToolRegen` - Enable separate (faster) regeneration for tools
- `toolRegenIntervalSeconds` - Seconds between regeneration ticks for tools (default: 4)
- `toolRegenPercentage` - Percentage to restore per tick for tools (0.02 = 2%)

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
- `useRandomAffinity` - Each item gets random upgrade speed (prevents armor pieces from all upgrading simultaneously)
- `minAffinity` / `maxAffinity` - Range for random speed multiplier (0.8 = 20% slower, 1.2 = 20% faster)

### Enchantment Upgrades

```json
{
  "enchantUpgradeBaseUses": 150,
  "enchantUpgradeModifier": 1.2,
  "enchantArmorWeaponModifier": 0.4,
  "maxEnchantLevelPickaxe": 6,
  "maxEnchantLevelSword": 7,
  "maxEnchantLevelAxe": 7,
  "maxEnchantLevelShovel": 5,
  "maxEnchantLevelHoe": 5,
  "maxEnchantLevelArmor": 6
}
```

- `enchantUpgradeBaseUses` - Uses required for first enchantment level
- `enchantUpgradeModifier` - Multiplier for each subsequent level
- `enchantArmorWeaponModifier` - Multiplier for weapons/armor (0.4 = 60% fewer uses required)
- `maxEnchantLevel[Type]` - Maximum enchantment level for each item type

**Formula for tools:** `usesRequired = baseUses × modifier^(level-1)`

**Formula for weapons/armor:** `usesRequired = baseUses × armorWeaponModifier × modifier^(level-1)`

**Example progression (tools, default settings):**
- Level 1: 150 uses
- Level 2: 270 uses (150 × 1.8)
- Level 3: 486 uses (150 × 1.8²)
- Level 4: 874 uses (150 × 1.8³)
- Level 5: 1574 uses (150 × 1.8⁴)
- Level 6: 2834 uses (150 × 1.8⁵)

**Example progression (weapons/armor, default settings):**
- Level 1: 60 uses (150 × 0.4)
- Level 2: 108 uses (150 × 0.4 × 1.2)
- Level 3: 194 uses (150 × 0.4 × 1.2²)
- Level 4: 350 uses (150 × 0.4 × 1.2³)

### Durability Upgrades

```json
{
  "durabilityUpgradeBaseUses": 20,
  "durabilityUpgradeModifier": 1.003,
  "durabilityArmorWeaponModifier": 0.4,
  "maxDurabilityLevel": 1200,
  "miningLevelIronThreshold": 120,
  "miningLevelDiamondThreshold": 500
}
```

- `durabilityUpgradeBaseUses` - Uses required for first durability upgrade (tools only)
- `durabilityUpgradeModifier` - Multiplier for each subsequent upgrade (very gradual scaling)
- `durabilityArmorWeaponModifier` - Multiplier for weapons/armor (0.4 = 60% fewer uses required)
- `maxDurabilityLevel` - Maximum number of durability upgrades
- `miningLevelIronThreshold` - Durability level needed for iron-tier mining (pickaxes only)
- `miningLevelDiamondThreshold` - Durability level needed for diamond-tier mining (pickaxes only)

**Formula for tools:** `usesRequired = baseUses × modifier^(level-1)`

**Formula for weapons/armor:** `usesRequired = baseUses × armorWeaponModifier × modifier^(level-1)`

## Installation

### Requirements
- Fabric Loader (0.18.4+)
- Fabric API
- Minecraft 1.20.1
- Java 17+

### Optional Dependencies
- Mod Menu (for in-game config GUI)
- Cloth Config (required for Mod Menu GUI)

## Compatibility

**Server-side mod.** Clients do not need it installed to join servers running this mod. The mod only affects server logic—durability regeneration and upgrades happen server-side, so vanilla clients work perfectly.

## Planned Features

- Configurable enchantments per item type
- Cross-version support (1.21.1, 1.19.2, 1.18.2)

## License

MIT License - See [LICENSE](LICENSE) for details.

## Credits
Created by Neuromuser
