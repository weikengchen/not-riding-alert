package com.chenweikeng.nra.handler;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class WindowMinimizeHandler {
  private static final WindowMinimizeHandler INSTANCE = new WindowMinimizeHandler();

  private WindowMinimizeHandler() {}

  public static WindowMinimizeHandler getInstance() {
    return INSTANCE;
  }

  public void minimizeWindow() {
    Minecraft client = Minecraft.getInstance();
    if (client.getWindow() == null) {
      return;
    }

    long handle = client.getWindow().handle();
    boolean isMinimized = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;

    if (!isMinimized) {
      client.execute(
          () -> {
            GLFW.glfwIconifyWindow(handle);
          });
    }
  }

  public void restoreWindow() {
    Minecraft client = Minecraft.getInstance();
    if (client.getWindow() == null) {
      return;
    }

    long handle = client.getWindow().handle();
    boolean isMinimized = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;

    if (isMinimized) {
      client.execute(
          () -> {
            GLFW.glfwRestoreWindow(handle);
            GLFW.glfwFocusWindow(handle);
          });
    }
  }
}
