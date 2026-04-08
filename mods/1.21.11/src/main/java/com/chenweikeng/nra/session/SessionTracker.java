package com.chenweikeng.nra.session;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.ride.RideName;

public class SessionTracker {
  private static SessionTracker instance;

  private DailySessionData data;

  private SessionTracker() {}

  public static SessionTracker getInstance() {
    if (instance == null) {
      instance = new SessionTracker();
    }
    return instance;
  }

  public void onSessionStart() {
    data = DailySessionData.load();
    data.checkDateRollover();
    data.startSession();
    data.forceSave();
  }

  public void onRideCompleted(RideName ride) {
    if (data == null) {
      return;
    }
    data.checkDateRollover();
    int rideTime = ride.getRideTime();
    long onlineBefore = data.getOnlineSeconds();
    long rideTimeBefore = data.totalRideTimeSeconds;
    data.onRideCompleted(rideTime);
    NotRidingAlertClient.LOGGER.info(
        "[SessionDebug] rideCompleted: ride={} rideTime={}s onlineSeconds={} rideTimeBefore={}s rideTimeAfter={}s",
        ride.name(),
        rideTime,
        onlineBefore,
        rideTimeBefore,
        data.totalRideTimeSeconds);
    MilestoneHandler.checkMilestone(data.ridesCompleted, data.totalRideTimeSeconds);
  }

  public int getRidesToday() {
    return data != null ? data.ridesCompleted : 0;
  }

  public long getRideTimeToday() {
    return data != null ? data.totalRideTimeSeconds : 0;
  }

  public long getOnlineSeconds() {
    return data != null ? data.getOnlineSeconds() : 0;
  }

  public int getCurrentStreak() {
    return data != null ? data.currentStreak : 0;
  }

  public void checkAndSaveIfNeeded() {
    if (data != null) {
      data.checkDateRollover();
      data.saveIfDirty();
    }
  }

  public void onSessionEnd() {
    if (data != null) {
      data.endSession();
      data.forceSave();
    }
  }

  public boolean isActive() {
    return data != null;
  }
}
