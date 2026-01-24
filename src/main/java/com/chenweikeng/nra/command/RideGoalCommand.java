package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import com.chenweikeng.nra.util.TimeFormatUtil;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class RideGoalCommand {
    private static void calculateAndSendFeedback(FabricClientCommandSource source, int goal) {
        RideCountManager countManager = RideCountManager.getInstance();
        long totalSecondsNeeded = 0;

        for (RideName ride : RideName.values()) {
            if (ride == RideName.UNKNOWN) {
                continue;
            }

            if (ride.isSeasonal()) {
                continue;
            }

            int currentCount = countManager.getRideCount(ride);

            if (currentCount >= goal) {
                continue;
            }

            int ridesNeeded = goal - currentCount;
            int rideTimeSeconds = ride.getRideTime();

            if (rideTimeSeconds >= 99999) {
                continue;
            }

            totalSecondsNeeded += (long) ridesNeeded * rideTimeSeconds;
        }

        source.sendFeedback(Component.literal("Total time needed: " + TimeFormatUtil.formatDuration(totalSecondsNeeded)));
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("nra:1k")
            .executes(context -> {
                calculateAndSendFeedback(context.getSource(), 1000);
                return 1;
            })
        );
        
        dispatcher.register(ClientCommandManager.literal("nra:5k")
            .executes(context -> {
                calculateAndSendFeedback(context.getSource(), 5000);
                return 1;
            })
        );
        
        dispatcher.register(ClientCommandManager.literal("nra:10k")
            .executes(context -> {
                calculateAndSendFeedback(context.getSource(), 10000);
                return 1;
            })
        );
    }
}
