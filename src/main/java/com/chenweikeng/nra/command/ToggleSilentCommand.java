package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class ToggleSilentCommand {
  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("nra:silent")
            .requires(src -> NotRidingAlertClient.isImagineFunServer())
            .executes(
                context -> {
                  boolean currentState = NotRidingAlertClient.getConfig().isSilent();
                  boolean newState = !currentState;
                  NotRidingAlertClient.getConfig().setSilent(newState);

                  String status = newState ? "enabled" : "disabled";
                  context
                      .getSource()
                      .sendFeedback(Component.literal("Silent mode is now " + status));
                  NotRidingAlertClient.LOGGER.info("Silent mode toggled to: {}", status);
                  return 1;
                }));
  }
}
