package com.chenweikeng.nra.config;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class ModConfig {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Path CONFIG_PATH = Path.of("config/not-riding-alert.json");
  private static ModConfig instance;

  public boolean enabled = true;
  public String soundId = "entity.experience_orb.pickup";
  public boolean blindWhenRiding = false;
  public boolean defocusCursor = true;
  public boolean silent = true;
  public boolean autograb = true;
  public Integer minRideTimeMinutes = null;
  public int rideDisplayCount = 16;
  public List<String> hiddenRides = Lists.newArrayList();
  public boolean hideScoreboard = false;
  public boolean hideChat = false;
  public boolean hideHealth = true;
  public boolean onlyAutograbbing = false;

  public static ModConfig getInstance() {
    if (instance == null) {
      instance = load();
    }
    return instance;
  }

  public static ModConfig load() {
    File configFile = CONFIG_PATH.toFile();
    if (!configFile.exists()) {
      return new ModConfig();
    }

    try (FileReader reader = new FileReader(configFile)) {
      return GSON.fromJson(reader, ModConfig.class);
    } catch (IOException e) {
      return new ModConfig();
    }
  }

  public void save() {
    try {
      Files.createDirectories(CONFIG_PATH.getParent());
      try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
        GSON.toJson(this, writer);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ModConfig modConfig = (ModConfig) o;
    return enabled == modConfig.enabled
        && blindWhenRiding == modConfig.blindWhenRiding
        && defocusCursor == modConfig.defocusCursor
        && silent == modConfig.silent
        && autograb == modConfig.autograb
        && rideDisplayCount == modConfig.rideDisplayCount
        && hideScoreboard == modConfig.hideScoreboard
        && hideChat == modConfig.hideChat
        && hideHealth == modConfig.hideHealth
        && onlyAutograbbing == modConfig.onlyAutograbbing
        && Objects.equals(soundId, modConfig.soundId)
        && Objects.equals(minRideTimeMinutes, modConfig.minRideTimeMinutes)
        && Objects.equals(hiddenRides, modConfig.hiddenRides);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        enabled,
        soundId,
        blindWhenRiding,
        defocusCursor,
        silent,
        autograb,
        minRideTimeMinutes,
        rideDisplayCount,
        hiddenRides,
        hideScoreboard,
        hideChat,
        hideHealth,
        onlyAutograbbing);
  }
}
