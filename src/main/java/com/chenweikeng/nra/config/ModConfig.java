package com.chenweikeng.nra.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "not-riding-alert.json");
    
    private String soundId = "entity.experience_orb.pickup"; // Default sound
    private boolean enabled = true; // Default enabled
    
    public String getSoundId() {
        return soundId;
    }
    
    public void setSoundId(String soundId) {
        this.soundId = soundId;
        save();
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }
    
    public static ModConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                com.chenweikeng.nra.NotRidingAlertClient.LOGGER.error("Failed to load config", e);
            }
        }
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }
    
    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            com.chenweikeng.nra.NotRidingAlertClient.LOGGER.error("Failed to save config", e);
        }
    }
}

