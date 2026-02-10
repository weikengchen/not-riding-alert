package com.chenweikeng.nra.ride;

import net.minecraft.client.player.LocalPlayer;

public enum RegionHolder {
  ALICE_IN_WONDERLAND(
      RideName.ALICE_IN_WONDERLAND,
      new Point(-115.33, 685.67),
      64,
      new Point(-116, 682),
      new Point(-110, 685),
      new Point(-120, 690)),
  BIG_THUNDER_MOUNTAIN_RAILROAD(
      RideName.BIG_THUNDER_MOUNTAIN_RAILROAD,
      new Point(265.25, 543.74),
      65,
      new Point(254, 541),
      new Point(255, 539),
      new Point(257, 537),
      new Point(259, 537),
      new Point(268, 541),
      new Point(275, 545),
      new Point(276, 546),
      new Point(276, 547),
      new Point(274, 550),
      new Point(272, 551),
      new Point(260, 545),
      new Point(255, 542)),
  CASEY_JR_CIRCUS_TRAIN(
      RideName.CASEY_JR_CIRCUS_TRAIN,
      new Point(51.57, 797.71),
      66,
      new Point(46, 798),
      new Point(55, 794),
      new Point(56, 794),
      new Point(57, 795),
      new Point(58, 797),
      new Point(51, 800),
      new Point(46, 802)),
  DISNEYLAND_MONORAIL(
      RideName.DISNEYLAND_MONORAIL,
      new Point(-395.80, 560.22),
      70,
      new Point(-423, 571),
      new Point(-422, 570),
      new Point(-413, 565),
      new Point(-394, 555),
      new Point(-376, 546),
      new Point(-374, 546),
      new Point(-372, 547),
      new Point(-369, 550),
      new Point(-369, 551),
      new Point(-395, 564),
      new Point(-412, 572),
      new Point(-421, 576),
      new Point(-423, 572)),
  DISNEYLAND_RAILROAD(
      RideName.DISNEYLAND_RAILROAD,
      new Point(20.00, -54.50),
      70,
      new Point(-23, -58),
      new Point(63, -58),
      new Point(63, -51),
      new Point(-23, -51)),
  FINDING_NEMO_SUBMARINE_VOYAGE(
      RideName.FINDING_NEMO_SUBMARINE_VOYAGE,
      new Point(-374.65, 565.45),
      64,
      new Point(-379, 561),
      new Point(-379, 561),
      new Point(-369, 562),
      new Point(-370, 568),
      new Point(-379, 571),
      new Point(-379, 571),
      new Point(-379, 570),
      new Point(-379, 563)),
  JUNGLE_CRUISE(
      RideName.JUNGLE_CRUISE,
      new Point(211.22, 239.31),
      64,
      new Point(208, 238),
      new Point(212, 236),
      new Point(214, 238),
      new Point(215, 240),
      new Point(215, 241),
      new Point(208, 242)),
  MICKEY_AND_FRIENDS_PARKING_TRAM(
      RideName.MICKEY_AND_FRIENDS_PARKING_TRAM,
      new Point(1143.48, 752.25),
      64,
      new Point(1138, 726),
      new Point(1149, 726),
      new Point(1149, 778),
      new Point(1138, 779)),
  ROGER_RABBITS_CAR_TOON_SPIN(
      RideName.ROGER_RABBITS_CAR_TOON_SPIN,
      new Point(-172.81, 1084.51),
      64,
      new Point(-175, 1082),
      new Point(-174, 1081),
      new Point(-170, 1084),
      new Point(-170, 1086),
      new Point(-171, 1088),
      new Point(-176, 1085)),
  SPACE_MOUNTAIN(
      RideName.SPACE_MOUNTAIN,
      new Point(-257.00, 198.50),
      60,
      new Point(-263, 197),
      new Point(-251, 197),
      new Point(-251, 200),
      new Point(-263, 200)),
  HAUNTED_MANSION(
      RideName.HAUNTED_MANSION,
      new Point(606.33, 289.67),
      58,
      new Point(605, 287),
      new Point(607, 290),
      new Point(607, 292)),
  INDIANA_JONES_ADVENTURE(
      RideName.INDIANA_JONES_ADVENTURE,
      new Point(447.00, -45.50),
      64,
      new Point(445, -45),
      new Point(449, -45),
      new Point(449, -46),
      new Point(445, -46)),
  MATTERHORN_BOBSLEDS_LEFT(
      RideName.MATTERHORN_BOBSLEDS,
      new Point(-226.74, 706.91),
      64,
      new Point(-232, 704),
      new Point(-227, 704),
      new Point(-226, 704),
      new Point(-221, 705),
      new Point(-222, 705),
      new Point(-222, 710),
      new Point(-227, 710),
      new Point(-231, 709),
      new Point(-232, 705)),
  MATTERHORN_BOBSLEDS_RIGHT(
      RideName.MATTERHORN_BOBSLEDS,
      new Point(-191.67, 707.73),
      64,
      new Point(-188, 704),
      new Point(-188, 703),
      new Point(-188, 708),
      new Point(-193, 710),
      new Point(-198, 710)),
  MR_TOADS_WILD_RIDE(
      RideName.MR_TOADS_WILD_RIDE,
      new Point(-49.00, 692.50),
      64,
      new Point(-50, 692),
      new Point(-48, 692),
      new Point(-48, 693),
      new Point(-50, 693)),
  STORYBOOK_LAND_CANAL_BOATS(
      RideName.STORYBOOK_LAND_CANAL_BOATS,
      new Point(-113.00, 779.00),
      64,
      new Point(-116, 777),
      new Point(-110, 777),
      new Point(-110, 781),
      new Point(-116, 781)),
  PETER_PANS_FLIGHT(
      RideName.PETER_PANS_FLIGHT,
      new Point(-20.50, 623.50),
      65,
      new Point(-21, 623),
      new Point(-20, 623),
      new Point(-20, 624),
      new Point(-21, 624)),
  PINOCCHIOS_DARING_JOURNEY(
      RideName.PINOCCHIOS_DARING_JOURNEY,
      new Point(45.50, 667.50),
      65,
      new Point(44, 666),
      new Point(47, 666),
      new Point(47, 669),
      new Point(44, 669)),
  THE_LITTLE_MERMAID_ARIELS_UNDERSEA_ADVENTURE(
      RideName.THE_LITTLE_MERMAID_ARIELS_UNDERSEA_ADVENTURE,
      new Point(423.50, -804.00),
      65,
      new Point(422, -805),
      new Point(425, -805),
      new Point(425, -803),
      new Point(422, -803)),
  THE_MANY_ADVENTURES_OF_WINNIE_THE_POOH(
      RideName.THE_MANY_ADVENTURES_OF_WINNIE_THE_POOH,
      new Point(721.02, 483.22),
      64,
      new Point(723, 482),
      new Point(722, 485),
      new Point(721, 485),
      new Point(719, 483),
      new Point(719, 482)),
  RADIATOR_SPRINGS_RACERS(
      RideName.RADIATOR_SPRINGS_RACERS,
      new Point(25.34, -1108.52),
      64,
      new Point(16, -1111),
      new Point(30, -1112),
      new Point(35, -1112),
      new Point(35, -1107),
      new Point(32, -1106),
      new Point(24, -1105),
      new Point(16, -1105)),
  INCREDICOASTER(
      RideName.INCREDICOASTER,
      new Point(268.80, -1118.29),
      63,
      new Point(264, -1115),
      new Point(265, -1119),
      new Point(267, -1126),
      new Point(268, -1127),
      new Point(274, -1127),
      new Point(274, -1124),
      new Point(273, -1120),
      new Point(270, -1109),
      new Point(268, -1109),
      new Point(264, -1110)),
  SPLASH_MOUNTAIN(
      RideName.SPLASH_MOUNTAIN,
      new Point(668.83, 415.63),
      60,
      new Point(666, 413),
      new Point(670, 413),
      new Point(672, 415),
      new Point(672, 418),
      new Point(666, 418));

  private final RideName ride;
  private final Point center;
  private final double y;
  private final Point[] points;

  RegionHolder(RideName ride, Point center, double y, Point... points) {
    this.ride = ride;
    this.center = center;
    this.y = y;
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

  public Point getCenter() {
    return center;
  }

  public double getY() {
    return y;
  }

  public Point[] getPoints() {
    return points;
  }

  public boolean contains(double x, double y, double z) {
    if (Math.abs(y - this.y) > 3) {
      return false;
    }
    double dx = x - center.x;
    double dz = z - center.z;
    double dy = y - this.y;
    if (Math.sqrt(dx * dx + dz * dz + dy * dy) > 100) {
      return false;
    }
    return pointInPolygon(x, z, points);
  }

  public static RideName getRideAtLocation(double x, double y, double z) {
    for (RegionHolder region : values()) {
      if (region.contains(x, y, z)) {
        return region.ride;
      }
    }
    return null;
  }

  public static RideName getRideAtLocation(LocalPlayer player) {
    if (player == null) {
      return null;
    }
    return getRideAtLocation(player.getX(), player.getY(), player.getZ());
  }

  public static boolean hasAutograb(RideName ride) {
    return AUTOGRAB_RIDES.contains(ride);
  }

  private static final java.util.Set<RideName> AUTOGRAB_RIDES = new java.util.HashSet<>();

  static {
    for (RegionHolder region : values()) {
      AUTOGRAB_RIDES.add(region.ride);
    }
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
