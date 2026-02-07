package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class RideDisplayCommand {
  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("nra:display")
            .requires(src -> NotRidingAlertClient.isImagineFunServer())
            .then(
                ClientCommandManager.argument("num", IntegerArgumentType.integer(1))
                    .executes(
                        context -> {
                          int num = IntegerArgumentType.getInteger(context, "num");
                          NotRidingAlertClient.getConfig().setRideDisplayCount(num);
                          context
                              .getSource()
                              .sendFeedback(Component.literal("Ride display count set to " + num));
                          NotRidingAlertClient.LOGGER.info("Ride display count set to {}", num);
                          return 1;
                        }))
            .executes(
                context -> {
                  NotRidingAlertClient.getConfig().setRideDisplayCount(0);
                  context
                      .getSource()
                      .sendFeedback(Component.literal("Ride display count set to 0 (hidden)"));
                  NotRidingAlertClient.LOGGER.info("Ride display count set to 0 (hidden)");
                  return 1;
                }));
  }
}
