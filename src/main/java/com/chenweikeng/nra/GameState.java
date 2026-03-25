package com.chenweikeng.nra;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

public class GameState {
  private static final GameState INSTANCE = new GameState();

  private long absoluteTickCounter = 0;
  private boolean riding = false;
  private boolean automaticallyReleasedCursor = false;
  private volatile boolean monkeyAttached = false;
  private long lastSitCommand = -400;
  private boolean sitting = false;
  private boolean autograbFailureActive = false;

  private GameState() {}

  public static GameState getInstance() {
    return INSTANCE;
  }

  public long getAbsoluteTickCounter() {
    return absoluteTickCounter;
  }

  public void incrementTickCounter() {
    absoluteTickCounter++;
  }

  public boolean isRiding() {
    return riding;
  }

  public void setRiding(boolean riding) {
    this.riding = riding;
  }

  public boolean isAutomaticallyReleasedCursor() {
    return automaticallyReleasedCursor;
  }

  public void setAutomaticallyReleasedCursor(boolean released) {
    this.automaticallyReleasedCursor = released;
  }

  public boolean isMonkeyAttached() {
    return monkeyAttached;
  }

  public void setMonkeyAttached(boolean attached) {
    this.monkeyAttached = attached;
  }

  public void setLastSitCommand() {
    this.lastSitCommand = absoluteTickCounter;
  }

  public boolean isSitting() {
    return sitting;
  }

  public void setSitting(boolean sitting) {
    this.sitting = sitting;
  }

  public void updateSittingState(boolean wasPassenger, boolean isPassenger) {
    if (!wasPassenger && isPassenger && (absoluteTickCounter - lastSitCommand < 400)) {
      sitting = true;
    }
  }

  public void clearSittingIfNotPassenger(boolean isPassenger) {
    if (!isPassenger) {
      sitting = false;
    }
  }

  public boolean isAutograbFailureActive() {
    return autograbFailureActive;
  }

  public void setAutograbFailureActive(boolean active) {
    this.autograbFailureActive = active;
  }

  public boolean isValidPassenger(LocalPlayer player) {
    if (player == null) {
      return false;
    }

    if (sitting) {
      return false;
    }

    if (!player.isPassenger()) {
      return false;
    }

    if (!(player.getVehicle() instanceof ArmorStand)) {
      return false;
    }

    return true;
  }

  public void reset() {
    absoluteTickCounter = 0;
    riding = false;
    automaticallyReleasedCursor = false;
    lastSitCommand = -400;
    sitting = false;
    autograbFailureActive = false;
  }
}
