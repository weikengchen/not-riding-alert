package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class HideScoreboardCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        // Register both /nra:hidescoreboard and /nra:hidesb
        dispatcher.register(ClientCommandManager.literal("nra:hidescoreboard")
            .executes(context -> {
                boolean currentState = NotRidingAlertClient.getConfig().isHideScoreboard();
                boolean newState = !currentState;
                NotRidingAlertClient.getConfig().setHideScoreboard(newState);
                
                String status = newState ? "hidden" : "visible";
                context.getSource().sendFeedback(Component.literal("Scoreboard is now " + status));
                NotRidingAlertClient.LOGGER.info("Scoreboard toggled to: {}", status);
                return 1;
            })
        );
        
        dispatcher.register(ClientCommandManager.literal("nra:hidesb")
            .executes(context -> {
                boolean currentState = NotRidingAlertClient.getConfig().isHideScoreboard();
                boolean newState = !currentState;
                NotRidingAlertClient.getConfig().setHideScoreboard(newState);
                
                String status = newState ? "hidden" : "visible";
                context.getSource().sendFeedback(Component.literal("Scoreboard is now " + status));
                NotRidingAlertClient.LOGGER.info("Scoreboard toggled to: {}", status);
                return 1;
            })
        );
    }
}
