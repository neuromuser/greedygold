package com.neuromuser.greedygold.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main configuration class for Greedy Gold mod.
 * Handles loading, saving, and accessing config values.
 */
public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("GreedyGold/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("greedy-gold.json");

    private static ModConfig INSTANCE;
    private ConfigValues values = new ConfigValues();

    private ModConfig() {
    }

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ModConfig();
            INSTANCE.load();
        }
        return INSTANCE;
    }

    /**
     * Get config values (read-only access)
     */
    public ConfigValues getValues() {
        return values;
    }

    /**
     * Load configuration from file. Creates default if doesn't exist.
     */
    public void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                ConfigValues loadedValues = GSON.fromJson(json, ConfigValues.class);

                if (loadedValues != null) {
                    this.values = loadedValues;
                    LOGGER.info("Configuration loaded from {}", CONFIG_PATH);
                } else {
                    LOGGER.warn("Failed to parse config, using defaults");
                    save(); // Save defaults
                }
            } else {
                LOGGER.info("Config file not found, creating default configuration");
                save(); // Create default config
            }

            // Validate loaded values
            values.validate();

        } catch (IOException e) {
            LOGGER.error("Failed to load configuration", e);
        }
    }

    /**
     * Save current configuration to file.
     */
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(values);
            Files.writeString(CONFIG_PATH, json);
            LOGGER.info("Configuration saved to {}", CONFIG_PATH);
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration", e);
        }
    }

    /**
     * Reload configuration from file.
     */
    public void reload() {
        load();
    }

    /**
     * Reset to default values and save.
     */
    public void resetToDefaults() {
        values = new ConfigValues();
        save();
        LOGGER.info("Configuration reset to defaults");
    }
}