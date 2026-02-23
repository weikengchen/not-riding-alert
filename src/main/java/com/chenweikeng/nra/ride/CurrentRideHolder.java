package com.chenweikeng.nra.ride;

/**
 * Holds the currently ridden ride from the scoreboard sidebar. Null when the "Current Ride" block
 * is not shown (player not riding).
 */
public class CurrentRideHolder {
  private static RideName currentRide = null;
  private static Integer currentProgressPercent = null;
  private static Integer elapsedSeconds = null;

  public static RideName getCurrentRide() {
    return currentRide;
  }

  public static void setCurrentRide(RideName ride) {
    currentRide = ride;
    if (ride == null) {
      currentProgressPercent = null;
      elapsedSeconds = null;
    }
  }

  public static Integer getCurrentProgressPercent() {
    return currentProgressPercent;
  }

  public static void setCurrentProgressPercent(Integer percent) {
    currentProgressPercent = percent;
  }

  public static Integer getElapsedSeconds() {
    return elapsedSeconds;
  }

  public static void setElapsedSeconds(Integer seconds) {
    elapsedSeconds = seconds;
  }
}
