package com.chenweikeng.nra.report.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Renders a full-body 2D "paper doll" view of a player skin texture. Bypasses the 3D entity/PiP
 * rendering pipeline (which may not work reliably with certain resource packs or server
 * configurations) and instead blits body-part UV regions directly from the 64×64 skin texture.
 *
 * <p>Supports both wide (Steve) and slim (Alex) arm models.
 */
public final class SkinRenderer2D {
  private static final int SHEET = 64;

  private SkinRenderer2D() {}

  /**
   * Renders the full front-facing body from a skin texture, centered in the given widget bounds.
   *
   * @param graphics the GUI graphics extractor
   * @param texture the skin texture identifier (64×64 skin format)
   * @param x widget left edge
   * @param y widget top edge
   * @param width widget width
   * @param height widget height
   * @param slim true for 3-pixel-wide arms (slim/Alex model), false for 4-pixel (wide/Steve model)
   */
  public static void renderFullBody(
      GuiGraphicsExtractor graphics,
      Identifier texture,
      int x,
      int y,
      int width,
      int height,
      boolean slim) {

    // Arm width differs: 3 for slim, 4 for wide
    int armTex = slim ? 3 : 4;
    int totalTexW = armTex + 8 + armTex; // 14 (slim) or 16 (wide)
    int totalTexH = 32; // 8 head + 12 body + 12 legs

    float scale = Math.min((float) width / totalTexW, (float) height / totalTexH);
    int scaledW = Math.round(totalTexW * scale);
    int scaledH = Math.round(totalTexH * scale);
    int baseX = x + (width - scaledW) / 2;
    int baseY = y + (height - scaledH) / 2;

    // Scaled part dimensions
    int headS = Math.round(8 * scale);
    int bodyW = Math.round(8 * scale);
    int bodyH = Math.round(12 * scale);
    int armW = Math.round(armTex * scale);
    int limbH = Math.round(12 * scale);
    int legW = Math.round(4 * scale);

    // Part positions
    int headX = baseX + (scaledW - headS) / 2;
    int torsoX = baseX + armW;
    int torsoY = baseY + headS;
    int legY = torsoY + bodyH;

    // ── Base layer ──
    //                                              destX            destY   srcU srcV  dW     dH
    //  sW      sH
    part(graphics, texture, headX, baseY, 8, 8, headS, headS, 8, 8); // Head
    part(graphics, texture, torsoX, torsoY, 20, 20, bodyW, bodyH, 8, 12); // Body
    part(graphics, texture, baseX, torsoY, 44, 20, armW, limbH, armTex, 12); // Right arm
    part(graphics, texture, torsoX + bodyW, torsoY, 36, 52, armW, limbH, armTex, 12); // Left arm
    part(graphics, texture, torsoX, legY, 4, 20, legW, limbH, 4, 12); // Right leg
    part(graphics, texture, torsoX + legW, legY, 20, 52, legW, limbH, 4, 12); // Left leg

    // ── Overlay layer (hat, jacket, sleeves, pants) ──
    part(graphics, texture, headX, baseY, 40, 8, headS, headS, 8, 8); // Hat
    part(graphics, texture, torsoX, torsoY, 20, 36, bodyW, bodyH, 8, 12); // Jacket
    part(graphics, texture, baseX, torsoY, 44, 36, armW, limbH, armTex, 12); // Right sleeve
    part(graphics, texture, torsoX + bodyW, torsoY, 52, 52, armW, limbH, armTex, 12); // Left sleeve
    part(graphics, texture, torsoX, legY, 4, 36, legW, limbH, 4, 12); // Right pants
    part(graphics, texture, torsoX + legW, legY, 4, 52, legW, limbH, 4, 12); // Left pants
  }

  /** Blits a single body part from the skin texture. */
  private static void part(
      GuiGraphicsExtractor g,
      Identifier tex,
      int dx,
      int dy,
      float u,
      float v,
      int dw,
      int dh,
      int sw,
      int sh) {
    g.blit(RenderPipelines.GUI_TEXTURED, tex, dx, dy, u, v, dw, dh, sw, sh, SHEET, SHEET, -1);
  }
}
