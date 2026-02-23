package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.config.ModConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

  @Inject(
      at = @At("HEAD"),
      method =
          "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V",
      cancellable = true)
  private void onRenderScoreboardSidebar(
      GuiGraphics context, Objective objective, CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    if ((!ModConfig.getInstance().keepUnchanged && NotRidingAlertClient.isMonkeyAttached())
        || ModConfig.getInstance().hideScoreboard) {
      ci.cancel();
    }
  }

  @Inject(
      at = @At("HEAD"),
      method =
          "renderChat(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
      cancellable = true)
  private void onRenderChat(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    if ((!ModConfig.getInstance().keepUnchanged && NotRidingAlertClient.isMonkeyAttached())
        || ModConfig.getInstance().hideChat) {
      ci.cancel();
    }
  }

  @Inject(at = @At("HEAD"), method = "renderPlayerHealth", cancellable = true)
  private void onRenderPlayerHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    if (NotRidingAlertClient.isMonkeyAttached() || ModConfig.getInstance().hideHealth) {
      ci.cancel();
    }
  }

  @Inject(at = @At("HEAD"), method = "renderVehicleHealth", cancellable = true)
  private void onRenderVehicleHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    if (NotRidingAlertClient.isMonkeyAttached() || ModConfig.getInstance().hideHealth) {
      ci.cancel();
    }
  }
}
