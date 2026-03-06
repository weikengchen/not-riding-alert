package com.chenweikeng.nra.config.profile.ui;

import com.chenweikeng.nra.config.ClothConfigScreen;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.config.profile.BuiltInProfiles;
import com.chenweikeng.nra.config.profile.ProfileManager;
import com.chenweikeng.nra.config.profile.StoredProfile;
import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import com.chenweikeng.nra.util.TimeFormatUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ProfileManagementScreen extends Screen {
  private static final int PADDING = 20;
  private static final int FOOTER_HEIGHT = 50;
  private static final int BUTTON_HEIGHT = 20;
  private static final int LABEL_COLOR = 0xFFFFFFFF;

  private final Screen parent;
  private CurrentSettingsEntry currentSettingsEntry;
  private ProfileListWidget profileList;
  private Button resetButton;
  private Button closeButton;

  private StoredProfile pendingDeleteProfile = null;

  public ProfileManagementScreen(Screen parent) {
    super(Component.literal("Profiles"));
    this.parent = parent;
  }

  public ProfileManagementScreen() {
    this(null);
  }

  @Override
  protected void init() {
    super.init();

    int currentSettingsHeight = 36;
    int titleGap = 15;
    int listGap = 15;
    int listY = PADDING + titleGap + currentSettingsHeight + listGap;
    int listHeight = height - FOOTER_HEIGHT - PADDING - titleGap - currentSettingsHeight - listGap;

    int footerY = height - FOOTER_HEIGHT + 10;
    int buttonWidth = 100;
    int buttonGap = 10;

    int totalButtonWidth = buttonWidth * 2 + buttonGap;
    int startX = (width - totalButtonWidth) / 2;

    resetButton =
        Button.builder(Component.literal("Reset Builtins"), this::onResetClicked)
            .bounds(startX, footerY, buttonWidth, BUTTON_HEIGHT)
            .build();
    addRenderableWidget(resetButton);

    closeButton =
        Button.builder(Component.literal("Close"), this::onCloseClicked)
            .bounds(startX + buttonWidth + buttonGap, footerY, buttonWidth, BUTTON_HEIGHT)
            .build();
    addRenderableWidget(closeButton);

    currentSettingsEntry =
        new CurrentSettingsEntry(
            minecraft,
            PADDING,
            PADDING + titleGap,
            width - PADDING * 2,
            this::openSettings,
            this::openSaveCurrent);

    profileList =
        new ProfileListWidget(
            minecraft,
            width,
            listHeight,
            listY,
            this::onProfileApply,
            this::onProfileRename,
            this::onProfileEdit,
            this::onProfileDeleteRequest,
            this::onSelectionChange);

    addRenderableWidget(profileList);
    profileList.setProfiles(ProfileManager.getAllProfiles());
  }

  private void updateButtonStates() {}

  private void onSelectionChange(StoredProfile profile) {
    updateButtonStates();
  }

  private void onProfileApply(StoredProfile profile) {
    if (profile == null) return;

    ProfileManager.activateProfile(profile.id);
    profileList.refreshProfiles();
  }

  private void onSaveCurrentClicked(Button button) {
    minecraft.setScreen(ProfileEditScreen.createNew(this, this::onProfileSaved));
  }

  private void openSaveCurrent() {
    onSaveCurrentClicked(null);
  }

  private void onProfileSaved(StoredProfile profile) {
    profileList.refreshProfiles();
    if (profile != null) {
      profileList.selectProfile(profile.id);
    }
  }

  private void onProfileRename(StoredProfile profile) {
    if (profile == null) return;
    minecraft.setScreen(ProfileEditScreen.createRename(this, profile, this::onProfileRenamed));
  }

  private void onProfileRenamed(StoredProfile profile) {
    profileList.refreshProfiles();
    if (profile != null) {
      profileList.selectProfile(profile.id);
    }
  }

  private void onProfileEdit(StoredProfile profile) {
    if (profile == null) return;

    minecraft.setScreen(
        (Screen)
            ClothConfigScreen.createScreen(
                this,
                profile.data,
                () -> {
                  profile.modifiedAt = System.currentTimeMillis();
                  ProfileManager.save();
                  profileList.refreshProfiles();
                }));
  }

  private void onProfileDeleteRequest(StoredProfile profile) {
    if (profile == null) return;
    pendingDeleteProfile = profile;
    confirmDelete();
  }

  private void confirmDelete() {
    if (pendingDeleteProfile == null) return;

    ProfileManager.deleteProfile(pendingDeleteProfile.id);
    profileList.refreshProfiles();

    pendingDeleteProfile = null;
  }

  private void onResetClicked(Button button) {
    // Get all builtin profiles
    java.util.List<StoredProfile> builtInProfiles = BuiltInProfiles.all();
    java.util.List<StoredProfile> allProfiles = ProfileManager.getAllProfiles();

    // For each builtin profile, check if there's a profile with same name
    for (StoredProfile builtIn : builtInProfiles) {
      boolean found = false;

      // Find profile with matching name
      for (StoredProfile existing : allProfiles) {
        if (existing.name.equalsIgnoreCase(builtIn.name)) {
          // Restore the builtin profile's data to the existing profile
          existing.data = builtIn.data.copy();
          existing.modifiedAt = System.currentTimeMillis();
          found = true;
          break;
        }
      }

      // If no profile with matching name found, add builtin profile at beginning
      if (!found) {
        ProfileManager.addProfileAtStart(
            builtIn.id, builtIn.name, builtIn.description, builtIn.data.copy());
        // Refresh allProfiles to include the newly added profile
        allProfiles = ProfileManager.getAllProfiles();
      }
    }

    // Save changes and refresh the list
    ProfileManager.save();
    profileList.refreshProfiles();
  }

  private void onCloseClicked(Button button) {
    onClose();
  }

  private void onSettingsClicked(Button button) {
    minecraft.setScreen((Screen) ClothConfigScreen.createScreen(this));
  }

  private void openSettings() {
    onSettingsClicked(null);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    renderDarkBackground(graphics);

    String progressDescription =
        String.format(
            " [1k (%s), 5k (%s), 10k (%s)]",
            calculateProgress(1000), calculateProgress(5000), calculateProgress(10000));
    Component titleWithProgress =
        Component.literal("Not Riding Alert").append(Component.literal(progressDescription));

    graphics.drawCenteredString(font, titleWithProgress, width / 2, 8, LABEL_COLOR);

    if (currentSettingsEntry != null) {
      currentSettingsEntry.render(graphics, mouseX, mouseY);
    }

    int footerY = height - FOOTER_HEIGHT;
    graphics.fill(0, footerY, width, height, 0xDD000000);

    super.render(graphics, mouseX, mouseY, delta);
  }

  private void renderDarkBackground(GuiGraphics graphics) {
    graphics.fill(0, 0, this.width, this.height, 0xCC000000);
  }

  private String calculateProgress(int goal) {
    RideCountManager countManager = RideCountManager.getInstance();
    long totalSecondsNeeded = 0;
    long totalSecondsFromZero = 0;
    long completedSeconds = 0;

    for (RideName ride : RideName.values()) {
      if (ride == RideName.UNKNOWN) {
        continue;
      }

      if (ModConfig.currentSetting.hiddenRides.contains(ride.toMatchString())) {
        continue;
      }

      int currentCount = countManager.getRideCount(ride);
      int rideTimeSeconds = ride.getRideTime();

      if (rideTimeSeconds >= 99999) {
        continue;
      }

      totalSecondsFromZero += (long) goal * rideTimeSeconds;

      if (currentCount >= goal) {
        completedSeconds += (long) goal * rideTimeSeconds;
      } else {
        completedSeconds += (long) currentCount * rideTimeSeconds;
        int ridesNeeded = goal - currentCount;
        totalSecondsNeeded += (long) ridesNeeded * rideTimeSeconds;
      }
    }

    double progressPercentage = 0.0;
    if (totalSecondsFromZero > 0) {
      progressPercentage = ((double) completedSeconds / totalSecondsFromZero) * 100.0;
    }

    return String.format(
        "%.2f%%, %s", progressPercentage, TimeFormatUtil.formatDuration(totalSecondsNeeded));
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    if (currentSettingsEntry != null) {
      if (currentSettingsEntry.mouseClicked(event)) {
        return true;
      }
    }
    return super.mouseClicked(event, doubleClick);
  }

  @Override
  public boolean shouldCloseOnEsc() {
    return true;
  }

  @Override
  public void onClose() {
    if (minecraft != null) {
      minecraft.setScreen(parent);
    }
  }
}
