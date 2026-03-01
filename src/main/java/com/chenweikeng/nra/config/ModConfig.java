package com.chenweikeng.nra.config;

import com.chenweikeng.nra.ride.RideName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ModConfig {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Path CONFIG_PATH = Path.of("config/not-riding-alert.json");
  private static ModConfig instance;

  public boolean globalEnable = ConfigDefaults.GLOBAL_ENABLE;
  public boolean enabled = ConfigDefaults.ENABLED;
  public String soundId = ConfigDefaults.SOUND_ID;
  public boolean blindWhenRiding = ConfigDefaults.BLIND_WHEN_RIDING;
  public FullbrightMode fullbrightMode = ConfigDefaults.FULLBRIGHT_MODE;
  public CursorReleaseTiming cursorReleaseTiming = ConfigDefaults.CURSOR_RELEASE_TIMING;
  public boolean silent = ConfigDefaults.SILENT;
  public boolean autograb = ConfigDefaults.AUTOGRAB;
  public Integer minRideTimeMinutes = null;
  public int rideDisplayCount = ConfigDefaults.RIDE_DISPLAY_COUNT;
  public Set<String> hiddenRides =
      Arrays.stream(RideName.values())
          .filter(RideName::isSeasonal)
          .map(RideName::toMatchString)
          .collect(Collectors.toSet());
  public boolean hideScoreboard = ConfigDefaults.HIDE_SCOREBOARD;
  public boolean hideChat = ConfigDefaults.HIDE_CHAT;
  public boolean hideHealth = ConfigDefaults.HIDE_HEALTH;
  public boolean hideNameTag = ConfigDefaults.HIDE_NAME_TAG;
  public boolean hideHotbar = ConfigDefaults.HIDE_HOTBAR;
  public boolean hideExperienceLevel = ConfigDefaults.HIDE_EXPERIENCE_LEVEL;
  public boolean onlyAutograbbing = ConfigDefaults.ONLY_AUTOGRABBING;
  public boolean alertAutograbFailure = ConfigDefaults.ALERT_AUTOGRAB_FAILURE;
  public boolean hideLovePotionMessages = ConfigDefaults.HIDE_LOVE_POTION_MESSAGES;
  public boolean displayShortName = ConfigDefaults.DISPLAY_SHORT_NAME;
  public boolean keepUnchanged = ConfigDefaults.KEEP_UNCHANGED;
  public boolean hasOpenedConfig = ConfigDefaults.HAS_OPENED_CONFIG;
  public int hudBackgroundOpacity = ConfigDefaults.HUD_BACKGROUND_OPACITY;
  public WindowMinimizeTiming minimizeWindow = ConfigDefaults.MINIMIZE_WINDOW;
  public boolean hibernationWhenRiding = ConfigDefaults.HIBERNATION_WHEN_RIDING;
  public StrategyHudRendererVersion strategyHudRendererVersion =
      ConfigDefaults.STRATEGY_HUD_RENDERER_VERSION;
  public int trackerNormalColor = ConfigDefaults.TRACKER_NORMAL_COLOR;
  public int trackerAutograbbingColor = ConfigDefaults.TRACKER_AUTOGRABBING_COLOR;
  public int trackerRidingColor = ConfigDefaults.TRACKER_RIDING_COLOR;
  public int trackerErrorColor = ConfigDefaults.TRACKER_ERROR_COLOR;
  public AudioBoostReminderMode audioBoostReminderMode = ConfigDefaults.AUDIO_BOOST_REMINDER_MODE;
  public boolean relocateClosedCaption = ConfigDefaults.RELOCATE_CLOSED_CAPTION;
  public boolean enableTracker = ConfigDefaults.ENABLE_TRACKER;

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
        && fullbrightMode == modConfig.fullbrightMode
        && cursorReleaseTiming == modConfig.cursorReleaseTiming
        && silent == modConfig.silent
        && autograb == modConfig.autograb
        && rideDisplayCount == modConfig.rideDisplayCount
        && hideScoreboard == modConfig.hideScoreboard
        && hideChat == modConfig.hideChat
        && hideHealth == modConfig.hideHealth
        && hideNameTag == modConfig.hideNameTag
        && hideHotbar == modConfig.hideHotbar
        && hideExperienceLevel == modConfig.hideExperienceLevel
        && onlyAutograbbing == modConfig.onlyAutograbbing
        && alertAutograbFailure == modConfig.alertAutograbFailure
        && displayShortName == modConfig.displayShortName
        && keepUnchanged == modConfig.keepUnchanged
        && hasOpenedConfig == modConfig.hasOpenedConfig
        && hudBackgroundOpacity == modConfig.hudBackgroundOpacity
        && minimizeWindow == modConfig.minimizeWindow
        && hibernationWhenRiding == modConfig.hibernationWhenRiding
        && hideLovePotionMessages == modConfig.hideLovePotionMessages
        && strategyHudRendererVersion == modConfig.strategyHudRendererVersion
        && trackerNormalColor == modConfig.trackerNormalColor
        && trackerAutograbbingColor == modConfig.trackerAutograbbingColor
        && trackerRidingColor == modConfig.trackerRidingColor
        && trackerErrorColor == modConfig.trackerErrorColor
        && audioBoostReminderMode == modConfig.audioBoostReminderMode
        && relocateClosedCaption == modConfig.relocateClosedCaption
        && enableTracker == modConfig.enableTracker
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
        fullbrightMode,
        cursorReleaseTiming,
        silent,
        autograb,
        minRideTimeMinutes,
        rideDisplayCount,
        hiddenRides,
        hideScoreboard,
        hideChat,
        hideHealth,
        hideNameTag,
        hideHotbar,
        hideExperienceLevel,
        onlyAutograbbing,
        alertAutograbFailure,
        displayShortName,
        keepUnchanged,
        hasOpenedConfig,
        hudBackgroundOpacity,
        minimizeWindow,
        hibernationWhenRiding,
        hideLovePotionMessages,
        strategyHudRendererVersion,
        trackerNormalColor,
        trackerAutograbbingColor,
        trackerRidingColor,
        trackerErrorColor,
        audioBoostReminderMode,
        relocateClosedCaption,
        enableTracker);
  }
}
