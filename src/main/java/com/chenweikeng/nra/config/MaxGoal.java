package com.chenweikeng.nra.config;

public enum MaxGoal {
  K1(1000),
  K5(5000),
  K10(10000);

  private final int value;

  MaxGoal(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
