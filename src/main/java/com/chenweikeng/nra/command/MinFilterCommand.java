package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class MinFilterCommand {
  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("nra:minfilter")
            .then(
                ClientCommandManager.argument("minutes", IntegerArgumentType.integer(1))
                    .executes(
                        context -> {
                          int minutes = IntegerArgumentType.getInteger(context, "minutes");
                          NotRidingAlertClient.getConfig().setMinRideTimeMinutes(minutes);
                          context
                              .getSource()
                              .sendFeedback(
                                  Component.literal(
                                      "Minimum ride time filter set to " + minutes + " minutes"));
                          NotRidingAlertClient.LOGGER.info(
                              "Min ride time filter set to {} minutes", minutes);
                          return 1;
                        }))
            .executes(
                context -> {
                  Integer current = NotRidingAlertClient.getConfig().getMinRideTimeMinutes();
                  if (current == null) {
                    context
                        .getSource()
                        .sendFeedback(
                            Component.literal("Minimum ride time filter is already disabled"));
                  } else {
                    NotRidingAlertClient.getConfig().setMinRideTimeMinutes(null);
                    context
                        .getSource()
                        .sendFeedback(
                            Component.literal(
                                "Minimum ride time filter has been cleared (was "
                                    + current
                                    + " minutes)"));
                    NotRidingAlertClient.LOGGER.info(
                        "Min ride time filter cleared (was {} minutes)", current);
                  }
                  return 1;
                }));
  }
}
