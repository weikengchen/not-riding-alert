package com.chenweikeng.nra;

import com.chenweikeng.nra.config.ClothConfigScreen;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.handler.AutograbFailureHandler;
import com.chenweikeng.nra.handler.DayTimeHandler;
import com.chenweikeng.nra.ride.CurrentRideHolder;
import com.chenweikeng.nra.ride.RegionHolder;
import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import com.chenweikeng.nra.strategy.StrategyHudRenderer;
import com.chenweikeng.nra.tracker.PlayerMovementTracker;
import com.chenweikeng.nra.tracker.RideStateTracker;
import com.chenweikeng.nra.tracker.SuppressionRegionTracker;
import com.chenweikeng.nra.util.SoundHelper;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotRidingAlertClient implements ClientModInitializer {
  public static final String MOD_ID = "not-riding-alert";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private static final int CHECK_INTERVAL = 200;

  private final PlayerMovementTracker movementTracker = new PlayerMovementTracker();
  private final RideStateTracker rideStateTracker = new RideStateTracker();
  private final SuppressionRegionTracker suppressionRegionTracker = new SuppressionRegionTracker();
  private final DayTimeHandler dayTimeHandler = new DayTimeHandler();
  private final AutograbFailureHandler autograbFailureHandler = new AutograbFailureHandler();

  private int tickCounter = 0;
  private long absoluteTickCounter = 0;
  private static boolean isRiding = false;
  private boolean wasRiding = false;
  private RideName previousRegionRide = null;
  private static boolean automaticallyReleasedCursor = false;

  @Override
  public void onInitializeClient() {
    LOGGER.info("Not Riding Alert client initialized");

    ClientPlayConnectionEvents.JOIN.register(
        (handler, sender, client) -> {
          ServerState.onJoin(client);
        });

    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> {
          ServerState.onDisconnect();
          resetAllTrackers();
        });

    ClientTickEvents.END_CLIENT_TICK.register(
        client -> {
          if (!ServerState.isImagineFunServer()) {
            return;
          }
          if (client.player == null) {
            isRiding = false;
            return;
          }

          ModConfig modConfig = ModConfig.getInstance();
          boolean isPassenger = client.player.isPassenger();
          RideName regionRide = modConfig.autograb ? RegionHolder.getRideAtLocation(client) : null;
          isRiding =
              isPassenger || CurrentRideHolder.getCurrentRide() != null || regionRide != null;

          handleCursorManagement(client, modConfig, isPassenger, regionRide);

          wasRiding = isRiding;
          absoluteTickCounter++;

          movementTracker.track(client, absoluteTickCounter);
          rideStateTracker.trackRideCompletion(absoluteTickCounter);
          rideStateTracker.trackVehicleState(client, absoluteTickCounter);
          suppressionRegionTracker.trackLincolnRegionEntryExit(client, rideStateTracker);
          dayTimeHandler.resetDayTimeIfNeeded(client);
          autograbFailureHandler.track(client, absoluteTickCounter, movementTracker);

          RideCountManager.getInstance().checkAndSaveIfNeeded();

          tickCounter++;
          if (tickCounter >= CHECK_INTERVAL) {
            tickCounter = 0;
            checkNotRidingAlert(client);
          }
        });

    ClientCommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess) -> {
          registerNraCommand(dispatcher);
        });

    Identifier beforeChatId =
        Identifier.fromNamespaceAndPath(NotRidingAlertClient.MOD_ID, "before_chat");
    if (beforeChatId != null) {
      HudElementRegistry.attachElementBefore(
          VanillaHudElements.CHAT, beforeChatId, StrategyHudRenderer::render);
    }
  }

  private void handleCursorManagement(
      Minecraft client, ModConfig modConfig, boolean isPassenger, RideName regionRide) {
    if (regionRide != null && !isPassenger && modConfig.autograb) {
      if (regionRide != previousRegionRide) {
        client.setScreen(null);
        if (client.mouseHandler.isMouseGrabbed()) {
          client.mouseHandler.releaseMouse();
          automaticallyReleasedCursor = true;
        }
        previousRegionRide = regionRide;
      }
    } else {
      previousRegionRide = null;
    }

    if (modConfig.defocusCursor) {
      if (!wasRiding && isRiding) {
        client.mouseHandler.releaseMouse();
        automaticallyReleasedCursor = true;
      } else if (wasRiding && !isRiding) {
        automaticallyReleasedCursor = false;
        if (client.screen == null) {
          client.mouseHandler.grabMouse();
        }
      } else if (automaticallyReleasedCursor
          && isRiding
          && client.mouseHandler.isRightPressed()
          && client.screen == null) {
        client.mouseHandler.releaseMouse();
      }
    }
  }

  private void checkNotRidingAlert(Minecraft client) {
    if (client.player == null) {
      return;
    }

    if (!ModConfig.getInstance().enabled) {
      return;
    }

    if (!isRiding
        && !movementTracker.hasPlayerMovedRecently(absoluteTickCounter)
        && !rideStateTracker.hasRidenRecently(absoluteTickCounter)
        && !rideStateTracker.hasVehicleRecently(absoluteTickCounter)
        && !suppressionRegionTracker.isInROTRExceptionArea(client)
        && !rideStateTracker.isLincolnSuppressionActive()) {
      SoundHelper.playConfiguredSound(client);
    }
  }

  private void resetAllTrackers() {
    movementTracker.reset();
    rideStateTracker.reset();
    suppressionRegionTracker.reset();
    autograbFailureHandler.reset();
    tickCounter = 0;
    absoluteTickCounter = 0;
    wasRiding = false;
    previousRegionRide = null;
    automaticallyReleasedCursor = false;
  }

  public static boolean isRiding(net.minecraft.client.player.LocalPlayer player) {
    if (player == null) {
      return isRiding || CurrentRideHolder.getCurrentRide() != null;
    }
    RideName regionRide =
        ModConfig.getInstance().autograb
            ? RegionHolder.getRideAtLocation(Minecraft.getInstance())
            : null;
    return isRiding || CurrentRideHolder.getCurrentRide() != null || regionRide != null;
  }

  public static boolean isImagineFunServer() {
    return ServerState.isImagineFunServer();
  }

  public static boolean isAutomaticallyReleasedCursor() {
    return automaticallyReleasedCursor;
  }

  private static void registerNraCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("nra")
            .executes(
                context -> {
                  Minecraft client = Minecraft.getInstance();
                  client.execute(
                      () -> {
                        client.setScreen((Screen) ClothConfigScreen.createScreen(client.screen));
                      });
                  return 1;
                }));
  }
}
