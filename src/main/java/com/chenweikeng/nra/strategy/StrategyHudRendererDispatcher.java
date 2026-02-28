package com.chenweikeng.nra.strategy;

import com.chenweikeng.nra.config.ModConfig;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class StrategyHudRendererDispatcher {

  public static void update() {
    switch (ModConfig.getInstance().strategyHudRendererVersion) {
      case V0 -> StrategyHudRendererV0.update();
      case V1 -> StrategyHudRendererV1.update();
      case V2 -> StrategyHudRendererV2.update();
    }
  }

  public static void setError(String error) {
    switch (ModConfig.getInstance().strategyHudRendererVersion) {
      case V0 -> StrategyHudRendererV0.setError(error);
      case V1 -> StrategyHudRendererV1.setError(error);
      case V2 -> StrategyHudRendererV2.setError(error);
    }
  }

  public static String getError() {
    return switch (ModConfig.getInstance().strategyHudRendererVersion) {
      case V0 -> StrategyHudRendererV0.getError();
      case V1 -> StrategyHudRendererV1.getError();
      case V2 -> StrategyHudRendererV2.getError();
    };
  }

  public static void render(GuiGraphics context, DeltaTracker tickCounter) {
    if (!ModConfig.getInstance().enableTracker) {
      return;
    }
    switch (ModConfig.getInstance().strategyHudRendererVersion) {
      case V0 -> StrategyHudRendererV0.render(context, tickCounter);
      case V1 -> StrategyHudRendererV1.render(context, tickCounter);
      case V2 -> StrategyHudRendererV2.render(context, tickCounter);
    }
  }

  public static List<RideGoal> getTopGoals() {
    return switch (ModConfig.getInstance().strategyHudRendererVersion) {
      case V0 -> StrategyHudRendererV0.getTopGoals();
      case V1 -> StrategyHudRendererV1.getTopGoals();
      case V2 -> StrategyHudRendererV2.getTopGoals();
    };
  }
}
