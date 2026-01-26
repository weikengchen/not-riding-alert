package com.chenweikeng.nra.strategy;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.ride.CurrentRideHolder;
import com.chenweikeng.nra.ride.RideName;
import com.chenweikeng.nra.util.TimeFormatUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Handles rendering of strategy recommendations on the HUD. */
public class StrategyHudRenderer {
  private static List<RideGoal> topGoals = new ArrayList<>();
  private static int updateCounter = 0;
  private static final int UPDATE_INTERVAL_TICKS = 40; // Update every 2 seconds (40 ticks)
  private static String currentError = null; // Stores the latest error message

  /** Updates the top goals to display. Should be called periodically. */
  public static void update() {
    updateCounter++;
    if (updateCounter >= UPDATE_INTERVAL_TICKS) {
      updateCounter = 0;
      int displayCount = NotRidingAlertClient.getConfig().getRideDisplayCount();
      topGoals = StrategyCalculator.getTopGoals(displayCount);
    }
  }

  /**
   * Sets an error message to display on the HUD.
   *
   * @param error The error message (null to clear)
   */
  public static void setError(String error) {
    currentError = error;
  }

  /** Gets the current error message. */
  public static String getError() {
    return currentError;
  }

  /**
   * Renders the strategy recommendations on the HUD.
   *
   * @param context The GUI graphics context
   */
  public static void render(GuiGraphics context, DeltaTracker tickCounter) {
    update();

    Minecraft client = Minecraft.getInstance();
    if (client == null || client.player == null || client.font == null) {
      return;
    }

    int xLeft = 50;
    int xRight = 450; // Right column x position
    int y = 50;
    int lineHeight = 10;
    int colorRed = 0xFFFF0000; // Red
    int colorGreen = 0xFF00FF00; // Green (for goal matching current ride)
    int errorColor = 0xFFFF6600; // Orange for errors

    // Render error at the top if present
    if (currentError != null && !currentError.isEmpty()) {
      context.drawString(client.font, "ERROR: " + currentError, xLeft, y, errorColor, false);
      y += lineHeight; // Move down for the goals
    }

    RideName currentRide = CurrentRideHolder.getCurrentRide();
    if (topGoals.isEmpty() && currentRide == null) {
      return;
    }
    int displayCount = NotRidingAlertClient.getConfig().getRideDisplayCount();
    boolean currentRideInTop =
        currentRide != null && topGoals.stream().anyMatch(g -> g.getRide() == currentRide);

    // Split goals into columns based on display count
    List<RideGoal> leftGoals;
    List<RideGoal> rightGoals = new ArrayList<>();

    if (displayCount < 8) {
      // Single column: all goals on the left
      leftGoals = topGoals;
    } else {
      // Two columns: left gets one more if odd, otherwise split evenly
      int leftCount = (displayCount + 1) / 2; // Left gets one more if odd
      leftGoals = topGoals.size() > leftCount ? topGoals.subList(0, leftCount) : topGoals;
      if (topGoals.size() > leftCount) {
        rightGoals = topGoals.subList(leftCount, Math.min(displayCount, topGoals.size()));
      }
    }

    // Render left column
    for (int i = 0; i < leftGoals.size(); i++) {
      RideGoal goal = leftGoals.get(i);
      String rideName = goal.getRide().getDisplayName();
      // Add progress percentage if this is the current ride and progress is available
      if (currentRide != null && currentRide != RideName.UNKNOWN && goal.getRide() == currentRide) {
        Integer progress = CurrentRideHolder.getCurrentProgressPercent();
        if (progress != null) {
          rideName += " (" + progress + "%)";
        }
      }
      String text =
          String.format(
              "%s - %d rides needed, %s",
              rideName,
              goal.getRidesNeeded(),
              TimeFormatUtil.formatDuration(goal.getTimeNeededSeconds()));
      int color = (currentRide != null && goal.getRide() == currentRide) ? colorGreen : colorRed;
      context.drawString(client.font, text, xLeft, y + (i * lineHeight), color, false);
    }

    // Render right column (only if displayCount >= 8)
    if (displayCount >= 8) {
      for (int i = 0; i < rightGoals.size(); i++) {
        RideGoal goal = rightGoals.get(i);
        String rideName = goal.getRide().getDisplayName();
        // Add progress percentage if this is the current ride and progress is available
        if (currentRide != null
            && currentRide != RideName.UNKNOWN
            && goal.getRide() == currentRide) {
          Integer progress = CurrentRideHolder.getCurrentProgressPercent();
          if (progress != null) {
            rideName += " (" + progress + "%)";
          }
        }
        String text =
            String.format(
                "%s - %d rides needed, %s",
                rideName,
                goal.getRidesNeeded(),
                TimeFormatUtil.formatDuration(goal.getTimeNeededSeconds()));
        int color = (currentRide != null && goal.getRide() == currentRide) ? colorGreen : colorRed;
        context.drawString(client.font, text, xRight, y + (i * lineHeight), color, false);
      }
    }

    // If currently riding and that ride is not in the displayed goals, show it after a blank line
    // in green
    if (currentRide != null && currentRide != RideName.UNKNOWN && !currentRideInTop) {
      int maxColumnHeight = Math.max(leftGoals.size(), rightGoals.size());
      int extraY = y + (maxColumnHeight * lineHeight) + lineHeight; // blank line, then current ride
      RideGoal currentGoal = StrategyCalculator.getGoalForRide(currentRide);
      String rideName = currentRide.getDisplayName();
      // Add progress percentage if available
      Integer progress = CurrentRideHolder.getCurrentProgressPercent();
      if (progress != null) {
        rideName += " (" + progress + "%)";
      }
      String text =
          currentGoal != null
              ? String.format(
                  "%s - %d rides needed, %s",
                  rideName,
                  currentGoal.getRidesNeeded(),
                  TimeFormatUtil.formatDuration(currentGoal.getTimeNeededSeconds()))
              : "Riding: " + rideName;
      context.drawString(client.font, text, xLeft, extraY, colorGreen, false);
    }
  }

  public static List<RideGoal> getTopGoals() {
    return new ArrayList<>(topGoals);
  }
}
