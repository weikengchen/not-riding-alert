package com.chenweikeng.nra.strategy;

import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.config.StrategyHudRendererVersion;
import com.chenweikeng.nra.ride.RideCountManager;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class StrategyHudRendererDispatcher {
  private static final Map<StrategyHudRendererVersion, StrategyHudRenderer> RENDERERS =
      new EnumMap<>(StrategyHudRendererVersion.class);

  static {
    RENDERERS.put(
        StrategyHudRendererVersion.V0,
        new StrategyHudRenderer() {
          @Override
          public void update() {
            StrategyHudRendererV0.update();
          }

          @Override
          public void setError(String error) {
            StrategyHudRendererV0.setError(error);
          }

          @Override
          public String getError() {
            return StrategyHudRendererV0.getError();
          }

          @Override
          public void render(GuiGraphics context, DeltaTracker tickCounter) {
            StrategyHudRendererV0.render(context, tickCounter);
          }

          @Override
          public List<RideGoal> getTopGoals() {
            return StrategyHudRendererV0.getTopGoals();
          }
        });

    RENDERERS.put(
        StrategyHudRendererVersion.V1,
        new StrategyHudRenderer() {
          @Override
          public void update() {
            StrategyHudRendererV1.update();
          }

          @Override
          public void setError(String error) {
            StrategyHudRendererV1.setError(error);
          }

          @Override
          public String getError() {
            return StrategyHudRendererV1.getError();
          }

          @Override
          public void render(GuiGraphics context, DeltaTracker tickCounter) {
            StrategyHudRendererV1.render(context, tickCounter);
          }

          @Override
          public List<RideGoal> getTopGoals() {
            return StrategyHudRendererV1.getTopGoals();
          }
        });

    RENDERERS.put(
        StrategyHudRendererVersion.V2,
        new StrategyHudRenderer() {
          @Override
          public void update() {
            StrategyHudRendererV2.update();
          }

          @Override
          public void setError(String error) {
            StrategyHudRendererV2.setError(error);
          }

          @Override
          public String getError() {
            return StrategyHudRendererV2.getError();
          }

          @Override
          public void render(GuiGraphics context, DeltaTracker tickCounter) {
            StrategyHudRendererV2.render(context, tickCounter);
          }

          @Override
          public List<RideGoal> getTopGoals() {
            return StrategyHudRendererV2.getTopGoals();
          }
        });
  }

  private static StrategyHudRenderer getCurrent() {
    return RENDERERS.get(ModConfig.currentSetting.strategyHudRendererVersion);
  }

  public static void update() {
    getCurrent().update();
  }

  public static void setError(String error) {
    getCurrent().setError(error);
  }

  public static String getError() {
    return getCurrent().getError();
  }

  public static void render(GuiGraphics context, DeltaTracker tickCounter) {
    if (!ModConfig.currentSetting.enableTracker) {
      return;
    }
    if (!RideCountManager.getInstance().hasBasicCounts()) {
      return;
    }
    getCurrent().render(context, tickCounter);
  }

  public static List<RideGoal> getTopGoals() {
    return getCurrent().getTopGoals();
  }
}
