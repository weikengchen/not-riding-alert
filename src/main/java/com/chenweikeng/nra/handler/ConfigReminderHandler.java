package com.chenweikeng.nra.handler;

import com.chenweikeng.nra.ServerState;
import com.chenweikeng.nra.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ConfigReminderHandler {
  private static final int INITIAL_DELAY_TICKS = 600;
  private static final int REMINDER_INTERVAL_TICKS = 12000;

  private boolean hasShownInitialReminder = false;
  private long lastReminderTick = 0;

  public void track(Minecraft client, long currentTick) {
    if (!ServerState.isImagineFunServer()) {
      return;
    }

    if (ModConfig.getInstance().hasOpenedConfig) {
      return;
    }

    if (client.player == null) {
      return;
    }

    if (!hasShownInitialReminder) {
      if (currentTick >= INITIAL_DELAY_TICKS) {
        sendReminder(client);
        hasShownInitialReminder = true;
        lastReminderTick = currentTick;
      }
    } else {
      if (currentTick - lastReminderTick >= REMINDER_INTERVAL_TICKS) {
        sendReminder(client);
        lastReminderTick = currentTick;
      }
    }
  }

  private void sendReminder(Minecraft client) {
    if (client.player == null) {
      return;
    }

    Component message =
        Component.empty()
            .withStyle(ChatFormatting.GOLD)
            .append(Component.literal("========== ").withStyle(ChatFormatting.YELLOW))
            .append(
                Component.literal("Not Riding Alert")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .append(Component.literal(" ==========").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("\n"))
            .append(
                Component.literal("This mod modifies your gameplay experience significantly.\n"))
            .append(Component.literal("To customize it to your preferences, please run "))
            .append(
                Component.literal("/nra").withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE))
            .append(Component.literal(" to open the configuration screen."))
            .append(Component.literal("\n"))
            .append(
                Component.literal("========================================")
                    .withStyle(ChatFormatting.YELLOW));

    client.player.displayClientMessage(message, false);
  }

  public void reset() {
    hasShownInitialReminder = false;
    lastReminderTick = 0;
  }
}
