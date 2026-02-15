package com.chenweikeng.nra;

import com.chenweikeng.nra.config.ModConfig;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerState {
  private static final Logger LOGGER = LoggerFactory.getLogger(NotRidingAlertClient.MOD_ID);
  private static boolean isImagineFunServer = false;

  public static boolean isImagineFunServer() {
    return isImagineFunServer && ModConfig.getInstance().globalEnable;
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
