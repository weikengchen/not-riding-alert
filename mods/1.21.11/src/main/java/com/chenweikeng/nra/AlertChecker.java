package com.chenweikeng.nra;

import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.tracker.PlayerMovementTracker;
import com.chenweikeng.nra.tracker.RideStateTracker;
import com.chenweikeng.nra.tracker.SuppressionRegionTracker;
import com.chenweikeng.nra.util.SoundHelper;
import net.minecraft.client.Minecraft;

public class AlertChecker {

  public void check(
      Minecraft client,
      boolean autograbFailureActive,
      PlayerMovementTracker movementTracker,
      RideStateTracker rideStateTracker,
      SuppressionRegionTracker suppressionRegionTracker) {
    if (client.player == null) {
      return;
    }

    if (autograbFailureActive) {
      SoundHelper.playConfiguredSound(client);
      return;
    }

    if (!ModConfig.currentSetting.enabled) {
      return;
    }

    GameState state = GameState.getInstance();
    if (!state.isRiding()
        && !movementTracker.hasPlayerMovedRecently(state.getAbsoluteTickCounter())
        && !rideStateTracker.hasRidenRecently(state.getAbsoluteTickCounter())
        && !rideStateTracker.hasVehicleRecently(state.getAbsoluteTickCounter())
        && !suppressionRegionTracker.isInROTRExceptionArea(client)
        && !rideStateTracker.isLincolnSuppressionActive()) {
      SoundHelper.playConfiguredSound(client);
    }
  }
}
