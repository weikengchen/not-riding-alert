package com.chenweikeng.nra.strategy;

import com.chenweikeng.nra.ride.CurrentRideHolder;
import com.chenweikeng.nra.ride.RideName;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles rendering of strategy recommendations on the HUD.
 */
public class StrategyHudRenderer {
    private static List<RideGoal> topGoals = new ArrayList<>();
    private static int updateCounter = 0;
    private static final int UPDATE_INTERVAL_TICKS = 40; // Update every 2 seconds (40 ticks)
    private static String currentError = null; // Stores the latest error message
    
    /**
     * Updates the top goals to display.
     * Should be called periodically.
     */
    public static void update() {
        updateCounter++;
        if (updateCounter >= UPDATE_INTERVAL_TICKS) {
            updateCounter = 0;
            topGoals = StrategyCalculator.getTopGoals(8);
        }
    }
    
    /**
     * Sets an error message to display on the HUD.
     * @param error The error message (null to clear)
     */
    public static void setError(String error) {
        currentError = error;
    }
    
    /**
     * Gets the current error message.
     */
    public static String getError() {
        return currentError;
    }
    
    /**
     * Renders the strategy recommendations on the HUD.
     * @param context The GUI graphics context
     */
    public static void render(DrawContext context, RenderTickCounter tickCounter){
        update();
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.textRenderer == null) {
            return;
        }
        
        int x = 50;
        int y = 50;
        int lineHeight = 10;
        int colorRed = 0xFFFF0000; // Red
        int colorGreen = 0xFF00FF00; // Green (for goal matching current ride)
        int errorColor = 0xFFFF6600; // Orange for errors
        
        // Render error at the top if present
        if (currentError != null && !currentError.isEmpty()) {
            context.drawText(client.textRenderer, "ERROR: " + currentError, x, y, errorColor, false);
            y += lineHeight; // Move down for the goals
        }
        
        RideName currentRide = CurrentRideHolder.getCurrentRide();
        if (topGoals.isEmpty() && currentRide == null) {
            return;
        }
        boolean currentRideInTop8 = currentRide != null && topGoals.stream().anyMatch(g -> g.getRide() == currentRide);

        // Render top 8 goals
        for (int i = 0; i < topGoals.size(); i++) {
            RideGoal goal = topGoals.get(i);
            String text = String.format("%s - %d rides needed, %s",
                goal.getRide().getDisplayName(),
                goal.getRidesNeeded(),
                formatTime(goal.getTimeNeededSeconds()));
            int color = (currentRide != null && goal.getRide() == currentRide) ? colorGreen : colorRed;
            context.drawText(client.textRenderer, text, x, y + (i * lineHeight), color, false);
        }

        // If currently riding and that ride is not in the top 8, show it after a blank line in green
        if (currentRide != null && !currentRideInTop8) {
            int extraY = y + (topGoals.size() * lineHeight) + lineHeight; // blank line, then current ride
            RideGoal currentGoal = StrategyCalculator.getGoalForRide(currentRide);
            String text = currentGoal != null
                ? String.format("%s - %d rides needed, %s",
                    currentGoal.getRide().getDisplayName(),
                    currentGoal.getRidesNeeded(),
                    formatTime(currentGoal.getTimeNeededSeconds()))
                : "Riding: " + currentRide.getDisplayName();
            context.drawText(client.textRenderer, text, x, extraY, colorGreen, false);
        }
    }
    
    /**
     * Formats time in seconds to a readable string.
     */
    private static String formatTime(long seconds) {
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
    
    /**
     * Gets the current top goals (for testing/debugging).
     */
    public static List<RideGoal> getTopGoals() {
        return new ArrayList<>(topGoals);
    }
}
