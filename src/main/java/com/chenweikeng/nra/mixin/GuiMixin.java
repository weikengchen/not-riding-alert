package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.ride.CurrentRideHolder;
import com.chenweikeng.nra.ride.LastRideHolder;
import com.chenweikeng.nra.ride.RideName;
import java.util.Comparator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
  @Shadow private static Comparator<?> SCORE_DISPLAY_ORDER;

  /** Helper class to store team information */
  private static class TeamInfo {
    private final String teamName;
    private final String prefixStr;

    public TeamInfo(String teamName, String prefixStr) {
      this.teamName = teamName;
      this.prefixStr = prefixStr;
    }

    public String getTeamName() {
      return teamName;
    }

    public String getPrefixStr() {
      return prefixStr;
    }
  }

  @Inject(
      at = @At("HEAD"),
      method =
          "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V",
      cancellable = true)
  private void onRenderScoreboardSidebar(
      GuiGraphics context, Objective objective, CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    Scoreboard scoreboard = objective.getScoreboard();

    @SuppressWarnings("unchecked")
    Comparator<Object> comparator = (Comparator<Object>) SCORE_DISPLAY_ORDER;

    // Extract team information into a sorted array
    java.util.List<TeamInfo> teamInfos = new java.util.ArrayList<>();
    scoreboard.listPlayerScores(objective).stream()
        .filter(score -> !score.isHidden())
        .sorted(comparator)
        .limit(15L)
        .forEach(
            scoreboardEntry -> {
              PlayerTeam team = scoreboard.getPlayersTeam(scoreboardEntry.owner());
              if (team == null) return;
              String teamName = team.getName();
              Component prefixText = team.getPlayerPrefix();
              String prefixStr = prefixText != null ? prefixText.getString() : "null";
              teamInfos.add(new TeamInfo(teamName, prefixStr));
            });

    // Sort by team name to ensure consistent ordering
    teamInfos.sort(java.util.Comparator.comparing(TeamInfo::getTeamName));

    // Find the team with "Current Ride"
    int currentRideIndex = -1;
    for (int i = 0; i < teamInfos.size(); i++) {
      if (teamInfos.get(i).getPrefixStr().contains("Current Ride")) {
        currentRideIndex = i;
        break;
      }
    }

    // Extract ride name and time from subsequent rows
    String currentRidePrefix = null;
    String timePrefix = null;

    if (currentRideIndex >= 0 && currentRideIndex + 1 < teamInfos.size()) {
      currentRidePrefix = teamInfos.get(currentRideIndex + 1).getPrefixStr();
      if (currentRideIndex + 2 < teamInfos.size()) {
        timePrefix = teamInfos.get(currentRideIndex + 2).getPrefixStr();
      }
    }

    // Process the current ride information
    if (currentRidePrefix == null) {
      CurrentRideHolder.setCurrentRide(null);
    } else {
      RideName resolved = RideName.fromTruncatedString(currentRidePrefix);
      CurrentRideHolder.setCurrentRide(resolved);
      LastRideHolder.setLastRide(resolved);

      // Calculate progress from time prefix if available and ride is not excluded
      if (resolved != RideName.DAVY_CROCKETTS_EXPLORER_CANOES && timePrefix != null) {
        int elapsedSeconds = parseTimeString(timePrefix);
        if (elapsedSeconds >= 0) {
          int rideTimeSeconds = resolved.getRideTime();
          if (rideTimeSeconds > 0) {
            int percent = Math.min(100, Math.max(0, (elapsedSeconds * 100) / rideTimeSeconds));
            CurrentRideHolder.setCurrentProgressPercent(percent);
          } else {
            CurrentRideHolder.setCurrentProgressPercent(null);
          }
        } else {
          CurrentRideHolder.setCurrentProgressPercent(null);
        }
      } else {
        CurrentRideHolder.setCurrentProgressPercent(null);
      }
    }

    // Check if scoreboard rendering should be cancelled (after processing ride info)
    if (NotRidingAlertClient.getConfig().isHideScoreboard()) {
      ci.cancel();
    }
  }

  @Inject(
      at = @At("HEAD"),
      method =
          "renderChat(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
      cancellable = true)
  private void onRenderChat(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    // Check if chat should be hidden
    if (NotRidingAlertClient.getConfig().isHideChat()) {
      ci.cancel();
    }
  }

  @Inject(at = @At("HEAD"), method = "renderPlayerHealth", cancellable = true)
  private void onRenderPlayerHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    // Check if player health should be hidden
    if (NotRidingAlertClient.getConfig().isHideHealth()) {
      ci.cancel();
    }
  }

  @Inject(at = @At("HEAD"), method = "renderVehicleHealth", cancellable = true)
  private void onRenderVehicleHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    // Check if vehicle health should be hidden
    if (NotRidingAlertClient.getConfig().isHideHealth()) {
      ci.cancel();
    }
  }

  /**
   * Parses a time string like "⏐ Time: 5s", "⏐ Time: 1m", "⏐ Time: 1m 5s" to seconds. Returns -1 if
   * parsing fails.
   */
  private int parseTimeString(String timeStr) {
    if (timeStr == null || timeStr.isEmpty()) {
      return -1;
    }

    // Remove "⏐ Time: " prefix if present
    String cleaned = timeStr.trim();
    if (cleaned.startsWith("⏐ ")) {
      cleaned = cleaned.substring(2).trim();
    }
    if (cleaned.startsWith("Time: ")) {
      cleaned = cleaned.substring(6).trim();
    }

    int totalSeconds = 0;
    int currentNumber = 0;

    for (int i = 0; i < cleaned.length(); i++) {
      char c = cleaned.charAt(i);
      if (Character.isDigit(c)) {
        currentNumber = currentNumber * 10 + (c - '0');
      } else if (c == 's' || c == 'S') {
        // Seconds
        totalSeconds += currentNumber;
        currentNumber = 0;
      } else if (c == 'm' || c == 'M') {
        // Minutes
        totalSeconds += currentNumber * 60;
        currentNumber = 0;
      } else if (c == 'h' || c == 'H') {
        // Hours
        totalSeconds += currentNumber * 3600;
        currentNumber = 0;
      } else if (c == ' ' || c == '\t') {
        // Ignore spaces
      } else {
        // Unknown character, might be invalid format
        // Continue anyway in case it's just a separator
      }
    }

    // Handle case where number is at the end without unit (assume seconds)
    if (currentNumber > 0) {
      totalSeconds += currentNumber;
    }

    return totalSeconds >= 0 ? totalSeconds : -1;
  }
}
