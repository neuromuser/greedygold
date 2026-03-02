package com.neuromuser.greedygold.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMaxDamageMixin {

    @Shadow
    public abstract NbtCompound getOrCreateNbt();

    @Shadow
    public abstract boolean isDamageable();

    @Inject(method = "getMaxDamage", at = @At("RETURN"), cancellable = true)
    private void modifyMaxDamage(CallbackInfoReturnable<Integer> cir) {
        if (!this.isDamageable()) return;
        NbtCompound nbt = ((ItemStack)(Object)this).getNbt(); // was getOrCreateNbt()
        if (nbt != null && nbt.contains("GreedyGoldMaxDamageBonus")) {
            cir.setReturnValue(cir.getReturnValue() + nbt.getInt("GreedyGoldMaxDamageBonus"));
        }
    }
    @Inject(method = "getItemBarStep", at = @At("RETURN"), cancellable = true)
    private void modifyBarStep(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (this.isDamageable()) {
            NbtCompound nbt = this.getOrCreateNbt();
            if (nbt.contains("GreedyGoldMaxDamageBonus")) {
                int maxDamage = stack.getMaxDamage();
                int damage = stack.getDamage();

                int step = Math.round(13.0F - (float)damage * 13.0F / (float)maxDamage);
                cir.setReturnValue(step);
            }
        }
    }

    @Inject(method = "getItemBarColor", at = @At("RETURN"), cancellable = true)
    private void modifyBarColor(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (this.isDamageable()) {
            NbtCompound nbt = this.getOrCreateNbt();
            if (nbt.contains("GreedyGoldMaxDamageBonus")) {
                int maxDamage = stack.getMaxDamage();
                int damage = stack.getDamage();

                float f = Math.max(0.0F, (float)(maxDamage - damage) / (float)maxDamage);

                int color = net.minecraft.util.math.MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);

                cir.setReturnValue(color);
            }
        }
    }
}