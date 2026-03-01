package com.chenweikeng.nra.wizard.pages;

import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.wizard.WizardPage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Page4Autograb extends WizardPage {
  public Page4Autograb() {
    super(3);
  }

  @Override
  public Component getTitle() {
    return text("Autograbbing");
  }

  @Override
  public Component getText(Minecraft client) {
    boolean autograbEnabled = ModConfig.getInstance().autograb;
    ChatFormatting statusColor = autograbEnabled ? ChatFormatting.GREEN : ChatFormatting.RED;
    String statusText = autograbEnabled ? "ON" : "OFF";

    return Component.empty()
        .append(text("Walk into a ride's queue area and the mod handles the rest!\n\n"))
        .append(text("Autograbbing is currently: "))
        .append(colored(statusText, statusColor))
        .append(text("\n\n"))
        .append(bold("["))
        .append(link("Toggle", "config:autograb:" + !autograbEnabled))
        .append(bold("]"))
        .append(text("\n\n"))
        .append(link("Skip to config", "page:4"));
  }

  @Override
  public boolean isSkipAllowed() {
    return true;
  }
}
