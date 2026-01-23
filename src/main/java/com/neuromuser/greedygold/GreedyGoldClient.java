package com.neuromuser.greedygold;

import com.neuromuser.greedygold.config.ClientCache;
import com.neuromuser.greedygold.network.ConfigSync;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class GreedyGoldClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigSync.ID, (client, handler, buf, sender) -> {
            var config = ConfigSync.decode(buf);
            client.execute(() -> ClientCache.set(config));
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientCache.clear());
    }
}