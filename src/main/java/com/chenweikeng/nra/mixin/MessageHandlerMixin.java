package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.ride.LastRideHolder;
import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
public class MessageHandlerMixin {
    private static final String RIDE_OVERVIEW_MARKER = "<<-----------| Ride Overview |----------->>";
    private static final String ATTRACTION_OVERVIEW_MARKER = "<<-----------| Attraction Overview |----------->>";

    @Inject(at = @At("HEAD"), method = "onGameMessage")
    private void onGameMessage(Text message, boolean overlay, CallbackInfo ci) {
        if (message == null) return;
        String msg = message.getString();
        if (!msg.contains(RIDE_OVERVIEW_MARKER) && !msg.contains(ATTRACTION_OVERVIEW_MARKER)) return;

        RideName lastRide = LastRideHolder.getLastRide();
        if (lastRide == null) {
            return;
        }
        if (lastRide == RideName.UNKNOWN) {
            return;
        }

        RideCountManager countManager = RideCountManager.getInstance();
        int current = countManager.getRideCount(lastRide);
        countManager.updateRideCount(lastRide, current + 1);    }
}
