package com.chenweikeng.nra.config;

import com.chenweikeng.nra.compat.MonkeycraftCompat;
import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import com.chenweikeng.nra.util.TimeFormatUtil;
import java.util.stream.Collectors;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public class ClothConfigScreen {

  public static Object createScreen(net.minecraft.client.gui.screens.Screen parent) {
    ModConfig.getInstance().hasOpenedConfig = true;
    ModConfig.getInstance().save();

    String progressDescription =
        String.format(
            " 1k (%s), 5k (%s), 10k (%s)",
            calculateProgress(1000), calculateProgress(5000), calculateProgress(10000));

    ConfigBuilder builder =
        ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(
                Component.translatable("config.not-riding-alert.title")
                    .append(Component.literal(progressDescription)))
            .setSavingRunnable(() -> ModConfig.getInstance().save());

    ConfigEntryBuilder entryBuilder = builder.entryBuilder();

    Minecraft client = Minecraft.getInstance();

    ConfigCategory general =
        builder.getOrCreateCategory(
            Component.translatable("config.not-riding-alert.category.general"));

    general.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.globalEnable"),
                ModConfig.getInstance().globalEnable)
            .setDefaultValue(ConfigDefaults.GLOBAL_ENABLE)
            .setTooltip(Component.translatable("config.not-riding-alert.globalEnable.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().globalEnable = newValue)
            .build());

    general.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.enabled"),
                ModConfig.getInstance().enabled)
            .setDefaultValue(ConfigDefaults.ENABLED)
            .setTooltip(Component.translatable("config.not-riding-alert.enabled.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().enabled = newValue)
            .build());

    general.addEntry(
        entryBuilder
            .startDropdownMenu(
                Component.translatable("config.not-riding-alert.soundId"),
                DropdownMenuBuilder.TopCellElementBuilder.of(
                    ModConfig.getInstance().soundId, e -> e),
                DropdownMenuBuilder.CellCreatorBuilder.of())
            .setDefaultValue(ConfigDefaults.SOUND_ID)
            .setTooltip(Component.translatable("config.not-riding-alert.soundId.tooltip"))
            .setSelections(
                client.level.registryAccess().lookupOrThrow(Registries.SOUND_EVENT).stream()
                    .map(soundEvent -> soundEvent.location().getPath())
                    .sorted()
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new)))
            .setSuggestionMode(true)
            .setSaveConsumer(soundId -> ModConfig.getInstance().soundId = soundId)
            .build());

    general.addEntry(
        entryBuilder
            .startEnumSelector(
                Component.translatable("config.not-riding-alert.cursorReleaseTiming"),
                CursorReleaseTiming.class,
                ModConfig.getInstance().cursorReleaseTiming)
            .setDefaultValue(CursorReleaseTiming.NONE)
            .setTooltip(
                Component.translatable("config.not-riding-alert.cursorReleaseTiming.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().cursorReleaseTiming = newValue)
            .setEnumNameProvider(
                timing ->
                    Component.translatable(
                        "config.not-riding-alert.cursorReleaseTiming."
                            + timing.name().toLowerCase()))
            .build());

    general.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.silent"),
                ModConfig.getInstance().silent)
            .setDefaultValue(ConfigDefaults.SILENT)
            .setTooltip(Component.translatable("config.not-riding-alert.silent.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().silent = newValue)
            .build());

    general.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.alertAutograbFailure"),
                ModConfig.getInstance().alertAutograbFailure)
            .setDefaultValue(ConfigDefaults.ALERT_AUTOGRAB_FAILURE)
            .setTooltip(
                Component.translatable("config.not-riding-alert.alertAutograbFailure.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().alertAutograbFailure = newValue)
            .build());

    general.addEntry(
        entryBuilder
            .startEnumSelector(
                Component.translatable("config.not-riding-alert.minimizeWindow"),
                WindowMinimizeTiming.class,
                ModConfig.getInstance().minimizeWindow)
            .setDefaultValue(WindowMinimizeTiming.NONE)
            .setTooltip(Component.translatable("config.not-riding-alert.minimizeWindow.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().minimizeWindow = newValue)
            .setEnumNameProvider(
                timing ->
                    Component.translatable(
                        "config.not-riding-alert.minimizeWindow." + timing.name().toLowerCase()))
            .build());

    ConfigCategory visual =
        builder.getOrCreateCategory(
            Component.translatable("config.not-riding-alert.category.visual"));

    visual.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.blindWhenRiding"),
                ModConfig.getInstance().blindWhenRiding)
            .setDefaultValue(ConfigDefaults.BLIND_WHEN_RIDING)
            .setTooltip(Component.translatable("config.not-riding-alert.blindWhenRiding.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().blindWhenRiding = newValue)
            .build());

    visual.addEntry(
        entryBuilder
            .startEnumSelector(
                Component.translatable("config.not-riding-alert.fullbright"),
                FullbrightMode.class,
                ModConfig.getInstance().fullbrightMode)
            .setDefaultValue(FullbrightMode.NONE)
            .setTooltip(Component.translatable("config.not-riding-alert.fullbright.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().fullbrightMode = newValue)
            .setEnumNameProvider(
                mode ->
                    Component.translatable(
                        "config.not-riding-alert.fullbright." + mode.name().toLowerCase()))
            .build());

    visual.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.hideScoreboard"),
                ModConfig.getInstance().hideScoreboard)
            .setDefaultValue(ConfigDefaults.HIDE_SCOREBOARD)
            .setTooltip(Component.translatable("config.not-riding-alert.hideScoreboard.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().hideScoreboard = newValue)
            .build());

    visual.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.hideChat"),
                ModConfig.getInstance().hideChat)
            .setDefaultValue(ConfigDefaults.HIDE_CHAT)
            .setTooltip(Component.translatable("config.not-riding-alert.hideChat.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().hideChat = newValue)
            .build());

    visual.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.hideHealth"),
                ModConfig.getInstance().hideHealth)
            .setDefaultValue(ConfigDefaults.HIDE_HEALTH)
            .setTooltip(Component.translatable("config.not-riding-alert.hideHealth.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().hideHealth = newValue)
            .build());

    visual.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.hideNameTag"),
                ModConfig.getInstance().hideNameTag)
            .setDefaultValue(ConfigDefaults.HIDE_NAME_TAG)
            .setTooltip(Component.translatable("config.not-riding-alert.hideNameTag.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().hideNameTag = newValue)
            .build());

    visual.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.hideHotbar"),
                ModConfig.getInstance().hideHotbar)
            .setDefaultValue(ConfigDefaults.HIDE_HOTBAR)
            .setTooltip(Component.translatable("config.not-riding-alert.hideHotbar.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().hideHotbar = newValue)
            .build());

    visual.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.hideExperienceLevel"),
                ModConfig.getInstance().hideExperienceLevel)
            .setDefaultValue(ConfigDefaults.HIDE_EXPERIENCE_LEVEL)
            .setTooltip(
                Component.translatable("config.not-riding-alert.hideExperienceLevel.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().hideExperienceLevel = newValue)
            .build());

    visual.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.hideLovePotionMessages"),
                ModConfig.getInstance().hideLovePotionMessages)
            .setDefaultValue(ConfigDefaults.HIDE_LOVE_POTION_MESSAGES)
            .setTooltip(
                Component.translatable("config.not-riding-alert.hideLovePotionMessages.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().hideLovePotionMessages = newValue)
            .build());

    visual.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.relocateClosedCaption"),
                ModConfig.getInstance().relocateClosedCaption)
            .setDefaultValue(ConfigDefaults.RELOCATE_CLOSED_CAPTION)
            .setTooltip(
                Component.translatable("config.not-riding-alert.relocateClosedCaption.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().relocateClosedCaption = newValue)
            .build());

    ConfigCategory tracker =
        builder.getOrCreateCategory(
            Component.translatable("config.not-riding-alert.category.rides"));

    tracker.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.enableTracker"),
                ModConfig.getInstance().enableTracker)
            .setDefaultValue(ConfigDefaults.ENABLE_TRACKER)
            .setTooltip(Component.translatable("config.not-riding-alert.enableTracker.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().enableTracker = newValue)
            .build());

    tracker.addEntry(
        entryBuilder
            .startEnumSelector(
                Component.translatable("config.not-riding-alert.strategyHudRendererVersion"),
                StrategyHudRendererVersion.class,
                ModConfig.getInstance().strategyHudRendererVersion)
            .setDefaultValue(ConfigDefaults.STRATEGY_HUD_RENDERER_VERSION)
            .setTooltip(
                Component.translatable(
                    "config.not-riding-alert.strategyHudRendererVersion.tooltip"))
            .setSaveConsumer(
                newValue -> ModConfig.getInstance().strategyHudRendererVersion = newValue)
            .setEnumNameProvider(
                version ->
                    Component.translatable(
                        "config.not-riding-alert.strategyHudRendererVersion."
                            + version.name().toLowerCase()))
            .build());

    tracker.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.autograb"),
                ModConfig.getInstance().autograb)
            .setDefaultValue(ConfigDefaults.AUTOGRAB)
            .setTooltip(Component.translatable("config.not-riding-alert.autograb.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().autograb = newValue)
            .build());

    tracker.addEntry(
        entryBuilder
            .startIntSlider(
                Component.translatable("config.not-riding-alert.rideDisplayCount"),
                ModConfig.getInstance().rideDisplayCount,
                0,
                60)
            .setDefaultValue(ConfigDefaults.RIDE_DISPLAY_COUNT)
            .setTooltip(Component.translatable("config.not-riding-alert.rideDisplayCount.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().rideDisplayCount = newValue)
            .build());

    tracker.addEntry(
        entryBuilder
            .startIntSlider(
                Component.translatable("config.not-riding-alert.minRideTimeMinutes"),
                ModConfig.getInstance().minRideTimeMinutes != null
                    ? ModConfig.getInstance().minRideTimeMinutes
                    : 0,
                0,
                16)
            .setDefaultValue(0)
            .setTooltip(
                Component.translatable("config.not-riding-alert.minRideTimeMinutes.tooltip"))
            .setSaveConsumer(
                newValue -> {
                  if (newValue <= 0) {
                    ModConfig.getInstance().minRideTimeMinutes = null;
                  } else {
                    ModConfig.getInstance().minRideTimeMinutes = newValue;
                  }
                })
            .build());

    tracker.addEntry(
        entryBuilder
            .startIntSlider(
                Component.translatable("config.not-riding-alert.hudBackgroundOpacity"),
                ModConfig.getInstance().hudBackgroundOpacity,
                0,
                100)
            .setDefaultValue(ConfigDefaults.HUD_BACKGROUND_OPACITY)
            .setTooltip(
                Component.translatable("config.not-riding-alert.hudBackgroundOpacity.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().hudBackgroundOpacity = newValue)
            .build());

    tracker.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.onlyAutograbbing"),
                ModConfig.getInstance().onlyAutograbbing)
            .setDefaultValue(ConfigDefaults.ONLY_AUTOGRABBING)
            .setTooltip(Component.translatable("config.not-riding-alert.onlyAutograbbing.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().onlyAutograbbing = newValue)
            .build());

    tracker.addEntry(
        entryBuilder
            .startColorField(
                Component.translatable("config.not-riding-alert.trackerNormalColor"),
                TextColor.fromRgb(ModConfig.getInstance().trackerNormalColor & 0x00FFFFFF))
            .setDefaultValue(TextColor.fromRgb(ConfigDefaults.TRACKER_NORMAL_COLOR & 0x00FFFFFF))
            .setTooltip(
                Component.translatable("config.not-riding-alert.trackerNormalColor.tooltip"))
            .setSaveConsumer2(
                color -> ModConfig.getInstance().trackerNormalColor = color.getColor() | 0xFF000000)
            .build());

    tracker.addEntry(
        entryBuilder
            .startColorField(
                Component.translatable("config.not-riding-alert.trackerAutograbbingColor"),
                TextColor.fromRgb(ModConfig.getInstance().trackerAutograbbingColor & 0x00FFFFFF))
            .setDefaultValue(
                TextColor.fromRgb(ConfigDefaults.TRACKER_AUTOGRABBING_COLOR & 0x00FFFFFF))
            .setTooltip(
                Component.translatable("config.not-riding-alert.trackerAutograbbingColor.tooltip"))
            .setSaveConsumer2(
                color ->
                    ModConfig.getInstance().trackerAutograbbingColor =
                        color.getColor() | 0xFF000000)
            .build());

    tracker.addEntry(
        entryBuilder
            .startColorField(
                Component.translatable("config.not-riding-alert.trackerRidingColor"),
                TextColor.fromRgb(ModConfig.getInstance().trackerRidingColor & 0x00FFFFFF))
            .setDefaultValue(TextColor.fromRgb(ConfigDefaults.TRACKER_RIDING_COLOR & 0x00FFFFFF))
            .setTooltip(
                Component.translatable("config.not-riding-alert.trackerRidingColor.tooltip"))
            .setSaveConsumer2(
                color -> ModConfig.getInstance().trackerRidingColor = color.getColor() | 0xFF000000)
            .build());

    tracker.addEntry(
        entryBuilder
            .startColorField(
                Component.translatable("config.not-riding-alert.trackerErrorColor"),
                TextColor.fromRgb(ModConfig.getInstance().trackerErrorColor & 0x00FFFFFF))
            .setDefaultValue(TextColor.fromRgb(ConfigDefaults.TRACKER_ERROR_COLOR & 0x00FFFFFF))
            .setTooltip(Component.translatable("config.not-riding-alert.trackerErrorColor.tooltip"))
            .setSaveConsumer(
                color -> ModConfig.getInstance().trackerErrorColor = color | 0xFF000000)
            .build());

    tracker.addEntry(
        entryBuilder
            .startEnumSelector(
                Component.translatable("config.not-riding-alert.audioBoostReminderMode"),
                AudioBoostReminderMode.class,
                ModConfig.getInstance().audioBoostReminderMode)
            .setDefaultValue(ConfigDefaults.AUDIO_BOOST_REMINDER_MODE)
            .setTooltip(
                Component.translatable("config.not-riding-alert.audioBoostReminderMode.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().audioBoostReminderMode = newValue)
            .setEnumNameProvider(
                mode ->
                    Component.translatable(
                        "config.not-riding-alert.audioBoostReminderMode."
                            + mode.name().toLowerCase()))
            .build());

    ConfigCategory rides =
        builder.getOrCreateCategory(
            Component.translatable("config.not-riding-alert.category.rideDisplay"));

    for (RideName ride : RideName.values()) {
      if (ride == RideName.UNKNOWN) {
        continue;
      }
      boolean currentValue = !ModConfig.getInstance().hiddenRides.contains(ride.toMatchString());
      rides.addEntry(
          entryBuilder
              .startBooleanToggle(formatRideLabel(ride), currentValue)
              .setDefaultValue(!ride.isSeasonal())
              .setTooltip(Component.translatable("config.not-riding-alert.rideDisplay.tooltip"))
              .setSaveConsumer(
                  newValue -> {
                    if (!newValue) {
                      ModConfig.getInstance().hiddenRides.add(ride.toMatchString());
                    } else {
                      ModConfig.getInstance().hiddenRides.remove(ride.toMatchString());
                    }
                  })
              .build());
    }

    if (MonkeycraftCompat.isAvailable()) {
      ConfigCategory monkeyCraft = builder.getOrCreateCategory(Component.literal("MonkeyCraft"));

      monkeyCraft.addEntry(
          entryBuilder
              .startBooleanToggle(
                  Component.literal("Keep the player screen unchanged after connection"),
                  ModConfig.getInstance().keepUnchanged)
              .setDefaultValue(ConfigDefaults.KEEP_UNCHANGED)
              .setTooltip(
                  Component.literal(
                      "When enabled, the mod will not hide scoreboard, chat, or strategy bar when MonkeyCraft is attached."))
              .setSaveConsumer(newValue -> ModConfig.getInstance().keepUnchanged = newValue)
              .build());

      monkeyCraft.addEntry(
          entryBuilder
              .startBooleanToggle(
                  Component.literal("Hibernating when riding"),
                  ModConfig.getInstance().hibernationWhenRiding)
              .setDefaultValue(ConfigDefaults.HIBERNATION_WHEN_RIDING)
              .setTooltip(
                  Component.literal(
                      "When enabled, the mod will start hibernation when riding. When disabled, new hibernations won't start, but existing ones can still be updated or ended."))
              .setSaveConsumer(newValue -> ModConfig.getInstance().hibernationWhenRiding = newValue)
              .build());
    }

    return builder.build();
  }

  private static Component formatRideLabel(RideName ride) {
    String timeString = TimeFormatUtil.formatDuration(ride.getRideTime());
    return Component.literal(String.format("%s (Time: %s)", ride.getDisplayName(), timeString));
  }

  private static String calculateProgress(int goal) {
    RideCountManager countManager = RideCountManager.getInstance();
    long totalSecondsNeeded = 0;
    long totalSecondsFromZero = 0;
    long completedSeconds = 0;

    for (RideName ride : RideName.values()) {
      if (ride == RideName.UNKNOWN) {
        continue;
      }

      if (ModConfig.getInstance().hiddenRides.contains(ride.toMatchString())) {
        continue;
      }

      int currentCount = countManager.getRideCount(ride);
      int rideTimeSeconds = ride.getRideTime();

      if (rideTimeSeconds >= 99999) {
        continue;
      }

      // Calculate total time needed if starting from 0
      totalSecondsFromZero += (long) goal * rideTimeSeconds;

      if (currentCount >= goal) {
        // Player has completed this ride goal, add all time to completed
        completedSeconds += (long) goal * rideTimeSeconds;
      } else {
        // Player has partially completed this ride
        completedSeconds += (long) currentCount * rideTimeSeconds;
        int ridesNeeded = goal - currentCount;
        totalSecondsNeeded += (long) ridesNeeded * rideTimeSeconds;
      }
    }

    // Calculate progress percentage
    double progressPercentage = 0.0;
    if (totalSecondsFromZero > 0) {
      progressPercentage = ((double) completedSeconds / totalSecondsFromZero) * 100.0;
    }

    return String.format(
        "%.2f%%, %s", progressPercentage, TimeFormatUtil.formatDuration(totalSecondsNeeded));
  }
}
