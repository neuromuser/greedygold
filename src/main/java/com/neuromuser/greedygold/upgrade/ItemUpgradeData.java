package com.neuromuser.greedygold.upgrade;

public class ItemUpgradeData {
    private int enchantLevel;
    private int enchantUses;
    private int durabilityLevel;
    private int durabilityUses;

    public ItemUpgradeData() {
        this(0, 0, 0, 0);
    }

    public ItemUpgradeData(int enchantLevel, int enchantUses, int durabilityLevel, int durabilityUses) {
        this.enchantLevel = enchantLevel;
        this.enchantUses = enchantUses;
        this.durabilityLevel = durabilityLevel;
        this.durabilityUses = durabilityUses;
    }

    public int getEnchantLevel() {
        return enchantLevel;
    }

    public void setEnchantLevel(int level) {
        this.enchantLevel = level;
    }

    public int getEnchantUses() {
        return enchantUses;
    }

    public void setEnchantUses(int uses) {
        this.enchantUses = uses;
    }

    public void incrementEnchantUses() {
        this.enchantUses++;
    }

    public int getDurabilityLevel() {
        return durabilityLevel;
    }

    public void setDurabilityLevel(int level) {
        this.durabilityLevel = level;
    }

    public int getDurabilityUses() {
        return durabilityUses;
    }

    public void setDurabilityUses(int uses) {
        this.durabilityUses = uses;
    }

    public void incrementDurabilityUses() {
        this.durabilityUses++;
    }
}