package com.chenweikeng.nra;

import com.chenweikeng.nra.command.NraCommand;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.ride.CurrentRideHolder;
import com.chenweikeng.nra.ride.RegionHolder;
import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import com.chenweikeng.nra.strategy.StrategyHudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotRidingAlertClient implements ClientModInitializer {
  public static final String MOD_ID = "not-riding-alert";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  private static boolean isImagineFunServer = false;
  private int tickCounter = 0;
  private static final int CHECK_INTERVAL = 200; // Check every 200 ticks (10 seconds)
  private static boolean isRiding = false; // Cached riding state
  private boolean wasRiding = false; // Previous riding state
  private long absoluteTickCounter = 0; // Absolute tick counter (never resets)
  private long lastPlayerMovementTick = -1; // Last absolute tick when player moved via keyboard
  private static final int MOVEMENT_SUPPRESSION_TICKS =
      600; // Suppress sound for 600 ticks (30 seconds) after movement
  private long lastRideTick = -1; // Last absolute tick when player was on a ride
  private static final int RIDE_COMPLETION_SUPPRESSION_TICKS =
      100; // Suppress sound for 100 ticks (5 seconds) after ride completion
  private long lastVehicleTick = -1; // Last absolute tick when player had a vehicle
  private static final int VEHICLE_SUPPRESSION_TICKS =
      100; // Suppress sound for 100 ticks (5 seconds) after having a vehicle
  private RideName previousRide = null; // Track previous ride state to detect completion
  private static final double SUPPRESSION_LOCATION_X = 674.0;
  private static final double SUPPRESSION_LOCATION_Y = 65.0;
  private static final double SUPPRESSION_LOCATION_Z = 984.0;
  private static final double SUPPRESSION_LOCATION_RADIUS = 4.0; // Block distance of 4
  private static final int LINCOLN_SUPPRESSION_X_MIN = -132;
  private static final int LINCOLN_SUPPRESSION_X_MAX = -106;
  private static final int LINCOLN_SUPPRESSION_Z_MIN = 11;
  private static final int LINCOLN_SUPPRESSION_Z_MAX = 52;
  private boolean wasInLincolnRegion = false; // Track previous tick region state
  private boolean lincolnSuppressionActive = false; // Lincoln suppression flag
  private static boolean automaticallyReleasedCursor = false; // Track if cursor was auto-released

  @Override
  public void onInitializeClient() {
    LOGGER.info("Not Riding Alert client initialized");

    WorldRenderEvents.AFTER_ENTITIES.register(context -> RegionHolder.render(context));

    ClientPlayConnectionEvents.JOIN.register(
        (handler, sender, client) -> {
          onJoin(client);
        });

    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> {
          onDisconnect();
        });

    // Register tick event to update cached riding state and check periodically (for sound alert)
    ClientTickEvents.END_CLIENT_TICK.register(
        client -> {
          if (!isImagineFunServer) {
            return;
          }
          if (client.player == null) {
            isRiding = false;
            return;
          }

          // Update cached riding state every tick
          RideName regionRide =
              ModConfig.getInstance().autograb
                  ? RegionHolder.getRideAtLocation(client.player)
                  : null;
          isRiding =
              client.player.isPassenger()
                  || CurrentRideHolder.getCurrentRide() != null
                  || regionRide != null;

          // Handle cursor lock/unlock based on riding state (if defocus cursor is enabled)
          if (ModConfig.getInstance().defocusCursor) {
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
          wasRiding = isRiding;

          // Increment absolute tick counter
          absoluteTickCounter++;

          // Track player keyboard movement (WASD keys)
          trackPlayerMovement(client);

          // Track ride completion (when currentRide changes from non-null to null)
          trackRideCompletion();

          // Track vehicle state (update tick while player has vehicle)
          trackVehicleState(client);

          // Track Lincoln region entry/exit
          trackLincolnRegionEntryExit(client);

          // Check and save ride counts if needed (every tick, but manager handles 15s interval)
          RideCountManager.getInstance().checkAndSaveIfNeeded();

          tickCounter++;
          if (tickCounter >= CHECK_INTERVAL) {
            tickCounter = 0;
            checkNotRidingAlert(client);
          }
        });

    // Register commands
    ClientCommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess) -> {
          NraCommand.register(dispatcher);
        });

    Identifier beforeChatId =
        Identifier.fromNamespaceAndPath(NotRidingAlertClient.MOD_ID, "before_chat");
    if (beforeChatId != null) {
      HudElementRegistry.attachElementBefore(
          VanillaHudElements.CHAT, beforeChatId, StrategyHudRenderer::render);
    }
  }

  /**
   * Tracks if player is moving via keyboard input (WASD keys) or mouse clicks. Updates
   * lastPlayerMovementTick when movement keys are pressed or mouse is clicked.
   */
  private void trackPlayerMovement(Minecraft client) {
    if (client.options == null) {
      return;
    }

    // Check if any movement keys are pressed
    boolean isMoving =
        client.options.keyUp.isDown()
            || client.options.keyDown.isDown()
            || client.options.keyLeft.isDown()
            || client.options.keyRight.isDown()
            || client.options.keyJump.isDown()
            || client.options.keyShift.isDown();

    // Check if mouse buttons are pressed (left or right click)
    boolean isMouseClicking =
        client.mouseHandler.isLeftPressed() || client.mouseHandler.isRightPressed();

    if (isMoving || isMouseClicking) {
      lastPlayerMovementTick = absoluteTickCounter;
    }
  }

  /**
   * Tracks when a ride is completed (currentRide changes from non-null to null). Also updates the
   * counter while the player is on a ride to ensure at least 5 seconds of suppression even for
   * rides where the player is not sitting.
   */
  private void trackRideCompletion() {
    RideName currentRide = CurrentRideHolder.getCurrentRide();

    // Update counter when player is on a ride or just completed one
    if (currentRide != null || previousRide != null) {
      lastRideTick = absoluteTickCounter;
    }

    // Check if Lincoln ride just completed, disable Lincoln suppression
    if (previousRide == RideName.GREAT_MOMENTS_WITH_MR_LINCOLN && currentRide == null) {
      lincolnSuppressionActive = false;
    }

    previousRide = currentRide;
  }

  /**
   * Tracks vehicle state. Updates the counter while the player has a vehicle to ensure at least 5
   * seconds of suppression after they stop having a vehicle.
   */
  private void trackVehicleState(Minecraft client) {
    if (client.player != null && client.player.isPassenger()) {
      lastVehicleTick = absoluteTickCounter;
    }
  }

  /** Checks if player has moved recently (within MOVEMENT_SUPPRESSION_TICKS). */
  private boolean hasPlayerMovedRecently() {
    // If player has never moved, return false
    if (lastPlayerMovementTick < 0) {
      return false;
    }

    long ticksSinceLastMovement = absoluteTickCounter - lastPlayerMovementTick;
    return ticksSinceLastMovement < MOVEMENT_SUPPRESSION_TICKS;
  }

  /** Checks if player has been on a ride recently (within RIDE_COMPLETION_SUPPRESSION_TICKS). */
  private boolean hasRidenRecently() {
    // If player has never been on a ride, return false
    if (lastRideTick < 0) {
      return false;
    }

    long ticksSinceLastRide = absoluteTickCounter - lastRideTick;
    return ticksSinceLastRide < RIDE_COMPLETION_SUPPRESSION_TICKS;
  }

  /** Checks if player had a vehicle recently (within VEHICLE_SUPPRESSION_TICKS). */
  private boolean hasVehicleRecently() {
    // If player has never had a vehicle, return false
    if (lastVehicleTick < 0) {
      return false;
    }

    long ticksSinceLastVehicle = absoluteTickCounter - lastVehicleTick;
    return ticksSinceLastVehicle < VEHICLE_SUPPRESSION_TICKS;
  }

  /**
   * Tracks Lincoln region entry and exit. Sets lincolnSuppressionActive to true when player enters
   * the region, and to false when player leaves the region.
   */
  private void trackLincolnRegionEntryExit(Minecraft client) {
    boolean currentlyInRegion = isInLincolnSuppressionArea(client);

    if (currentlyInRegion && !wasInLincolnRegion) {
      // Player just entered the region
      lincolnSuppressionActive = true;
    } else if (!currentlyInRegion && wasInLincolnRegion) {
      // Player just left the region
      lincolnSuppressionActive = false;
    }

    wasInLincolnRegion = currentlyInRegion;
  }

  /** Checks if player is within the ROTR suppression area (circular radius). */
  private boolean isInROTRExceptionArea(Minecraft client) {
    if (client.player == null) {
      return false;
    }

    double dx = client.player.getX() - SUPPRESSION_LOCATION_X;
    double dy = client.player.getY() - SUPPRESSION_LOCATION_Y;
    double dz = client.player.getZ() - SUPPRESSION_LOCATION_Z;
    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

    return distance <= SUPPRESSION_LOCATION_RADIUS;
  }

  /** Checks if player is within the Lincoln suppression area (rectangular region). */
  private boolean isInLincolnSuppressionArea(Minecraft client) {
    if (client.player == null) {
      return false;
    }

    double x = client.player.getX();
    double z = client.player.getZ();

    return x >= LINCOLN_SUPPRESSION_X_MIN
        && x <= LINCOLN_SUPPRESSION_X_MAX
        && z >= LINCOLN_SUPPRESSION_Z_MIN
        && z <= LINCOLN_SUPPRESSION_Z_MAX;
  }

  private void checkNotRidingAlert(Minecraft client) {
    if (client.player == null) {
      return;
    }

    // Check if feature is enabled
    if (!ModConfig.getInstance().enabled) {
      return;
    }

    // Play sound when player is NOT riding, but suppress if player has been moving recently, just
    // completed a ride, had a vehicle recently, or is in a suppression exception area
    if (!isRiding
        && !hasPlayerMovedRecently()
        && !hasRidenRecently()
        && !hasVehicleRecently()
        && !isInROTRExceptionArea(client)
        && !lincolnSuppressionActive) {
      playSound(client);
    }
  }

  private void playSound(Minecraft client) {
    if (client.player == null) return;
    if (client.level == null) return;

    try {
      String soundId = ModConfig.getInstance().soundId;
      Identifier soundIdentifier = Identifier.parse(soundId);

      // Try to get the sound event from the registry
      SoundEvent soundEvent = null;
      try {
        // Try to get from sound registry
        var registry = client.level.registryAccess().lookupOrThrow(Registries.SOUND_EVENT);
        soundEvent = registry.getValue(soundIdentifier);
      } catch (Exception e) {
        // If not found, try common sound events
      }

      // Fallback to default if not found
      if (soundEvent == null) {
        soundEvent = SoundEvents.EXPERIENCE_ORB_PICKUP;
      }

      // Play sound at player's position
      client.level.playSound(
          client.player,
          client.player.getX(),
          client.player.getY(),
          client.player.getZ(),
          soundEvent,
          SoundSource.MASTER,
          1.0f,
          1.0f);
    } catch (Exception e) {
      // Fallback to default sound
      if (client.player != null && client.level != null) {
        SoundEvent fallbackSound = SoundEvents.EXPERIENCE_ORB_PICKUP;
        client.level.playSound(
            client.player,
            client.player.getX(),
            client.player.getY(),
            client.player.getZ(),
            fallbackSound,
            SoundSource.MASTER,
            1.0f,
            1.0f);
      }
    }
  }

  public static boolean isRiding(net.minecraft.client.player.LocalPlayer player) {
    if (player == null) {
      return isRiding || CurrentRideHolder.getCurrentRide() != null;
    }
    RideName regionRide =
        ModConfig.getInstance().autograb ? RegionHolder.getRideAtLocation(player) : null;
    return isRiding || CurrentRideHolder.getCurrentRide() != null || regionRide != null;
  }

  public static boolean isImagineFunServer() {
    return isImagineFunServer;
  }

  public static boolean isAutomaticallyReleasedCursor() {
    return automaticallyReleasedCursor;
  }

  public static void onJoin(Minecraft client) {
    if (client.getCurrentServer() == null || client.getCurrentServer().ip == null) {
      isImagineFunServer = false;
      LOGGER.info("No server info available on join");
      return;
    }

    String serverIp = client.getCurrentServer().ip.toLowerCase();
    isImagineFunServer = serverIp.endsWith(".imaginefun.net");

    if (isImagineFunServer) {
      LOGGER.info("Joined ImagineFun.net server: {}", serverIp);
    } else {
      LOGGER.info("Joined non-ImagineFun.net server: {}", serverIp);
    }
  }

  public static void onDisconnect() {
    LOGGER.info("Disconnected from server");
    isImagineFunServer = false;
  }
}
