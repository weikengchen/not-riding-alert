package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.ride.CurrentRideHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public class OptionsMixin {
    @Inject(
        method = "getFinalSoundSourceVolume(Lnet/minecraft/sounds/SoundSource;)F",
        at = @At("RETURN"),
        cancellable = true
    )
    private void suppressVolumeWhenRiding(SoundSource category, CallbackInfoReturnable<Float> cir) {
        // Check if the feature is enabled
        if (!NotRidingAlertClient.getConfig().isEnabled()) {
            return;
        }
        
        Minecraft client = Minecraft.getInstance();
        
        // Check if player is riding an entity (Minecraft's hasVehicle) or on a ride (CurrentRideHolder)
        if (client != null && client.player != null 
            && (client.player.isPassenger() || CurrentRideHolder.getCurrentRide() != null)) {
            // Return 0 volume when riding
            cir.setReturnValue(0.0f);
        }
    }
}

