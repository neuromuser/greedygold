package com.neuromuser.greedygold.mixin;

import com.neuromuser.greedygold.upgrade.ItemUpgradeManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityArmorUpgradeMixin {
    @Inject(
        method = "damageEquipment(Lnet/minecraft/entity/damage/DamageSource;F[Lnet/minecraft/entity/EquipmentSlot;)V",
        at = @At("HEAD")
    )
    private void onDamageEquipment(DamageSource source, float amount, EquipmentSlot[] slots, CallbackInfo ci) {
        if (amount <= 0.0F) return;
        if (!(source.getAttacker() instanceof LivingEntity)) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity player)) return;

        for (EquipmentSlot slot : slots) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ArmorItem armorItem)) continue;
            if (armorItem.getMaterial() != ArmorMaterials.GOLD) continue;

            ItemUpgradeManager.onItemUsed(stack, player);
        }
    }
}