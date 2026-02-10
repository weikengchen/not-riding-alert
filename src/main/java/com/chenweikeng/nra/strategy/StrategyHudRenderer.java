package com.chenweikeng.nra.strategy;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.ride.CurrentRideHolder;
import com.chenweikeng.nra.ride.RegionHolder;
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

  private enum RideStatus {
    NORMAL,
    RIDING,
    AUTOGRABBING
  }

  private static class FormattedRide {
    private final String name;
    private final RideStatus status;

    FormattedRide(String name, RideStatus status) {
      this.name = name;
      this.status = status;
    }

    String getName() {
      return name;
    }

    RideStatus getStatus() {
      return status;
    }
  }

  /** Updates the top goals to display. Should be called periodically. */
  public static void update() {
    updateCounter++;
    if (updateCounter >= UPDATE_INTERVAL_TICKS) {
      updateCounter = 0;
      int displayCount = ModConfig.getInstance().rideDisplayCount;
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
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }

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
    int colorPurple = 0xFFEE00FF; // Purple (for autograbbing)
    int errorColor = 0xFFFF6600; // Orange for errors

    // Render error at the top if present
    if (currentError != null && !currentError.isEmpty()) {
      context.drawString(client.font, "ERROR: " + currentError, xLeft, y, errorColor, false);
      y += lineHeight; // Move down for the goals
    }

    int displayCount = ModConfig.getInstance().rideDisplayCount;
    if (displayCount == 0) {
      return;
    }

    RideName currentRide = CurrentRideHolder.getCurrentRide();
    RideName regionRide =
        ModConfig.getInstance().autograb && !client.player.isPassenger()
            ? RegionHolder.getRideAtLocation(client.player)
            : null;
    RideName effectiveRide = currentRide != null ? currentRide : regionRide;
    boolean currentRideInTop =
        effectiveRide != null && topGoals.stream().anyMatch(g -> g.getRide() == effectiveRide);

    // Split goals into columns based on actual number of goals
    List<RideGoal> leftGoals;
    List<RideGoal> rightGoals = new ArrayList<>();
    int topGoalsSize = topGoals.size();

    if (topGoalsSize < 8) {
      // Single column: all goals on the left
      leftGoals = topGoals;
    } else {
      // Two columns: left gets one more if odd, otherwise split evenly based on actual goals
      int leftCount = (topGoalsSize + 1) / 2; // Left gets one more if odd
      leftGoals = topGoals.subList(0, Math.min(leftCount, topGoalsSize));
      if (topGoalsSize > leftCount) {
        rightGoals = topGoals.subList(leftCount, topGoalsSize);
      }
    }

    // Render left column
    for (int i = 0; i < leftGoals.size(); i++) {
      RideGoal goal = leftGoals.get(i);
      FormattedRide formattedRide = formatRideName(goal.getRide(), currentRide, regionRide);
      String text =
          String.format(
              "%s - %d rides needed, %s",
              formattedRide.getName(),
              goal.getRidesNeeded(),
              TimeFormatUtil.formatDuration(goal.getTimeNeededSeconds()));
      int color = getColorForStatus(formattedRide.getStatus(), colorRed, colorGreen, colorPurple);
      context.drawString(client.font, text, xLeft, y + (i * lineHeight), color, false);
    }

    // Render right column (only if topGoalsSize >= 8)
    if (topGoalsSize >= 8) {
      for (int i = 0; i < rightGoals.size(); i++) {
        RideGoal goal = rightGoals.get(i);
        FormattedRide formattedRide = formatRideName(goal.getRide(), currentRide, regionRide);
        String text =
            String.format(
                "%s - %d rides needed, %s",
                formattedRide.getName(),
                goal.getRidesNeeded(),
                TimeFormatUtil.formatDuration(goal.getTimeNeededSeconds()));
        int color = getColorForStatus(formattedRide.getStatus(), colorRed, colorGreen, colorPurple);
        context.drawString(client.font, text, xRight, y + (i * lineHeight), color, false);
      }
    }

    // If currently riding (or in region) and that ride is not in the displayed goals, show it after
    // a
    // blank line in green
    if (effectiveRide != null && effectiveRide != RideName.UNKNOWN && !currentRideInTop) {
      int maxColumnHeight = Math.max(leftGoals.size(), rightGoals.size());
      int extraY = y + (maxColumnHeight * lineHeight) + lineHeight; // blank line, then current ride
      RideGoal currentGoal = StrategyCalculator.getGoalForRide(effectiveRide);
      FormattedRide formattedRide = formatRideName(effectiveRide, currentRide, regionRide);
      String text =
          currentGoal != null
              ? String.format(
                  "%s - %d rides needed, %s",
                  formattedRide.getName(),
                  currentGoal.getRidesNeeded(),
                  TimeFormatUtil.formatDuration(currentGoal.getTimeNeededSeconds()))
              : "Riding: " + formattedRide.getName();
      int color = getColorForStatus(formattedRide.getStatus(), colorRed, colorGreen, colorPurple);
      context.drawString(client.font, text, xLeft, extraY, color, false);
    }
  }

  public static List<RideGoal> getTopGoals() {
    return new ArrayList<>(topGoals);
  }

  private static String getAnimatedDots() {
    long currentTimeMillis = System.currentTimeMillis();
    int quarterSecond = (int) ((currentTimeMillis % 2000) / 500);
    return switch (quarterSecond) {
      case 0 -> "";
      case 1 -> ".";
      case 2 -> "..";
      case 3 -> "...";
      default -> "";
    };
  }

  private static FormattedRide formatRideName(
      RideName ride, RideName currentRide, RideName regionRide) {
    String rideName = ride.getDisplayName();
    RideStatus status = RideStatus.NORMAL;

    if (currentRide != null && ride == currentRide) {
      Integer progress = CurrentRideHolder.getCurrentProgressPercent();
      if (progress != null) {
        rideName += " (" + progress + "%)";
      }
      status = RideStatus.RIDING;
    } else if (currentRide == null && regionRide != null && ride == regionRide) {
      rideName += " (Autograbbing" + getAnimatedDots() + ")";
      status = RideStatus.AUTOGRABBING;
    }

    return new FormattedRide(rideName, status);
  }

  private static int getColorForStatus(
      RideStatus status, int colorRed, int colorGreen, int colorLightBlue) {
    return switch (status) {
      case RIDING -> colorGreen;
      case AUTOGRABBING -> colorLightBlue;
      case NORMAL -> colorRed;
    };
  }
}
