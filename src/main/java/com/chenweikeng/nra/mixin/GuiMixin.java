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

  @Inject(
      at = @At("HEAD"),
      method =
          "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V",
      cancellable = true)
  private void onRenderScoreboardSidebar(
      GuiGraphics context, Objective objective, CallbackInfo ci) {
    Scoreboard scoreboard = objective.getScoreboard();

    @SuppressWarnings("unchecked")
    Comparator<Object> comparator = (Comparator<Object>) SCORE_DISPLAY_ORDER;

    final String[] prefix8 = {null};
    final String[] prefix9 = {null};
    final String[] prefixA = {null};
    scoreboard.listPlayerScores(objective).stream()
        .filter(score -> !score.isHidden())
        .sorted(comparator)
        .limit(15L)
        .forEach(
            scoreboardEntry -> {
              PlayerTeam team = scoreboard.getPlayersTeam(scoreboardEntry.owner());
              if (team == null) return;
              String name = team.getName();
              Component prefixText = team.getPlayerPrefix();
              String prefixStr = prefixText != null ? prefixText.getString() : "null";
              if ("§8".equals(name)) prefix8[0] = prefixStr;
              else if ("§9".equals(name)) prefix9[0] = prefixStr;
              else if ("§a".equals(name)) prefixA[0] = prefixStr;
            });

    // Only analyze when §8 prefix contains "Current Ride"
    if (prefix8[0] == null) {
      CurrentRideHolder.setCurrentRide(null);
    } else if (!prefix8[0].contains("Current Ride")) {
      CurrentRideHolder.setCurrentRide(null);
    } else if (prefix9[0] == null) {
      CurrentRideHolder.setCurrentRide(null);
    } else {
      RideName resolved = RideName.fromTruncatedString(prefix9[0]);
      CurrentRideHolder.setCurrentRide(resolved);
      LastRideHolder.setLastRide(resolved);

      // Calculate progress from §A team if available and ride is not excluded
      if (resolved != RideName.DAVY_CROCKETTS_EXPLORER_CANOES && prefixA[0] != null) {
        int elapsedSeconds = parseTimeString(prefixA[0]);
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
    // Check if chat should be hidden
    if (NotRidingAlertClient.getConfig().isHideChat()) {
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
