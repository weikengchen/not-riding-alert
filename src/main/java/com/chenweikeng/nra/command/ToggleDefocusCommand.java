package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class ToggleDefocusCommand {
  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("nra:defocus")
            .requires(src -> NotRidingAlertClient.isImagineFunServer())
            .executes(
                context -> {
                  boolean currentState = NotRidingAlertClient.getConfig().isDefocusCursor();
                  boolean newState = !currentState;
                  NotRidingAlertClient.getConfig().setDefocusCursor(newState);

                  String status = newState ? "enabled" : "disabled";
                  context
                      .getSource()
                      .sendFeedback(Component.literal("Defocus cursor is now " + status));
                  NotRidingAlertClient.LOGGER.info("Defocus cursor toggled to: {}", status);
                  return 1;
                }));
  }
}
