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

  public boolean globalEnable = ConfigDefaults.GLOBAL_ENABLE;
  public boolean enabled = ConfigDefaults.ENABLED;
  public String soundId = ConfigDefaults.SOUND_ID;
  public boolean blindWhenRiding = ConfigDefaults.BLIND_WHEN_RIDING;
  public boolean fullbrightWhenNotRiding = ConfigDefaults.FULLBRIGHT_WHEN_NOT_RIDING;
  public boolean defocusCursor = ConfigDefaults.DEFOCUS_CURSOR;
  public boolean silent = ConfigDefaults.SILENT;
  public boolean autograb = ConfigDefaults.AUTOGRAB;
  public Integer minRideTimeMinutes = null;
  public int rideDisplayCount = ConfigDefaults.RIDE_DISPLAY_COUNT;
  public List<String> hiddenRides = Lists.newArrayList();
  public boolean hideScoreboard = ConfigDefaults.HIDE_SCOREBOARD;
  public boolean hideChat = ConfigDefaults.HIDE_CHAT;
  public boolean hideHealth = ConfigDefaults.HIDE_HEALTH;
  public boolean onlyAutograbbing = ConfigDefaults.ONLY_AUTOGRABBING;
  public boolean alertAutograbFailure = ConfigDefaults.ALERT_AUTOGRAB_FAILURE;
  public boolean displayShortName = ConfigDefaults.DISPLAY_SHORT_NAME;

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
    return globalEnable == modConfig.globalEnable
        && enabled == modConfig.enabled
        && blindWhenRiding == modConfig.blindWhenRiding
        && fullbrightWhenNotRiding == modConfig.fullbrightWhenNotRiding
        && defocusCursor == modConfig.defocusCursor
        && silent == modConfig.silent
        && autograb == modConfig.autograb
        && rideDisplayCount == modConfig.rideDisplayCount
        && hideScoreboard == modConfig.hideScoreboard
        && hideChat == modConfig.hideChat
        && hideHealth == modConfig.hideHealth
        && onlyAutograbbing == modConfig.onlyAutograbbing
        && alertAutograbFailure == modConfig.alertAutograbFailure
        && displayShortName == modConfig.displayShortName
        && Objects.equals(soundId, modConfig.soundId)
        && Objects.equals(minRideTimeMinutes, modConfig.minRideTimeMinutes)
        && Objects.equals(hiddenRides, modConfig.hiddenRides);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        globalEnable,
        enabled,
        soundId,
        blindWhenRiding,
        fullbrightWhenNotRiding,
        defocusCursor,
        silent,
        autograb,
        minRideTimeMinutes,
        rideDisplayCount,
        hiddenRides,
        hideScoreboard,
        hideChat,
        hideHealth,
        onlyAutograbbing,
        alertAutograbFailure,
        displayShortName);
  }
}
