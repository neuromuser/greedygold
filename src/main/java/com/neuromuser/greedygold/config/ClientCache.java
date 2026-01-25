package com.neuromuser.greedygold.config;

public class ClientCache {
    private static ConfigValues server = null;

    public static void set(ConfigValues c) {
        server = c;
    }

    public static void clear() {
        server = null;
    }

    public static ConfigValues get() {
        return server != null ? server : ModConfig.getInstance().getValues();
    }
}