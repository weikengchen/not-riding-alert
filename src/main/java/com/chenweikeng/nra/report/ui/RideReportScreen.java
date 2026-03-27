package com.chenweikeng.nra.report.ui;

import com.chenweikeng.nra.report.DailyReport;
import com.chenweikeng.nra.report.DailyReportGenerator;
import com.chenweikeng.nra.report.DailyRideSnapshot;
import com.chenweikeng.nra.report.RideReportNotifier;
import com.chenweikeng.nra.session.SessionTracker;
import com.chenweikeng.nra.util.TimeFormatUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

public class RideReportScreen extends Screen {
  private static final int PANEL_PADDING = 16;
  private static final int LINE_HEIGHT = 12;
  private static final int HEADER_COLOR = 0xFFFFD700;
  private static final int TEXT_COLOR = 0xFFFFFFFF;
  private static final int DIM_COLOR = 0xFFAAAAAA;
  private static final int ACCENT_COLOR = 0xFF55FFFF;
  private static final int MVP_COLOR = 0xFFFFAA00;
  private static final int MILESTONE_COLOR = 0xFF55FF55;
  private static final int SEPARATOR_COLOR = 0x44FFFFFF;
  private static final int LIVE_COLOR = 0xFFFF5555;
  private static final long LIVE_REFRESH_TICKS = 100; // Refresh live data every 5 seconds
  private static final Identifier NPC_SKIN_TRUEMONKEYKING =
      Identifier.parse("not-riding-alert:textures/npc-truemonkeyking.png");
  private static final Identifier NPC_SKIN_SHIPUP =
      Identifier.parse("not-riding-alert:textures/npc-shipup.png");
  private static final Identifier NPC_SKIN_BLUEGERUDO =
      Identifier.parse("not-riding-alert:textures/npc-bluegerudo.png");
  private static final Identifier NPC_SKIN_GRIMREAPER =
      Identifier.parse("not-riding-alert:textures/npc-grimreaper.png");
  private static final int MODEL_HEIGHT = 50;
  private static final int MODEL_WIDTH = 30;
  private static final float MODEL_PIVOT_Y = -1.0625F;

  private static final int NAV_BUTTON_WIDTH = 30;
  private static final int CLOSE_BUTTON_WIDTH = 80;
  private static final int BUTTON_GAP = 6;

  private final Screen parent;
  private String date; // null means live/today
  private boolean liveMode;
  private DailyReport report;
  private int panelX;
  private int panelY;
  private int panelW;
  private int panelH;
  private int scrollOffset = 0;
  private int maxScroll = 0;
  private long ticksSinceRefresh = 0;
  private PlayerModel wideModel;
  private PlayerModel slimModel;
  private Button prevButton;
  private Button nextButton;

  /** Create a report screen for a specific historical date. */
  public RideReportScreen(Screen parent, String date) {
    super(Component.literal("Ride Report"));
    this.parent = parent;
    this.date = date;
    this.liveMode = false;
  }

  /** Create a live report screen for today. */
  public static RideReportScreen createLive(Screen parent) {
    return new RideReportScreen(parent, null, true);
  }

  private RideReportScreen(Screen parent, String date, boolean liveMode) {
    super(Component.literal("Ride Report"));
    this.parent = parent;
    this.date = date;
    this.liveMode = liveMode;
  }

  @Override
  protected void init() {
    super.init();
    refreshReport();
    RideReportNotifier.getInstance().markViewed();
    EntityModelSet models = Minecraft.getInstance().getEntityModels();
    wideModel = new PlayerModel(models.bakeLayer(ModelLayers.PLAYER), false);
    slimModel = new PlayerModel(models.bakeLayer(ModelLayers.PLAYER_SLIM), true);

    panelW = (int) (width * 0.7);
    panelH = (int) (height * 0.8);
    panelX = (width - panelW) / 2;
    panelY = (height - panelH) / 2;

    int buttonY = panelY + panelH - 28;

    // Layout: [< Prev] [Close] [Next >]
    int totalWidth =
        NAV_BUTTON_WIDTH + BUTTON_GAP + CLOSE_BUTTON_WIDTH + BUTTON_GAP + NAV_BUTTON_WIDTH;
    int startX = panelX + (panelW - totalWidth) / 2;

    prevButton =
        Button.builder(Component.literal("<"), btn -> navigatePrev())
            .bounds(startX, buttonY, NAV_BUTTON_WIDTH, 20)
            .build();
    addRenderableWidget(prevButton);

    addRenderableWidget(
        Button.builder(Component.literal("Close"), btn -> onClose())
            .bounds(startX + NAV_BUTTON_WIDTH + BUTTON_GAP, buttonY, CLOSE_BUTTON_WIDTH, 20)
            .build());

    nextButton =
        Button.builder(Component.literal(">"), btn -> navigateNext())
            .bounds(
                startX + NAV_BUTTON_WIDTH + BUTTON_GAP + CLOSE_BUTTON_WIDTH + BUTTON_GAP,
                buttonY,
                NAV_BUTTON_WIDTH,
                20)
            .build();
    addRenderableWidget(nextButton);

    updateNavButtons();
  }

  private void refreshReport() {
    if (liveMode) {
      report = DailyReportGenerator.generateLive();
    } else {
      report = DailyReportGenerator.generate(date);
    }
  }

  private void navigatePrev() {
    DailyRideSnapshot snapshots = DailyRideSnapshot.getInstance();
    if (liveMode) {
      // From live → most recent historical snapshot
      String mostRecent = snapshots.getMostRecentDate();
      if (mostRecent != null) {
        liveMode = false;
        date = mostRecent;
        scrollOffset = 0;
        refreshReport();
        updateNavButtons();
      }
    } else if (date != null) {
      String prev = snapshots.getPreviousDate(date);
      if (prev != null) {
        date = prev;
        scrollOffset = 0;
        refreshReport();
        updateNavButtons();
      }
    }
  }

  private void navigateNext() {
    DailyRideSnapshot snapshots = DailyRideSnapshot.getInstance();
    if (liveMode) {
      // Already at newest — nothing to do
      return;
    }
    String next = snapshots.getNextDate(date);
    if (next != null) {
      date = next;
      scrollOffset = 0;
      refreshReport();
      updateNavButtons();
    } else if (SessionTracker.getInstance().isActive()) {
      // No newer snapshot → go to live
      liveMode = true;
      date = null;
      scrollOffset = 0;
      refreshReport();
      updateNavButtons();
    }
  }

  private void updateNavButtons() {
    DailyRideSnapshot snapshots = DailyRideSnapshot.getInstance();
    if (liveMode) {
      prevButton.active = snapshots.getMostRecentDate() != null;
      nextButton.active = false;
    } else {
      prevButton.active = date != null && snapshots.getPreviousDate(date) != null;
      nextButton.active = true; // Can always go forward (to newer snapshot or live)
    }
  }

  @Override
  public void tick() {
    super.tick();
    if (liveMode) {
      ticksSinceRefresh++;
      if (ticksSinceRefresh >= LIVE_REFRESH_TICKS) {
        ticksSinceRefresh = 0;
        refreshReport();
      }
    }
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    // Dark translucent full-screen backdrop
    graphics.fill(0, 0, width, height, 0xAA000000);

    // Panel background with border
    graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xEE1A1A2E);
    graphics.fill(panelX, panelY, panelX + panelW, panelY + 1, 0xFF6644AA);
    graphics.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, 0xFF6644AA);
    graphics.fill(panelX, panelY, panelX + 1, panelY + panelH, 0xFF6644AA);
    graphics.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, 0xFF6644AA);

    if (report == null) {
      String msg =
          liveMode ? "No rides yet today - go ride something!" : "No report data for " + date;
      graphics.drawCenteredString(
          font,
          Component.literal(msg).withStyle(ChatFormatting.RED),
          width / 2,
          height / 2,
          TEXT_COLOR);
      super.render(graphics, mouseX, mouseY, delta);
      return;
    }

    // Enable scissor to clip scrollable content
    int contentX = panelX + PANEL_PADDING;
    int contentTop = panelY + PANEL_PADDING;
    int contentWidth = panelW - PANEL_PADDING * 2;
    int contentBottom = panelY + panelH - 36;

    graphics.enableScissor(panelX + 1, contentTop, panelX + panelW - 1, contentBottom);

    int y = contentTop - scrollOffset;

    // Header: Date + Grade + optional LIVE badge
    String dateDisplay = formatDateFriendly(report.date);
    Component gradeComp =
        Component.literal("[" + report.grade.label + "] ").withColor(report.grade.color);
    Component titleComp = Component.empty().append(gradeComp);
    if (report.isLive) {
      titleComp = titleComp.copy().append(Component.literal("[LIVE] ").withColor(LIVE_COLOR));
    }
    titleComp =
        titleComp
            .copy()
            .append(
                Component.literal(dateDisplay)
                    .withStyle(ChatFormatting.BOLD)
                    .withColor(HEADER_COLOR));
    graphics.drawCenteredString(font, titleComp, width / 2, y, TEXT_COLOR);
    y += LINE_HEIGHT + 2;

    // Fun title
    graphics.drawCenteredString(
        font,
        Component.literal("~ " + report.title + " ~").withColor(report.grade.color),
        width / 2,
        y,
        TEXT_COLOR);
    y += LINE_HEIGHT + 6;

    // Player models: NPC on left, player on right
    Identifier npcSkin =
        switch (report.grade) {
          case S -> NPC_SKIN_TRUEMONKEYKING;
          case A -> NPC_SKIN_SHIPUP;
          case B -> NPC_SKIN_BLUEGERUDO;
          case C -> NPC_SKIN_TRUEMONKEYKING;
          case D -> NPC_SKIN_GRIMREAPER;
        };
    int modelTop = contentTop - scrollOffset + 2;
    float modelScale = 0.97F * MODEL_HEIGHT / 2.125F;

    // NPC model (left)
    int npcX0 = contentX;
    int npcY0 = modelTop;
    int npcX1 = npcX0 + MODEL_WIDTH;
    int npcY1 = npcY0 + MODEL_HEIGHT;
    float npcCenterX = (npcX0 + npcX1) / 2.0F;
    float npcCenterY = (npcY0 + npcY1) / 2.0F;
    float npcRotY = (float) Math.toDegrees(Math.atan((double) (npcCenterX - mouseX) / 40.0));
    float npcRotX = -(float) Math.toDegrees(Math.atan((double) (npcCenterY - mouseY) / 40.0));
    npcRotX = Mth.clamp(npcRotX, -50.0F, 50.0F);
    graphics.submitSkinRenderState(
        wideModel,
        npcSkin,
        modelScale,
        npcRotX,
        npcRotY,
        MODEL_PIVOT_Y,
        npcX0,
        npcY0,
        npcX1,
        npcY1);

    // Player model (right)
    Minecraft client = Minecraft.getInstance();
    if (client.player != null) {
      PlayerSkin playerSkin = client.player.getSkin();
      PlayerModel playerModel = playerSkin.model() == PlayerModelType.SLIM ? slimModel : wideModel;
      int playerX1 = contentX + contentWidth;
      int playerX0 = playerX1 - MODEL_WIDTH;
      int playerY0 = modelTop;
      int playerY1 = playerY0 + MODEL_HEIGHT;
      float playerCenterX = (playerX0 + playerX1) / 2.0F;
      float playerCenterY = (playerY0 + playerY1) / 2.0F;
      float playerRotY =
          (float) Math.toDegrees(Math.atan((double) (playerCenterX - mouseX) / 40.0));
      float playerRotX =
          -(float) Math.toDegrees(Math.atan((double) (playerCenterY - mouseY) / 40.0));
      playerRotX = Mth.clamp(playerRotX, -50.0F, 50.0F);
      graphics.submitSkinRenderState(
          playerModel,
          playerSkin.body().texturePath(),
          modelScale,
          playerRotX,
          playerRotY,
          MODEL_PIVOT_Y,
          playerX0,
          playerY0,
          playerX1,
          playerY1);
    }

    // Ensure text below clears the model area
    y = Math.max(y, modelTop + MODEL_HEIGHT + 4);

    // Summary
    String rideTimeStr = TimeFormatUtil.formatDuration(report.totalRideTimeSeconds);
    String onlineStr = TimeFormatUtil.formatDuration(report.totalOnlineSeconds);
    String completedVerb = report.isLive ? "You've ridden " : "You completed ";
    String rideSuffix = report.isLive ? " rides so far" : " rides";
    graphics.drawCenteredString(
        font,
        Component.empty()
            .append(Component.literal(completedVerb).withColor(TEXT_COLOR))
            .append(
                Component.literal(report.totalRides + rideSuffix)
                    .withStyle(ChatFormatting.BOLD)
                    .withColor(ACCENT_COLOR))
            .append(Component.literal(" in ").withColor(TEXT_COLOR))
            .append(Component.literal(rideTimeStr).withColor(ACCENT_COLOR))
            .append(Component.literal(" of ride time!").withColor(TEXT_COLOR)),
        width / 2,
        y,
        TEXT_COLOR);
    y += LINE_HEIGHT;

    graphics.drawCenteredString(
        font,
        Component.empty()
            .append(Component.literal("Online for ").withColor(DIM_COLOR))
            .append(Component.literal(onlineStr).withColor(DIM_COLOR)),
        width / 2,
        y,
        TEXT_COLOR);
    y += LINE_HEIGHT;

    // Comparison with previous day
    if (report.previousDayRides != null) {
      int diff = report.totalRides - report.previousDayRides;
      if (diff > 0) {
        graphics.drawCenteredString(
            font,
            Component.literal(diff + " more rides than yesterday!").withColor(MILESTONE_COLOR),
            width / 2,
            y,
            TEXT_COLOR);
      } else if (diff < 0) {
        graphics.drawCenteredString(
            font,
            Component.literal(Math.abs(diff) + " fewer rides than yesterday").withColor(DIM_COLOR),
            width / 2,
            y,
            TEXT_COLOR);
      } else {
        graphics.drawCenteredString(
            font,
            Component.literal("Same as yesterday - consistency!").withColor(ACCENT_COLOR),
            width / 2,
            y,
            TEXT_COLOR);
      }
      y += LINE_HEIGHT;
    }
    y += 10;

    // First day notice
    if (report.isFirstDay) {
      graphics.drawCenteredString(
          font,
          Component.literal("Day 1 - Baseline Recorded!")
              .withStyle(ChatFormatting.BOLD)
              .withColor(HEADER_COLOR),
          width / 2,
          y,
          TEXT_COLOR);
      y += LINE_HEIGHT + 2;
      graphics.drawCenteredString(
          font,
          Component.literal("This is your first tracked day.").withColor(DIM_COLOR),
          width / 2,
          y,
          TEXT_COLOR);
      y += LINE_HEIGHT;
      graphics.drawCenteredString(
          font,
          Component.literal("Per-ride breakdowns start from tomorrow!").withColor(DIM_COLOR),
          width / 2,
          y,
          TEXT_COLOR);
      y += LINE_HEIGHT + 4;
      graphics.fill(contentX, y, contentX + contentWidth, y + 1, SEPARATOR_COLOR);
      y += 6;
    }

    // Fun stats section
    if (report.mvpRide != null) {
      graphics.drawString(
          font,
          Component.empty()
              .append(Component.literal("MVP Ride: ").withColor(MVP_COLOR))
              .append(
                  Component.literal(report.mvpRide + " (" + report.mvpRideCount + "x)")
                      .withColor(TEXT_COLOR)),
          contentX,
          y,
          TEXT_COLOR,
          false);
      y += LINE_HEIGHT;
    }

    if (report.speedDemonRide != null) {
      graphics.drawString(
          font,
          Component.empty()
              .append(Component.literal("Speed Demon: ").withColor(0xFFFF5555))
              .append(Component.literal(report.speedDemonRide).withColor(TEXT_COLOR)),
          contentX,
          y,
          TEXT_COLOR,
          false);
      y += LINE_HEIGHT;
    }

    if (!report.newMilestones.isEmpty()) {
      graphics.drawString(
          font,
          Component.literal("New Heights Reached!").withColor(MILESTONE_COLOR),
          contentX,
          y,
          TEXT_COLOR,
          false);
      y += LINE_HEIGHT;
      for (DailyReport.MilestoneReached m : report.newMilestones) {
        graphics.drawString(
            font,
            Component.empty()
                .append(Component.literal("  ").withColor(TEXT_COLOR))
                .append(
                    Component.literal(m.ride.getDisplayName())
                        .withStyle(ChatFormatting.BOLD)
                        .withColor(MILESTONE_COLOR))
                .append(Component.literal(" reached " + m.milestone + "!").withColor(TEXT_COLOR)),
            contentX,
            y,
            TEXT_COLOR,
            false);
        y += LINE_HEIGHT;
      }
    }

    if (report.mvpRide != null
        || report.speedDemonRide != null
        || !report.newMilestones.isEmpty()) {
      y += 4;
      graphics.fill(contentX, y, contentX + contentWidth, y + 1, SEPARATOR_COLOR);
      y += 6;
    }

    // Ride breakdown header
    graphics.drawString(
        font,
        Component.literal("Ride Breakdown").withStyle(ChatFormatting.BOLD).withColor(ACCENT_COLOR),
        contentX,
        y,
        TEXT_COLOR,
        false);
    y += LINE_HEIGHT + 2;

    // Column headers
    int nameColX = contentX;
    int countColX = contentX + contentWidth - 140;
    int totalColX = contentX + contentWidth - 85;
    int timeColX = contentX + contentWidth - 40;

    graphics.drawString(
        font, Component.literal("Ride").withColor(DIM_COLOR), nameColX, y, TEXT_COLOR, false);
    graphics.drawString(
        font, Component.literal("+").withColor(DIM_COLOR), countColX, y, TEXT_COLOR, false);
    graphics.drawString(
        font, Component.literal("Total").withColor(DIM_COLOR), totalColX, y, TEXT_COLOR, false);
    graphics.drawString(
        font, Component.literal("Time").withColor(DIM_COLOR), timeColX, y, TEXT_COLOR, false);
    y += LINE_HEIGHT;

    // Ride rows
    for (DailyReport.RideDelta rd : report.rideDeltas) {
      int rowColor = TEXT_COLOR;
      String rideName = rd.ride.getDisplayName();
      if (font.width(rideName) > countColX - nameColX - 8) {
        rideName = rd.ride.getShortName();
      }
      graphics.drawString(
          font, Component.literal(rideName).withColor(rowColor), nameColX, y, TEXT_COLOR, false);
      graphics.drawString(
          font,
          Component.literal("+" + rd.countIncrease).withColor(MILESTONE_COLOR),
          countColX,
          y,
          TEXT_COLOR,
          false);
      graphics.drawString(
          font,
          Component.literal(String.valueOf(rd.newTotal)).withColor(DIM_COLOR),
          totalColX,
          y,
          TEXT_COLOR,
          false);
      graphics.drawString(
          font,
          Component.literal(TimeFormatUtil.formatDuration(rd.timeContributedSeconds))
              .withColor(DIM_COLOR),
          timeColX,
          y,
          TEXT_COLOR,
          false);
      y += LINE_HEIGHT;
    }

    if (report.rideDeltas.isEmpty()) {
      graphics.drawCenteredString(
          font,
          Component.literal("No ride data recorded").withColor(DIM_COLOR),
          width / 2,
          y,
          TEXT_COLOR);
      y += LINE_HEIGHT;
    }

    y += PANEL_PADDING;

    graphics.disableScissor();

    int totalContentHeight = y + scrollOffset - contentTop;
    int visibleHeight = contentBottom - contentTop;
    maxScroll = Math.max(0, totalContentHeight - visibleHeight);

    // Scroll indicators
    if (scrollOffset > 0) {
      graphics.drawCenteredString(
          font, Component.literal("^").withColor(DIM_COLOR), width / 2, contentTop, TEXT_COLOR);
    }
    if (scrollOffset < maxScroll) {
      graphics.drawCenteredString(
          font,
          Component.literal("v").withColor(DIM_COLOR),
          width / 2,
          contentBottom - LINE_HEIGHT,
          TEXT_COLOR);
    }

    super.render(graphics, mouseX, mouseY, delta);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
    scrollOffset -= (int) (deltaY * 10);
    scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    return true;
  }

  private String formatDateFriendly(String dateStr) {
    try {
      LocalDate d = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
      String dayOfWeek = d.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
      String month = d.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
      return dayOfWeek + ", " + month + " " + d.getDayOfMonth() + ", " + d.getYear();
    } catch (Exception e) {
      return dateStr;
    }
  }

  @Override
  public boolean shouldCloseOnEsc() {
    return true;
  }

  @Override
  public void onClose() {
    if (minecraft != null) {
      minecraft.setScreen(parent);
    }
  }
}
