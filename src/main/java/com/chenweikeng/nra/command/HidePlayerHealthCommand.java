package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class HidePlayerHealthCommand {
  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("nra:hidehp")
            .requires(src -> NotRidingAlertClient.isImagineFunServer())
            .executes(
                context -> {
                  boolean currentState = NotRidingAlertClient.getConfig().isHidePlayerHealth();
                  boolean newState = !currentState;
                  NotRidingAlertClient.getConfig().setHidePlayerHealth(newState);

                  String status = newState ? "hidden" : "visible";
                  context
                      .getSource()
                      .sendFeedback(Component.literal("Player health is now " + status));
                  NotRidingAlertClient.LOGGER.info("Player health toggled to: {}", status);
                  return 1;
                }));
  }
}
