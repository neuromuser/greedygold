package com.neuromuser.greedygold.upgrade;

import com.neuromuser.greedygold.config.ClientCache;
import com.neuromuser.greedygold.config.ConfigValues;
import com.neuromuser.greedygold.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ItemUpgradeManager {
    private static final String NBT_ENCHANT_LEVEL = "GreedyGoldEnchantLevel";
    private static final String NBT_ENCHANT_USES = "GreedyGoldEnchantUses";
    private static final String NBT_DURABILITY_LEVEL = "GreedyGoldDurabilityLevel";
    private static final String NBT_DURABILITY_USES = "GreedyGoldDurabilityUses";
    private static final String NBT_AFFINITY = "GreedyGoldAffinity";
    private static final Random RANDOM = new Random();

    public static ItemUpgradeData getData(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
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
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(NBT_ENCHANT_LEVEL, data.getEnchantLevel());
        nbt.putInt(NBT_ENCHANT_USES, data.getEnchantUses());
        nbt.putInt(NBT_DURABILITY_LEVEL, data.getDurabilityLevel());
        nbt.putInt(NBT_DURABILITY_USES, data.getDurabilityUses());
    }

    private static void initializeFromExistingEnchants(ItemStack stack, ItemUpgradeData data) {
        Enchantment primaryEnchant = getPrimaryEnchantment(stack.getItem());
        if (primaryEnchant == null) return;

        Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
        if (enchants.containsKey(primaryEnchant)) {
            data.setEnchantLevel(enchants.get(primaryEnchant));
        } else if (hasConflictingEnchantment(enchants, primaryEnchant)) {
            data.setEnchantLevel(getMaxEnchantLevel(stack.getItem()));
        }
    }

    private static void syncEnchantLevel(ItemStack stack, ItemUpgradeData data) {
        Enchantment primaryEnchant = getPrimaryEnchantment(stack.getItem());
        if (primaryEnchant == null) return;

        Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
        boolean changed = false;

        if (enchants.containsKey(primaryEnchant)) {
            int actualLevel = enchants.get(primaryEnchant);
            if (actualLevel > data.getEnchantLevel()) {
                data.setEnchantLevel(actualLevel);
                data.setEnchantUses(0);
                changed = true;
            }
        } else if (hasConflictingEnchantment(enchants, primaryEnchant)) {
            int maxLevel = getMaxEnchantLevel(stack.getItem());
            if (data.getEnchantLevel() < maxLevel) {
                data.setEnchantLevel(maxLevel);
                data.setEnchantUses(0);
                changed = true;
            }
        }

        if (changed) saveData(stack, data);
    }

    private static boolean hasConflictingEnchantment(Map<Enchantment, Integer> enchants, Enchantment primary) {
        if (primary != Enchantments.PROTECTION) return false;
        return enchants.containsKey(Enchantments.BLAST_PROTECTION)
                || enchants.containsKey(Enchantments.FIRE_PROTECTION)
                || enchants.containsKey(Enchantments.PROJECTILE_PROTECTION);
    }

    private static void initializeAffinity(ItemStack stack) {
        ConfigValues config;
        if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            config = ClientCache.get();
        } else {
            config = ModConfig.getInstance().getValues();
        }
        if (!config.useRandomAffinity) return;

        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains(NBT_AFFINITY)) {
            double affinity = config.minAffinity +
                    (config.maxAffinity - config.minAffinity) * RANDOM.nextDouble();
            nbt.putDouble(NBT_AFFINITY, affinity);
        }
    }

    public static double getAffinity(ItemStack stack) {
        ConfigValues config;
        if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            config = ClientCache.get();
        } else {
            config = ModConfig.getInstance().getValues();
        }
        if (!config.useRandomAffinity) return 1.0;

        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains(NBT_AFFINITY)) {
            initializeAffinity(stack);
        }
        return nbt.getDouble(NBT_AFFINITY);
    }

    public static void onItemUsed(ItemStack stack, ServerPlayerEntity player) {
        ConfigValues config;
        if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            config = ClientCache.get();
        } else {
            config = ModConfig.getInstance().getValues();
        }
        if (!config.upgradesEnabled) return;
        if (!isGoldenItem(stack)) return;

        ItemUpgradeData data = getData(stack);
        double affinity = getAffinity(stack);

        data.incrementEnchantUses();
        data.incrementDurabilityUses();

        boolean upgraded = false;

        int maxEnchantLevel = getMaxEnchantLevel(stack.getItem());
        if (data.getEnchantLevel() < maxEnchantLevel) {
            int nextLevel = data.getEnchantLevel() + 1;
            int requiredUses;
            if (stack.getItem() instanceof SwordItem ||
                    stack.getItem() instanceof AxeItem ||
                    stack.getItem() instanceof ArmorItem) {
                requiredUses = (int) (config.getArmorWeaponUsesForEnchantLevel(nextLevel) / affinity);
            } else {
                requiredUses = (int) (config.getUsesForEnchantLevel(nextLevel) / affinity);
            }

            if (data.getEnchantUses() >= requiredUses) {
                upgradeEnchantment(stack, data, player);
                data.setEnchantUses(0);
                upgraded = true;
            }
        }

        if (data.getDurabilityLevel() < config.maxDurabilityLevel) {
            int nextLevel = data.getDurabilityLevel() + 1;
            int requiredUses;
            if (stack.getItem() instanceof SwordItem ||
                    stack.getItem() instanceof AxeItem ||
                    stack.getItem() instanceof ArmorItem)
            {requiredUses = (int) (config.getArmorWeaponUsesForDurabilityLevel(nextLevel) / affinity);}
            else
                requiredUses = (int) (config.getUsesForDurabilityLevel(nextLevel) / affinity);


            if (data.getDurabilityUses() >= requiredUses) {
                upgradeDurability(stack, data, player);
                data.setDurabilityUses(0);
                upgraded = true;
            }
        }

        saveData(stack, data);

        if (upgraded) {
            playUpgradeEffects(player);
        }
    }

    private static void upgradeEnchantment(ItemStack stack, ItemUpgradeData data, ServerPlayerEntity player) {
        Enchantment enchant = getPrimaryEnchantment(stack.getItem());
        if (enchant == null) return;

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
        int actualCurrentLevel = enchantments.getOrDefault(enchant, 0);
        int newLevel = actualCurrentLevel + 1;

        Map<Enchantment, Integer> updated = new HashMap<>(enchantments);
        updated.put(enchant, newLevel);
        EnchantmentHelper.set(updated, stack);

        data.setEnchantLevel(newLevel);

        player.sendMessage(
                Text.literal("✦ ").formatted(Formatting.GOLD)
                        .append(Text.translatable("upgrade.greedy-gold.enchant",
                                stack.getName(), newLevel).formatted(Formatting.YELLOW)),
                true
        );

        player.getWorld().playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP,
                SoundCategory.PLAYERS, 0.5f, 1.5f
        );
    }

    private static void upgradeDurability(ItemStack stack, ItemUpgradeData data, ServerPlayerEntity player) {
        int newLevel = data.getDurabilityLevel() + 1;
        data.setDurabilityLevel(newLevel);
        ConfigValues config;
        if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            config = ClientCache.get();
        } else {
            config = ModConfig.getInstance().getValues();
        }
        int iron = config.miningLevelIronThreshold;
        int diamond = config.miningLevelDiamondThreshold;
        if (stack.getItem() instanceof PickaxeItem){
            if (newLevel == iron || newLevel == diamond) {
                player.sendMessage(Text.translatable("upgrade.greedy-gold.mining_level", stack.getName())
                        .formatted(Formatting.AQUA), true);
            }
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        int currentBonus = nbt.getInt("GreedyGoldMaxDamageBonus");
        nbt.putInt("GreedyGoldMaxDamageBonus", currentBonus + 1);

        player.getWorld().playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ANVIL_USE,
                SoundCategory.PLAYERS, 0.15f, 1.8f
        );
    }

    private static void playUpgradeEffects(ServerPlayerEntity player) {
    }

    public static Enchantment getPrimaryEnchantment(Item item) {
        if (item instanceof PickaxeItem) return Enchantments.EFFICIENCY;
        if (item instanceof SwordItem) return Enchantments.SHARPNESS;
        if (item instanceof AxeItem) return Enchantments.SHARPNESS;
        if (item instanceof ShovelItem) return Enchantments.EFFICIENCY;
        if (item instanceof HoeItem) return Enchantments.FORTUNE;
        if (item instanceof ArmorItem) return Enchantments.PROTECTION;
        return null;
    }

    public static int getMaxEnchantLevel(Item item) {
        ConfigValues config;
        if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            config = ClientCache.get();
        } else {
            config = ModConfig.getInstance().getValues();
        }
        if (item instanceof PickaxeItem) return config.maxEnchantLevelPickaxe;
        if (item instanceof SwordItem) return config.maxEnchantLevelSword;
        if (item instanceof AxeItem) return config.maxEnchantLevelAxe;
        if (item instanceof ShovelItem) return config.maxEnchantLevelShovel;
        if (item instanceof HoeItem) return config.maxEnchantLevelHoe;
        if (item instanceof ArmorItem) return config.maxEnchantLevelArmor;
        return 0;
    }

    public static int getUsesUntilNextEnchantUpgrade(ItemStack stack) {
        ConfigValues config;
        if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            config = ClientCache.get();
        } else {
            config = ModConfig.getInstance().getValues();
        }
        ItemUpgradeData data = getData(stack);

        int maxLevel = getMaxEnchantLevel(stack.getItem());
        if (data.getEnchantLevel() >= maxLevel) {
            return -1;
        }

        int nextLevel = data.getEnchantLevel() + 1;
        double affinity = getAffinity(stack);
        int requiredUses;

        if (stack.getItem() instanceof SwordItem ||
                stack.getItem() instanceof AxeItem ||
                stack.getItem() instanceof ArmorItem) {
            requiredUses = (int) (config.getArmorWeaponUsesForEnchantLevel(nextLevel) / affinity);
        } else {
            requiredUses = (int) (config.getUsesForEnchantLevel(nextLevel) / affinity);
        }

        return Math.max(0, requiredUses - data.getEnchantUses());
    }

    private static boolean isGoldenItem(ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof ToolItem toolItem) {
            return toolItem.getMaterial() == ToolMaterials.GOLD;
        }

        if (item instanceof ArmorItem armorItem) {
            return armorItem.getMaterial() == ArmorMaterials.GOLD;
        }

        return false;
    }

}