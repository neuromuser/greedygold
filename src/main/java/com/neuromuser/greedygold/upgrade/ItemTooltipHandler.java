package com.neuromuser.greedygold.upgrade;

import com.neuromuser.greedygold.config.ClientCache;
import com.neuromuser.greedygold.config.ConfigValues;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ItemTooltipHandler {

    public static void addUpgradeTooltip(ItemStack stack, List<Text> tooltip) {
        if (!isGoldenItem(stack)) return;

        ConfigValues config = ClientCache.get();
        if (!config.upgradesEnabled || !config.showUpgradeTooltip) return;

        int usesLeft = ItemUpgradeManager.getUsesUntilNextEnchantUpgrade(stack);
        if (usesLeft < 0) return;

        tooltip.add(Text.literal(""));
        tooltip.add(
                Text.literal("⚡ ").formatted(Formatting.GOLD)
                        .append(Text.translatable("tooltip.greedy-gold.upgrade_progress", usesLeft)
                                .formatted(Formatting.GRAY))
        );
    }

    private static boolean isGoldenItem(ItemStack stack) {
        if (stack.getItem() instanceof ToolItem toolItem) {
            return toolItem.getMaterial() == ToolMaterials.GOLD;
        }
        if (stack.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getMaterial() == ArmorMaterials.GOLD;
        }
        return false;
    }
}