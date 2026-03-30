package com.chenweikeng.nra.report.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

/**
 * Renders the local player's skin as a 2D "paper doll" front view, bypassing the 3D entity/PiP
 * rendering pipeline which may not work reliably with all resource packs.
 */
public class WalkingPlayerSkinWidget extends AbstractWidget {

  public WalkingPlayerSkinWidget(int width, int height) {
    super(0, 0, width, height, CommonComponents.EMPTY);
  }

  @Override
  protected void extractWidgetRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    Minecraft client = Minecraft.getInstance();
    if (client.player == null) return;

    PlayerSkin skin = client.player.getSkin();
    Identifier texture = skin.body().texturePath();
    boolean slim = skin.model() == PlayerModelType.SLIM;
    SkinRenderer2D.renderFullBody(
        graphics, texture, this.getX(), this.getY(), this.getWidth(), this.getHeight(), slim);
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output) {}
}
