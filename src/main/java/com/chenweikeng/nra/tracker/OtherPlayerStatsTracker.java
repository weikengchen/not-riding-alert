package com.chenweikeng.nra.tracker;

public class OtherPlayerStatsTracker {
  private static final OtherPlayerStatsTracker INSTANCE = new OtherPlayerStatsTracker();

  private boolean suppressUpdates = false;

  private OtherPlayerStatsTracker() {}

  public static OtherPlayerStatsTracker getInstance() {
    return INSTANCE;
  }

  public void setSuppressUpdates(boolean suppress) {
    this.suppressUpdates = suppress;
  }

  public boolean shouldSuppressUpdates() {
    return suppressUpdates;
  }

  public void reset() {
    suppressUpdates = false;
  }
}
