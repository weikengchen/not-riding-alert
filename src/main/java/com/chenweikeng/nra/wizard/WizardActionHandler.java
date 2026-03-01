package com.chenweikeng.nra.wizard;

import com.chenweikeng.nra.config.ModConfig;
import net.minecraft.client.Minecraft;

public class WizardActionHandler {

  public static void handle(String action, Minecraft client) {
    if (action == null || action.isEmpty()) {
      return;
    }

    if (action.startsWith("page:")) {
      handlePageAction(action.substring(5), client);
    } else if (action.startsWith("config:")) {
      handleConfigAction(action.substring(7));
    } else if (action.startsWith("command:")) {
      handleCommandAction(action.substring(8), client);
    } else if (action.equals("factory_reset")) {
      handleFactoryReset();
    } else if (action.equals("finish")) {
      handleFinish(client);
    }
  }

  private static void handlePageAction(String pageStr, Minecraft client) {
    try {
      int pageIndex = Integer.parseInt(pageStr.trim());
      TutorialManager.getInstance().goToPage(pageIndex);
      if (client.screen instanceof WizardScreen wizardScreen) {
        wizardScreen.goToPage(pageIndex);
      }
    } catch (NumberFormatException e) {
      // Invalid page number
    }
  }

  private static void handleConfigAction(String configAction) {
    String[] parts = configAction.split(":", 2);
    if (parts.length != 2) {
      return;
    }

    String key = parts[0];
    String value = parts[1];
    boolean boolValue = Boolean.parseBoolean(value);

    ModConfig config = ModConfig.getInstance();

    switch (key) {
      case "enabled" -> {
        config.enabled = boolValue;
        config.save();
      }
      case "autograb" -> {
        config.autograb = boolValue;
        config.save();
      }
      case "hideChat" -> {
        config.hideChat = boolValue;
        config.save();
      }
      case "hideScoreboard" -> {
        config.hideScoreboard = boolValue;
        config.save();
      }
      case "hideHealth" -> {
        config.hideHealth = boolValue;
        config.save();
      }
      case "hideHotbar" -> {
        config.hideHotbar = boolValue;
        config.save();
      }
      default -> {
        // Unknown config key
      }
    }
  }

  private static void handleCommandAction(String command, Minecraft client) {
    if (client.player != null) {
      client.player.connection.sendCommand(command);
    }
  }

  private static void handleFactoryReset() {
    ModConfig.getInstance().resetToDefaults();
  }

  private static void handleFinish(Minecraft client) {
    TutorialManager.getInstance().finishTutorial();
    client.setScreen(null);
  }
}
