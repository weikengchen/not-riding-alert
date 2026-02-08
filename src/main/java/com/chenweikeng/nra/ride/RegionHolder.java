package com.chenweikeng.nra.ride;

import net.minecraft.client.player.LocalPlayer;

public enum RegionHolder {
  ROGER_RABBITS_CAR_TOON_SPIN(
      RideName.ROGER_RABBITS_CAR_TOON_SPIN,
      new Point(-175, 1082),
      new Point(-174, 1081),
      new Point(-170, 1084),
      new Point(-170, 1086),
      new Point(-171, 1088),
      new Point(-176, 1085)),
  SPACE_MOUNTAIN(
      RideName.SPACE_MOUNTAIN,
      new Point(-263, 197),
      new Point(-251, 197),
      new Point(-251, 200),
      new Point(-263, 200)),
  JUNGLE_CRUISE(
      RideName.JUNGLE_CRUISE,
      new Point(208, 238),
      new Point(212, 236),
      new Point(214, 238),
      new Point(215, 240),
      new Point(215, 241),
      new Point(208, 242)),
  DISNEYLAND_RAILROAD(
      RideName.DISNEYLAND_RAILROAD,
      new Point(-23, -58),
      new Point(63, -58),
      new Point(63, -51),
      new Point(-23, -51));

  private final RideName ride;
  private final Point[] points;

  RegionHolder(RideName ride, Point... points) {
    this.ride = ride;
    this.points = ensureCounterClockwise(points);
  }

  private static Point[] ensureCounterClockwise(Point[] points) {
    double area = calculateSignedArea(points);
    if (area < 0) {
      Point[] reversed = new Point[points.length];
      for (int i = 0; i < points.length; i++) {
        reversed[i] = points[points.length - 1 - i];
      }
      return reversed;
    }
    return points;
  }

  private static double calculateSignedArea(Point[] points) {
    double area = 0;
    int n = points.length;
    for (int i = 0; i < n; i++) {
      int j = (i + 1) % n;
      area += (points[j].x - points[i].x) * (points[j].z + points[i].z);
    }
    return area / 2;
  }

  public RideName getRide() {
    return ride;
  }

  public Point[] getPoints() {
    return points;
  }

  public boolean contains(double x, double z) {
    return pointInPolygon(x, z, points);
  }

  public static RideName getRideAtLocation(double x, double z) {
    for (RegionHolder region : values()) {
      if (region.contains(x, z)) {
        return region.ride;
      }
    }
    return null;
  }

  public static RideName getRideAtLocation(LocalPlayer player) {
    if (player == null) {
      return null;
    }
    return getRideAtLocation(player.getX(), player.getZ());
  }

  private static boolean pointInPolygon(double x, double z, Point[] polygon) {
    int n = polygon.length;
    boolean inside = false;
    for (int i = 0, j = n - 1; i < n; j = i++) {
      double xi = polygon[i].x, zi = polygon[i].z;
      double xj = polygon[j].x, zj = polygon[j].z;
      if (((zi > z) != (zj > z)) && (x < (xj - xi) * (z - zi) / (zj - zi) + xi)) {
        inside = !inside;
      }
    }
    return inside;
  }

  public static class Point {
    public final double x;
    public final double z;

    public Point(double x, double z) {
      this.x = x;
      this.z = z;
    }
  }
}
