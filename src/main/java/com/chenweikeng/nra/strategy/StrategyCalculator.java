package com.chenweikeng.nra.strategy;

import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.ride.RegionHolder;
import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Calculates the best strategy for reaching ride count goals. */
public class StrategyCalculator {
  private static final int[] GOALS = {1, 10, 100, 500, 1000, 5000, 10000};
  private static final int MAX_GOAL = 10000;

  /**
   * Calculates the top N easiest goals (goals that take the least time to reach).
   *
   * @param topN The number of top goals to return
   * @return List of RideGoal objects sorted by time needed (easiest first)
   */
  public static List<RideGoal> getTopGoals(int topN) {
    List<RideGoal> allGoals = new ArrayList<>();
    RideCountManager countManager = RideCountManager.getInstance();

    // Get config for filtering
    Integer minRideTimeMinutes = ModConfig.getInstance().minRideTimeMinutes;
    boolean onlyAutograbbing = ModConfig.getInstance().onlyAutograbbing;

    // Calculate goals for each ride
    for (RideName ride : RideName.values()) {
      // Skip UNKNOWN ride
      if (ride == RideName.UNKNOWN) {
        continue;
      }

      // Skip non-autograbbing rides if onlyAutograbbing is enabled
      if (onlyAutograbbing && !RegionHolder.hasAutograb(ride)) {
        continue;
      }

      // Skip hidden rides
      if (ModConfig.getInstance().hiddenRides.contains(ride.getDisplayName())) {
        continue;
      }

      int currentCount = countManager.getRideCount(ride);

      // Skip if already at max goal
      if (currentCount >= MAX_GOAL) {
        continue;
      }

      // Find the next goal
      int nextGoal = findNextGoal(currentCount);

      if (nextGoal == -1) {
        continue; // No more goals
      }

      // Calculate rides needed and time
      int ridesNeeded = nextGoal - currentCount;
      int rideTimeSeconds = ride.getRideTime();

      // Skip if ride time is invalid (99999 means not provided)
      if (rideTimeSeconds >= 99999) {
        StrategyHudRenderer.setError("Ride time not provided for: " + ride.getDisplayName());
        continue;
      }

      // Skip if ride time is below minimum filter
      if (minRideTimeMinutes != null) {
        int rideTimeMinutes = rideTimeSeconds / 60;
        if (rideTimeMinutes < minRideTimeMinutes) {
          continue;
        }
      }

      long timeNeededSeconds = (long) ridesNeeded * rideTimeSeconds;

      allGoals.add(new RideGoal(ride, currentCount, nextGoal, ridesNeeded, timeNeededSeconds));
    }

    // Sort by time needed (easiest first)
    allGoals.sort(Comparator.comparingLong(RideGoal::getTimeNeededSeconds));

    // Return top N
    int returnCount = Math.min(topN, allGoals.size());
    return allGoals.subList(0, returnCount);
  }

  /**
   * Returns a RideGoal for a single ride, or null if it can't be computed (e.g. UNKNOWN, or ride
   * time not set). Does not apply seasonal/hidden filters (for use when showing the current ride
   * outside the top list).
   */
  public static RideGoal getGoalForRide(RideName ride) {
    if (ride == null || ride == RideName.UNKNOWN) {
      return null;
    }
    RideCountManager countManager = RideCountManager.getInstance();
    int currentCount = countManager.getRideCount(ride);
    int nextGoal = findNextGoal(currentCount);
    if (nextGoal == -1) {
      return new RideGoal(ride, currentCount, currentCount, 0, 0);
    }
    int ridesNeeded = nextGoal - currentCount;
    int rideTimeSeconds = ride.getRideTime();
    if (rideTimeSeconds >= 99999) {
      return null;
    }
    long timeNeededSeconds = (long) ridesNeeded * rideTimeSeconds;
    return new RideGoal(ride, currentCount, nextGoal, ridesNeeded, timeNeededSeconds);
  }

  /**
   * Finds the next goal for a given current count.
   *
   * @param currentCount The current ride count
   * @return The next goal, or -1 if no more goals (already at max)
   */
  private static int findNextGoal(int currentCount) {
    for (int goal : GOALS) {
      if (currentCount < goal) {
        return goal;
      }
    }
    return -1; // Already at max goal
  }
}
