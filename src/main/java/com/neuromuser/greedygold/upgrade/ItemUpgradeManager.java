package com.neuromuser.greedygold.upgrade;

import com.neuromuser.greedygold.config.ConfigValues;
import com.neuromuser.greedygold.config.ModConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.Random;

public class ItemUpgradeManager {
    private static final String NBT_ENCHANT_LEVEL = "GreedyGoldEnchantLevel";
    private static final String NBT_ENCHANT_USES = "GreedyGoldEnchantUses";
    private static final String NBT_DURABILITY_LEVEL = "GreedyGoldDurabilityLevel";
    private static final String NBT_DURABILITY_USES = "GreedyGoldDurabilityUses";
    private static final String NBT_AFFINITY = "GreedyGoldAffinity";
    private static final Random RANDOM = new Random();

    private static NbtCompound getNbt(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
    }

    private static void setNbt(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public static ItemUpgradeData getData(ItemStack stack) {
        NbtCompound nbt = getNbt(stack);
        int enchantLevel = nbt.getInt(NBT_ENCHANT_LEVEL);
        int enchantUses = nbt.getInt(NBT_ENCHANT_USES);
        int durabilityLevel = nbt.getInt(NBT_DURABILITY_LEVEL);
        int durabilityUses = nbt.getInt(NBT_DURABILITY_USES);

        ItemUpgradeData data = new ItemUpgradeData(enchantLevel, enchantUses, durabilityLevel, durabilityUses);

        if (!nbt.contains(NBT_ENCHANT_LEVEL)) {
            initializeFromExistingEnchants(stack, data);
            initializeAffinity(stack);
            saveData(stack, data);
        }

        return data;
    }

    public static void saveData(ItemStack stack, ItemUpgradeData data) {
        NbtCompound nbt = getNbt(stack);
        nbt.putInt(NBT_ENCHANT_LEVEL, data.getEnchantLevel());
        nbt.putInt(NBT_ENCHANT_USES, data.getEnchantUses());
        nbt.putInt(NBT_DURABILITY_LEVEL, data.getDurabilityLevel());
        nbt.putInt(NBT_DURABILITY_USES, data.getDurabilityUses());
        setNbt(stack, nbt);
    }

    private static void initializeFromExistingEnchants(ItemStack stack, ItemUpgradeData data) {
        RegistryKey<Enchantment> primaryEnchantKey = getPrimaryEnchantment(stack.getItem());
        if (primaryEnchantKey != null) {
            ItemEnchantmentsComponent enchantments = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

            // Find matching enchantment and get its level
            for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : enchantments.getEnchantmentEntries()) {
                if (entry.getKey().matchesKey(primaryEnchantKey)) {
                    data.setEnchantLevel(entry.getValue());
                    break;
                }
            }
        }
    }

    private static void initializeAffinity(ItemStack stack) {
        ConfigValues config = ModConfig.getInstance().getValues();
        if (!config.useRandomAffinity) return;

        NbtCompound nbt = getNbt(stack);
        if (!nbt.contains(NBT_AFFINITY)) {
            double affinity = config.minAffinity +
                    (config.maxAffinity - config.minAffinity) * RANDOM.nextDouble();
            nbt.putDouble(NBT_AFFINITY, affinity);
            setNbt(stack, nbt);
        }
    }

    public static double getAffinity(ItemStack stack) {
        ConfigValues config = ModConfig.getInstance().getValues();
        if (!config.useRandomAffinity) return 1.0;

        NbtCompound nbt = getNbt(stack);
        if (!nbt.contains(NBT_AFFINITY)) {
            initializeAffinity(stack);
            nbt = getNbt(stack);
        }
        return nbt.getDouble(NBT_AFFINITY);
    }

    public static void onItemUsed(ItemStack stack, ServerPlayerEntity player) {
        ConfigValues config = ModConfig.getInstance().getValues();
        if (!config.upgradesEnabled) return;

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
                    stack.getItem() instanceof ArmorItem) {
                requiredUses = (int) (config.getArmorWeaponUsesForDurabilityLevel(nextLevel) / affinity);
            } else {
                requiredUses = (int) (config.getUsesForDurabilityLevel(nextLevel) / affinity);
            }

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
        RegistryKey<Enchantment> enchantKey = getPrimaryEnchantment(stack.getItem());
        if (enchantKey == null) return;

        int newLevel = data.getEnchantLevel() + 1;

        // Get current enchantments
        ItemEnchantmentsComponent currentEnchantments = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(currentEnchantments);

        // Find and update the primary enchantment
        player.getWorld().getRegistryManager().get(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .getEntry(enchantKey)
                .ifPresent(entry -> builder.set(entry, newLevel));

        // Set the new enchantments
        stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());

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

        int iron = ModConfig.getInstance().getValues().miningLevelIronThreshold;
        int diamond = ModConfig.getInstance().getValues().miningLevelDiamondThreshold;
        if (stack.getItem() instanceof PickaxeItem) {
            if (newLevel == iron || newLevel == diamond) {
                player.sendMessage(Text.translatable("upgrade.greedy-gold.mining_level", stack.getName())
                        .formatted(Formatting.AQUA), true);
            }
        }

        NbtCompound nbt = getNbt(stack);
        int currentBonus = nbt.getInt("GreedyGoldMaxDamageBonus");
        nbt.putInt("GreedyGoldMaxDamageBonus", currentBonus + 1);
        setNbt(stack, nbt);

        player.getWorld().playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ANVIL_USE,
                SoundCategory.PLAYERS, 0.15f, 1.8f
        );
    }

    @SuppressWarnings("unused")
    private static void playUpgradeEffects(ServerPlayerEntity player) {
    }

    public static RegistryKey<Enchantment> getPrimaryEnchantment(Item item) {
        if (item instanceof PickaxeItem) return Enchantments.EFFICIENCY;
        if (item instanceof SwordItem) return Enchantments.SHARPNESS;
        if (item instanceof AxeItem) return Enchantments.SHARPNESS;
        if (item instanceof ShovelItem) return Enchantments.EFFICIENCY;
        if (item instanceof HoeItem) return Enchantments.FORTUNE;
        if (item instanceof ArmorItem) return Enchantments.PROTECTION;
        return null;
    }

    public static int getMaxEnchantLevel(Item item) {
        ConfigValues config = ModConfig.getInstance().getValues();
        if (item instanceof PickaxeItem) return config.maxEnchantLevelPickaxe;
        if (item instanceof SwordItem) return config.maxEnchantLevelSword;
        if (item instanceof AxeItem) return config.maxEnchantLevelAxe;
        if (item instanceof ShovelItem) return config.maxEnchantLevelShovel;
        if (item instanceof HoeItem) return config.maxEnchantLevelHoe;
        if (item instanceof ArmorItem) return config.maxEnchantLevelArmor;
        return 0;
    }

    public static int getUsesUntilNextEnchantUpgrade(ItemStack stack) {
        ConfigValues config = ModConfig.getInstance().getValues();
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
}