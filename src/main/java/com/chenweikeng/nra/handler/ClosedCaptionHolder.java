package com.chenweikeng.nra.handler;

import net.minecraft.network.chat.Component;

public class ClosedCaptionHolder {
  private static final ClosedCaptionHolder INSTANCE = new ClosedCaptionHolder();
  private static final int DELAY_TICKS = 1;

  private Component currentCaption = null;
  private Component pendingCaption = null;
  private int delayCounter = 0;

  private ClosedCaptionHolder() {}

  public static ClosedCaptionHolder getInstance() {
    return INSTANCE;
  }

  public void tick() {
    if (delayCounter > 0) {
      delayCounter--;
      if (delayCounter == 0 && pendingCaption != null) {
        currentCaption = pendingCaption;
        pendingCaption = null;
      }
    }
  }

  public void setCaption(Component caption) {
    if (caption == null) {
      currentCaption = null;
      pendingCaption = null;
      delayCounter = 0;
    } else if (currentCaption == null) {
      currentCaption = caption;
    } else {
      currentCaption = null;
      pendingCaption = caption;
      delayCounter = DELAY_TICKS;
    }
  }

  public Component getCaption() {
    return currentCaption;
  }

  public void clear() {
    currentCaption = null;
    pendingCaption = null;
    delayCounter = 0;
  }
}
