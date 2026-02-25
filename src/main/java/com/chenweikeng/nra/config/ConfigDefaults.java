package com.chenweikeng.nra.config;

public final class ConfigDefaults {
  private ConfigDefaults() {}

  public static final boolean GLOBAL_ENABLE = true;
  public static final boolean ENABLED = true;
  public static final String SOUND_ID = "entity.experience_orb.pickup";
  public static final boolean BLIND_WHEN_RIDING = true;
  public static final FullbrightMode FULLBRIGHT_MODE = FullbrightMode.NONE;
  public static final CursorReleaseTiming CURSOR_RELEASE_TIMING = CursorReleaseTiming.NONE;
  public static final boolean SILENT = true;
  public static final boolean AUTOGRAB = true;
  public static final int RIDE_DISPLAY_COUNT = 10;
  public static final boolean HIDE_SCOREBOARD = false;
  public static final boolean HIDE_CHAT = false;
  public static final boolean HIDE_HEALTH = true;
  public static final boolean HIDE_NAME_TAG = false;
  public static final boolean ONLY_AUTOGRABBING = false;
  public static final boolean HIDE_LOVE_POTION_MESSAGES = false;
  public static final boolean ALERT_AUTOGRAB_FAILURE = true;
  public static final boolean DISPLAY_SHORT_NAME = false;
  public static final boolean KEEP_UNCHANGED = true;
  public static final boolean HAS_OPENED_CONFIG = false;
  public static final int HUD_BACKGROUND_OPACITY = 80;
  public static final WindowMinimizeTiming MINIMIZE_WINDOW = WindowMinimizeTiming.NONE;
  public static final boolean HIBERNATION_WHEN_RIDING = true;
  public static final StrategyHudRendererVersion STRATEGY_HUD_RENDERER_VERSION =
      StrategyHudRendererVersion.V2;

  public static final int TRACKER_NORMAL_COLOR = 0xFFFFFFFF;
  public static final int TRACKER_AUTOGRABBING_COLOR = 0xFFFF00EE;
  public static final int TRACKER_RIDING_COLOR = 0xFF00FF00;
  public static final int TRACKER_ERROR_COLOR = 0xFF0066FF;
  public static final AudioBoostReminderMode AUDIO_BOOST_REMINDER_MODE =
      AudioBoostReminderMode.ONLY_WHEN_RIDING;
}
