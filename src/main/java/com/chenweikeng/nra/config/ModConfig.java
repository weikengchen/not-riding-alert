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
    private boolean blindWhenRiding = false; // Default disabled
    private boolean seasonalRidesEnabled = true; // Default enabled
    private java.util.List<String> hiddenRides = new java.util.ArrayList<>(); // List of hidden ride display names
    private int rideDisplayCount = 16; // Default number of rides to display
    private boolean hideScoreboard = false; // Default disabled
    private boolean hideChat = false; // Default disabled
    private Integer minRideTimeMinutes = null; // Minimum ride time filter in minutes (null = no filter)
    
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
    
    public boolean isBlindWhenRiding() {
        return blindWhenRiding;
    }
    
    public void setBlindWhenRiding(boolean blindWhenRiding) {
        this.blindWhenRiding = blindWhenRiding;
        save();
    }
    
    public boolean isSeasonalRidesEnabled() {
        return seasonalRidesEnabled;
    }
    
    public void setSeasonalRidesEnabled(boolean seasonalRidesEnabled) {
        this.seasonalRidesEnabled = seasonalRidesEnabled;
        save();
    }
    
    public java.util.List<String> getHiddenRides() {
        return hiddenRides;
    }
    
    public void setHiddenRides(java.util.List<String> hiddenRides) {
        this.hiddenRides = hiddenRides != null ? hiddenRides : new java.util.ArrayList<>();
        save();
    }
    
    public boolean isRideHidden(String rideDisplayName) {
        return hiddenRides.contains(rideDisplayName);
    }
    
    public void toggleRideHidden(String rideDisplayName) {
        if (hiddenRides.contains(rideDisplayName)) {
            hiddenRides.remove(rideDisplayName);
        } else {
            hiddenRides.add(rideDisplayName);
        }
        save();
    }
    
    public int getRideDisplayCount() {
        return rideDisplayCount;
    }
    
    public void setRideDisplayCount(int rideDisplayCount) {
        this.rideDisplayCount = Math.max(1, rideDisplayCount); // Ensure at least 1
        save();
    }
    
    public boolean isHideScoreboard() {
        return hideScoreboard;
    }
    
    public void setHideScoreboard(boolean hideScoreboard) {
        this.hideScoreboard = hideScoreboard;
        save();
    }
    
    public boolean isHideChat() {
        return hideChat;
    }
    
    public void setHideChat(boolean hideChat) {
        this.hideChat = hideChat;
        save();
    }

    public Integer getMinRideTimeMinutes() {
        return minRideTimeMinutes;
    }

    public void setMinRideTimeMinutes(Integer minRideTimeMinutes) {
        this.minRideTimeMinutes = minRideTimeMinutes;
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

