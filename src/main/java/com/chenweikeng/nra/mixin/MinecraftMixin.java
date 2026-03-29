package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.GameState;
import com.chenweikeng.nra.ServerState;
import com.chenweikeng.nra.report.ui.RideReportScreen;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
  @Inject(method = "pauseIfInactive", at = @At("HEAD"), cancellable = true)
  private void onPauseIfInactive(CallbackInfo ci) {
    if (GameState.getInstance().isAutomaticallyReleasedCursor()) {
      ci.cancel();
    }
  }

  /**
   * On ImagineFun, override the Advancements key to open the Ride Report instead. The vanilla
   * Advancements screen is meaningless on this server, so we intercept before handleKeybinds()
   * processes it.
   */
  @Inject(method = "handleKeybinds", at = @At("HEAD"))
  private void overrideAdvancementsKey(CallbackInfo ci) {
    if (!ServerState.isImagineFunServer()) {
      return;
    }
    Minecraft client = (Minecraft) (Object) this;
    if (client.player == null || client.screen != null) {
      return;
    }
    while (client.options.keyAdvancements.consumeClick()) {
      client.setScreen(RideReportScreen.createLive(null));
    }
  }
}
