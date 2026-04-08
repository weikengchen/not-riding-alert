package com.chenweikeng.nra;

import com.chenweikeng.nra.compat.MonkeycraftCompat;
import com.chenweikeng.nra.config.CursorReleaseTiming;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.config.WindowMinimizeTiming;
import com.chenweikeng.nra.handler.WindowMinimizeHandler;
import com.chenweikeng.nra.ride.AutograbHolder;
import com.chenweikeng.nra.ride.CurrentRideHolder;
import com.chenweikeng.nra.ride.RideName;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class CursorManager {
  public static final Component DYNAMIC_FPS_COMPATIBILITY_MESSAGE =
      Component.empty()
          .withStyle(ChatFormatting.AQUA)
          .append(
              Component.literal("[NRA] ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
          .append(
              Component.literal(
                      "For compatibility with Dynamic FPS, the window will not be minimized when MonkeyCraft client is connected.")
                  .withStyle(ChatFormatting.WHITE));

  private boolean wasRiding = false;
  private boolean wasOnVehicle = false;
  private boolean wasPassenger = false;
  private boolean minimizedDuringAutograb = false;
  private boolean autograbFailureRestored = false;
  private RideName previousAutograbRide = null;
  private long lastCanoeMessageTick = -Timing.CANOE_MESSAGE_COOLDOWN_TICKS;
  private long lastDynamicFpsMessageTick = -Timing.DYNAMIC_FPS_MESSAGE_COOLDOWN_TICKS;
  private final WindowMinimizeHandler windowMinimizeHandler = WindowMinimizeHandler.getInstance();

  public void tick(Minecraft client, boolean isPassenger, boolean isRiding, RideName autograbRide) {
    GameState state = GameState.getInstance();
    CursorReleaseTiming timing = ModConfig.currentSetting.cursorReleaseTiming;

    if (timing == CursorReleaseTiming.ON_ZONE_ENTRY && autograbRide != null && !isPassenger) {
      if (autograbRide != previousAutograbRide) {
        client.setScreen(null);
        if (client.mouseHandler.isMouseGrabbed()) {
          client.mouseHandler.releaseMouse();
          state.setAutomaticallyReleasedCursor(true);
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
        state.setAutomaticallyReleasedCursor(true);
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
        state.setAutomaticallyReleasedCursor(false);
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

      if ((isCurrentlyRiding || client.player.isPassenger())
          && client.mouseHandler.isRightPressed()
          && client.screen == null) {
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
        // Suppress pause-on-focus-loss for 10 ticks (500ms) to give GLFW time to
        // process the restore/focus chain before setWindowActive() kicks in.
        state.setWindowRestoreGrace(10);
        windowMinimizeHandler.restoreWindow();
      }
      if (wasRiding && !isRiding) {
        windowMinimizeHandler.requestAttention();
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

    wasRiding = isRiding;
    wasOnVehicle = isOnVehicle;
    wasPassenger = isPassenger;
  }

  public boolean wasPassenger() {
    return wasPassenger;
  }

  public void clearAutograbFailureRestored() {
    autograbFailureRestored = false;
  }

  public void handleAutograbFailureRestore() {
    if (autograbFailureRestored) {
      return;
    }
    autograbFailureRestored = true;
    if (ModConfig.currentSetting.minimizeWindow != WindowMinimizeTiming.NONE) {
      windowMinimizeHandler.restoreWindow();
      minimizedDuringAutograb = false;
    }
    windowMinimizeHandler.requestAttention();
  }

  private void sendCanoeMessageIfNeeded(Minecraft client, RideName ride) {
    if (client.player == null || ride != RideName.DAVY_CROCKETTS_EXPLORER_CANOES) {
      return;
    }

    GameState state = GameState.getInstance();
    if (state.getAbsoluteTickCounter() - lastCanoeMessageTick
        < Timing.CANOE_MESSAGE_COOLDOWN_TICKS) {
      return;
    }

    lastCanoeMessageTick = state.getAbsoluteTickCounter();

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

    GameState state = GameState.getInstance();
    if (state.getAbsoluteTickCounter() - lastDynamicFpsMessageTick
        < Timing.DYNAMIC_FPS_MESSAGE_COOLDOWN_TICKS) {
      return;
    }

    lastDynamicFpsMessageTick = state.getAbsoluteTickCounter();

    client.player.displayClientMessage(DYNAMIC_FPS_COMPATIBILITY_MESSAGE, false);
  }

  public void reset() {
    wasRiding = false;
    wasOnVehicle = false;
    wasPassenger = false;
    minimizedDuringAutograb = false;
    autograbFailureRestored = false;
    previousAutograbRide = null;
    lastCanoeMessageTick = -Timing.CANOE_MESSAGE_COOLDOWN_TICKS;
    lastDynamicFpsMessageTick = -Timing.DYNAMIC_FPS_MESSAGE_COOLDOWN_TICKS;
  }
}
