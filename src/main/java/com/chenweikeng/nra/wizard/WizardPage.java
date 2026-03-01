package com.chenweikeng.nra.wizard;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public abstract class WizardPage {
  protected final int pageIndex;
  private static final String ACTION_PREFIX = "wizard_action:";

  protected WizardPage(int pageIndex) {
    this.pageIndex = pageIndex;
  }

  public abstract Component getTitle();

  public abstract Component getText(Minecraft client);

  public abstract boolean isSkipAllowed();

  public int getPageIndex() {
    return pageIndex;
  }

  public void onPageOpen(Minecraft client) {}

  public void onPageClose(Minecraft client) {}

  protected Component link(String text, String action) {
    String command = ACTION_PREFIX + action;
    ClickEvent clickEvent = new ClickEvent.RunCommand(command);
    HoverEvent hoverEvent =
        new HoverEvent.ShowText(Component.literal(getActionDescription(action)));

    Style linkStyle =
        Style.EMPTY
            .withColor(ChatFormatting.AQUA)
            .withUnderlined(true)
            .withClickEvent(clickEvent)
            .withHoverEvent(hoverEvent);

    return Component.literal(text).withStyle(linkStyle);
  }

  protected Component bold(String text) {
    return Component.literal(text).withStyle(Style.EMPTY.withBold(true));
  }

  protected Component italic(String text) {
    return Component.literal(text).withStyle(Style.EMPTY.withItalic(true));
  }

  protected Component colored(String text, ChatFormatting color) {
    return Component.literal(text).withStyle(color);
  }

  protected Component text(String text) {
    return Component.literal(text);
  }

  protected Component newline() {
    return Component.literal("\n");
  }

  private String getActionDescription(String action) {
    if (action.startsWith("page:")) {
      return "Go to page " + action.substring(5);
    } else if (action.startsWith("config:")) {
      return "Change setting";
    } else if (action.equals("open:config")) {
      return "Open settings";
    } else if (action.equals("finish")) {
      return "Finish tutorial";
    }
    return action;
  }
}
