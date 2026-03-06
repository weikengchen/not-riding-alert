package com.chenweikeng.nra.wizard.pages;

import com.chenweikeng.nra.wizard.WizardPage;
import com.chenweikeng.nra.wizard.layout.RenderBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class Page7Profiles extends WizardPage {

  public Page7Profiles() {
    super(6);
  }

  @Override
  public Component getTitle() {
    return literal("Settings and Profiles");
  }

  @Override
  public List<RenderBlock> getBlocks(Minecraft client) {
    List<RenderBlock> blocks = new ArrayList<>();

    // Section 1: How to edit the current setting
    blocks.add(text(editCurrentSettingSection()));
    blocks.add(spacer(20));

    Identifier editCurrentImg =
        Identifier.fromNamespaceAndPath("not-riding-alert", "textures/setting.png");
    blocks.add(image(editCurrentImg, 1200, 70));

    blocks.add(separator(25));

    // Section 2: How to use the profile management
    blocks.add(text(profileManagementSection()));
    blocks.add(spacer(20));

    Identifier profileMgmtImg =
        Identifier.fromNamespaceAndPath("not-riding-alert", "textures/profile.png");
    blocks.add(image(profileMgmtImg, 1200, 103));

    blocks.add(separator(25));

    // Welcome message
    blocks.add(text(welcomeSection()));

    return blocks;
  }

  private Component editCurrentSettingSection() {
    Component content = literal("");
    content = append(content, literal("To edit the current configuration, enter "));
    content = append(content, link("/nra", "command:nra"));
    content = append(content, literal("\n"));
    content = append(content, colored("  • Edit", ChatFormatting.GREEN));
    content = append(content, literal(" - Modify the current settings\n"));
    content = append(content, colored("  • Save to..", ChatFormatting.GREEN));
    content = append(content, literal(" - Save the current settings as a profile"));
    return content;
  }

  private Component profileManagementSection() {
    Component content = literal("");
    content = append(content, literal("The Profile Management screen lets you:\n\n"));
    content = append(content, colored("  • Apply", ChatFormatting.GREEN));
    content = append(content, literal(" - Switch to a saved profile\n"));
    content = append(content, colored("  • Rename", ChatFormatting.GREEN));
    content = append(content, literal(" - Change the profile name and description\n"));
    content = append(content, colored("  • Edit", ChatFormatting.GREEN));
    content = append(content, literal(" - Modify the profile's settings\n"));
    content = append(content, colored("  • Delete", ChatFormatting.GREEN));
    content = append(content, literal(" - Remove a saved profile"));
    return content;
  }

  private Component welcomeSection() {
    Component content = literal("");
    content = append(content, colored("Welcome to Not Riding Alert!\n\n", ChatFormatting.GOLD));
    content = append(content, literal("Thank you for using this mod. "));
    content = append(content, colored("Enjoy your rides! ", ChatFormatting.GREEN));
    content = append(content, colored("🎢", ChatFormatting.WHITE));
    return content;
  }

  private Component append(Component base, Component toAppend) {
    return base.copy().append(toAppend);
  }
}
