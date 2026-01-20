package com.neuromuser.greedygold;

import com.neuromuser.greedygold.config.ModConfig;
import com.neuromuser.greedygold.config.ConfigValues;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreedyGold implements ModInitializer {
	public static final String MOD_ID = "greedy-gold";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private int tickCounter = 0;
	private int toolTickCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Greedy Gold");

		ModConfig config = ModConfig.getInstance();
		ConfigValues values = config.getValues();

		LOGGER.info("Regeneration: Armor/Weapons {} seconds, Tools {} seconds",
				values.regenIntervalSeconds,
				values.useSeparateToolRegen ? values.toolRegenIntervalSeconds : values.regenIntervalSeconds);

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

			int armorRegenInterval = currentValues.getRegenIntervalTicks();
			int armorCyclePosition = tickCounter % armorRegenInterval;

			for (int i = 0; i < totalPlayers; i++) {
				if (i % armorRegenInterval == armorCyclePosition) {
					processPlayerInventory(players.get(i), currentValues, false);
				}
			}

			if (currentValues.useSeparateToolRegen) {
				int toolRegenInterval = currentValues.getToolRegenIntervalTicks();
				int toolCyclePosition = toolTickCounter % toolRegenInterval;

				for (int i = 0; i < totalPlayers; i++) {
					if (i % toolRegenInterval == toolCyclePosition) {
						processPlayerInventory(players.get(i), currentValues, true);
					}
				}

				toolTickCounter++;
			}

			tickCounter++;
		});

		LOGGER.info("Greedy Gold initialized");
	}

	private void processPlayerInventory(ServerPlayerEntity player, ConfigValues config, boolean toolsOnly) {
		PlayerInventory inventory = player.getInventory();

		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);

			if (shouldRegenerate(stack, toolsOnly)) {
				int currentDamage = stack.getDamage();
				int maxDurability = stack.getMaxDamage();
				boolean isTool = stack.getItem() instanceof ToolItem && !(stack.getItem() instanceof SwordItem);
				int regenAmount = config.getRegenAmount(maxDurability, isTool);
				int newDamage = Math.max(0, currentDamage - regenAmount);

				stack.setDamage(newDamage);
			}
		}
	}

	private boolean shouldRegenerate(ItemStack stack, boolean toolsOnly) {
		if (stack.isEmpty() || stack.getDamage() <= 0) {
			return false;
		}

		Item item = stack.getItem();

		if (item instanceof ToolItem toolItem && toolItem.getMaterial() == ToolMaterials.GOLD) {
			boolean isSword = item instanceof SwordItem;
			if (toolsOnly) {
				return !isSword;
			} else {
				return isSword;
			}
		}

		if (item instanceof ArmorItem armorItem && armorItem.getMaterial() == ArmorMaterials.GOLD) {
			return !toolsOnly;
		}

		return false;
	}
}