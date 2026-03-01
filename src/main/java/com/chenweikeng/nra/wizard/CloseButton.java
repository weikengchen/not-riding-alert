package com.chenweikeng.nra.wizard;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class CloseButton extends AbstractButton {
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

    int color = isHoveredOrFocused() ? X_HOVER_COLOR : X_COLOR;

    int thickness = Math.max(2, size / 15);
    int armLength = halfSize - 4;
    int halfThick = thickness / 2;

    for (int i = -armLength; i <= armLength; i++) {
      int x1 = centerX + i;
      int y1 = centerY + i;
      graphics.fill(x1 - halfThick, y1, x1 + halfThick + 1, y1 + 1, color);

      int x2 = centerX + i;
      int y2 = centerY - i;
      graphics.fill(x2 - halfThick, y2, x2 + halfThick + 1, y2 + 1, color);
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
