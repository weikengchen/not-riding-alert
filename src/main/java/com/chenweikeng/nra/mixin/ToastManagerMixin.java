package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.report.ui.RideReportScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses toast rendering (e.g. "Chat messages can't be verified") while the ride report screen
 * is active so they don't clutter the report or appear in shared screenshots.
 */
@Mixin(ToastManager.class)
public class ToastManagerMixin {
  @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
  private void suppressToastsDuringRideReport(GuiGraphicsExtractor graphics, CallbackInfo ci) {
    if (Minecraft.getInstance().screen instanceof RideReportScreen) {
      ci.cancel();
    }
  }
}
