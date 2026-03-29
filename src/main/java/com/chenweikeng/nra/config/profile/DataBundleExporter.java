package com.chenweikeng.nra.config.profile;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.chenweikeng.nra.config.ConfigSetting;
import com.chenweikeng.nra.config.ModConfig;
import com.chenweikeng.nra.report.DailyRideSnapshot;
import com.chenweikeng.nra.ride.RideCountManager;
import com.chenweikeng.nra.ride.RideName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class DataBundleExporter {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private DataBundleExporter() {}

  public static class ExportBundle {
    public int version = 1;
    public long exportedAt;
    public ConfigSetting settings;
    public Map<String, DailyRideSnapshot.SnapshotEntry> rideSnapshots;
    public Map<String, Integer> rideCounts;
  }

  public static ExportBundle createBundle() {
    ExportBundle bundle = new ExportBundle();
    bundle.exportedAt = System.currentTimeMillis();
    bundle.settings = ModConfig.currentSetting.copy();

    bundle.rideSnapshots = DailyRideSnapshot.getInstance().getAllSnapshots();

    RideCountManager countManager = RideCountManager.getInstance();
    Map<RideName, Integer> allCounts = countManager.getAllRideCounts();
    bundle.rideCounts = new HashMap<>();
    for (Map.Entry<RideName, Integer> entry : allCounts.entrySet()) {
      if (entry.getKey() != RideName.UNKNOWN) {
        bundle.rideCounts.put(entry.getKey().toMatchString(), entry.getValue());
      }
    }

    return bundle;
  }

  public static void exportToFile(Path filePath) throws Exception {
    ExportBundle bundle = createBundle();
    try (FileWriter writer = new FileWriter(filePath.toFile())) {
      GSON.toJson(bundle, writer);
    }
  }

  public static ImportResult importFromFile(Path filePath) {
    try {
      ExportBundle bundle;
      try (FileReader reader = new FileReader(filePath.toFile())) {
        bundle = GSON.fromJson(reader, ExportBundle.class);
      }

      if (bundle == null) {
        return new ImportResult(false, "Empty or invalid file");
      }
      if (bundle.version != 1) {
        return new ImportResult(false, "Unsupported format version: " + bundle.version);
      }
      if (bundle.settings == null) {
        return new ImportResult(false, "No settings found in file");
      }

      // Apply settings
      ModConfig.currentSetting = bundle.settings.copy();
      ModConfig.save();

      // Merge ride snapshots
      int snapshotCount = 0;
      if (bundle.rideSnapshots != null) {
        snapshotCount = bundle.rideSnapshots.size();
        DailyRideSnapshot.getInstance().mergeSnapshots(bundle.rideSnapshots);
      }

      // Merge cumulative ride counts
      int rideCountUpdates = 0;
      if (bundle.rideCounts != null) {
        RideCountManager countManager = RideCountManager.getInstance();
        for (Map.Entry<String, Integer> entry : bundle.rideCounts.entrySet()) {
          RideName ride = RideName.fromMatchString(entry.getKey());
          if (ride != RideName.UNKNOWN) {
            int before = countManager.getRideCount(ride);
            countManager.importRideCount(ride, entry.getValue());
            if (countManager.getRideCount(ride) > before) {
              rideCountUpdates++;
            }
          }
        }
        countManager.forceSave();
      }

      return new ImportResult(
          true,
          "Imported settings, "
              + snapshotCount
              + " day(s) of history, "
              + rideCountUpdates
              + " ride count update(s)");

    } catch (Exception e) {
      NotRidingAlertClient.LOGGER.error("Failed to import data bundle", e);
      return new ImportResult(false, "Import failed: " + e.getMessage());
    }
  }

  /** Opens a native macOS save dialog on a background thread. Returns null path on cancel. */
  public static CompletableFuture<Path> pickExportFile() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            String defaultName = "nra-export-" + LocalDate.now() + ".json";
            Process process =
                new ProcessBuilder(
                        "osascript",
                        "-e",
                        "POSIX path of (choose file name with prompt \"Export NRA Data\""
                            + " default name \""
                            + defaultName
                            + "\")")
                    .redirectErrorStream(true)
                    .start();
            String output = new String(process.getInputStream().readAllBytes()).trim();
            int exitCode = process.waitFor();
            if (exitCode != 0 || output.isEmpty()) return null;
            Path path = Path.of(output);
            if (!output.endsWith(".json")) {
              path = Path.of(output + ".json");
            }
            return path;
          } catch (Exception e) {
            NotRidingAlertClient.LOGGER.warn("Failed to open save dialog", e);
            return null;
          }
        });
  }

  /** Opens a native macOS open dialog on a background thread. Returns null path on cancel. */
  public static CompletableFuture<Path> pickImportFile() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Process process =
                new ProcessBuilder(
                        "osascript",
                        "-e",
                        "POSIX path of (choose file of type {\"public.json\"}"
                            + " with prompt \"Import NRA Data\")")
                    .redirectErrorStream(true)
                    .start();
            String output = new String(process.getInputStream().readAllBytes()).trim();
            int exitCode = process.waitFor();
            if (exitCode != 0 || output.isEmpty()) return null;
            return Path.of(output);
          } catch (Exception e) {
            NotRidingAlertClient.LOGGER.warn("Failed to open file dialog", e);
            return null;
          }
        });
  }

  public record ImportResult(boolean success, String message) {}
}
