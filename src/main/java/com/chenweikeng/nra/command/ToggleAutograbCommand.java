package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class ToggleAutograbCommand {
  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("nra:autograb")
            .requires(src -> NotRidingAlertClient.isImagineFunServer())
            .executes(
                context -> {
                  boolean currentState = NotRidingAlertClient.getConfig().isAutograb();
                  boolean newState = !currentState;
                  NotRidingAlertClient.getConfig().setAutograb(newState);

                  String status = newState ? "enabled" : "disabled";
                  context.getSource().sendFeedback(Component.literal("Autograb is now " + status));
                  NotRidingAlertClient.LOGGER.info("Autograb toggled to: {}", status);
                  return 1;
                }));
  }
}
