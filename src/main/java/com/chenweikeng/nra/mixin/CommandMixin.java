package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.NotRidingAlertClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class CommandMixin {
  @Inject(method = "sendCommand", at = @At("HEAD"))
  private void onSendCommand(String command, CallbackInfo ci) {
    if (command.equals("sit")) {
      NotRidingAlertClient.setLastSitCommand();
    }
  }
}
