package com.neuromuser.greedygold.network;

import com.neuromuser.greedygold.config.ConfigValues;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ConfigSync {
    public static final Identifier ID = new Identifier("greedy-gold", "config");

    public static PacketByteBuf encode(ConfigValues c) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(c.enabled);
        buf.writeInt(c.regenIntervalSeconds);
        buf.writeInt(c.regenAmount);
        buf.writeBoolean(c.usePercentage);
        buf.writeDouble(c.regenPercentage);
        buf.writeBoolean(c.useSeparateToolRegen);
        buf.writeInt(c.toolRegenIntervalSeconds);
        buf.writeDouble(c.toolRegenPercentage);
        buf.writeBoolean(c.upgradesEnabled);
        buf.writeBoolean(c.showUpgradeTooltip);
        buf.writeBoolean(c.useRandomAffinity);
        buf.writeDouble(c.minAffinity);
        buf.writeDouble(c.maxAffinity);
        buf.writeInt(c.enchantUpgradeBaseUses);
        buf.writeDouble(c.enchantUpgradeModifier);
        buf.writeDouble(c.enchantArmorWeaponModifier);
        buf.writeInt(c.maxEnchantLevelPickaxe);
        buf.writeInt(c.maxEnchantLevelSword);
        buf.writeInt(c.maxEnchantLevelAxe);
        buf.writeInt(c.maxEnchantLevelShovel);
        buf.writeInt(c.maxEnchantLevelHoe);
        buf.writeInt(c.maxEnchantLevelArmor);
        buf.writeInt(c.durabilityUpgradeBaseUses);
        buf.writeDouble(c.durabilityUpgradeModifier);
        buf.writeDouble(c.durabilityArmorWeaponModifier);
        buf.writeInt(c.maxDurabilityLevel);
        buf.writeInt(c.miningLevelIronThreshold);
        buf.writeInt(c.miningLevelDiamondThreshold);

        return buf;
    }

    public static ConfigValues decode(PacketByteBuf buf) {
        ConfigValues c = new ConfigValues();
        // Regeneration settings
        c.enabled = buf.readBoolean();
        c.regenIntervalSeconds = buf.readInt();
        c.regenAmount = buf.readInt();
        c.usePercentage = buf.readBoolean();
        c.regenPercentage = buf.readDouble();
        c.useSeparateToolRegen = buf.readBoolean();
        c.toolRegenIntervalSeconds = buf.readInt();
        c.toolRegenPercentage = buf.readDouble();
        c.upgradesEnabled = buf.readBoolean();
        c.showUpgradeTooltip = buf.readBoolean();
        c.useRandomAffinity = buf.readBoolean();
        c.minAffinity = buf.readDouble();
        c.maxAffinity = buf.readDouble();
        c.enchantUpgradeBaseUses = buf.readInt();
        c.enchantUpgradeModifier = buf.readDouble();
        c.enchantArmorWeaponModifier = buf.readDouble();
        c.maxEnchantLevelPickaxe = buf.readInt();
        c.maxEnchantLevelSword = buf.readInt();
        c.maxEnchantLevelAxe = buf.readInt();
        c.maxEnchantLevelShovel = buf.readInt();
        c.maxEnchantLevelHoe = buf.readInt();
        c.maxEnchantLevelArmor = buf.readInt();
        c.durabilityUpgradeBaseUses = buf.readInt();
        c.durabilityUpgradeModifier = buf.readDouble();
        c.durabilityArmorWeaponModifier = buf.readDouble();
        c.maxDurabilityLevel = buf.readInt();
        c.miningLevelIronThreshold = buf.readInt();
        c.miningLevelDiamondThreshold = buf.readInt();

        return c;
    }
}