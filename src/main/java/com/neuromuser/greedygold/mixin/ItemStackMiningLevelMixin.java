package com.neuromuser.greedygold.mixin;

import com.neuromuser.greedygold.config.ModConfig;
import net.minecraft.block.BlockState;
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

        if (!(item instanceof ToolItem)) return;
        if (!(item instanceof PickaxeItem || item instanceof AxeItem ||
                item instanceof ShovelItem || item instanceof HoeItem)) return;

        ToolItem tool = (ToolItem) item;
        if (tool.getMaterial() != ToolMaterials.GOLD) return;

        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains("GreedyGoldDurabilityLevel")) return;

        int durabilityLevel = nbt.getInt("GreedyGoldDurabilityLevel");
        int ironThreshold = ModConfig.getInstance().getValues().miningLevelIronThreshold;
        int diamondThreshold = ModConfig.getInstance().getValues().miningLevelDiamondThreshold;

        if (durabilityLevel < ironThreshold) return;

        boolean isDiamondLevel = durabilityLevel >= diamondThreshold;

        if (item instanceof PickaxeItem) {
            if (isDiamondLevel && state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
                cir.setReturnValue(true);
            } else if (state.isIn(BlockTags.NEEDS_IRON_TOOL)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "getMiningSpeedMultiplier", at = @At("RETURN"), cancellable = true)
    private void modifyMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        Item item = stack.getItem();

        if (!(item instanceof ToolItem)) return;
        if (!(item instanceof PickaxeItem || item instanceof AxeItem ||
                item instanceof ShovelItem || item instanceof HoeItem)) return;

        ToolItem tool = (ToolItem) item;
        if (tool.getMaterial() != ToolMaterials.GOLD) return;

        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains("GreedyGoldDurabilityLevel")) return;

        int durabilityLevel = nbt.getInt("GreedyGoldDurabilityLevel");
        int ironThreshold = ModConfig.getInstance().getValues().miningLevelIronThreshold;
        int diamondThreshold = ModConfig.getInstance().getValues().miningLevelDiamondThreshold;

        ToolMaterial effectiveMaterial;
        if (durabilityLevel >= diamondThreshold) {
            effectiveMaterial = ToolMaterials.DIAMOND;
        } else if (durabilityLevel >= ironThreshold) {
            effectiveMaterial = ToolMaterials.IRON;
        } else {
            effectiveMaterial = ToolMaterials.STONE;
        }

        if (item.isSuitableFor(state)) {
            float originalSpeed = cir.getReturnValue();
            float upgradedSpeed = effectiveMaterial.getMiningSpeedMultiplier();

            if (upgradedSpeed > originalSpeed) {
                cir.setReturnValue(upgradedSpeed);
            }
            else cir.setReturnValue(originalSpeed);

        }
    }
}