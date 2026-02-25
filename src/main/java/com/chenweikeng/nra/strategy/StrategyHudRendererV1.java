package com.chenweikeng.nra.strategy;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.mixin.BossHealthOverlayAccessor;
import com.chenweikeng.nra.ride.CurrentRideHolder;
import com.chenweikeng.nra.ride.RegionHolder;
import com.chenweikeng.nra.ride.RideName;
import com.chenweikeng.nra.util.TimeFormatUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;

/** Handles rendering of strategy recommendations on the HUD. */
public class StrategyHudRendererV1 {
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

  private record LayoutDecision(boolean useShortNames, boolean visible) {}

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

    if (!ModConfig.getInstance().keepUnchanged && NotRidingAlertClient.isMonkeyAttached()) {
      return;
    }

    update();

    Minecraft client = Minecraft.getInstance();
    if (client == null || client.player == null || client.font == null) {
      return;
    }
    BossHealthOverlay bossOverlay = client.gui.getBossOverlay();
    Map<UUID, LerpingBossEvent> bossEvents = ((BossHealthOverlayAccessor) bossOverlay).getEvents();
    if (bossEvents != null && !bossEvents.isEmpty()) {
      return;
    }

    int screenWidth = client.getWindow().getGuiScaledWidth();
    int textPadding = 10;
    int xLeft = textPadding;
    int xRight = screenWidth - textPadding;
    int yStart = 0;
    int lineHeight = 10;
    int colorWhite = 0xFFFFFFFF;
    int colorGreen = 0xFF00FF00;
    int colorPurple = 0xFFEE00FF;
    int errorColor = 0xFFFF6600;

    int displayCount = ModConfig.getInstance().rideDisplayCount;

    RideName currentRide = CurrentRideHolder.getCurrentRide();
    RideName regionRide =
        ModConfig.getInstance().autograb ? RegionHolder.getRideAtLocation(client) : null;
    RideName effectiveRide = currentRide != null ? currentRide : regionRide;
    boolean currentRideInTop =
        effectiveRide != null && topGoals.stream().anyMatch(g -> g.getRide() == effectiveRide);
    boolean isPassengerForLayout = client.player.isPassenger();

    int topGoalsSize = topGoals.size();
    int halfWidth = screenWidth / 2 - textPadding * 2;
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
            halfWidth,
            0);
    LayoutDecision decision = decideLayout(layoutInput, ModConfig.getInstance().displayShortName);
    if (!decision.visible) {
      return;
    }
    boolean useShortNames = decision.useShortNames;
    boolean isPassenger = client.player.isPassenger();

    List<RideGoal> leftGoals;
    List<RideGoal> rightGoals;
    if (displayCount > 0) {
      int leftCount = (topGoalsSize + 1) / 2;
      leftGoals = topGoals.subList(0, Math.min(leftCount, topGoalsSize));
      rightGoals = topGoalsSize > leftCount ? topGoals.subList(leftCount, topGoalsSize) : List.of();
    } else {
      leftGoals = List.of();
      rightGoals = List.of();
    }

    int maxColumnHeight = Math.max(leftGoals.size(), rightGoals.size());

    boolean hasError = currentError != null && !currentError.isEmpty();
    boolean hasExtraRide =
        effectiveRide != null && effectiveRide != RideName.UNKNOWN && !currentRideInTop;

    int totalLines = (hasError ? 1 : 0) + maxColumnHeight + (hasExtraRide ? 2 : 0);
    int bgHeight = totalLines * lineHeight;

    int bgY1 = yStart;
    int bgY2 = yStart + bgHeight + 4;

    int opacity = ModConfig.getInstance().hudBackgroundOpacity;
    if (opacity > 0) {
      int alpha = (int) (opacity * 2.55);
      int bgColor = (alpha << 24);
      context.fill(0, bgY1, screenWidth, bgY2, bgColor);
    }

    int y = yStart + 2;

    if (hasError) {
      context.drawString(client.font, "ERROR: " + currentError, xLeft, y, errorColor, false);
      y += lineHeight;
    }

    if (displayCount > 0) {
      for (int i = 0; i < leftGoals.size(); i++) {
        RideGoal goal = leftGoals.get(i);
        FormattedRide formattedRide =
            formatRideName(goal.getRide(), currentRide, regionRide, useShortNames, isPassenger);
        String text = formatGoalText(formattedRide, goal);
        int color =
            getColorForStatus(formattedRide.getStatus(), colorWhite, colorGreen, colorPurple);
        context.drawString(client.font, text, xLeft, y + (i * lineHeight), color, false);
      }

      for (int i = 0; i < rightGoals.size(); i++) {
        RideGoal goal = rightGoals.get(i);
        FormattedRide formattedRide =
            formatRideName(goal.getRide(), currentRide, regionRide, useShortNames, isPassenger);
        String text = formatGoalText(formattedRide, goal);
        int color =
            getColorForStatus(formattedRide.getStatus(), colorWhite, colorGreen, colorPurple);
        int textWidth = client.font.width(text);
        context.drawString(
            client.font, text, xRight - textWidth, y + (i * lineHeight), color, false);
      }
    }

    if (hasExtraRide) {
      int extraY = yStart + 2 + ((hasError ? 1 : 0) + maxColumnHeight + 1) * lineHeight;
      RideGoal currentGoal = StrategyCalculator.getGoalForRide(effectiveRide);
      FormattedRide formattedRide =
          formatRideName(effectiveRide, currentRide, regionRide, useShortNames, isPassenger);
      String text =
          currentGoal != null
              ? formatGoalText(formattedRide, currentGoal)
              : "Riding: " + formattedRide.getName();
      int color = getColorForStatus(formattedRide.getStatus(), colorWhite, colorGreen, colorPurple);
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
      Integer elapsed = CurrentRideHolder.getElapsedSeconds();
      Integer progress = CurrentRideHolder.getCurrentProgressPercent();
      if (elapsed != null && progress != null) {
        int totalSeconds = ride.getRideTime();
        int remainingSeconds = totalSeconds - elapsed;
        if (remainingSeconds < 0) {
          remainingSeconds = 0;
        }
        String timeLeft = TimeFormatUtil.formatDuration(remainingSeconds);
        rideName += " (" + progress + "%, " + timeLeft + " left)";
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

  private static LayoutDecision decideLayout(LayoutInput layoutInput, boolean baseUseShortNames) {
    boolean useShortNames = baseUseShortNames;
    if (fitsLayout(layoutInput, useShortNames)) {
      return new LayoutDecision(useShortNames, true);
    }
    useShortNames = true;
    if (fitsLayout(layoutInput, useShortNames)) {
      return new LayoutDecision(useShortNames, true);
    }
    return new LayoutDecision(useShortNames, false);
  }

  private static boolean fitsLayout(LayoutInput layoutInput, boolean useShortNames) {
    int maxWidth = 0;
    if (layoutInput.error != null && !layoutInput.error.isEmpty()) {
      maxWidth = Math.max(maxWidth, layoutInput.client.font.width("ERROR: " + layoutInput.error));
    }

    if (!layoutInput.goals.isEmpty()) {
      int goalsSize = layoutInput.goals.size();
      int leftCount = (goalsSize + 1) / 2;
      List<RideGoal> leftGoals = layoutInput.goals.subList(0, Math.min(leftCount, goalsSize));
      List<RideGoal> rightGoals =
          goalsSize > leftCount ? layoutInput.goals.subList(leftCount, goalsSize) : List.of();

      int leftMax = computeMaxWidth(layoutInput, leftGoals, useShortNames);
      int rightMax = computeMaxWidth(layoutInput, rightGoals, useShortNames);
      maxWidth = Math.max(maxWidth, leftMax);
      maxWidth = Math.max(maxWidth, rightMax);
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
