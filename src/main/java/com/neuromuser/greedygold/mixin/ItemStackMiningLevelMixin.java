package com.neuromuser.greedygold.mixin;

import com.neuromuser.greedygold.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMiningLevelMixin {

    @Inject(method = "isSuitableFor", at = @At("HEAD"), cancellable = true)
    private void modifyMiningLevel(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        Item item = stack.getItem();

        if (item != Items.GOLDEN_PICKAXE) return;

        if (state.isIn(BlockTags.NEEDS_STONE_TOOL)) {
            cir.setReturnValue(true);
        }

        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        if (nbt == null || !nbt.contains("GreedyGoldDurabilityLevel")) return;

        int durabilityLevel = nbt.getInt("GreedyGoldDurabilityLevel");
        int ironThreshold = ModConfig.getInstance().getValues().miningLevelIronThreshold;
        int diamondThreshold = ModConfig.getInstance().getValues().miningLevelDiamondThreshold;

        if (durabilityLevel >= ironThreshold && state.isIn(BlockTags.NEEDS_IRON_TOOL)) {
            cir.setReturnValue(true);
        }

        if (durabilityLevel >= diamondThreshold) {
            if (state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "getMiningSpeedMultiplier", at = @At("RETURN"), cancellable = true)
    private void modifyMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        Item item = stack.getItem();

        if (!(item instanceof ToolItem tool) || tool.getMaterial() != ToolMaterials.GOLD) return;

        if (stack.isSuitableFor(state)) {
            cir.setReturnValue(ToolMaterials.GOLD.getMiningSpeedMultiplier());
        }
    }
}