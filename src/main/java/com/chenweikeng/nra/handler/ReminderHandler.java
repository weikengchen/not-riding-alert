package com.chenweikeng.nra.handler;

import com.chenweikeng.nra.GameState;
import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.Timing;
import com.chenweikeng.nra.config.AudioBoostReminderMode;
import com.chenweikeng.nra.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ReminderHandler {
  private static ReminderHandler instance;

  private boolean audioConnected = false;
  public long lastAudioReminderTick = -Timing.REMINDER_INTERVAL_TICKS;

  private ReminderHandler() {}

  public static ReminderHandler getInstance() {
    if (instance == null) {
      instance = new ReminderHandler();
    }
    return instance;
  }

  public void setAudioConnected(boolean connected) {
    this.audioConnected = connected;
  }

  public boolean isAudioConnected() {
    return audioConnected;
  }

  public void track(Minecraft client, long currentTick) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }

    if (client.player == null) {
      return;
    }

    checkAudioBoostReminder(client, currentTick);
  }

  private void checkAudioBoostReminder(Minecraft client, long currentTick) {
    AudioBoostReminderMode mode = ModConfig.currentSetting.audioBoostReminderMode;

    if (mode == AudioBoostReminderMode.DISABLED) {
      return;
    }

    if (mode == AudioBoostReminderMode.ONLY_WHEN_RIDING && !GameState.getInstance().isRiding()) {
      return;
    }

    if (audioConnected) {
      return;
    }

    if (currentTick - lastAudioReminderTick < Timing.REMINDER_INTERVAL_TICKS) {
      return;
    }

    lastAudioReminderTick = currentTick;

    Component message =
        Component.literal("MISSING AUDIO BOOST").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

    client.player.displayClientMessage(message, true);
  }

  public void reset() {
    audioConnected = false;
    lastAudioReminderTick = -Timing.REMINDER_INTERVAL_TICKS;
  }
}
