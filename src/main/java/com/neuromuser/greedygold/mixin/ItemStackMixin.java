package com.neuromuser.greedygold.mixin;

import com.neuromuser.greedygold.upgrade.ItemUpgradeManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"))
    private <T extends LivingEntity> void onItemDamaged(int amount, T entity, Consumer<T> breakCallback, CallbackInfo ci) {
        if (entity.getWorld().isClient) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ToolItem || stack.getItem() instanceof ArmorItem) {
            ItemUpgradeManager.onItemUsed(stack, player);
        }
    }
}