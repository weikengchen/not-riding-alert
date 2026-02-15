package com.chenweikeng.nra.handler;

import com.chenweikeng.nra.ServerState;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.ride.RegionHolder;
import com.chenweikeng.nra.ride.RideName;
import com.chenweikeng.nra.tracker.PlayerMovementTracker;
import com.chenweikeng.nra.util.SoundHelper;
import net.minecraft.client.Minecraft;

public class AutograbFailureHandler {
  private static final int AUTOGRAB_TIMEOUT_TICKS = 600;
  private static final int AUTOGRAB_DLRR_TIMEOUT_TICKS = 1200;

  private long autograbRegionEntryTick = -1;
  private RideName currentAutograbRegion = null;
  private boolean autograbFailureAlertActive = false;

  public void track(Minecraft client, long currentTick, PlayerMovementTracker movementTracker) {
    if (!ServerState.isImagineFunServer()) {
      return;
    }
    if (!ModConfig.getInstance().alertAutograbFailure) {
      return;
    }
    if (client.player == null) {
      return;
    }

    boolean isPassenger = client.player.isPassenger();
    RideName regionRide = null;
    if (ModConfig.getInstance().autograb) {
      regionRide = RegionHolder.getRideAtLocation(client);
    }

    if (regionRide != null && !isPassenger) {
      if (currentAutograbRegion != regionRide) {
        currentAutograbRegion = regionRide;
        autograbRegionEntryTick = currentTick;
        autograbFailureAlertActive = false;
      }

      int timeoutTicks =
          regionRide == RideName.DISNEYLAND_RAILROAD
              ? AUTOGRAB_DLRR_TIMEOUT_TICKS
              : AUTOGRAB_TIMEOUT_TICKS;

      if (autograbRegionEntryTick >= 0 && currentTick - autograbRegionEntryTick >= timeoutTicks) {
        autograbFailureAlertActive = true;
      }
    } else {
      currentAutograbRegion = null;
      autograbRegionEntryTick = -1;
      autograbFailureAlertActive = false;
    }

    if (autograbFailureAlertActive && !movementTracker.hasPlayerMovedRecently(currentTick)) {
      SoundHelper.playConfiguredSound(client);
    }
  }

  public void reset() {
    autograbRegionEntryTick = -1;
    currentAutograbRegion = null;
    autograbFailureAlertActive = false;
  }
}
