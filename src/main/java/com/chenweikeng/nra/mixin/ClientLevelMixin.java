package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.ride.CurrentRideHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
  private static final Identifier RIDE_COMPLETE_SOUND =
      Identifier.fromNamespaceAndPath("minecraft", "ride.complete");

  @Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
  private void onPlaySound(
      double d,
      double e,
      double f,
      SoundEvent soundEvent,
      SoundSource soundSource,
      float g,
      float h,
      boolean bl,
      long l,
      CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    if (soundEvent != null) {
      // Don't suppress the ride complete sound
      Identifier soundId = soundEvent.location();
      if (soundId != null && soundId.equals(RIDE_COMPLETE_SOUND)) {
        return; // Let the ride complete sound play
      }

      // Suppress other sounds when riding
      if (ModConfig.getInstance().silent) {
        Minecraft client = Minecraft.getInstance();

        // Check if player is riding an entity (Minecraft's hasVehicle) or on a ride
        // (CurrentRideHolder)
        if (client != null
            && client.player != null
            && (client.player.isPassenger() || CurrentRideHolder.getCurrentRide() != null)) {
          // Cancel sound when riding (except ride.complete)
          ci.cancel();
        }
      }
    }
  }
}
