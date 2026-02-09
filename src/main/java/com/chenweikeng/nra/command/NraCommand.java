package com.chenweikeng.nra.command;

import com.chenweikeng.nra.config.ClothConfigScreen;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class NraCommand {
  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
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
