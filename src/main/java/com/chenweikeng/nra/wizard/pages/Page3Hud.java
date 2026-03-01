package com.chenweikeng.nra.wizard.pages;

import com.chenweikeng.nra.wizard.WizardPage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Page3Hud extends WizardPage {
  public Page3Hud() {
    super(2);
  }

  @Override
  public Component getTitle() {
    return text("Strategy HUD");
  }

  @Override
  public Component getText(Minecraft client) {
    return Component.empty()
        .append(text("The HUD shows recommended rides to grind based on your progress.\n\n"))
        .append(colored("Green", ChatFormatting.GREEN))
        .append(text(" = currently riding\n"))
        .append(colored("Yellow", ChatFormatting.YELLOW))
        .append(text(" = waiting for autograb\n"))
        .append(colored("White", ChatFormatting.WHITE))
        .append(text(" = available to ride\n\n"))
        .append(link("Skip to config", "page:4"));
  }

  @Override
  public boolean isSkipAllowed() {
    return true;
  }
}
