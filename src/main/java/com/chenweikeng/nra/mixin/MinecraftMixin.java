package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.NotRidingAlertClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
  @Inject(method = "setWindowActive", at = @At("HEAD"), cancellable = true)
  private void onSetWindowActive(boolean bl, CallbackInfo ci) {
    if (NotRidingAlertClient.isAutomaticallyReleasedCursor()) {
      ci.cancel();
    }
  }
}
