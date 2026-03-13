package com.neuromuser.greedygold.mixin;

import com.neuromuser.greedygold.upgrade.ItemUpgradeManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(
            method = "damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V",
            at = @At("HEAD")
    )
    private void onItemDamaged(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        if (entity.getWorld().isClient) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        ItemStack stack = (ItemStack) (Object) this;
        Item item = stack.getItem();

        if (item instanceof ToolItem || item instanceof SwordItem) {
            ItemUpgradeManager.onItemUsed(stack, player);
        }
    }
}