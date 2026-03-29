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
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

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
  private static final Identifier NPC_SKIN_ARROWARROW =
      Identifier.parse("not-riding-alert:textures/npc-arrowarrow.png");
  private static final int MODEL_HEIGHT = 90;
  private static final int MODEL_WIDTH = 50;
  private static final int MODEL_OVERFLOW = 10;

  private static final int NAV_BUTTON_WIDTH = 30;
  private static final int CLOSE_BUTTON_WIDTH = 80;
  private static final int SHARE_BUTTON_WIDTH = 50;
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
  private WalkingSkinWidget npcWidget;
  private WalkingPlayerSkinWidget playerWidget;
  private Button prevButton;
  private Button nextButton;
  private int screenshotCountdown = 0;
  private final long animStartNanos = System.nanoTime();

  // Static capture state — set during render(), consumed by GameRendererMixin at TAIL of
  // GameRenderer.render() when the render target has the complete frame (world + GUI).
  private static volatile boolean pendingCapture = false;
  private static int captCropX, captCropY, captCropW, captCropH;

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
    // Clear existing toasts (e.g. "Chat messages can't be verified") so they don't clutter
    // the report. Rendering is also suppressed while this screen is active via ToastManagerMixin.
    Minecraft.getInstance().getToastManager().clear();
    EntityModelSet models = Minecraft.getInstance().getEntityModels();

    panelW = (int) (width * 0.7);
    panelH = (int) (height * 0.8);
    panelX = (width - panelW) / 2;
    panelY = (height - panelH) / 2;

    addRenderableOnly(
        (graphics, mouseX, mouseY, delta) -> renderContent(graphics, mouseX, mouseY, delta));

    // NPC model (left) — uses a dedicated widget so each model gets its own PiP render slot
    Identifier npcSkin = getNpcSkin();
    npcWidget = new WalkingSkinWidget(MODEL_WIDTH, MODEL_HEIGHT, models, npcSkin, animStartNanos);
    npcWidget.setPosition(panelX - MODEL_OVERFLOW, panelY);
    addRenderableWidget(npcWidget);

    // Player model (right)
    playerWidget = new WalkingPlayerSkinWidget(MODEL_WIDTH, MODEL_HEIGHT, models, animStartNanos);
    playerWidget.setPosition(panelX + panelW + MODEL_OVERFLOW - MODEL_WIDTH, panelY);
    addRenderableWidget(playerWidget);

    int buttonY = panelY + panelH - 28;

    // Layout: [< Prev] [Close] [Next >]  [Share]
    int navTotalWidth =
        NAV_BUTTON_WIDTH + BUTTON_GAP + CLOSE_BUTTON_WIDTH + BUTTON_GAP + NAV_BUTTON_WIDTH;
    int totalWidth = navTotalWidth + BUTTON_GAP * 2 + SHARE_BUTTON_WIDTH;
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

    addRenderableWidget(
        Button.builder(Component.literal("Share"), btn -> screenshotCountdown = 2)
            .bounds(startX + navTotalWidth + BUTTON_GAP * 2, buttonY, SHARE_BUTTON_WIDTH, 20)
            .build());

    updateNavButtons();
  }

  private void refreshReport() {
    if (liveMode) {
      report = DailyReportGenerator.generateLive();
    } else {
      report = DailyReportGenerator.generate(date);
    }
    if (npcWidget != null) {
      npcWidget.setTexture(getNpcSkin());
    }
  }

  private Identifier getNpcSkin() {
    if (report == null) return NPC_SKIN_TRUEMONKEYKING;
    return switch (report.grade) {
      case S -> NPC_SKIN_TRUEMONKEYKING;
      case A -> NPC_SKIN_SHIPUP;
      case B -> NPC_SKIN_BLUEGERUDO;
      case C -> NPC_SKIN_ARROWARROW;
      case D -> NPC_SKIN_GRIMREAPER;
    };
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

  private void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
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
      graphics.centeredText(
          font,
          Component.literal(msg).withStyle(ChatFormatting.RED),
          width / 2,
          height / 2,
          TEXT_COLOR);
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
    graphics.centeredText(font, titleComp, width / 2, y, TEXT_COLOR);
    y += LINE_HEIGHT + 2;

    // Fun title
    graphics.centeredText(
        font,
        Component.literal("~ " + report.title + " ~").withColor(report.grade.color),
        width / 2,
        y,
        TEXT_COLOR);
    y += LINE_HEIGHT + 6;

    // Summary
    String rideTimeStr = TimeFormatUtil.formatDuration(report.totalRideTimeSeconds);
    String onlineStr = TimeFormatUtil.formatDuration(report.totalOnlineSeconds);
    String completedVerb = report.isLive ? "You've ridden " : "You completed ";
    String rideSuffix = report.isLive ? " rides so far" : " rides";
    graphics.centeredText(
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

    graphics.centeredText(
        font,
        Component.empty()
            .append(Component.literal("Online for ").withColor(DIM_COLOR))
            .append(Component.literal(onlineStr).withColor(DIM_COLOR)),
        width / 2,
        y,
        TEXT_COLOR);
    y += LINE_HEIGHT;

    // Comparison with previous ride day (may be older than yesterday if there were gap days)
    if (report.previousDayRides != null) {
      boolean wasYesterday = isPreviousCalendarDay(report.date, report.previousRideDate);
      String compLabel = wasYesterday ? "yesterday" : "last ride day";
      int diff = report.totalRides - report.previousDayRides;
      if (diff > 0) {
        graphics.centeredText(
            font,
            Component.literal(diff + " more rides than " + compLabel + "!")
                .withColor(MILESTONE_COLOR),
            width / 2,
            y,
            TEXT_COLOR);
      } else if (diff < 0) {
        graphics.centeredText(
            font,
            Component.literal(Math.abs(diff) + " fewer rides than " + compLabel)
                .withColor(DIM_COLOR),
            width / 2,
            y,
            TEXT_COLOR);
      } else {
        graphics.centeredText(
            font,
            Component.literal("Same as " + compLabel + " - consistency!").withColor(ACCENT_COLOR),
            width / 2,
            y,
            TEXT_COLOR);
      }
      y += LINE_HEIGHT;
    }
    y += 10;

    // First day notice
    if (report.isFirstDay) {
      graphics.centeredText(
          font,
          Component.literal("Day 1 - Baseline Recorded!")
              .withStyle(ChatFormatting.BOLD)
              .withColor(HEADER_COLOR),
          width / 2,
          y,
          TEXT_COLOR);
      y += LINE_HEIGHT + 2;
      graphics.centeredText(
          font,
          Component.literal("This is your first tracked day.").withColor(DIM_COLOR),
          width / 2,
          y,
          TEXT_COLOR);
      y += LINE_HEIGHT;
      graphics.centeredText(
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
      graphics.text(
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
      graphics.text(
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
      graphics.text(
          font,
          Component.literal("New Heights Reached!").withColor(MILESTONE_COLOR),
          contentX,
          y,
          TEXT_COLOR,
          false);
      y += LINE_HEIGHT;
      for (DailyReport.MilestoneReached m : report.newMilestones) {
        graphics.text(
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
    graphics.text(
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

    graphics.text(
        font, Component.literal("Ride").withColor(DIM_COLOR), nameColX, y, TEXT_COLOR, false);
    graphics.text(
        font, Component.literal("+").withColor(DIM_COLOR), countColX, y, TEXT_COLOR, false);
    graphics.text(
        font, Component.literal("Total").withColor(DIM_COLOR), totalColX, y, TEXT_COLOR, false);
    graphics.text(
        font, Component.literal("Time").withColor(DIM_COLOR), timeColX, y, TEXT_COLOR, false);
    y += LINE_HEIGHT;

    // Ride rows
    for (DailyReport.RideDelta rd : report.rideDeltas) {
      int rowColor = TEXT_COLOR;
      String rideName = rd.ride.getDisplayName();
      if (font.width(rideName) > countColX - nameColX - 8) {
        rideName = rd.ride.getShortName();
      }
      graphics.text(
          font, Component.literal(rideName).withColor(rowColor), nameColX, y, TEXT_COLOR, false);
      graphics.text(
          font,
          Component.literal("+" + rd.countIncrease).withColor(MILESTONE_COLOR),
          countColX,
          y,
          TEXT_COLOR,
          false);
      graphics.text(
          font,
          Component.literal(String.valueOf(rd.newTotal)).withColor(DIM_COLOR),
          totalColX,
          y,
          TEXT_COLOR,
          false);
      graphics.text(
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
      graphics.centeredText(
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
      graphics.centeredText(
          font, Component.literal("^").withColor(DIM_COLOR), width / 2, contentTop, TEXT_COLOR);
    }
    if (scrollOffset < maxScroll) {
      graphics.centeredText(
          font,
          Component.literal("v").withColor(DIM_COLOR),
          width / 2,
          contentBottom - LINE_HEIGHT,
          TEXT_COLOR);
    }

    // Deferred screenshot capture for Discord sharing.
    // We can't capture here because GUI draws are still batched in guiRenderState and haven't
    // been committed to the render target yet. Instead, set a static flag that is consumed by
    // GameRendererMixin at the TAIL of GameRenderer.render(), after guiRenderer.render() has
    // committed all GUI draws — the render target then has the complete frame.
    if (screenshotCountdown > 0) {
      screenshotCountdown--;
      if (screenshotCountdown == 0) {
        int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        captCropX = Math.max(0, (panelX - MODEL_OVERFLOW) * guiScale);
        captCropY = Math.max(0, panelY * guiScale);
        captCropW = (panelW + MODEL_OVERFLOW * 2) * guiScale;
        captCropH = panelH * guiScale;
        pendingCapture = true;
      }
    }
  }

  /**
   * Called by {@link com.chenweikeng.nra.mixin.GameRendererMixin} at the TAIL of {@code
   * GameRenderer.render()}, after {@code guiRenderer.render()} has committed all GUI draws to the
   * render target. At this point the framebuffer has the complete frame (world + screen + models).
   */
  public static void executePendingCapture() {
    if (!pendingCapture) return;
    pendingCapture = false;

    Minecraft client = Minecraft.getInstance();
    // Don't capture if the game is shutting down — async GPU callback would crash on destroyed
    // native resources
    if (client.getWindow() == null || !(client.screen instanceof RideReportScreen)) {
      return;
    }
    int cropX = captCropX;
    int cropY = captCropY;
    int cropW = captCropW;
    int cropH = captCropH;

    Screenshot.takeScreenshot(
        client.getMainRenderTarget(),
        image -> {
          try {
            java.awt.image.BufferedImage cropped =
                DiscordShareUtil.nativeImageToBufferedImage(image, cropX, cropY, cropW, cropH);
            boolean copied = DiscordShareUtil.copyImageToClipboard(cropped);
            if (copied) {
              DiscordShareUtil.openDiscordChannel();
            }
            client.execute(
                () -> {
                  if (client.player != null) {
                    client.player.sendSystemMessage(
                        Component.literal(
                                copied
                                    ? "Report copied! Paste in Discord to share."
                                    : "Failed to copy to clipboard.")
                            .withColor(copied ? ACCENT_COLOR : 0xFFFF5555));
                  }
                });
          } catch (Exception e) {
            // Silently ignore errors during shutdown (native resources may be gone)
            if (client.getWindow() != null) {
              com.chenweikeng.nra.NotRidingAlertClient.LOGGER.warn(
                  "Failed to share report to Discord", e);
            }
          } finally {
            try {
              image.close();
            } catch (Exception ignored) {
            }
          }
        });
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
    pendingCapture = false;
    screenshotCountdown = 0;
    if (minecraft != null) {
      minecraft.setScreen(parent);
    }
  }

  /** Returns true if {@code prevDate} is exactly one calendar day before {@code reportDate}. */
  private boolean isPreviousCalendarDay(String reportDate, String prevDate) {
    if (reportDate == null || prevDate == null) return false;
    try {
      LocalDate report = LocalDate.parse(reportDate, DateTimeFormatter.ISO_LOCAL_DATE);
      LocalDate prev = LocalDate.parse(prevDate, DateTimeFormatter.ISO_LOCAL_DATE);
      return prev.equals(report.minusDays(1));
    } catch (Exception e) {
      return false;
    }
  }
}
