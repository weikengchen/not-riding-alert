package com.chenweikeng.nra.mixin;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.handler.ClosedCaptionHolder;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
  @Shadow private int titleTime;
  @Shadow private int titleFadeInTime;
  @Shadow private int titleStayTime;
  @Shadow private int titleFadeOutTime;
  @Shadow @Final private Minecraft minecraft;

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

  @Inject(at = @At("HEAD"), method = "renderItemHotbar", cancellable = true)
  private void onRenderItemHotbar(
      GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    if (ModConfig.getInstance().hideHotbar) {
      ci.cancel();
    }
  }

  @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
  private void onRenderCrosshair(
      GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    if (!ModConfig.getInstance().relocateClosedCaption) {
      return;
    }
    boolean isRiding = NotRidingAlertClient.isRiding(minecraft.player);
    if (!isRiding && titleTime <= 0) {
      ClosedCaptionHolder.getInstance().clear();
      return;
    }
    Component caption = ClosedCaptionHolder.getInstance().getCaption();
    if (caption == null) {
      return;
    }
    ci.cancel();
  }

  @Inject(at = @At("HEAD"), method = "renderTitle", cancellable = true)
  private void onRenderTitle(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    if (!NotRidingAlertClient.isImagineFunServer()) {
      return;
    }
    if (!ModConfig.getInstance().relocateClosedCaption) {
      return;
    }

    boolean isRiding = NotRidingAlertClient.isRiding(minecraft.player);
    if (!isRiding && titleTime <= 0) {
      return;
    }

    Component caption = ClosedCaptionHolder.getInstance().getCaption();
    if (caption == null) {
      return;
    }

    ci.cancel();

    Font font = minecraft.font;
    int guiWidth = guiGraphics.guiWidth();
    int guiHeight = guiGraphics.guiHeight();

    float scale = 2.0f;
    int maxWidth = (int) (guiWidth * 0.8f / scale);
    int textColor = ARGB.color(255, 255, 255, 255);
    int shadowColor = ARGB.color(77, 255, 255, 255);
    Component shadowCaption = scaleCaptionColors(caption);
    List<net.minecraft.util.FormattedCharSequence> linesShadow =
        font.split(shadowCaption, maxWidth);
    List<net.minecraft.util.FormattedCharSequence> lines = font.split(caption, maxWidth);

    if (lines.isEmpty()) {
      return;
    }

    int lineHeight = font.lineHeight;
    int lineSpacing = 2;
    int totalHeight = lines.size() * (lineHeight + lineSpacing) - lineSpacing;

    guiGraphics.nextStratum();

    int[][] directions = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

    for (int[] dir : directions) {
      guiGraphics.pose().pushMatrix();
      guiGraphics.pose().translate((float) guiWidth / 2 + dir[0], (float) guiHeight / 2 + dir[1]);
      guiGraphics.pose().pushMatrix();
      guiGraphics.pose().scale(scale, scale);

      int startY = -totalHeight / 2;
      for (int i = 0; i < lines.size(); i++) {
        net.minecraft.util.FormattedCharSequence lineShadow = linesShadow.get(i);
        int lineWidth = font.width(lineShadow);
        int x = -lineWidth / 2;
        guiGraphics.drawString(font, lineShadow, x, startY, shadowColor, false);
        startY += lineHeight + lineSpacing;
      }

      guiGraphics.pose().popMatrix();
      guiGraphics.pose().popMatrix();
    }

    guiGraphics.pose().pushMatrix();
    guiGraphics.pose().translate((float) guiWidth / 2, (float) guiHeight / 2);
    guiGraphics.pose().pushMatrix();
    guiGraphics.pose().scale(scale, scale);

    int startY = -totalHeight / 2;
    for (int i = 0; i < lines.size(); i++) {
      net.minecraft.util.FormattedCharSequence line = lines.get(i);
      int lineWidth = font.width(line);
      int x = -lineWidth / 2;
      guiGraphics.drawString(font, line, x, startY, textColor, false);
      startY += lineHeight + lineSpacing;
    }

    guiGraphics.pose().popMatrix();
    guiGraphics.pose().popMatrix();
  }

  private static Component scaleCaptionColors(Component component) {
    List<Component> flatList = component.toFlatList(Style.EMPTY);
    MutableComponent result = Component.empty();
    for (Component part : flatList) {
      result.append(
          part.copy()
              .withStyle(
                  s -> {
                    Integer color = s.getColor() != null ? s.getColor().getValue() : 0xFFFFFF;
                    int scaledColor = ARGB.scaleRGB(ARGB.opaque(color), 0.25f);
                    return s.withColor(scaledColor);
                  }));
    }
    return result;
  }
}
