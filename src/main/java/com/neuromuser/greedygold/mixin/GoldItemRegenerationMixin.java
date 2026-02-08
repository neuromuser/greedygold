package com.neuromuser.greedygold.mixin;

import com.neuromuser.greedygold.GreedyGold;
import com.neuromuser.greedygold.config.ConfigValues;
import com.neuromuser.greedygold.config.ModConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class GoldItemRegenerationMixin extends LivingEntity {
    @Unique private int armorTimer = 0;
    @Unique private int toolTimer = 0;

    protected GoldItemRegenerationMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void greedygodl$onTick(CallbackInfo ci){
        if (this.getWorld() == null) return;

        ConfigValues config = ModConfig.getInstance().getValues();
        if (!config.enabled) return;

        PlayerEntity player = (PlayerEntity) (Object) this;

        armorTimer++;
        if (armorTimer >= config.getRegenIntervalTicks()){
            GreedyGold.processPlayerInventory(player, config, !config.useSeparateToolRegen);
            armorTimer = 0;
        }

        if (config.useSeparateToolRegen){
            toolTimer++;
            if (toolTimer >= config.getToolRegenIntervalTicks()){
                GreedyGold.processPlayerInventory(player, config, true);
                toolTimer = 0;
            }
        }
    }

}
