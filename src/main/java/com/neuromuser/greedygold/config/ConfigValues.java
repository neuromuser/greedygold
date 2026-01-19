package com.neuromuser.greedygold.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigValues {
    private static final Logger LOGGER = LoggerFactory.getLogger("GreedyGold/ConfigValues");

    public boolean enabled = true;
    public int regenIntervalSeconds = 10;
    public int regenAmount = 1;
    public boolean usePercentage = true;
    public double regenPercentage = 0.01;

    public boolean upgradesEnabled = true;
    public boolean showUpgradeTooltip = true;

    public int enchantUpgradeBaseUses = 150;
    public double enchantUpgradeModifier = 1.8;
    public int maxEnchantLevel = 6;

    public int durabilityUpgradeBaseUses = 25;
    public double durabilityUpgradeModifier = 1.002;
    public int maxDurabilityLevel = 1200;
    public int miningLevelIronThreshold = 200;
    public int miningLevelDiamondThreshold = 550;

    public boolean useRandomAffinity = true;
    public double minAffinity = 0.8;
    public double maxAffinity = 1.2;

    public void validate() {
        boolean changed = false;

        if (regenIntervalSeconds < 1) {
            LOGGER.warn("regenIntervalSeconds was {}, clamping to 1", regenIntervalSeconds);
            regenIntervalSeconds = 1;
            changed = true;
        }

        if (regenAmount < 1) {
            LOGGER.warn("regenAmount was {}, clamping to 1", regenAmount);
            regenAmount = 1;
            changed = true;
        }

        if (regenPercentage < 0.0 || regenPercentage > 1.0) {
            LOGGER.warn("regenPercentage was {}, clamping to 0.01", regenPercentage);
            regenPercentage = 0.01;
            changed = true;
        }

        if (enchantUpgradeBaseUses < 1) {
            LOGGER.warn("enchantUpgradeBaseUses was {}, clamping to 1", enchantUpgradeBaseUses);
            enchantUpgradeBaseUses = 1;
            changed = true;
        }

        if (enchantUpgradeModifier < 1.0) {
            LOGGER.warn("enchantUpgradeModifier was {}, clamping to 1.0", enchantUpgradeModifier);
            enchantUpgradeModifier = 1.0;
            changed = true;
        }

        if (maxEnchantLevel < 1) {
            LOGGER.warn("maxEnchantLevel was {}, clamping to 1", maxEnchantLevel);
            maxEnchantLevel = 1;
            changed = true;
        }

        if (durabilityUpgradeBaseUses < 1) {
            LOGGER.warn("durabilityUpgradeBaseUses was {}, clamping to 1", durabilityUpgradeBaseUses);
            durabilityUpgradeBaseUses = 1;
            changed = true;
        }

        if (durabilityUpgradeModifier < 1.0) {
            LOGGER.warn("durabilityUpgradeModifier was {}, clamping to 1.0", durabilityUpgradeModifier);
            durabilityUpgradeModifier = 1.0;
            changed = true;
        }

        if (maxDurabilityLevel < 1) {
            LOGGER.warn("maxDurabilityLevel was {}, clamping to 1", maxDurabilityLevel);
            maxDurabilityLevel = 1;
            changed = true;
        }

        if (minAffinity < 0.1 || minAffinity > 10.0) {
            LOGGER.warn("minAffinity was {}, clamping to 0.8", minAffinity);
            minAffinity = 0.8;
            changed = true;
        }

        if (maxAffinity < 0.1 || maxAffinity > 10.0) {
            LOGGER.warn("maxAffinity was {}, clamping to 1.2", maxAffinity);
            maxAffinity = 1.2;
            changed = true;
        }

        if (minAffinity > maxAffinity) {
            LOGGER.warn("minAffinity ({}) > maxAffinity ({}), swapping", minAffinity, maxAffinity);
            double temp = minAffinity;
            minAffinity = maxAffinity;
            maxAffinity = temp;
            changed = true;
        }

        if (changed) {
            LOGGER.info("Some config values were invalid and have been corrected");
        }
    }

    public int getRegenIntervalTicks() {
        return regenIntervalSeconds * 20;
    }

    public int getRegenAmount(int maxDurability) {
        if (usePercentage) {
            int percentageAmount = (int) (maxDurability * regenPercentage);
            return Math.max(1, percentageAmount);
        }
        return regenAmount;
    }

    public int getUsesForEnchantLevel(int level) {
        if (level <= 0) return 0;
        return (int) (enchantUpgradeBaseUses * Math.pow(enchantUpgradeModifier, level - 1));
    }

    public int getUsesForDurabilityLevel(int level) {
        if (level <= 0) return 0;
        return (int) (durabilityUpgradeBaseUses * Math.pow(durabilityUpgradeModifier, level - 1));
    }
}