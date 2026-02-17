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

  private record LayoutInput(
      Minecraft client,
      List<RideGoal> goals,
      RideName currentRide,
      RideName regionRide,
      RideName effectiveRide,
      boolean currentRideInTop,
      boolean isPassenger,
      String error,
      int availableWidth,
      int gap) {}

  private record LayoutDecision(boolean useShortNames, boolean twoColumns, boolean visible) {}

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

    if (NotRidingAlertClient.isMonkeyAttached()) {
      return;
    }

    update();

    Minecraft client = Minecraft.getInstance();
    if (client == null || client.player == null || client.font == null) {
      return;
    }

    int screenWidth = client.getWindow().getGuiScaledWidth();
    int xLeft = 50;
    int xRight = screenWidth - 50;
    int y = 50;
    int lineHeight = 10;
    int colorRed = 0xFFFF0000; // Red
    int colorGreen = 0xFF00FF00; // Green (for goal matching current ride)
    int colorPurple = 0xFFEE00FF; // Purple (for autograbbing)
    int errorColor = 0xFFFF6600; // Orange for errors

    int displayCount = ModConfig.getInstance().rideDisplayCount;

    RideName currentRide = CurrentRideHolder.getCurrentRide();
    RideName regionRide =
        ModConfig.getInstance().autograb ? RegionHolder.getRideAtLocation(client) : null;
    RideName effectiveRide = currentRide != null ? currentRide : regionRide;
    boolean currentRideInTop =
        effectiveRide != null && topGoals.stream().anyMatch(g -> g.getRide() == effectiveRide);
    boolean isPassengerForLayout = client.player.isPassenger();

    int topGoalsSize = topGoals.size();
    boolean wantsTwoColumns = topGoalsSize >= 8;
    int availableWidth = xRight - xLeft;
    int gap = 10;
    List<RideGoal> goalsForFit = displayCount > 0 ? topGoals : List.of();

    LayoutInput layoutInput =
        new LayoutInput(
            client,
            goalsForFit,
            currentRide,
            regionRide,
            effectiveRide,
            currentRideInTop,
            isPassengerForLayout,
            currentError,
            availableWidth,
            gap);
    LayoutDecision decision =
        decideLayout(layoutInput, ModConfig.getInstance().displayShortName, wantsTwoColumns);
    if (!decision.visible) {
      return;
    }
    boolean useShortNames = decision.useShortNames;
    boolean twoColumns = decision.twoColumns;
    boolean isPassenger = client.player.isPassenger();

    if (currentError != null && !currentError.isEmpty()) {
      context.drawString(client.font, "ERROR: " + currentError, xLeft, y, errorColor, false);
      y += lineHeight;
    }

    int maxColumnHeight = 0;

    if (displayCount > 0) {
      List<RideGoal> leftGoals;
      List<RideGoal> rightGoals;
      if (!twoColumns) {
        leftGoals = topGoals;
        rightGoals = List.of();
      } else {
        int leftCount = (topGoalsSize + 1) / 2;
        leftGoals = topGoals.subList(0, Math.min(leftCount, topGoalsSize));
        rightGoals =
            topGoalsSize > leftCount ? topGoals.subList(leftCount, topGoalsSize) : List.of();
      }

      // Render left column
      for (int i = 0; i < leftGoals.size(); i++) {
        RideGoal goal = leftGoals.get(i);
        FormattedRide formattedRide =
            formatRideName(goal.getRide(), currentRide, regionRide, useShortNames, isPassenger);
        String text = formatGoalText(formattedRide, goal);
        int color = getColorForStatus(formattedRide.getStatus(), colorRed, colorGreen, colorPurple);
        context.drawString(client.font, text, xLeft, y + (i * lineHeight), color, false);
      }

      if (twoColumns) {
        for (int i = 0; i < rightGoals.size(); i++) {
          RideGoal goal = rightGoals.get(i);
          FormattedRide formattedRide =
              formatRideName(goal.getRide(), currentRide, regionRide, useShortNames, isPassenger);
          String text = formatGoalText(formattedRide, goal);
          int color =
              getColorForStatus(formattedRide.getStatus(), colorRed, colorGreen, colorPurple);
          int textWidth = client.font.width(text);
          context.drawString(
              client.font, text, xRight - textWidth, y + (i * lineHeight), color, false);
        }
      }

      maxColumnHeight = Math.max(leftGoals.size(), rightGoals.size());
    }

    // If currently riding (or in region) and that ride is not in the displayed goals, show it after
    // a blank line in green
    if (effectiveRide != null && effectiveRide != RideName.UNKNOWN && !currentRideInTop) {
      int extraY = y + (maxColumnHeight * lineHeight) + lineHeight; // blank line, then current ride
      RideGoal currentGoal = StrategyCalculator.getGoalForRide(effectiveRide);
      FormattedRide formattedRide =
          formatRideName(effectiveRide, currentRide, regionRide, useShortNames, isPassenger);
      String text =
          currentGoal != null
              ? formatGoalText(formattedRide, currentGoal)
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
      RideName ride,
      RideName currentRide,
      RideName regionRide,
      boolean useShortNames,
      boolean isPassenger) {
    String rideName = useShortNames ? ride.getShortName() : ride.getDisplayName();
    RideStatus status = RideStatus.NORMAL;

    if (currentRide != null && ride == currentRide) {
      Integer progress = CurrentRideHolder.getCurrentProgressPercent();
      if (progress != null) {
        rideName += " (" + progress + "%)";
      }
      status = RideStatus.RIDING;
    } else if (currentRide == null && regionRide != null && ride == regionRide && !isPassenger) {
      rideName += " (Autograbbing" + getAnimatedDots() + ")";
      status = RideStatus.AUTOGRABBING;
    }

    return new FormattedRide(rideName, status);
  }

  private static String formatGoalText(FormattedRide formattedRide, RideGoal goal) {
    return String.format(
        "%s - %d more, %s",
        formattedRide.getName(),
        goal.getRidesNeeded(),
        TimeFormatUtil.formatDuration(goal.getTimeNeededSeconds()));
  }

  private static LayoutDecision decideLayout(
      LayoutInput layoutInput, boolean baseUseShortNames, boolean wantsTwoColumns) {
    boolean useShortNames = baseUseShortNames;
    boolean twoColumns = wantsTwoColumns;
    if (fitsLayout(layoutInput, useShortNames, twoColumns)) {
      return new LayoutDecision(useShortNames, twoColumns, true);
    }
    useShortNames = true;
    if (fitsLayout(layoutInput, useShortNames, twoColumns)) {
      return new LayoutDecision(useShortNames, twoColumns, true);
    }
    twoColumns = false;
    if (fitsLayout(layoutInput, useShortNames, twoColumns)) {
      return new LayoutDecision(useShortNames, twoColumns, true);
    }
    return new LayoutDecision(useShortNames, twoColumns, false);
  }

  private static boolean fitsLayout(
      LayoutInput layoutInput, boolean useShortNames, boolean twoColumns) {
    int maxWidth = 0;
    if (layoutInput.error != null && !layoutInput.error.isEmpty()) {
      maxWidth = Math.max(maxWidth, layoutInput.client.font.width("ERROR: " + layoutInput.error));
    }

    if (!layoutInput.goals.isEmpty()) {
      int goalsSize = layoutInput.goals.size();
      if (!twoColumns || goalsSize < 8) {
        int leftMax = computeMaxWidth(layoutInput, layoutInput.goals, useShortNames);
        maxWidth = Math.max(maxWidth, leftMax);
      } else {
        int leftCount = (goalsSize + 1) / 2;
        List<RideGoal> leftGoals = layoutInput.goals.subList(0, Math.min(leftCount, goalsSize));
        List<RideGoal> rightGoals =
            goalsSize > leftCount ? layoutInput.goals.subList(leftCount, goalsSize) : List.of();

        int leftMax = computeMaxWidth(layoutInput, leftGoals, useShortNames);
        int rightMax = computeMaxWidth(layoutInput, rightGoals, useShortNames);
        if (leftMax + rightMax + layoutInput.gap > layoutInput.availableWidth) {
          return false;
        }
        maxWidth = Math.max(maxWidth, leftMax);
        maxWidth = Math.max(maxWidth, rightMax);
      }
    }

    if (layoutInput.effectiveRide != null
        && layoutInput.effectiveRide != RideName.UNKNOWN
        && !layoutInput.currentRideInTop) {
      RideGoal currentGoal = StrategyCalculator.getGoalForRide(layoutInput.effectiveRide);
      FormattedRide formattedRide =
          formatRideName(
              layoutInput.effectiveRide,
              layoutInput.currentRide,
              layoutInput.regionRide,
              useShortNames,
              layoutInput.isPassenger);
      String text =
          currentGoal != null
              ? formatGoalText(formattedRide, currentGoal)
              : "Riding: " + formattedRide.getName();
      maxWidth = Math.max(maxWidth, layoutInput.client.font.width(text));
    }

    return maxWidth <= layoutInput.availableWidth;
  }

  private static int computeMaxWidth(
      LayoutInput layoutInput, List<RideGoal> goals, boolean useShortNames) {
    int max = 0;
    for (RideGoal goal : goals) {
      FormattedRide formattedRide =
          formatRideName(
              goal.getRide(),
              layoutInput.currentRide,
              layoutInput.regionRide,
              useShortNames,
              layoutInput.isPassenger);
      String text = formatGoalText(formattedRide, goal);
      max = Math.max(max, layoutInput.client.font.width(text));
    }
    return max;
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
