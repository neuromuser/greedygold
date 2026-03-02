package com.neuromuser.greedygold.mixin;

import com.neuromuser.greedygold.upgrade.ItemUpgradeManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
        if (stack.getItem() instanceof ToolItem
                || stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof ArmorItem) {
            ItemUpgradeManager.onItemUsed(stack, player);
        }
    }

    @Inject(method = "postMine", at = @At("HEAD"))
    private void onPostMine(World world, BlockState state, BlockPos pos, PlayerEntity miner, CallbackInfo ci) {
        if (world.isClient) return;
        if (!(miner instanceof ServerPlayerEntity player)) return;

        ItemStack stack = (ItemStack) (Object) this;
        if (!(stack.getItem() instanceof ToolItem tool)) return;
        if (tool.getMaterial() != ToolMaterials.GOLD) return;

        ItemUpgradeManager.onItemUsed(stack, player);
    }
}