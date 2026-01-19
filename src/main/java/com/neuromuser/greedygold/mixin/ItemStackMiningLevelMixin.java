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

        if (!(item instanceof PickaxeItem)) return;

        ToolItem tool = (ToolItem) item;
        if (tool.getMaterial() != ToolMaterials.GOLD) return;

        boolean needsStone = state.isIn(BlockTags.NEEDS_STONE_TOOL);
        boolean needsIron = state.isIn(BlockTags.NEEDS_IRON_TOOL);
        boolean needsDiamond = state.isIn(BlockTags.NEEDS_DIAMOND_TOOL);

        if (needsStone) {
            cir.setReturnValue(true);
            return;
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains("GreedyGoldDurabilityLevel")) return;

        int durabilityLevel = nbt.getInt("GreedyGoldDurabilityLevel");
        int ironThreshold = ModConfig.getInstance().getValues().miningLevelIronThreshold;
        int diamondThreshold = ModConfig.getInstance().getValues().miningLevelDiamondThreshold;

        boolean canMineAtIronLevel = durabilityLevel >= ironThreshold;
        boolean canMineAtDiamondLevel = durabilityLevel >= diamondThreshold;

        if (canMineAtDiamondLevel && needsDiamond) {
            cir.setReturnValue(true);
        } else if (canMineAtIronLevel && needsIron) {
            cir.setReturnValue(true);
        }
    }
}