package com.chenweikeng.nra.wizard;

import com.chenweikeng.nra.wizard.pages.*;
import java.util.List;

public final class TutorialPages {
  private static final List<WizardPage> PAGES =
      List.of(
          new Page1Welcome(),
          new Page2Alert(),
          new Page3Hud(),
          new Page4Autograb(),
          new Page5Config(),
          new Page6Done());

  private TutorialPages() {}

  public static WizardPage getPage(int index) {
    if (index < 0 || index >= PAGES.size()) {
      return null;
    }
    return PAGES.get(index);
  }

  public static int getPageCount() {
    return PAGES.size();
  }

  public static boolean hasPages() {
    return !PAGES.isEmpty();
  }
}
