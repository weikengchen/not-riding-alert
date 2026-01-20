package com.chenweikeng.nra.ride;

/**
 * Holds the currently ridden ride from the scoreboard sidebar.
 * Null when the "Current Ride" block is not shown (player not riding).
 */
public class CurrentRideHolder {
    private static RideName currentRide = null;
    private static Integer currentProgressPercent = null; // Percentage (0-100), null if not available

    public static RideName getCurrentRide() {
        return currentRide;
    }

    public static void setCurrentRide(RideName ride) {
        currentRide = ride;
        // Clear progress when ride changes
        if (ride == null) {
            currentProgressPercent = null;
        }
    }
    
    /**
     * Gets the current progress percentage (0-100), or null if not available.
     */
    public static Integer getCurrentProgressPercent() {
        return currentProgressPercent;
    }
    
    /**
     * Sets the current progress percentage (0-100), or null to clear.
     */
    public static void setCurrentProgressPercent(Integer percent) {
        currentProgressPercent = percent;
    }
}
