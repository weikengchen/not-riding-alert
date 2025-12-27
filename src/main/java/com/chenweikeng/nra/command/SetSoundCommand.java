package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class SetSoundCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("nonridesound")
            .then(ClientCommandManager.argument("soundId", StringArgumentType.string())
                .executes(context -> {
                    String soundId = StringArgumentType.getString(context, "soundId");
                    NotRidingAlertClient.getConfig().setSoundId(soundId);
                    context.getSource().sendFeedback(Text.literal("Sound ID set to: " + soundId));
                    NotRidingAlertClient.LOGGER.info("Sound ID changed to: {}", soundId);
                    return 1;
                })
            )
            .executes(context -> {
                String currentSound = NotRidingAlertClient.getConfig().getSoundId();
                context.getSource().sendFeedback(Text.literal("Current sound ID: " + currentSound));
                return 1;
            })
        );
    }
}

