package com.chenweikeng.nra;

import com.chenweikeng.nra.audio.OpenAudioMcService;
import com.chenweikeng.nra.compat.MonkeycraftCompat;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.config.profile.HistoryManager;
import com.chenweikeng.nra.config.profile.ProfileCommandHandler;
import com.chenweikeng.nra.config.profile.ProfileManager;
import com.chenweikeng.nra.config.profile.ui.ProfileManagementScreen;
import com.chenweikeng.nra.handler.AdvanceNoticeHandler;
import com.chenweikeng.nra.handler.AutograbFailureHandler;
import com.chenweikeng.nra.handler.AutograbRegionRenderer;
import com.chenweikeng.nra.handler.ClosedCaptionHolder;
import com.chenweikeng.nra.handler.ConfigReminderHandler;
import com.chenweikeng.nra.handler.DayTimeHandler;
import com.chenweikeng.nra.handler.FireworkViewingHandler;
import com.chenweikeng.nra.handler.HibernationHandler;
import com.chenweikeng.nra.handler.ReminderHandler;
import com.chenweikeng.nra.handler.ScoreboardHandler;
import com.chenweikeng.nra.report.DailyRideSnapshot;
import com.chenweikeng.nra.report.RideReportNotifier;
import com.chenweikeng.nra.report.ui.RideReportScreen;
import com.chenweikeng.nra.ride.AutograbHolder;
import com.chenweikeng.nra.ride.ClosestRideHolder;
import com.chenweikeng.nra.ride.CurrentRideHolder;
import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import com.chenweikeng.nra.session.SessionStatsHudRenderer;
import com.chenweikeng.nra.session.SessionTracker;
import com.chenweikeng.nra.strategy.StrategyHudRendererDispatcher;
import com.chenweikeng.nra.tracker.PlayerMovementTracker;
import com.chenweikeng.nra.tracker.RideStateTracker;
import com.chenweikeng.nra.tracker.SuppressionRegionTracker;
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
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotRidingAlertClient implements ClientModInitializer {
  public static final String MOD_ID = "not-riding-alert";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private final GameState gameState = GameState.getInstance();
  private final PlayerMovementTracker movementTracker = new PlayerMovementTracker();
  private final RideStateTracker rideStateTracker = new RideStateTracker();
  private final SuppressionRegionTracker suppressionRegionTracker = new SuppressionRegionTracker();
  private final DayTimeHandler dayTimeHandler = new DayTimeHandler();
  private final ConfigReminderHandler configReminderHandler = new ConfigReminderHandler();
  private final AutograbFailureHandler autograbFailureHandler = new AutograbFailureHandler();
  private final FireworkViewingHandler fireworkViewingHandler =
      FireworkViewingHandler.getInstance();
  private final ScoreboardHandler scoreboardHandler = new ScoreboardHandler();
  private final ReminderHandler reminderHandler = ReminderHandler.getInstance();
  private final AlertChecker alertChecker = new AlertChecker();
  private final CursorManager cursorManager = new CursorManager();
  private final AdvanceNoticeHandler advanceNoticeHandler = new AdvanceNoticeHandler();

  private int tickCounter = 0;

  @Override
  public void onInitializeClient() {
    ModConfig.load();
    ProfileManager.load();
    HistoryManager.load();
    DailyRideSnapshot.getInstance();
    LOGGER.info("Not Riding Alert client initialized");
    MonkeycraftCompat.init();
    AutograbRegionRenderer.register();

    ClientPlayConnectionEvents.JOIN.register(
        (handler, sender, client) -> {
          ServerState.onJoin(client);
          if (ServerState.isImagineFunServer()) {
            SessionTracker.getInstance().onSessionStart();
          }
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
          SessionTracker.getInstance().onSessionEnd();
          OpenAudioMcService.getInstance().disconnect();
          ServerState.onDisconnect();
          resetAllTrackers();
        });

    ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

    ClientCommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess) -> {
          registerNraCommand(dispatcher);
          registerOaCommand(dispatcher);
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

    Identifier sessionStatsId =
        Identifier.fromNamespaceAndPath(NotRidingAlertClient.MOD_ID, "session_stats");
    if (sessionStatsId != null) {
      HudElementRegistry.attachElementBefore(
          VanillaHudElements.CHAT, sessionStatsId, SessionStatsHudRenderer::render);
    }
  }

  private void onClientTick(Minecraft client) {
    if (!ServerState.isImagineFunServer()) {
      return;
    }
    if (client.player == null) {
      gameState.setRiding(false);
      return;
    }

    boolean wasPassenger = cursorManager.wasPassenger();
    boolean isPassenger = gameState.isValidPassenger(client.player);
    RideName autograbRide = AutograbHolder.getRideAtLocation(client);

    gameState.updateSittingState(wasPassenger, isPassenger);
    gameState.clearSittingIfNotPassenger(client.player.isPassenger());

    isPassenger = gameState.isValidPassenger(client.player);

    boolean isRiding =
        isPassenger || CurrentRideHolder.getCurrentRide() != null || autograbRide != null;
    gameState.setRiding(isRiding);

    cursorManager.tick(client, isPassenger, isRiding, autograbRide);
    gameState.incrementTickCounter();

    long currentTick = gameState.getAbsoluteTickCounter();
    movementTracker.track(client, currentTick);
    rideStateTracker.trackRideCompletion(currentTick);
    rideStateTracker.trackVehicleState(client, currentTick);
    suppressionRegionTracker.trackLincolnRegionEntryExit(client, rideStateTracker);
    fireworkViewingHandler.track(client);
    dayTimeHandler.resetDayTimeIfNeeded(client);
    boolean autograbFailureActive =
        autograbFailureHandler.track(client, currentTick, movementTracker);
    gameState.setAutograbFailureActive(autograbFailureActive);
    if (autograbFailureActive) {
      cursorManager.handleAutograbFailureRestore();
    }
    HibernationHandler.getInstance().track(client, currentTick);
    configReminderHandler.track(client, currentTick);
    scoreboardHandler.track(client);
    ClosestRideHolder.update(client);
    advanceNoticeHandler.tick(client);
    reminderHandler.track(client, currentTick);
    ClosedCaptionHolder.getInstance().tick();

    RideCountManager.getInstance().checkAndSaveIfNeeded();
    SessionTracker.getInstance().checkAndSaveIfNeeded();
    RideReportNotifier.getInstance().tick();

    tickCounter++;
    if (tickCounter >= Timing.ALERT_CHECK_INTERVAL) {
      tickCounter = 0;
      alertChecker.check(
          client,
          autograbFailureActive,
          movementTracker,
          rideStateTracker,
          suppressionRegionTracker);
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
    ClosestRideHolder.reset();
    reminderHandler.reset();
    ClosedCaptionHolder.getInstance().clear();
    cursorManager.reset();
    advanceNoticeHandler.reset();
    RideReportNotifier.getInstance().reset();
    gameState.reset();
    tickCounter = 0;
  }

  public static boolean isImagineFunServer() {
    return ServerState.isImagineFunServer();
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
                ClientCommandManager.literal("ridereport")
                    .executes(
                        context -> {
                          Minecraft client = Minecraft.getInstance();
                          client.execute(
                              () -> {
                                // Default: show live report for today
                                client.setScreen(RideReportScreen.createLive(client.screen));
                              });
                          return 1;
                        })
                    .then(
                        ClientCommandManager.argument(
                                "date", com.mojang.brigadier.arguments.StringArgumentType.word())
                            .executes(
                                context -> {
                                  String date =
                                      com.mojang.brigadier.arguments.StringArgumentType.getString(
                                          context, "date");
                                  Minecraft client = Minecraft.getInstance();
                                  client.execute(
                                      () -> {
                                        client.setScreen(new RideReportScreen(client.screen, date));
                                      });
                                  return 1;
                                })))
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

  private static void registerOaCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("oa")
            .then(
                ClientCommandManager.literal("connect")
                    .executes(
                        context -> {
                          OpenAudioMcService.getInstance().connectViaCommand();
                          return 1;
                        }))
            .then(
                ClientCommandManager.literal("disconnect")
                    .executes(
                        context -> {
                          OpenAudioMcService.getInstance().disconnectViaCommand();
                          return 1;
                        }))
            .then(
                ClientCommandManager.literal("reconnect")
                    .executes(
                        context -> {
                          OpenAudioMcService.getInstance().reconnectWithFallback();
                          return 1;
                        })));
  }
}
