package com.chenweikeng.nra.config;

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

public class ClothConfigScreen {

  public static Object createScreen(net.minecraft.client.gui.screens.Screen parent) {
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
                Component.translatable("config.not-riding-alert.enabled"),
                ModConfig.getInstance().enabled)
            .setDefaultValue(true)
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
            .setDefaultValue("entity.experience_orb.pickup")
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
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.blindWhenRiding"),
                ModConfig.getInstance().blindWhenRiding)
            .setDefaultValue(false)
            .setTooltip(Component.translatable("config.not-riding-alert.blindWhenRiding.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().blindWhenRiding = newValue)
            .build());

    general.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.defocusCursor"),
                ModConfig.getInstance().defocusCursor)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("config.not-riding-alert.defocusCursor.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().defocusCursor = newValue)
            .build());

    general.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.silent"),
                ModConfig.getInstance().silent)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("config.not-riding-alert.silent.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().silent = newValue)
            .build());

    general.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.hideScoreboard"),
                ModConfig.getInstance().hideScoreboard)
            .setDefaultValue(false)
            .setTooltip(Component.translatable("config.not-riding-alert.hideScoreboard.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().hideScoreboard = newValue)
            .build());

    general.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.hideChat"),
                ModConfig.getInstance().hideChat)
            .setDefaultValue(false)
            .setTooltip(Component.translatable("config.not-riding-alert.hideChat.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().hideChat = newValue)
            .build());

    general.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.hideHealth"),
                ModConfig.getInstance().hideHealth)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("config.not-riding-alert.hideHealth.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().hideHealth = newValue)
            .build());

    general.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.onlyAutograbbing"),
                ModConfig.getInstance().onlyAutograbbing)
            .setDefaultValue(false)
            .setTooltip(Component.translatable("config.not-riding-alert.onlyAutograbbing.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().onlyAutograbbing = newValue)
            .build());

    ConfigCategory tracker =
        builder.getOrCreateCategory(
            Component.translatable("config.not-riding-alert.category.rides"));

    tracker.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("config.not-riding-alert.autograb"),
                ModConfig.getInstance().autograb)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("config.not-riding-alert.autograb.tooltip"))
            .setSaveConsumer(newValue -> ModConfig.getInstance().autograb = newValue)
            .build());

    tracker.addEntry(
        entryBuilder
            .startIntSlider(
                Component.translatable("config.not-riding-alert.rideDisplayCount"),
                ModConfig.getInstance().rideDisplayCount,
                1,
                60)
            .setDefaultValue(10)
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

    ConfigCategory rides =
        builder.getOrCreateCategory(
            Component.translatable("config.not-riding-alert.category.rideDisplay"));

    for (RideName ride : RideName.values()) {
      if (ride == RideName.UNKNOWN) {
        continue;
      }
      boolean currentValue = !ModConfig.getInstance().hiddenRides.contains(ride.getDisplayName());
      rides.addEntry(
          entryBuilder
              .startBooleanToggle(formatRideLabel(ride), currentValue)
              .setDefaultValue(true)
              .setTooltip(Component.translatable("config.not-riding-alert.rideDisplay.tooltip"))
              .setSaveConsumer(
                  newValue -> {
                    if (!newValue) {
                      ModConfig.getInstance().hiddenRides.add(ride.getDisplayName());
                    } else {
                      ModConfig.getInstance().hiddenRides.remove(ride.getDisplayName());
                    }
                  })
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

      if (ModConfig.getInstance().hiddenRides.contains(ride.getDisplayName())) {
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
