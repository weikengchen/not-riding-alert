package com.chenweikeng.nra.session;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import net.fabricmc.loader.api.FabricLoader;

public class DailySessionData {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File DATA_FILE =
      new File(FabricLoader.getInstance().getConfigDir().toFile(), "not-riding-alert-session.json");
  private static final long SAVE_INTERVAL_MS = 15000;
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

  public String date;
  public int ridesCompleted;
  public long totalRideTimeSeconds;
  public long totalOnlineSeconds;
  public int currentStreak;
  public String lastActiveDate;

  private transient boolean dirty = false;
  private transient long lastSaveTime = 0;
  private transient long currentSessionStartMs = 0;

  public DailySessionData() {
    this.date = today();
    this.ridesCompleted = 0;
    this.totalRideTimeSeconds = 0;
    this.totalOnlineSeconds = 0;
    this.currentStreak = 0;
    this.lastActiveDate = null;
  }

  public void startSession() {
    currentSessionStartMs = System.currentTimeMillis();
  }

  public void endSession() {
    if (currentSessionStartMs > 0) {
      long sessionSeconds = (System.currentTimeMillis() - currentSessionStartMs) / 1000;
      totalOnlineSeconds += sessionSeconds;
      currentSessionStartMs = 0;
      dirty = true;
    }
  }

  public long getOnlineSeconds() {
    long live = 0;
    if (currentSessionStartMs > 0) {
      live = (System.currentTimeMillis() - currentSessionStartMs) / 1000;
    }
    return totalOnlineSeconds + live;
  }

  public void checkDateRollover() {
    String todayStr = today();
    if (todayStr.equals(date)) {
      return;
    }

    updateStreak(todayStr);
    date = todayStr;
    ridesCompleted = 0;
    totalRideTimeSeconds = 0;
    totalOnlineSeconds = 0;
    dirty = true;
  }

  private void updateStreak(String todayStr) {
    if (lastActiveDate == null) {
      return;
    }
    try {
      LocalDate last = LocalDate.parse(lastActiveDate, DATE_FORMAT);
      LocalDate today = LocalDate.parse(todayStr, DATE_FORMAT);
      long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(last, today);
      if (daysBetween == 1) {
        currentStreak++;
      } else if (daysBetween > 1) {
        currentStreak = 0;
      }
    } catch (Exception e) {
      currentStreak = 0;
    }
  }

  public void onRideCompleted(long rideTimeSeconds) {
    ridesCompleted++;
    totalRideTimeSeconds += rideTimeSeconds;

    String todayStr = today();
    if (lastActiveDate == null || !lastActiveDate.equals(todayStr)) {
      if (currentStreak == 0) {
        currentStreak = 1;
      }
      lastActiveDate = todayStr;
    }
    dirty = true;
  }

  public void markDirty() {
    dirty = true;
  }

  public void saveIfDirty() {
    if (!dirty) {
      return;
    }
    long now = System.currentTimeMillis();
    if (now - lastSaveTime < SAVE_INTERVAL_MS) {
      return;
    }
    save();
  }

  public void forceSave() {
    save();
  }

  private void save() {
    try (FileWriter writer = new FileWriter(DATA_FILE)) {
      GSON.toJson(this, writer);
      dirty = false;
      lastSaveTime = System.currentTimeMillis();
    } catch (IOException e) {
      NotRidingAlertClient.LOGGER.error("Failed to save session data", e);
    }
  }

  public static DailySessionData load() {
    if (DATA_FILE.exists()) {
      try (FileReader reader = new FileReader(DATA_FILE)) {
        DailySessionData data = GSON.fromJson(reader, DailySessionData.class);
        if (data != null) {
          return data;
        }
      } catch (Exception e) {
        NotRidingAlertClient.LOGGER.error("Failed to load session data", e);
      }
    }
    return new DailySessionData();
  }

  private static String today() {
    return LocalDate.now().format(DATE_FORMAT);
  }
}
