package com.chenweikeng.nra;

import com.chenweikeng.nra.compat.MonkeycraftCompat;
import com.chenweikeng.nra.config.CursorReleaseTiming;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.config.WindowMinimizeTiming;
import com.chenweikeng.nra.config.profile.ProfileCommandHandler;
import com.chenweikeng.nra.config.profile.ProfileManager;
import com.chenweikeng.nra.config.profile.ui.ProfileManagementScreen;
import com.chenweikeng.nra.handler.AutograbFailureHandler;
import com.chenweikeng.nra.handler.AutograbRegionRenderer;
import com.chenweikeng.nra.handler.ClosedCaptionHolder;
import com.chenweikeng.nra.handler.ConfigReminderHandler;
import com.chenweikeng.nra.handler.DayTimeHandler;
import com.chenweikeng.nra.handler.FireworkViewingHandler;
import com.chenweikeng.nra.handler.HibernationHandler;
import com.chenweikeng.nra.handler.ReminderHandler;
import com.chenweikeng.nra.handler.ScoreboardHandler;
import com.chenweikeng.nra.handler.WindowMinimizeHandler;
import com.chenweikeng.nra.ride.AutograbHolder;
import com.chenweikeng.nra.ride.CurrentRideHolder;
import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import com.chenweikeng.nra.strategy.StrategyHudRendererDispatcher;
import com.chenweikeng.nra.tracker.PlayerMovementTracker;
import com.chenweikeng.nra.tracker.RideStateTracker;
import com.chenweikeng.nra.tracker.SuppressionRegionTracker;
import com.chenweikeng.nra.util.SoundHelper;
import com.chenweikeng.nra.wizard.TutorialManager;
import com.chenweikeng.nra.wizard.WizardScreen;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotRidingAlertClient implements ClientModInitializer {
  public static final String MOD_ID = "not-riding-alert";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private static final int CHECK_INTERVAL = 200;
  private static final int CANOE_MESSAGE_COOLDOWN_TICKS = 200;
  private static final int DYNAMIC_FPS_MESSAGE_COOLDOWN_TICKS = 12000;

  public static final Component DYNAMIC_FPS_COMPATIBILITY_MESSAGE =
      Component.empty()
          .withStyle(ChatFormatting.AQUA)
          .append(
              Component.literal("[NRA] ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
          .append(
              Component.literal(
                      "For compatibility with Dynamic FPS, the window will not be minimized when MonkeyCraft client is connected.")
                  .withStyle(ChatFormatting.WHITE));

  private static volatile boolean isMonkeyAttached = false;

  private final PlayerMovementTracker movementTracker = new PlayerMovementTracker();
  private final RideStateTracker rideStateTracker = new RideStateTracker();
  private final SuppressionRegionTracker suppressionRegionTracker = new SuppressionRegionTracker();
  private final DayTimeHandler dayTimeHandler = new DayTimeHandler();
  private final ConfigReminderHandler configReminderHandler = new ConfigReminderHandler();
  private final AutograbFailureHandler autograbFailureHandler = new AutograbFailureHandler();
  private final FireworkViewingHandler fireworkViewingHandler =
      FireworkViewingHandler.getInstance();
  private final WindowMinimizeHandler windowMinimizeHandler = WindowMinimizeHandler.getInstance();
  private final ScoreboardHandler scoreboardHandler = new ScoreboardHandler();
  private final ReminderHandler reminderHandler = ReminderHandler.getInstance();

  private int tickCounter = 0;
  private long absoluteTickCounter = 0;
  private long lastCanoeMessageTick = -CANOE_MESSAGE_COOLDOWN_TICKS;
  private long lastDynamicFpsMessageTick = -DYNAMIC_FPS_MESSAGE_COOLDOWN_TICKS;
  private static boolean isRiding = false;
  private boolean wasRiding = false;
  private boolean wasOnVehicle = false;
  private boolean minimizedDuringAutograb = false;
  private RideName previousAutograbRide = null;
  private static boolean automaticallyReleasedCursor = false;

  @Override
  public void onInitializeClient() {
    ModConfig.load();
    ProfileManager.load();
    LOGGER.info("Not Riding Alert client initialized");
    MonkeycraftCompat.init();
    AutograbRegionRenderer.register();

    ClientPlayConnectionEvents.JOIN.register(
        (handler, sender, client) -> {
          ServerState.onJoin(client);
          if (ServerState.isImagineFunServer()
              && TutorialManager.getInstance().shouldStartTutorial()) {
            client.execute(
                () -> {
                  if (client.screen == null) {
                    client.setScreen(new WizardScreen());
                  }
                });
          }
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

          boolean isPassenger = isValidPassenger(client.player);
          RideName autograbRide = AutograbHolder.getRideAtLocation(client);
          isRiding =
              isPassenger || CurrentRideHolder.getCurrentRide() != null || autograbRide != null;

          handleCursorManagement(client, isPassenger, autograbRide);

          wasRiding = isRiding;
          absoluteTickCounter++;

          movementTracker.track(client, absoluteTickCounter);
          rideStateTracker.trackRideCompletion(absoluteTickCounter);
          rideStateTracker.trackVehicleState(client, absoluteTickCounter);
          suppressionRegionTracker.trackLincolnRegionEntryExit(client, rideStateTracker);
          fireworkViewingHandler.track(client);
          dayTimeHandler.resetDayTimeIfNeeded(client);
          boolean autograbFailureActive =
              autograbFailureHandler.track(client, absoluteTickCounter, movementTracker);
          if (autograbFailureActive
              && ModConfig.currentSetting.minimizeWindow != WindowMinimizeTiming.NONE) {
            windowMinimizeHandler.restoreWindow();
            minimizedDuringAutograb = false;
          }
          HibernationHandler.getInstance().track(client, absoluteTickCounter);
          configReminderHandler.track(client, absoluteTickCounter);
          scoreboardHandler.track(client);
          reminderHandler.track(client, absoluteTickCounter);
          ClosedCaptionHolder.getInstance().tick();

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

    WorldRenderEvents.AFTER_ENTITIES.register(
        context -> {
          if (!ServerState.isImagineFunServer()) {
            return;
          }
          AutograbRegionRenderer.render(context);
        });

    Identifier beforeChatId =
        Identifier.fromNamespaceAndPath(NotRidingAlertClient.MOD_ID, "before_chat");
    if (beforeChatId != null) {
      HudElementRegistry.attachElementBefore(
          VanillaHudElements.CHAT, beforeChatId, StrategyHudRendererDispatcher::render);
    }
  }

  private void handleCursorManagement(
      Minecraft client, boolean isPassenger, RideName autograbRide) {
    CursorReleaseTiming timing = ModConfig.currentSetting.cursorReleaseTiming;

    if (timing == CursorReleaseTiming.ON_ZONE_ENTRY && autograbRide != null && !isPassenger) {
      if (autograbRide != previousAutograbRide) {
        client.setScreen(null);
        if (client.mouseHandler.isMouseGrabbed()) {
          client.mouseHandler.releaseMouse();
          automaticallyReleasedCursor = true;
          sendCanoeMessageIfNeeded(client, autograbRide);
        }
        previousAutograbRide = autograbRide;
      }
    } else {
      previousAutograbRide = null;
    }

    boolean isOnVehicle = isPassenger || CurrentRideHolder.getCurrentRide() != null;
    if (timing != CursorReleaseTiming.NONE) {
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
          currentRide = AutograbHolder.getRideAtLocation(client);
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

      if (isCurrentlyRiding && client.mouseHandler.isRightPressed() && client.screen == null) {
        client.mouseHandler.releaseMouse();
      }
    }

    if (ModConfig.currentSetting.minimizeWindow != WindowMinimizeTiming.NONE) {
      WindowMinimizeTiming minimizeTiming = ModConfig.currentSetting.minimizeWindow;
      boolean shouldMinimizeOnZoneEntry = !wasRiding && isRiding;
      boolean shouldMinimizeOnVehicleMount =
          !wasOnVehicle && isOnVehicle && !minimizedDuringAutograb;

      boolean shouldMinimizeOnThisTick =
          switch (minimizeTiming) {
            case NONE -> false;
            case ON_ZONE_ENTRY -> shouldMinimizeOnZoneEntry || shouldMinimizeOnVehicleMount;
            case ON_VEHICLE_MOUNT -> shouldMinimizeOnVehicleMount;
          };

      if (shouldMinimizeOnThisTick) {
        if (MonkeycraftCompat.isClientConnected()
            && FabricLoader.getInstance().isModLoaded("dynamic_fps")) {
          sendDynamicFpsMessageIfNeeded(client);
        } else {
          if (shouldMinimizeOnZoneEntry && minimizeTiming == WindowMinimizeTiming.ON_ZONE_ENTRY) {
            minimizedDuringAutograb = true;
          }
          windowMinimizeHandler.minimizeWindow();
        }
      }

      if (!isRiding) {
        minimizedDuringAutograb = false;
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

      if (MonkeycraftCompat.isClientConnected()
          && FabricLoader.getInstance().isModLoaded("dynamic_fps")) {
        if (client.getWindow() != null) {
          long handle = client.getWindow().handle();
          boolean isMinimized =
              GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
          if (isMinimized) {
            windowMinimizeHandler.restoreWindow();
            sendDynamicFpsMessageIfNeeded(client);
          }
        }
      }
    }

    wasOnVehicle = isOnVehicle;
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

  private void sendDynamicFpsMessageIfNeeded(Minecraft client) {
    if (client.player == null) {
      return;
    }

    if (absoluteTickCounter - lastDynamicFpsMessageTick < DYNAMIC_FPS_MESSAGE_COOLDOWN_TICKS) {
      return;
    }

    lastDynamicFpsMessageTick = absoluteTickCounter;

    client.player.displayClientMessage(DYNAMIC_FPS_COMPATIBILITY_MESSAGE, false);
  }

  private void checkNotRidingAlert(Minecraft client, boolean autograbFailureActive) {
    if (client.player == null) {
      return;
    }

    if (autograbFailureActive) {
      SoundHelper.playConfiguredSound(client);
      return;
    }

    if (!ModConfig.currentSetting.enabled) {
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
    fireworkViewingHandler.reset();
    HibernationHandler.getInstance().reset();
    scoreboardHandler.reset();
    reminderHandler.reset();
    ClosedCaptionHolder.getInstance().clear();
    tickCounter = 0;
    absoluteTickCounter = 0;
    lastCanoeMessageTick = -CANOE_MESSAGE_COOLDOWN_TICKS;
    lastDynamicFpsMessageTick = -DYNAMIC_FPS_MESSAGE_COOLDOWN_TICKS;
    wasRiding = false;
    wasOnVehicle = false;
    previousAutograbRide = null;
    automaticallyReleasedCursor = false;
  }

  public static boolean isRiding() {
    return isRiding;
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

  public static boolean isValidPassenger(LocalPlayer player) {
    if (player == null) {
      return false;
    }

    // Check if player is a passenger
    if (!player.isPassenger()) {
      return false;
    }

    if (!(player.getVehicle() instanceof ArmorStand)) {
      return false;
    }

    return true;
  }

  private static void registerNraCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("nra")
            .executes(
                context -> {
                  Minecraft client = Minecraft.getInstance();
                  client.execute(
                      () -> {
                        client.setScreen(new ProfileManagementScreen(client.screen));
                      });
                  return 1;
                })
            .then(
                ClientCommandManager.literal("setup")
                    .executes(
                        context -> {
                          TutorialManager.getInstance().resetTutorial();
                          Minecraft client = Minecraft.getInstance();
                          client.execute(
                              () -> {
                                client.setScreen(new WizardScreen());
                              });
                          return 1;
                        }))
            .then(
                ClientCommandManager.literal("profile")
                    .then(
                        ClientCommandManager.argument(
                                "profileName",
                                com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                            .suggests(
                                (context, builder) -> {
                                  String remaining = builder.getRemaining().toLowerCase();

                                  ProfileManager.getAllProfiles().stream()
                                      .map(profile -> profile.name)
                                      .filter(name -> name.toLowerCase().startsWith(remaining))
                                      .forEach(builder::suggest);
                                  return builder.buildFuture();
                                })
                            .executes(
                                context -> {
                                  String profileName =
                                      com.mojang.brigadier.arguments.StringArgumentType.getString(
                                          context, "profileName");
                                  return ProfileCommandHandler.executeProfileSwitch(profileName);
                                }))));
  }
}
