package com.neuromuser.greedygold;

import com.neuromuser.greedygold.config.ConfigValues;
import com.neuromuser.greedygold.config.ModConfig;
import com.neuromuser.greedygold.network.ConfigSync;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreedyGold implements ModInitializer {
	public static final String MOD_ID = "greedy-gold";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Greedy Gold");

		ModConfig config = ModConfig.getInstance();
		ConfigValues values = config.getValues();

		PayloadTypeRegistry.playS2C().register(ConfigSync.ID, ConfigSync.CODEC);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			sender.sendPacket(new ConfigSync(ModConfig.getInstance().getValues()));
		});
	}
	public static void processPlayerInventory(PlayerEntity player, ConfigValues config, boolean toolsOnly) {
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

	private static boolean shouldRegenerate(ItemStack stack, boolean toolsOnly) {
		if (stack.isEmpty() || stack.getDamage() <= 0) return false;

		Item item = stack.getItem();

		if (item instanceof SwordItem sword && sword.getMaterial() == ToolMaterials.GOLD)
			return !toolsOnly;

		if (item instanceof ToolItem tool && tool.getMaterial() == ToolMaterials.GOLD)
			return toolsOnly;

		if (item instanceof ArmorItem armor && armor.getMaterial() == ArmorMaterials.GOLD)
			return !toolsOnly;

		return false;
	}
}