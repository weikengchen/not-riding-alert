package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class ToggleBlindWhenRidingCommand {
  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("nra:blind")
            .requires(src -> NotRidingAlertClient.isImagineFunServer())
            .executes(
                context -> {
                  boolean currentState = NotRidingAlertClient.getConfig().isBlindWhenRiding();
                  boolean newState = !currentState;
                  NotRidingAlertClient.getConfig().setBlindWhenRiding(newState);

                  String status = newState ? "enabled" : "disabled";
                  context
                      .getSource()
                      .sendFeedback(Component.literal("Blind when riding is now " + status));
                  NotRidingAlertClient.LOGGER.info("Blind when riding toggled to: {}", status);
                  return 1;
                }));
  }
}
