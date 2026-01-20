package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class HideChatCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("nra:hidechat")
            .executes(context -> {
                boolean currentState = NotRidingAlertClient.getConfig().isHideChat();
                boolean newState = !currentState;
                NotRidingAlertClient.getConfig().setHideChat(newState);
                
                String status = newState ? "hidden" : "visible";
                context.getSource().sendFeedback(Component.literal("Chat is now " + status));
                NotRidingAlertClient.LOGGER.info("Chat toggled to: {}", status);
                return 1;
            })
        );
    }
}
