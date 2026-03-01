package com.chenweikeng.nra.wizard.pages;

import com.chenweikeng.nra.wizard.WizardPage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Page1Welcome extends WizardPage {
  public Page1Welcome() {
    super(0);
  }

  @Override
  public Component getTitle() {
    return text("Welcome to Not Riding Alert");
  }

  @Override
  public Component getText(Minecraft client) {
    return Component.empty()
        .append(text("This mod helps you efficiently grind theme park rides.\n\n"))
        .append(bold("Features:\n"))
        .append(text("• Alerts when idle\n"))
        .append(text("• Progress tracking\n"))
        .append(text("• "))
        .append(colored("Autograbbing", ChatFormatting.GREEN))
        .append(text(" support\n\n"))
        .append(link("Enable autograb", "config:autograb:true"))
        .append(text(" or "))
        .append(link("configure later", "page:4"))
        .append(text("\n\n"))
        .append(colored("[TEST] ", ChatFormatting.YELLOW))
        .append(link("Run /ridestats", "command:ridestats"))
        .append(text(" | "))
        .append(link("Factory Reset", "factory_reset"));
  }

  @Override
  public boolean isSkipAllowed() {
    return true;
  }
}
