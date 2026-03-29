package com.chenweikeng.nra.report.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

/**
 * A widget that renders the local player's skin as a walking model. Uses the player's actual skin
 * texture and model type (wide/slim).
 */
public class WalkingPlayerSkinWidget extends AbstractWidget {

  private static final float MODEL_HEIGHT_BLOCKS = 2.125F;
  private static final float FIT_SCALE = 0.97F;
  private static final float PIVOT_Y = -1.0625F;

  private final PlayerModel wideModel;
  private final PlayerModel slimModel;
  private final long animStartNanos;

  public WalkingPlayerSkinWidget(
      int width, int height, EntityModelSet models, long animStartNanos) {
    super(0, 0, width, height, CommonComponents.EMPTY);
    this.wideModel = new PlayerModel(models.bakeLayer(ModelLayers.PLAYER), false);
    this.slimModel = new PlayerModel(models.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    this.animStartNanos = animStartNanos;
  }

  @Override
  protected void extractWidgetRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    Minecraft client = Minecraft.getInstance();
    if (client.player == null) return;

    PlayerSkin skin = client.player.getSkin();
    PlayerModel model = skin.model() == PlayerModelType.SLIM ? slimModel : wideModel;
    float scale = FIT_SCALE * this.getHeight() / MODEL_HEIGHT_BLOCKS;
    float walkTime = (float) ((System.nanoTime() - animStartNanos) / 1_000_000_000.0 * 20.0);
    applyWalkAnimation(model, walkTime);

    float centerX = (this.getX() + this.getRight()) / 2.0F;
    float centerY = (this.getY() + this.getBottom()) / 2.0F;
    float rotY = (float) Math.atan((double) (mouseX - centerX) / 40.0) * 20.0F;
    float rotX = (float) Math.atan((double) (centerY - mouseY) / 40.0) * 20.0F;

    graphics.skin(
        model,
        skin.body().texturePath(),
        scale,
        rotX,
        rotY,
        PIVOT_Y,
        this.getX(),
        this.getY(),
        this.getRight(),
        this.getBottom());
  }

  private void applyWalkAnimation(PlayerModel model, float walkTime) {
    model.head.resetPose();
    model.hat.resetPose();
    model.body.resetPose();
    model.jacket.resetPose();
    model.rightArm.resetPose();
    model.leftArm.resetPose();
    model.rightLeg.resetPose();
    model.leftLeg.resetPose();
    model.rightSleeve.resetPose();
    model.leftSleeve.resetPose();
    model.rightPants.resetPose();
    model.leftPants.resetPose();

    float speed = 0.6F;
    float cycle = walkTime * 0.3F;
    model.rightArm.xRot = Mth.cos(cycle * 0.6662F + (float) Math.PI) * 2.0F * speed * 0.5F;
    model.leftArm.xRot = Mth.cos(cycle * 0.6662F) * 2.0F * speed * 0.5F;
    model.rightLeg.xRot = Mth.cos(cycle * 0.6662F) * 1.4F * speed;
    model.leftLeg.xRot = Mth.cos(cycle * 0.6662F + (float) Math.PI) * 1.4F * speed;
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output) {}
}
