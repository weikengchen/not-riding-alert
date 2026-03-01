package com.chenweikeng.nra.wizard.pages;

import com.chenweikeng.nra.wizard.WizardPage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Page6Done extends WizardPage {
  public Page6Done() {
    super(5);
  }

  @Override
  public Component getTitle() {
    return text("Ready to Go!");
  }

  @Override
  public Component getText(Minecraft client) {
    return Component.empty()
        .append(text("You're all set!\n\n"))
        .append(text("Use "))
        .append(bold("/nra"))
        .append(text(" anytime to adjust settings.\n\n"))
        .append(link("Finish", "finish"))
        .append(text(" to start using the mod."));
  }

  @Override
  public boolean isSkipAllowed() {
    return false;
  }
}
