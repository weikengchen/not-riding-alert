package com.chenweikeng.nra;

import com.chenweikeng.nra.compat.MonkeycraftCompat;
import com.chenweikeng.nra.config.ClothConfigScreen;
import com.chenweikeng.nra.config.CursorReleaseTiming;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.config.WindowMinimizeTiming;
import com.chenweikeng.nra.handler.AutograbFailureHandler;
import com.chenweikeng.nra.handler.ConfigReminderHandler;
import com.chenweikeng.nra.handler.DayTimeHandler;
import com.chenweikeng.nra.handler.HibernationHandler;
import com.chenweikeng.nra.handler.ReminderHandler;
import com.chenweikeng.nra.handler.ScoreboardHandler;
import com.chenweikeng.nra.handler.WindowMinimizeHandler;
import com.chenweikeng.nra.ride.CurrentRideHolder;
import com.chenweikeng.nra.ride.RegionHolder;
import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import com.chenweikeng.nra.strategy.StrategyHudRendererDispatcher;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotRidingAlertClient implements ClientModInitializer {
  public static final String MOD_ID = "not-riding-alert";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private static final int CHECK_INTERVAL = 200;
  private static final int CANOE_MESSAGE_COOLDOWN_TICKS = 200;

  private static volatile boolean isMonkeyAttached = false;

  private final PlayerMovementTracker movementTracker = new PlayerMovementTracker();
  private final RideStateTracker rideStateTracker = new RideStateTracker();
  private final SuppressionRegionTracker suppressionRegionTracker = new SuppressionRegionTracker();
  private final DayTimeHandler dayTimeHandler = new DayTimeHandler();
  private final ConfigReminderHandler configReminderHandler = new ConfigReminderHandler();
  private final AutograbFailureHandler autograbFailureHandler = new AutograbFailureHandler();
  private final WindowMinimizeHandler windowMinimizeHandler = WindowMinimizeHandler.getInstance();
  private final ScoreboardHandler scoreboardHandler = new ScoreboardHandler();
  private final ReminderHandler reminderHandler = ReminderHandler.getInstance();

  private int tickCounter = 0;
  private long absoluteTickCounter = 0;
  private long lastCanoeMessageTick = -CANOE_MESSAGE_COOLDOWN_TICKS;
  private static boolean isRiding = false;
  private boolean wasRiding = false;
  private boolean wasOnVehicle = false;
  private RideName previousRegionRide = null;
  private static boolean automaticallyReleasedCursor = false;

  @Override
  public void onInitializeClient() {
    LOGGER.info("Not Riding Alert client initialized");
    MonkeycraftCompat.init();

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
          boolean autograbFailureActive =
              autograbFailureHandler.track(client, absoluteTickCounter, movementTracker);
          if (autograbFailureActive && modConfig.minimizeWindow != WindowMinimizeTiming.NONE) {
            windowMinimizeHandler.restoreWindow();
          }
          HibernationHandler.getInstance().track(client, absoluteTickCounter);
          configReminderHandler.track(client, absoluteTickCounter);
          scoreboardHandler.track(client);
          reminderHandler.track(client, absoluteTickCounter);

          RideCountManager.getInstance().checkAndSaveIfNeeded();

          tickCounter++;
          if (tickCounter >= CHECK_INTERVAL) {
            tickCounter = 0;
            checkNotRidingAlert(client, autograbFailureActive);
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
          VanillaHudElements.CHAT, beforeChatId, StrategyHudRendererDispatcher::render);
    }
  }

  private void handleCursorManagement(
      Minecraft client, ModConfig modConfig, boolean isPassenger, RideName regionRide) {
    CursorReleaseTiming timing = modConfig.cursorReleaseTiming;

    if (timing == CursorReleaseTiming.ON_ZONE_ENTRY
        && regionRide != null
        && !isPassenger
        && modConfig.autograb) {
      if (regionRide != previousRegionRide) {
        client.setScreen(null);
        if (client.mouseHandler.isMouseGrabbed()) {
          client.mouseHandler.releaseMouse();
          automaticallyReleasedCursor = true;
          sendCanoeMessageIfNeeded(client, regionRide);
        }
        previousRegionRide = regionRide;
      }
    } else {
      previousRegionRide = null;
    }

    if (timing != CursorReleaseTiming.NONE) {
      boolean isOnVehicle = isPassenger || CurrentRideHolder.getCurrentRide() != null;
      boolean shouldReleaseOnThisTick =
          switch (timing) {
            case NONE -> false;
            case ON_ZONE_ENTRY -> !wasRiding && isRiding;
            case ON_VEHICLE_MOUNT -> !wasOnVehicle && isOnVehicle;
          };

      if (shouldReleaseOnThisTick) {
        client.mouseHandler.releaseMouse();
        automaticallyReleasedCursor = true;
        RideName currentRide = CurrentRideHolder.getCurrentRide();
        if (currentRide == null) {
          currentRide = RegionHolder.getRideAtLocation(client);
        }
        sendCanoeMessageIfNeeded(client, currentRide);
      }

      boolean shouldGrabOnThisTick =
          switch (timing) {
            case NONE -> false;
            case ON_ZONE_ENTRY -> wasRiding && !isRiding;
            case ON_VEHICLE_MOUNT -> wasOnVehicle && !isOnVehicle;
          };

      if (shouldGrabOnThisTick) {
        automaticallyReleasedCursor = false;
        if (client.screen == null) {
          client.mouseHandler.grabMouse();
        }
      }

      boolean isCurrentlyRiding =
          switch (timing) {
            case NONE -> false;
            case ON_ZONE_ENTRY -> isRiding;
            case ON_VEHICLE_MOUNT -> isOnVehicle;
          };

      if (automaticallyReleasedCursor
          && isCurrentlyRiding
          && client.mouseHandler.isRightPressed()
          && client.screen == null) {
        client.mouseHandler.releaseMouse();
      }

      wasOnVehicle = isOnVehicle;
    }

    if (modConfig.minimizeWindow != WindowMinimizeTiming.NONE) {
      WindowMinimizeTiming minimizeTiming = modConfig.minimizeWindow;
      boolean isOnVehicle = isPassenger || CurrentRideHolder.getCurrentRide() != null;

      boolean shouldMinimizeOnThisTick =
          switch (minimizeTiming) {
            case NONE -> false;
            case ON_ZONE_ENTRY -> !wasRiding && isRiding;
            case ON_VEHICLE_MOUNT -> !wasOnVehicle && isOnVehicle;
          };

      if (shouldMinimizeOnThisTick && !MonkeycraftCompat.isClientConnected()) {
        windowMinimizeHandler.minimizeWindow();
      }

      boolean shouldRestoreOnThisTick =
          switch (minimizeTiming) {
            case NONE -> false;
            case ON_ZONE_ENTRY -> wasRiding && !isRiding;
            case ON_VEHICLE_MOUNT -> wasOnVehicle && !isOnVehicle;
          };

      if (shouldRestoreOnThisTick) {
        windowMinimizeHandler.restoreWindow();
      }
    }
  }

  private void sendCanoeMessageIfNeeded(Minecraft client, RideName ride) {
    if (client.player == null || ride != RideName.DAVY_CROCKETTS_EXPLORER_CANOES) {
      return;
    }

    if (absoluteTickCounter - lastCanoeMessageTick < CANOE_MESSAGE_COOLDOWN_TICKS) {
      return;
    }

    lastCanoeMessageTick = absoluteTickCounter;

    Component message =
        Component.empty()
            .withStyle(ChatFormatting.AQUA)
            .append(
                Component.literal("[NRA] ")
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
            .append(Component.literal("Please use ").withStyle(ChatFormatting.WHITE))
            .append(
                Component.literal("LEFT click")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
            .append(Component.literal(" to ride canoes.").withStyle(ChatFormatting.WHITE));

    client.player.displayClientMessage(message, false);
  }

  private void checkNotRidingAlert(Minecraft client, boolean autograbFailureActive) {
    if (client.player == null) {
      return;
    }

    if (autograbFailureActive) {
      SoundHelper.playConfiguredSound(client);
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
    configReminderHandler.reset();
    HibernationHandler.getInstance().reset();
    scoreboardHandler.reset();
    reminderHandler.reset();
    tickCounter = 0;
    absoluteTickCounter = 0;
    lastCanoeMessageTick = -CANOE_MESSAGE_COOLDOWN_TICKS;
    wasRiding = false;
    wasOnVehicle = false;
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

  public static boolean isMonkeyAttached() {
    return isMonkeyAttached;
  }

  public static void setMonkeyAttached(boolean attached) {
    isMonkeyAttached = attached;
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
