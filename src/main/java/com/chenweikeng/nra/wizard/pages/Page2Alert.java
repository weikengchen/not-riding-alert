package com.chenweikeng.nra.wizard.pages;

import com.chenweikeng.nra.wizard.WizardPage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Page2Alert extends WizardPage {
  public Page2Alert() {
    super(1);
  }

  @Override
  public Component getTitle() {
    return text("Alert System");
  }

  @Override
  public Component getText(Minecraft client) {
    return Component.empty()
        .append(text("When you're not riding and haven't moved, the mod plays a sound.\n\n"))
        .append(italic("Tip:"))
        .append(text(" You can customize the sound in "))
        .append(link("settings", "open:config"))
        .append(text("."));
  }

  @Override
  public boolean isSkipAllowed() {
    return true;
  }
}
