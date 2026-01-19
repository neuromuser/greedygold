package com.neuromuser.greedygold;

import com.neuromuser.greedygold.config.ModConfig;
import com.neuromuser.greedygold.config.ConfigValues;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreedyGold implements ModInitializer {
	public static final String MOD_ID = "greedy-gold";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private int tickCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Greedy Gold");

		ModConfig config = ModConfig.getInstance();
		ConfigValues values = config.getValues();

		LOGGER.info("Regeneration: {} seconds, {}",
				values.regenIntervalSeconds,
				values.usePercentage ? (values.regenPercentage * 100) + "%" : values.regenAmount + " points");

		if (values.upgradesEnabled) {
			LOGGER.info("Upgrades enabled: Enchant max {} / Durability max {}",
					values.maxEnchantLevel, values.maxDurabilityLevel);
		}

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ConfigValues currentValues = ModConfig.getInstance().getValues();
			if (!currentValues.enabled) return;

			var players = server.getPlayerManager().getPlayerList();
			if (players.isEmpty()) return;

			int totalPlayers = players.size();
			int regenInterval = currentValues.getRegenIntervalTicks();
			int cyclePosition = tickCounter % regenInterval;

			for (int i = 0; i < totalPlayers; i++) {
				if (i % regenInterval == cyclePosition) {
					processPlayerInventory(players.get(i), currentValues);
				}
			}

			tickCounter++;
		});

		LOGGER.info("Greedy Gold initialized");
	}

	private void processPlayerInventory(ServerPlayerEntity player, ConfigValues config) {
		PlayerInventory inventory = player.getInventory();

		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);

			if (shouldRegenerate(stack)) {
				int currentDamage = stack.getDamage();
				int maxDurability = stack.getMaxDamage();
				int regenAmount = config.getRegenAmount(maxDurability);
				int newDamage = Math.max(0, currentDamage - regenAmount);

				stack.setDamage(newDamage);
			}
		}
	}

	private boolean shouldRegenerate(ItemStack stack) {
		if (stack.isEmpty() || stack.getDamage() <= 0) {
			return false;
		}

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