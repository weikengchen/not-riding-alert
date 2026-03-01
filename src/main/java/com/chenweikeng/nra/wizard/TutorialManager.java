package com.chenweikeng.nra.wizard;

import com.chenweikeng.nra.NotRidingAlertClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class TutorialManager {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Path CONFIG_PATH = Path.of("config/notridingalert_tutorial.json");

  private static TutorialManager instance;

  private TutorialState state = TutorialState.NOT_STARTED;
  private boolean completed = false;

  private TutorialManager() {
    load();
  }

  public static TutorialManager getInstance() {
    if (instance == null) {
      instance = new TutorialManager();
    }
    return instance;
  }

  public boolean shouldStartTutorial() {
    return !completed && state == TutorialState.NOT_STARTED;
  }

  public boolean isTutorialActive() {
    return !completed && state.isActive();
  }

  public boolean isCompleted() {
    return completed;
  }

  public TutorialState getState() {
    return state;
  }

  public int getCurrentPageIndex() {
    return state.getPageIndex();
  }

  public void advanceToNextPage() {
    if (state != TutorialState.FINISHED) {
      state = state.getNext();
      if (state == TutorialState.FINISHED) {
        completed = true;
      }
      save();
    }
  }

  public void goToPage(int pageIndex) {
    TutorialState newState = TutorialState.fromPageIndex(pageIndex);
    if (newState.isActive()) {
      state = newState;
      save();
    }
  }

  public void finishTutorial() {
    state = TutorialState.FINISHED;
    completed = true;
    save();
  }

  public void resetTutorial() {
    state = TutorialState.NOT_STARTED;
    completed = false;
    save();
  }

  public void load() {
    File configFile = CONFIG_PATH.toFile();
    if (!configFile.exists()) {
      return;
    }

    try (FileReader reader = new FileReader(configFile)) {
      TutorialData data = GSON.fromJson(reader, TutorialData.class);
      if (data != null) {
        this.state = data.state != null ? data.state : TutorialState.NOT_STARTED;
        this.completed = data.completed;
      }
    } catch (IOException e) {
      NotRidingAlertClient.LOGGER.warn("Failed to load tutorial state", e);
    }
  }

  public void save() {
    try {
      File configFile = CONFIG_PATH.toFile();
      configFile.getParentFile().mkdirs();

      TutorialData data = new TutorialData();
      data.version = 1;
      data.state = this.state;
      data.completed = this.completed;

      try (FileWriter writer = new FileWriter(configFile)) {
        GSON.toJson(data, writer);
      }
    } catch (IOException e) {
      NotRidingAlertClient.LOGGER.warn("Failed to save tutorial state", e);
    }
  }

  private static class TutorialData {
    int version;
    TutorialState state;
    boolean completed;
  }
}
