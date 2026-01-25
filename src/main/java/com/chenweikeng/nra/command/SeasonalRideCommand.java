package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class SeasonalRideCommand {
  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("nra:seasonalride")
            .executes(
                context -> {
                  boolean currentState = NotRidingAlertClient.getConfig().isSeasonalRidesEnabled();
                  boolean newState = !currentState;
                  NotRidingAlertClient.getConfig().setSeasonalRidesEnabled(newState);

                  String status = newState ? "enabled" : "disabled";
                  context
                      .getSource()
                      .sendFeedback(
                          Component.literal("Seasonal rides are now " + status + " in top goals"));
                  NotRidingAlertClient.LOGGER.info("Seasonal rides toggled to: {}", status);
                  return 1;
                }));
  }
}
