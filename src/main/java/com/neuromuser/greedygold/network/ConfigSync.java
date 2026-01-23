package com.neuromuser.greedygold.network;

import com.neuromuser.greedygold.config.ConfigValues;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ConfigSync {
    public static final Identifier ID = new Identifier("greedy-gold", "config");

    public static PacketByteBuf encode(ConfigValues c) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(c.upgradesEnabled);
        buf.writeBoolean(c.showUpgradeTooltip);
        return buf;
    }

    public static ConfigValues decode(PacketByteBuf buf) {
        ConfigValues c = new ConfigValues();
        c.upgradesEnabled = buf.readBoolean();
        c.showUpgradeTooltip = buf.readBoolean();
        return c;
    }
}