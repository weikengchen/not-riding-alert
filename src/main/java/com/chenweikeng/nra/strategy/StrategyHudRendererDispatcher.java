package com.chenweikeng.nra.strategy;

import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.config.StrategyHudRendererVersion;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class StrategyHudRendererDispatcher {

  public static void update() {
    if (ModConfig.getInstance().strategyHudRendererVersion == StrategyHudRendererVersion.V1) {
      StrategyHudRendererV1.update();
    } else {
      StrategyHudRenderer.update();
    }
  }

  public static void setError(String error) {
    if (ModConfig.getInstance().strategyHudRendererVersion == StrategyHudRendererVersion.V1) {
      StrategyHudRendererV1.setError(error);
    } else {
      StrategyHudRenderer.setError(error);
    }
  }

  public static String getError() {
    if (ModConfig.getInstance().strategyHudRendererVersion == StrategyHudRendererVersion.V1) {
      return StrategyHudRendererV1.getError();
    } else {
      return StrategyHudRenderer.getError();
    }
  }

  public static void render(GuiGraphics context, DeltaTracker tickCounter) {
    if (ModConfig.getInstance().strategyHudRendererVersion == StrategyHudRendererVersion.V1) {
      StrategyHudRendererV1.render(context, tickCounter);
    } else {
      StrategyHudRenderer.render(context, tickCounter);
    }
  }

  public static List<RideGoal> getTopGoals() {
    if (ModConfig.getInstance().strategyHudRendererVersion == StrategyHudRendererVersion.V1) {
      return StrategyHudRendererV1.getTopGoals();
    } else {
      return StrategyHudRenderer.getTopGoals();
    }
  }
}
