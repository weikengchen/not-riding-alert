package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class RideDisplayCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("nra:ridedisplay")
            .then(ClientCommandManager.argument("num", IntegerArgumentType.integer(1))
                .executes(context -> {
                    int num = IntegerArgumentType.getInteger(context, "num");
                    NotRidingAlertClient.getConfig().setRideDisplayCount(num);
                    context.getSource().sendFeedback(Component.literal("Ride display count set to " + num));
                    NotRidingAlertClient.LOGGER.info("Ride display count set to {}", num);
                    return 1;
                })
            )
            .executes(context -> {
                int current = NotRidingAlertClient.getConfig().getRideDisplayCount();
                context.getSource().sendFeedback(Component.literal("Current ride display count: " + current));
                context.getSource().sendFeedback(Component.literal("Usage: /nra:ridedisplay <num> (minimum 1)"));
                return 1;
            })
        );
    }
}
