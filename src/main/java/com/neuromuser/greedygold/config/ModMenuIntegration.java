package com.neuromuser.greedygold.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.greedy-gold.title"))
                .setSavingRunnable(() -> ModConfig.getInstance().save());

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigValues values = ModConfig.getInstance().getValues();

        ConfigCategory regeneration = builder.getOrCreateCategory(
                Text.translatable("config.greedy-gold.category.regeneration"));

        regeneration.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.greedy-gold.enabled"),
                        values.enabled)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.greedy-gold.enabled.tooltip"))
                .setSaveConsumer(newValue -> values.enabled = newValue)
                .build());

        regeneration.addEntry(entryBuilder.startIntField(
                        Text.translatable("config.greedy-gold.regenIntervalSeconds"),
                        values.regenIntervalSeconds)
                .setDefaultValue(20)
                .setMin(1)
                .setMax(86400)
                .setTooltip(Text.translatable("config.greedy-gold.regenIntervalSeconds.tooltip"))
                .setSaveConsumer(newValue -> values.regenIntervalSeconds = newValue)
                .build());

        regeneration.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.greedy-gold.usePercentage"),
                        values.usePercentage)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.greedy-gold.usePercentage.tooltip"))
                .setSaveConsumer(newValue -> values.usePercentage = newValue)
                .build());

        regeneration.addEntry(entryBuilder.startIntField(
                        Text.translatable("config.greedy-gold.regenAmount"),
                        values.regenAmount)
                .setDefaultValue(1)
                .setMin(1)
                .setMax(1000)
                .setTooltip(Text.translatable("config.greedy-gold.regenAmount.tooltip"))
                .setSaveConsumer(newValue -> values.regenAmount = newValue)
                .build());

        regeneration.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("config.greedy-gold.regenPercentage"),
                        (int) (values.regenPercentage * 100),
                        0, 100)
                .setDefaultValue(1)
                .setTooltip(Text.translatable("config.greedy-gold.regenPercentage.tooltip"))
                .setSaveConsumer(newValue -> values.regenPercentage = newValue / 100.0)
                .setTextGetter(value -> Text.literal(value + "%"))
                .build());

        regeneration.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.greedy-gold.useSeparateToolRegen"),
                        values.useSeparateToolRegen)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.greedy-gold.useSeparateToolRegen.tooltip"))
                .setSaveConsumer(newValue -> values.useSeparateToolRegen = newValue)
                .build());

        regeneration.addEntry(entryBuilder.startIntField(
                        Text.translatable("config.greedy-gold.toolRegenIntervalSeconds"),
                        values.toolRegenIntervalSeconds)
                .setDefaultValue(4)
                .setMin(1)
                .setMax(86400)
                .setTooltip(Text.translatable("config.greedy-gold.toolRegenIntervalSeconds.tooltip"))
                .setSaveConsumer(newValue -> values.toolRegenIntervalSeconds = newValue)
                .build());

        regeneration.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("config.greedy-gold.toolRegenPercentage"),
                        (int) (values.toolRegenPercentage * 100),
                        0, 100)
                .setDefaultValue(1)
                .setTooltip(Text.translatable("config.greedy-gold.toolRegenPercentage.tooltip"))
                .setSaveConsumer(newValue -> values.toolRegenPercentage = newValue / 100.0)
                .setTextGetter(value -> Text.literal(value + "%"))
                .build());

        ConfigCategory upgrades = builder.getOrCreateCategory(
                Text.translatable("config.greedy-gold.category.upgrades"));

        upgrades.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.greedy-gold.upgradesEnabled"),
                        values.upgradesEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.greedy-gold.upgradesEnabled.tooltip"))
                .setSaveConsumer(newValue -> values.upgradesEnabled = newValue)
                .build());

        upgrades.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.greedy-gold.showUpgradeTooltip"),
                        values.showUpgradeTooltip)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.greedy-gold.showUpgradeTooltip.tooltip"))
                .setSaveConsumer(newValue -> values.showUpgradeTooltip = newValue)
                .build());

        upgrades.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.greedy-gold.useRandomAffinity"),
                        values.useRandomAffinity)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.greedy-gold.useRandomAffinity.tooltip"))
                .setSaveConsumer(newValue -> values.useRandomAffinity = newValue)
                .build());

        upgrades.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("config.greedy-gold.minAffinity"),
                        values.minAffinity)
                .setDefaultValue(0.8)
                .setMin(0.1)
                .setMax(10.0)
                .setTooltip(Text.translatable("config.greedy-gold.minAffinity.tooltip"))
                .setSaveConsumer(newValue -> values.minAffinity = newValue)
                .build());

        upgrades.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("config.greedy-gold.maxAffinity"),
                        values.maxAffinity)
                .setDefaultValue(1.2)
                .setMin(0.1)
                .setMax(10.0)
                .setTooltip(Text.translatable("config.greedy-gold.maxAffinity.tooltip"))
                .setSaveConsumer(newValue -> values.maxAffinity = newValue)
                .build());

        ConfigCategory enchantUpgrades = builder.getOrCreateCategory(
                Text.translatable("config.greedy-gold.category.enchantUpgrades"));

        enchantUpgrades.addEntry(entryBuilder.startIntField(
                        Text.translatable("config.greedy-gold.enchantUpgradeBaseUses"),
                        values.enchantUpgradeBaseUses)
                .setDefaultValue(100)
                .setMin(1)
                .setMax(10000)
                .setTooltip(Text.translatable("config.greedy-gold.enchantUpgradeBaseUses.tooltip"))
                .setSaveConsumer(newValue -> values.enchantUpgradeBaseUses = newValue)
                .build());

        enchantUpgrades.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("config.greedy-gold.enchantUpgradeModifier"),
                        values.enchantUpgradeModifier)
                .setDefaultValue(1.2)
                .setMin(1.0)
                .setMax(5.0)
                .setTooltip(Text.translatable("config.greedy-gold.enchantUpgradeModifier.tooltip"))
                .setSaveConsumer(newValue -> values.enchantUpgradeModifier = newValue)
                .build());

        enchantUpgrades.addEntry(entryBuilder.startIntField(
                        Text.translatable("config.greedy-gold.maxEnchantLevel"),
                        values.maxEnchantLevel)
                .setDefaultValue(5)
                .setMin(1)
                .setMax(10)
                .setTooltip(Text.translatable("config.greedy-gold.maxEnchantLevel.tooltip"))
                .setSaveConsumer(newValue -> values.maxEnchantLevel = newValue)
                .build());

        ConfigCategory durabilityUpgrades = builder.getOrCreateCategory(
                Text.translatable("config.greedy-gold.category.durabilityUpgrades"));

        durabilityUpgrades.addEntry(entryBuilder.startIntField(
                        Text.translatable("config.greedy-gold.durabilityUpgradeBaseUses"),
                        values.durabilityUpgradeBaseUses)
                .setDefaultValue(50)
                .setMin(1)
                .setMax(10000)
                .setTooltip(Text.translatable("config.greedy-gold.durabilityUpgradeBaseUses.tooltip"))
                .setSaveConsumer(newValue -> values.durabilityUpgradeBaseUses = newValue)
                .build());

        durabilityUpgrades.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("config.greedy-gold.durabilityUpgradeModifier"),
                        values.durabilityUpgradeModifier)
                .setDefaultValue(1.15)
                .setMin(1.0)
                .setMax(5.0)
                .setTooltip(Text.translatable("config.greedy-gold.durabilityUpgradeModifier.tooltip"))
                .setSaveConsumer(newValue -> values.durabilityUpgradeModifier = newValue)
                .build());

        durabilityUpgrades.addEntry(entryBuilder.startIntField(
                        Text.translatable("config.greedy-gold.maxDurabilityLevel"),
                        values.maxDurabilityLevel)
                .setDefaultValue(400)
                .setMin(1)
                .setMax(1000)
                .setTooltip(Text.translatable("config.greedy-gold.maxDurabilityLevel.tooltip"))
                .setSaveConsumer(newValue -> values.maxDurabilityLevel = newValue)
                .build());

        return builder.build();
    }
}