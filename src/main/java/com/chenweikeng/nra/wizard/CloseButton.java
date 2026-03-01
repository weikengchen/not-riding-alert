package com.chenweikeng.nra.wizard;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class CloseButton extends AbstractButton {
  private static final int BG_COLOR = 0x33FFFFFF;
  private static final int BG_HOVER_COLOR = 0x66FFFFFF;
  private static final int X_COLOR = 0xFF666666;
  private static final int X_HOVER_COLOR = 0xFFCC0000;

  private final OnPress onPress;

  public CloseButton(int x, int y, int size, OnPress onPress) {
    super(x, y, size, size, Component.empty());
    this.onPress = onPress;
  }

  @Override
  protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    int centerX = getX() + getWidth() / 2;
    int centerY = getY() + getHeight() / 2;
    int size = getWidth();
    int halfSize = size / 2;

    int bgColor = isHoveredOrFocused() ? BG_HOVER_COLOR : BG_COLOR;
    int color = isHoveredOrFocused() ? X_HOVER_COLOR : X_COLOR;

    graphics.fill(
        centerX - halfSize, centerY - halfSize, centerX + halfSize, centerY + halfSize, bgColor);

    int thickness = Math.max(2, size / 15);
    int armLength = halfSize - 3;

    for (int t = -thickness / 2; t <= thickness / 2; t++) {
      graphics.fill(
          centerX - armLength, centerY + t, centerX + armLength + 1, centerY + t + 1, color);
      graphics.fill(
          centerX + t, centerY - armLength, centerX + t + 1, centerY + armLength + 1, color);
    }
  }

  @Override
  public void onPress(InputWithModifiers input) {
    this.onPress.onPress(this);
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput narration) {
    this.defaultButtonNarrationText(narration);
  }

  public interface OnPress {
    void onPress(CloseButton button);
  }
}
