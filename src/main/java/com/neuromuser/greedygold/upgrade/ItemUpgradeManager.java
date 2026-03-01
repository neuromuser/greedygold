package com.neuromuser.greedygold.upgrade;

import com.neuromuser.greedygold.config.ClientCache;
import com.neuromuser.greedygold.config.ConfigValues;
import com.neuromuser.greedygold.config.ModConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Optional;
import java.util.Random;

public class ItemUpgradeManager {
    private static final String NBT_ENCHANT_LEVEL = "GreedyGoldEnchantLevel";
    private static final String NBT_ENCHANT_USES = "GreedyGoldEnchantUses";
    private static final String NBT_DURABILITY_LEVEL = "GreedyGoldDurabilityLevel";
    private static final String NBT_DURABILITY_USES = "GreedyGoldDurabilityUses";
    private static final String NBT_AFFINITY = "GreedyGoldAffinity";
    private static final Random RANDOM = new Random();

    public static ItemUpgradeData getData(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();

        int enchantLevel = nbt.getInt(NBT_ENCHANT_LEVEL);
        int enchantUses = nbt.getInt(NBT_ENCHANT_USES);
        int durabilityLevel = nbt.getInt(NBT_DURABILITY_LEVEL);
        int durabilityUses = nbt.getInt(NBT_DURABILITY_USES);

        ItemUpgradeData data = new ItemUpgradeData(enchantLevel, enchantUses, durabilityLevel, durabilityUses);

        if (!nbt.contains(NBT_ENCHANT_LEVEL)) {
            initializeFromExistingEnchants(stack, data);
            initializeAffinity(stack);
            saveData(stack, data);
        } else {
            syncEnchantLevel(stack, data);
        }

        return data;
    }

    public static void saveData(ItemStack stack, ItemUpgradeData data) {
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbtComponent ->
                nbtComponent.apply(nbt -> {
                    nbt.putInt(NBT_ENCHANT_LEVEL, data.getEnchantLevel());
                    nbt.putInt(NBT_ENCHANT_USES, data.getEnchantUses());
                    nbt.putInt(NBT_DURABILITY_LEVEL, data.getDurabilityLevel());
                    nbt.putInt(NBT_DURABILITY_USES, data.getDurabilityUses());
                })
        );
    }

    private static void initializeFromExistingEnchants(ItemStack stack, ItemUpgradeData data) {
        getPrimaryEnchantmentKey(stack.getItem()).ifPresent(key -> {
            ItemEnchantmentsComponent enchants = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            boolean foundPrimary = false;
            for (RegistryEntry<Enchantment> entry : enchants.getEnchantments()) {
                if (entry.matchesKey(key)) {
                    data.setEnchantLevel(enchants.getLevel(entry));
                    foundPrimary = true;
                    break;
                }
            }
            if (!foundPrimary && hasConflictingEnchantment(stack, key)) {
                data.setEnchantLevel(getMaxEnchantLevel(stack.getItem()));
            }
        });
    }

    private static void syncEnchantLevel(ItemStack stack, ItemUpgradeData data) {
        getPrimaryEnchantmentKey(stack.getItem()).ifPresent(key -> {
            ItemEnchantmentsComponent enchants = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            boolean foundPrimary = false;
            boolean changed = false;
            for (RegistryEntry<Enchantment> entry : enchants.getEnchantments()) {
                if (entry.matchesKey(key)) {
                    int actualLevel = enchants.getLevel(entry);
                    if (actualLevel > data.getEnchantLevel()) {
                        data.setEnchantLevel(actualLevel);
                        data.setEnchantUses(0);
                        changed = true;
                    }
                    foundPrimary = true;
                    break;
                }
            }
            if (!foundPrimary && hasConflictingEnchantment(stack, key)) {
                int maxLevel = getMaxEnchantLevel(stack.getItem());
                if (data.getEnchantLevel() < maxLevel) {
                    data.setEnchantLevel(maxLevel);
                    data.setEnchantUses(0);
                    changed = true;
                }
            }
            if (changed) saveData(stack, data);
        });
    }

    private static boolean hasConflictingEnchantment(ItemStack stack, RegistryKey<Enchantment> primaryKey) {
        if (!primaryKey.equals(Enchantments.PROTECTION)) return false;
        ItemEnchantmentsComponent enchants = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (RegistryEntry<Enchantment> entry : enchants.getEnchantments()) {
            if (entry.matchesKey(Enchantments.BLAST_PROTECTION)
                    || entry.matchesKey(Enchantments.FIRE_PROTECTION)
                    || entry.matchesKey(Enchantments.PROJECTILE_PROTECTION)) {
                return true;
            }
        }
        return false;
    }

    private static void initializeAffinity(ItemStack stack) {
        ConfigValues config = getConfig();
        if (!config.useRandomAffinity) return;

        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbtComponent ->
                nbtComponent.apply(nbt -> {
                    if (!nbt.contains(NBT_AFFINITY)) {
                        double affinity = config.minAffinity + (config.maxAffinity - config.minAffinity) * RANDOM.nextDouble();
                        nbt.putDouble(NBT_AFFINITY, affinity);
                    }
                })
        );
    }

    public static double getAffinity(ItemStack stack) {
        ConfigValues config = getConfig();
        if (!config.useRandomAffinity) return 1.0;

        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        if (nbt == null || !nbt.contains(NBT_AFFINITY)) {
            initializeAffinity(stack);
            nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        }
        return (nbt != null && nbt.contains(NBT_AFFINITY)) ? nbt.getDouble(NBT_AFFINITY) : 1.0;
    }

    public static void onItemUsed(ItemStack stack, ServerPlayerEntity player) {
        ConfigValues config = getConfig();
        if (!config.upgradesEnabled || !isGoldenItem(stack)) return;

        ItemUpgradeData data = getData(stack);
        double affinity = getAffinity(stack);

        data.incrementEnchantUses();
        data.incrementDurabilityUses();

        boolean upgraded = false;
        int maxEnchantLevel = getMaxEnchantLevel(stack.getItem());

        if (data.getEnchantLevel() < maxEnchantLevel) {
            int nextLevel = data.getEnchantLevel() + 1;
            int requiredUses = isCombatItem(stack.getItem())
                    ? (int) (config.getArmorWeaponUsesForEnchantLevel(nextLevel) / affinity)
                    : (int) (config.getUsesForEnchantLevel(nextLevel) / affinity);

            if (data.getEnchantUses() >= requiredUses) {
                upgradeEnchantment(stack, data, player);
                data.setEnchantUses(0);
                upgraded = true;
            }
        }

        if (data.getDurabilityLevel() < config.maxDurabilityLevel) {
            int nextLevel = data.getDurabilityLevel() + 1;
            int requiredUses = isCombatItem(stack.getItem())
                    ? (int) (config.getArmorWeaponUsesForDurabilityLevel(nextLevel) / affinity)
                    : (int) (config.getUsesForDurabilityLevel(nextLevel) / affinity);

            if (data.getDurabilityUses() >= requiredUses) {
                upgradeDurability(stack, data, player);
                data.setDurabilityUses(0);
                upgraded = true;
            }
        }

        saveData(stack, data);
        if (upgraded) playUpgradeEffects(player);
    }

    private static void upgradeEnchantment(ItemStack stack, ItemUpgradeData data, ServerPlayerEntity player) {
        getPrimaryEnchantmentKey(stack.getItem()).ifPresent(key -> {
            ItemEnchantmentsComponent currentEnchants = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            int actualCurrentLevel = 0;
            for (RegistryEntry<Enchantment> entry : currentEnchants.getEnchantments()) {
                if (entry.matchesKey(key)) {
                    actualCurrentLevel = currentEnchants.getLevel(entry);
                    break;
                }
            }
            int newLevel = actualCurrentLevel + 1;

            var registry = player.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT);
            Optional<RegistryEntry.Reference<Enchantment>> enchantEntry = registry.getEntry(key);

            enchantEntry.ifPresent(entry -> {
                stack.apply(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT, component -> {
                    ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(component);
                    builder.set(entry, newLevel);
                    return builder.build();
                });

                data.setEnchantLevel(newLevel);
                player.sendMessage(Text.literal("✦ ").formatted(Formatting.GOLD)
                        .append(Text.translatable("upgrade.greedy-gold.enchant", stack.getName(), newLevel).formatted(Formatting.YELLOW)), true);

                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5f, 1.5f);
            });
        });
    }

    private static void upgradeDurability(ItemStack stack, ItemUpgradeData data, ServerPlayerEntity player) {
        int newLevel = data.getDurabilityLevel() + 1;
        data.setDurabilityLevel(newLevel);
        ConfigValues config = getConfig();

        if (stack.getItem() instanceof PickaxeItem && (newLevel == config.miningLevelIronThreshold || newLevel == config.miningLevelDiamondThreshold)) {
            player.sendMessage(Text.translatable("upgrade.greedy-gold.mining_level", stack.getName()).formatted(Formatting.AQUA), true);
        }

        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbtComponent ->
                nbtComponent.apply(nbt -> {
                    int currentBonus = nbt.getInt("GreedyGoldMaxDamageBonus");
                    nbt.putInt("GreedyGoldMaxDamageBonus", currentBonus + 1);
                })
        );

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 0.15f, 1.8f);
    }

    public static int getUsesUntilNextEnchantUpgrade(ItemStack stack) {
        ConfigValues config = getConfig();
        ItemUpgradeData data = getData(stack);

        int maxLevel = getMaxEnchantLevel(stack.getItem());
        if (data.getEnchantLevel() >= maxLevel) return -1;

        int nextLevel = data.getEnchantLevel() + 1;
        double affinity = getAffinity(stack);

        int requiredUses = isCombatItem(stack.getItem())
                ? (int) (config.getArmorWeaponUsesForEnchantLevel(nextLevel) / affinity)
                : (int) (config.getUsesForEnchantLevel(nextLevel) / affinity);

        return Math.max(0, requiredUses - data.getEnchantUses());
    }

    private static void playUpgradeEffects(ServerPlayerEntity player) {}

    public static Optional<RegistryKey<Enchantment>> getPrimaryEnchantmentKey(Item item) {
        if (item instanceof PickaxeItem || item instanceof ShovelItem) return Optional.of(Enchantments.EFFICIENCY);
        if (item instanceof SwordItem || item instanceof AxeItem) return Optional.of(Enchantments.SHARPNESS);
        if (item instanceof HoeItem) return Optional.of(Enchantments.FORTUNE);
        if (item instanceof ArmorItem) return Optional.of(Enchantments.PROTECTION);
        return Optional.empty();
    }

    public static int getMaxEnchantLevel(Item item) {
        ConfigValues config = getConfig();
        if (item instanceof PickaxeItem) return config.maxEnchantLevelPickaxe;
        if (item instanceof SwordItem) return config.maxEnchantLevelSword;
        if (item instanceof AxeItem) return config.maxEnchantLevelAxe;
        if (item instanceof ShovelItem) return config.maxEnchantLevelShovel;
        if (item instanceof HoeItem) return config.maxEnchantLevelHoe;
        if (item instanceof ArmorItem) return config.maxEnchantLevelArmor;
        return 0;
    }

    private static boolean isGoldenItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ToolItem toolItem) {
            return toolItem.getMaterial() == ToolMaterials.GOLD;
        }
        if (item instanceof ArmorItem armorItem) {
            return armorItem.getMaterial().value() == ArmorMaterials.GOLD.value();
        }
        return false;
    }

    private static boolean isCombatItem(Item item) {
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof ArmorItem;
    }

    private static ConfigValues getConfig() {
        return (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
                ? ClientCache.get() : ModConfig.getInstance().getValues();
    }
}