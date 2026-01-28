package com.chenweikeng.nra.command;

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
    long totalSecondsFromZero = 0;
    long completedSeconds = 0;

    for (RideName ride : RideName.values()) {
      if (ride == RideName.UNKNOWN) {
        continue;
      }

      if (ride.isSeasonal()) {
        continue;
      }

      int currentCount = countManager.getRideCount(ride);
      int rideTimeSeconds = ride.getRideTime();

      if (rideTimeSeconds >= 99999) {
        continue;
      }

      // Calculate total time needed if starting from 0
      totalSecondsFromZero += (long) goal * rideTimeSeconds;

      if (currentCount >= goal) {
        // Player has completed this ride goal, add all time to completed
        completedSeconds += (long) goal * rideTimeSeconds;
      } else {
        // Player has partially completed this ride
        completedSeconds += (long) currentCount * rideTimeSeconds;
        int ridesNeeded = goal - currentCount;
        totalSecondsNeeded += (long) ridesNeeded * rideTimeSeconds;
      }
    }

    // Calculate progress percentage
    double progressPercentage = 0.0;
    if (totalSecondsFromZero > 0) {
      progressPercentage = ((double) completedSeconds / totalSecondsFromZero) * 100.0;
    }

    source.sendFeedback(
        Component.literal(
            String.format(
                "Progress: %.2f%% - Time remaining: %s",
                progressPercentage, TimeFormatUtil.formatDuration(totalSecondsNeeded))));
  }

  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("nra:1k")
            .executes(
                context -> {
                  calculateAndSendFeedback(context.getSource(), 1000);
                  return 1;
                }));

    dispatcher.register(
        ClientCommandManager.literal("nra:5k")
            .executes(
                context -> {
                  calculateAndSendFeedback(context.getSource(), 5000);
                  return 1;
                }));

    dispatcher.register(
        ClientCommandManager.literal("nra:10k")
            .executes(
                context -> {
                  calculateAndSendFeedback(context.getSource(), 10000);
                  return 1;
                }));
  }
}
