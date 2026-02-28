package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.handler.ClosedCaptionHolder;
import com.chenweikeng.nra.handler.HibernationHandler;
import com.chenweikeng.nra.handler.ReminderHandler;
import com.chenweikeng.nra.ride.LastRideHolder;
import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public class ChatListenerMixin {
  private static final String RIDE_OVERVIEW_MARKER = "<<-----------| Ride Overview |----------->>";
  private static final String ATTRACTION_OVERVIEW_MARKER =
      "<<-----------| Attraction Overview |----------->>";
  private static final String CC_MARKER = "[CC]";

  @Inject(at = @At("HEAD"), method = "handleSystemMessage", cancellable = true)
  private void onGameMessage(Component message, boolean overlay, CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    if (message == null) return;

    String msg = message.getString();

    if (msg.startsWith(CC_MARKER) && ModConfig.getInstance().relocateClosedCaption) {
      handleClosedCaption(message);
      ci.cancel();
      return;
    }
    if (ModConfig.getInstance().hideLovePotionMessages && msg.contains(": §d§o")) {
      ci.cancel();
      return;
    }

    if (msg.equals("You are now connected with the audio client!")) {
      ReminderHandler.getInstance().setAudioConnected(true);
      return;
    }
    if (msg.equals("Your audio session has been ended")) {
      ReminderHandler.getInstance().setAudioConnected(false);
      return;
    }

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
    countManager.updateRideCount(lastRide, current + 1);

    HibernationHandler.getInstance().cancelPendingCancellation();
  }

  private void handleClosedCaption(Component message) {
    List<Component> parts = message.toFlatList(Style.EMPTY);

    int separatorIndex = -1;
    for (int i = 0; i < parts.size(); i++) {
      Component part = parts.get(i);
      if (part.getString().equals(" ") && part.getStyle().isEmpty()) {
        separatorIndex = i;
        break;
      }
    }

    if (separatorIndex < 0 || separatorIndex >= parts.size() - 1) {
      return;
    }

    int startIndex = separatorIndex + 1;

    Component firstPart = parts.get(startIndex);
    TextColor firstColor = firstPart.getStyle().getColor();
    if (firstPart.getString().equals(": ")
        && firstColor != null
        && firstColor.getValue() == 0xFFFFFF) {
      startIndex++;
    }

    if (startIndex >= parts.size()) {
      return;
    }

    MutableComponent overlayComponent = Component.empty();
    for (int i = startIndex; i < parts.size(); i++) {
      overlayComponent.append(parts.get(i).copy());
    }

    ClosedCaptionHolder.getInstance().setCaption(overlayComponent);
    Minecraft.getInstance().gui.setTitle(Component.empty());
    Minecraft.getInstance().gui.setTimes(0, 200, 0);
  }
}
