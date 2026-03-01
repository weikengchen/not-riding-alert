package com.chenweikeng.nra.wizard;

import java.util.List;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

public class WizardScreen extends Screen {
  private static final Identifier BOOK_TEXTURE =
      Identifier.tryParse("not-riding-alert:textures/gui/tutorial/book_background.png");

  private static final int BOOK_WIDTH = 512;
  private static final int BOOK_HEIGHT = 320;

  private static final int CONTENT_X = 80;
  private static final int CONTENT_Y = 45;
  private static final int CONTENT_WIDTH = 350;
  private static final int CONTENT_HEIGHT = 225;
  private static final int TEXT_MARGIN = 10;

  private static final int CLOSE_X = BOOK_WIDTH - 70;
  private static final int CLOSE_Y = 20;
  private static final int CLOSE_WIDTH = 15;
  private static final int CLOSE_HEIGHT = 15;

  private static final int ARROW_WIDTH = 24;
  private static final int ARROW_HEIGHT = 14;
  private static final int BACK_X = CONTENT_X + TEXT_MARGIN;
  private static final int BACK_Y = CONTENT_Y + CONTENT_HEIGHT - 20;
  private static final int NEXT_X = CONTENT_X + CONTENT_WIDTH - TEXT_MARGIN - ARROW_WIDTH;
  private static final int NEXT_Y = CONTENT_Y + CONTENT_HEIGHT - 20;

  private static final int TITLE_Y_OFFSET = 10;
  private static final int TEXT_START_Y_OFFSET = 35;
  private static final int LINE_HEIGHT = 12;

  private static final int TITLE_COLOR = 0xFF333333;
  private static final int TEXT_COLOR = 0xFF222222;
  private static final String ACTION_PREFIX = "wizard_action:";

  private int currentPageIndex;
  private WizardPage currentPage;

  private PageButton backButton;
  private PageButton nextButton;
  private CloseButton closeButton;

  private float scale;
  private int bookLeft;
  private int bookTop;

  private int textStartY;

  public WizardScreen() {
    this(0);
  }

  public WizardScreen(int pageIndex) {
    super(Component.literal("Tutorial"));
    this.currentPageIndex = Math.max(0, pageIndex);
    this.currentPage = TutorialPages.getPage(this.currentPageIndex);
  }

  @Override
  protected void init() {
    super.init();

    calculateScale();

    if (currentPage != null) {
      currentPage.onPageOpen(minecraft);
    }

    int scaledCloseX = bookLeft + (int) (CLOSE_X * scale);
    int scaledCloseY = bookTop + (int) (CLOSE_Y * scale);
    int scaledCloseW = (int) (CLOSE_WIDTH * scale);
    int scaledCloseH = (int) (CLOSE_HEIGHT * scale);

    closeButton = new CloseButton(scaledCloseX, scaledCloseY, scaledCloseW, this::onCloseClicked);
    addRenderableWidget(closeButton);

    int scaledBackX = bookLeft + (int) (BACK_X * scale);
    int scaledBackY = bookTop + (int) (BACK_Y * scale);
    int scaledArrowW = (int) (ARROW_WIDTH * scale);
    int scaledArrowH = (int) (ARROW_HEIGHT * scale);

    backButton = new PageButton(scaledBackX, scaledBackY, false, this::onBackClicked, true);

    int scaledNextX = bookLeft + (int) (NEXT_X * scale);
    int scaledNextY = bookTop + (int) (NEXT_Y * scale);

    nextButton = new PageButton(scaledNextX, scaledNextY, true, this::onNextClicked, true);

    updateButtonVisibility();

    addRenderableWidget(closeButton);
    addRenderableWidget(backButton);
    addRenderableWidget(nextButton);
  }

  private void calculateScale() {
    float fontBasedScale = (float) font.lineHeight / LINE_HEIGHT;

    float maxBookWidth = this.width * 0.8f;
    float maxBookHeight = this.height * 0.8f;
    float scaleX = maxBookWidth / BOOK_WIDTH;
    float scaleY = maxBookHeight / BOOK_HEIGHT;
    float screenFitScale = Math.min(scaleX, scaleY);

    this.scale = Math.min(fontBasedScale, screenFitScale);

    int scaledWidth = (int) (BOOK_WIDTH * scale);
    int scaledHeight = (int) (BOOK_HEIGHT * scale);

    this.bookLeft = (this.width - scaledWidth) / 2;
    this.bookTop = (this.height - scaledHeight) / 2;
  }

  private void updateButtonVisibility() {
    boolean isFirstPage = currentPageIndex == 0;

    backButton.visible = !isFirstPage;
    nextButton.visible = true;
  }

  private void onBackClicked(Button button) {
    if (currentPageIndex > 0) {
      navigateToPage(currentPageIndex - 1);
    }
  }

  private void onNextClicked(Button button) {
    if (currentPageIndex < TutorialPages.getPageCount() - 1) {
      TutorialManager.getInstance().goToPage(currentPageIndex + 1);
      navigateToPage(currentPageIndex + 1);
    } else {
      TutorialManager.getInstance().finishTutorial();
      onClose();
    }
  }

  private void onCloseClicked(CloseButton button) {
    onClose();
  }

  private void navigateToPage(int pageIndex) {
    goToPage(pageIndex);
  }

  public void goToPage(int pageIndex) {
    if (pageIndex < 0 || pageIndex >= TutorialPages.getPageCount()) {
      return;
    }
    if (currentPage != null) {
      currentPage.onPageClose(minecraft);
    }
    this.currentPageIndex = pageIndex;
    this.currentPage = TutorialPages.getPage(pageIndex);
    if (currentPage != null) {
      currentPage.onPageOpen(minecraft);
    }
    updateButtonVisibility();
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
    if (mouseButtonEvent.button() == 0 && currentPage != null) {
      int mouseX = (int) mouseButtonEvent.x();
      int mouseY = (int) mouseButtonEvent.y();

      int unscaledMouseX = (int) ((mouseX - bookLeft) / scale);
      int unscaledMouseY = (int) ((mouseY - bookTop) / scale);

      Style clickedStyle = getStyleAtMouse(unscaledMouseX, unscaledMouseY);
      if (clickedStyle != null) {
        ClickEvent clickEvent = clickedStyle.getClickEvent();
        if (clickEvent instanceof ClickEvent.RunCommand runCommand) {
          String command = runCommand.command();
          if (command.startsWith(ACTION_PREFIX)) {
            String action = command.substring(ACTION_PREFIX.length());
            WizardActionHandler.handle(action, minecraft);
            return true;
          }
        }
      }
    }
    return super.mouseClicked(mouseButtonEvent, bl);
  }

  private Style getStyleAtMouse(int unscaledMouseX, int unscaledMouseY) {
    if (currentPage == null) {
      return null;
    }

    int textX = CONTENT_X + TEXT_MARGIN;
    int textWidth = CONTENT_WIDTH - TEXT_MARGIN * 2;

    ActiveTextCollector.ClickableStyleFinder finder =
        new ActiveTextCollector.ClickableStyleFinder(font, unscaledMouseX, unscaledMouseY);

    Component textComponent = currentPage.getText(minecraft);
    List<FormattedCharSequence> lines = font.split(textComponent, textWidth);

    int y = CONTENT_Y + TEXT_START_Y_OFFSET;
    for (FormattedCharSequence line : lines) {
      finder.accept(textX, y, line);
      y += LINE_HEIGHT;
    }

    return finder.result();
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    renderDarkBackground(graphics);

    graphics.pose().pushMatrix();
    graphics.pose().translate(bookLeft, bookTop);
    graphics.pose().scale(scale, scale);

    renderBookBackground(graphics);

    if (currentPage != null) {
      renderPageContent(graphics);
    } else {
      int centerX = BOOK_WIDTH / 2;
      graphics.drawCenteredString(font, "Tutorial", centerX, BOOK_HEIGHT / 2 - 40, 0xFFFFFF);
      graphics.drawCenteredString(
          font, "Page content not found", centerX, BOOK_HEIGHT / 2, 0xFF5555);
    }

    graphics.pose().popMatrix();

    super.render(graphics, mouseX, mouseY, delta);
  }

  private void renderPageContent(GuiGraphics graphics) {
    renderTitle(graphics);
    renderBodyText(graphics);
  }

  private void renderTitle(GuiGraphics graphics) {
    if (currentPage == null) {
      return;
    }
    int titleY = CONTENT_Y + TITLE_Y_OFFSET;
    int centerX = CONTENT_X + CONTENT_WIDTH / 2;

    graphics.drawCenteredString(font, currentPage.getTitle(), centerX, titleY, TITLE_COLOR);
  }

  private void renderBodyText(GuiGraphics graphics) {
    if (currentPage == null) {
      return;
    }
    int textX = CONTENT_X + TEXT_MARGIN;
    int textWidth = CONTENT_WIDTH - TEXT_MARGIN * 2;
    textStartY = CONTENT_Y + TEXT_START_Y_OFFSET;

    Component textComponent = currentPage.getText(minecraft);
    List<FormattedCharSequence> lines = font.split(textComponent, textWidth);

    int y = textStartY;
    for (FormattedCharSequence wrappedLine : lines) {
      graphics.drawString(font, wrappedLine, textX, y, TEXT_COLOR, false);
      y += LINE_HEIGHT;
    }
  }

  private void renderBookBackground(GuiGraphics graphics) {
    graphics.blit(BOOK_TEXTURE, 0, 0, BOOK_WIDTH, BOOK_HEIGHT, 0f, 1.0f, 0f, 1.0f);
  }

  private void renderDarkBackground(GuiGraphics graphics) {
    graphics.fill(0, 0, this.width, this.height, 0xCC000000);
  }

  @Override
  public boolean shouldCloseOnEsc() {
    return true;
  }

  @Override
  public void onClose() {
    if (currentPage != null) {
      currentPage.onPageClose(minecraft);
    }
    super.onClose();
  }
}
