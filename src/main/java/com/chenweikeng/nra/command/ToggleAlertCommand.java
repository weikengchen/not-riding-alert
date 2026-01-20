package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class ToggleAlertCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("nra:togglealert")
            .executes(context -> {
                boolean currentState = NotRidingAlertClient.getConfig().isEnabled();
                boolean newState = !currentState;
                NotRidingAlertClient.getConfig().setEnabled(newState);
                
                String status = newState ? "enabled" : "disabled";
                context.getSource().sendFeedback(Component.literal("Not Riding Alert is now " + status));
                NotRidingAlertClient.LOGGER.info("Not Riding Alert toggled to: {}", status);
                return 1;
            })
        );
    }
}

