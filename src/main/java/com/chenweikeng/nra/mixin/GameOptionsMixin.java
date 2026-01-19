package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.ride.CurrentRideHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
    @Inject(
        method = "getSoundVolume(Lnet/minecraft/sound/SoundCategory;)F",
        at = @At("RETURN"),
        cancellable = true
    )
    private void suppressVolumeWhenRiding(SoundCategory category, CallbackInfoReturnable<Float> cir) {
        // Check if the feature is enabled
        if (!NotRidingAlertClient.getConfig().isEnabled()) {
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Check if player is riding an entity (Minecraft's hasVehicle) or on a ride (CurrentRideHolder)
        if (client != null && client.player != null 
            && (client.player.hasVehicle() || CurrentRideHolder.getCurrentRide() != null)) {
            // Return 0 volume when riding
            cir.setReturnValue(0.0f);
        }
    }
}

