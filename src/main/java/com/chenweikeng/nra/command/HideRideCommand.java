package com.chenweikeng.nra.command;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.ride.RideName;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class HideRideCommand {
  private static final SuggestionProvider<FabricClientCommandSource> RIDE_SUGGESTIONS =
      (context, builder) -> {
        List<String> rideNames = RideName.getAllDisplayNames();
        for (String rideName : rideNames) {
          if (rideName.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
            builder.suggest(rideName);
          }
        }
        return builder.buildFuture();
      };

  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("nra:hideride")
            .then(
                ClientCommandManager.argument("rideName", StringArgumentType.greedyString())
                    .suggests(RIDE_SUGGESTIONS)
                    .executes(
                        context -> {
                          String rideName = StringArgumentType.getString(context, "rideName");

                          // Check if ride exists
                          RideName ride = RideName.fromString(rideName);
                          if (ride == RideName.UNKNOWN) {
                            context
                                .getSource()
                                .sendFeedback(Component.literal("Unknown ride: " + rideName));
                            return 0;
                          }

                          // Toggle hide state
                          boolean wasHidden =
                              NotRidingAlertClient.getConfig().isRideHidden(rideName);
                          NotRidingAlertClient.getConfig().toggleRideHidden(rideName);
                          boolean isNowHidden =
                              NotRidingAlertClient.getConfig().isRideHidden(rideName);

                          String action = isNowHidden ? "hidden" : "unhidden";
                          context
                              .getSource()
                              .sendFeedback(
                                  Component.literal("Ride \"" + rideName + "\" is now " + action));
                          NotRidingAlertClient.LOGGER.info("Ride {} is now {}", rideName, action);
                          return 1;
                        }))
            .executes(
                context -> {
                  context
                      .getSource()
                      .sendFeedback(Component.literal("Usage: /nra:hideride <rideName>"));
                  context
                      .getSource()
                      .sendFeedback(Component.literal("Use Tab to autocomplete ride names"));
                  return 1;
                }));
  }
}
