package com.chenweikeng.nra.ride;

/**
 * Holds the currently ridden ride from the scoreboard sidebar.
 * Null when the "Current Ride" block is not shown (player not riding).
 */
public class CurrentRideHolder {
    private static RideName currentRide = null;

    public static RideName getCurrentRide() {
        return currentRide;
    }

    public static void setCurrentRide(RideName ride) {
        currentRide = ride;
    }
}
