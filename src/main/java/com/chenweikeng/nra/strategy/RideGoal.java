package com.chenweikeng.nra.strategy;

import com.chenweikeng.nra.ride.RideName;

/**
 * Represents a goal for a specific ride with information about progress and time needed.
 */
public class RideGoal {
    private final RideName ride;
    private final int currentCount;
    private final int nextGoal;
    private final int ridesNeeded;
    private final long timeNeededSeconds; // Time in seconds needed to reach the goal
    
    public RideGoal(RideName ride, int currentCount, int nextGoal, int ridesNeeded, long timeNeededSeconds) {
        this.ride = ride;
        this.currentCount = currentCount;
        this.nextGoal = nextGoal;
        this.ridesNeeded = ridesNeeded;
        this.timeNeededSeconds = timeNeededSeconds;
    }
    
    public RideName getRide() {
        return ride;
    }
    
    public int getCurrentCount() {
        return currentCount;
    }
    
    public int getNextGoal() {
        return nextGoal;
    }
    
    public int getRidesNeeded() {
        return ridesNeeded;
    }
    
    public long getTimeNeededSeconds() {
        return timeNeededSeconds;
    }
    
    @Override
    public String toString() {
        return String.format("%s - %d rides needed, %s", 
            ride.getDisplayName(), 
            ridesNeeded, 
            formatTime(timeNeededSeconds));
    }
    
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            if (remainingSeconds == 0) {
                return minutes + "m";
            }
            return minutes + "m " + remainingSeconds + "s";
        } else {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            if (remainingMinutes == 0) {
                return hours + "h";
            }
            return hours + "h " + remainingMinutes + "m";
        }
    }
}
