package com.chenweikeng.nra.wizard.pages;

import com.chenweikeng.nra.wizard.WizardPage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Page5Config extends WizardPage {
  public Page5Config() {
    super(4);
  }

  @Override
  public Component getTitle() {
    return text("Visual Options");
  }

  @Override
  public Component getText(Minecraft client) {
    return Component.empty()
        .append(text("Choose what to hide while riding:\n\n"))
        .append(text("• "))
        .append(link("Chat", "config:hideChat:true"))
        .append(text(" / "))
        .append(link("Scoreboard", "config:hideScoreboard:true"))
        .append(text("\n• "))
        .append(link("Health bar", "config:hideHealth:true"))
        .append(text(" / "))
        .append(link("Hotbar", "config:hideHotbar:true"));
  }

  @Override
  public boolean isSkipAllowed() {
    return false;
  }
}
