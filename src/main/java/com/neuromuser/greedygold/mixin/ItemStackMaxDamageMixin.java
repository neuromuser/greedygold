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

        NbtCompound nbt = this.getOrCreateNbt();
        if (nbt.contains("GreedyGoldMaxDamageBonus")) {
            int bonus = nbt.getInt("GreedyGoldMaxDamageBonus");
            cir.setReturnValue(cir.getReturnValue() + bonus);
        }
    }

    @Inject(method = "getItemBarStep", at = @At("RETURN"), cancellable = true)
    private void modifyBarStep(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (this.isDamageable()) {
            NbtCompound nbt = this.getOrCreateNbt();
            if (nbt.contains("GreedyGoldMaxDamageBonus")) {
                // Calculate the step (0-13) based on NEW max damage
                // Minecraft formula: round(13.0 * (max - damage) / max)
                int maxDamage = stack.getMaxDamage(); // This already includes your bonus
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
                // Calculate the color based on NEW max damage
                int maxDamage = stack.getMaxDamage(); // Includes bonus
                int damage = stack.getDamage();

                // Calculate health percentage (0.0 to 1.0)
                float f = Math.max(0.0F, (float)(maxDamage - damage) / (float)maxDamage);

                // This is the standard Minecraft color formula:
                // It transitions from Red (low) to Green (high)
                // Color is HSB: Hue is health * 1/3 (0 to 120 degrees), Saturation 1.0, Brightness 1.0
                int color = net.minecraft.util.math.MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);

                cir.setReturnValue(color);
            }
        }
    }
}