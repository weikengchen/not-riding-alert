package com.chenweikeng.nra.tracker;

import net.minecraft.client.Minecraft;

public class PlayerMovementTracker {
  private static final int MOVEMENT_SUPPRESSION_TICKS = 600;

  private long lastPlayerMovementTick = -1;

  public void track(Minecraft client, long currentTick) {
    if (client.options == null) {
      return;
    }

    boolean isMoving =
        client.options.keyUp.isDown()
            || client.options.keyDown.isDown()
            || client.options.keyLeft.isDown()
            || client.options.keyRight.isDown()
            || client.options.keyJump.isDown()
            || client.options.keyShift.isDown();

    boolean isMouseClicking =
        client.mouseHandler.isLeftPressed() || client.mouseHandler.isRightPressed();

    if (isMoving || isMouseClicking) {
      lastPlayerMovementTick = currentTick;
    }
  }

  public boolean hasPlayerMovedRecently(long currentTick) {
    if (lastPlayerMovementTick < 0) {
      return false;
    }

    long ticksSinceLastMovement = currentTick - lastPlayerMovementTick;
    return ticksSinceLastMovement < MOVEMENT_SUPPRESSION_TICKS;
  }

  public void reset() {
    lastPlayerMovementTick = -1;
  }
}
