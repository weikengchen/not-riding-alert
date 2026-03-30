package com.chenweikeng.nra.report.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;

/**
 * Renders an NPC skin as a 2D "paper doll" front view, blitting body-part UV regions directly from
 * the 64×64 skin texture. This avoids the 3D entity/PiP rendering pipeline which may not work
 * reliably with all resource packs.
 */
public class WalkingSkinWidget extends AbstractWidget {

  private Identifier texture;

  public WalkingSkinWidget(int width, int height, Identifier texture, long animStartNanos) {
    super(0, 0, width, height, CommonComponents.EMPTY);
    this.texture = texture;
  }

  public void setTexture(Identifier texture) {
    this.texture = texture;
  }

  @Override
  protected void extractWidgetRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    SkinRenderer2D.renderFullBody(
        graphics, texture, this.getX(), this.getY(), this.getWidth(), this.getHeight(), false);
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output) {}
}
