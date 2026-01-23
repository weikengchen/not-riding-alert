package com.chenweikeng.nra.ride;

import java.util.HashMap;
import java.util.Map;

public enum RideName {
    // Disneyland rides
    ALICE_IN_WONDERLAND("Alice in Wonderland", 206),
    ASTRO_ORBITOR("Astro Orbitor", 88),
    AUTOPIA("Autopia", 246),
    BIG_THUNDER_MOUNTAIN_RAILROAD("Big Thunder Mountain Railroad", 201),
    BUZZ_LIGHTYEAR_ASTRO_BLASTERS("Buzz Lightyear Astro Blasters", 258),
    CASEY_JR_CIRCUS_TRAIN("Casey Jr. Circus Train", 152),
    CHIP_N_DALES_GADGET_COASTER("Chip 'N' Dale's Gadget Coaster", 44),
    DAVY_CROCKETTS_EXPLORER_CANOES("Davy Crockett's Explorer Canoes", 150),
    DISNEYLAND_MONORAIL("Disneyland Monorail", 485),
    DISNEYLAND_RAILROAD("Disneyland Railroad", 1025),
    DUMBO_THE_FLYING_ELEPHANT("Dumbo the Flying Elephant", 105),
    ENCHANTED_TIKI_ROOM("Enchanted Tiki Room", 750),
    FINDING_NEMO_SUBMARINE_VOYAGE("Finding Nemo Submarine Voyage", 790),
    GREAT_MOMENTS_WITH_MR_LINCOLN("Great Moments with Mr. Lincoln", 372),
    HAUNTED_MANSION("Haunted Mansion", 445),
    INDIANA_JONES_ADVENTURE("Indiana Jones Adventure", 225),
    JUNGLE_CRUISE("Jungle Cruise", 431),
    KING_ARTHUR_CARROUSEL("King Arthur Carrousel", 130),
    MAD_TEA_PARTY("Mad Tea Party", 90),
    MAIN_STREET_CARRIAGES("Main Street Carriages", 381),
    MATTERHORN_BOBSLEDS("Matterhorn Bobsleds", 120),
    MICKEY_AND_FRIENDS_PARKING_TRAM("Mickey & Friends Parking Tram", 550),
    MR_TOADS_WILD_RIDE("Mr Toad's Wild Ride", 109),
    PEOPLEMOVER("Peoplemover", 695),
    PETER_PANS_FLIGHT("Peter Pan's Flight", 124),
    PINOCCHIOS_DARING_JOURNEY("Pinocchio's Daring Journey", 168),
    PIRATES_OF_THE_CARIBBEAN("Pirates of the Caribbean", 841),
    ROGER_RABBITS_CAR_TOON_SPIN("Roger Rabbit's Car-Toon Spin", 168),
    SNOW_WHITES_ENCHANTED_WISH("Snow White's Enchanted Wish", 122),
    SPACE_MOUNTAIN("Space Mountain", 180),
    SPLASH_MOUNTAIN("Splash Mountain", 484),
    STAR_WARS_RISE_OF_THE_RESISTANCE("Star Wars: Rise of the Resistance", 294),
    STORYBOOK_LAND_CANAL_BOATS("Storybook Land Canal Boats", 303),
    THE_MANY_ADVENTURES_OF_WINNIE_THE_POOH("The Many Adventures of Winnie the Pooh", 200),
    TOM_SAWYER_ISLAND_RAFTS("Tom Sawyer Island Rafts", 129),
    
    // DCA rides
    GOLDEN_ZEPHYR("Golden Zephyr", 108),
    GOOFYS_SKY_SCHOOL("Goofy's Sky School", 91),
    GRIZZLY_RIVER_RUN("Grizzly River Run", 361),
    GUARDIANS_OF_THE_GALAXY_MISSION_BREAKOUT("Guardians of the Galaxy: Mission Breakout", 123),
    INCREDICOASTER("Incredicoaster", 135),
    INSIDE_OUT_EMOTIONAL_WHIRLWIND("Inside Out Emotional Whirlwind", 98),
    JESSIES_CRITTER_CAROUSEL("Jessie's Critter Carousel", 120),
    JUMPIN_JELLYFISH("Jumpin' Jellyfish", 45),
    LUIGIS_ROLICKIN_ROADSTERS("Luigi's Rollickin' Roadsters", 94),
    MATERS_JUNKYARD_JAMBOREE("Mater's Junkyard Jamboree", 90),
    MONSTERS_INC_MIKE_AND_SULLEY_TO_THE_RESCUE("Monster's Inc. Mike & Sulley to the Rescue!", 230),
    PIXAR_PAL_AROUND("Pixar Pal-A-Round", 547),
    RADIATOR_SPRINGS_RACERS("Radiator Springs Racers", 267),
    RED_CAR_TROLLEY("Red Car Trolley", 307),
    SILLY_SYMPHONY_SWINGS("Silly Symphony Swings", 87),
    THE_LITTLE_MERMAID_ARIELS_UNDERSEA_ADVENTURE("The Little Mermaid: Ariel's Undersea Adventure", 369),

    // Retro
    FLIKS_FLYERS("Flik's Flyers", 98),
    HEIMLICHS_CHEW_CHEW_TRAIN("Heimlich's Chew Chew Train", 102),
    THE_TWILIGHT_ZONE_TOWER_OF_TERROR("The Twilight Zone Tower of Terror", 139),

    // Seasonal
    HAUNTED_MANSION_HOLIDAY("Haunted Mansion Holiday", 445),
    GUARDIANS_OF_THE_GALAXY_MONSTERS_AFTER_DARK("Guardians of the Galaxy: Monsters After Dark", 123),
    THE_SUGARPINE_EXPRESS("The Sugarpine Express", 132),
    THE_SUGARPINE_MERRY_GO_ROUND("The Sugarpine Merry-Go-Round", 76),
    HYPERSPACE_MOUNTAIN("Hyperspace Mountain", 180),
    
    // Unknown ride (for rides not in the enum)
    UNKNOWN("Unknown", 99999);

    private final String displayName;
    private final int rideTime; // Ride time in seconds
    private static final Map<String, RideName> BY_DISPLAY_NAME = new HashMap<>();

    static {
        for (RideName ride : values()) {
            BY_DISPLAY_NAME.put(ride.displayName, ride);
        }
    }

    RideName(String displayName, int rideTime) {
        this.displayName = displayName;
        this.rideTime = rideTime;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the ride time in seconds.
     */
    public int getRideTime() {
        return rideTime;
    }

    /**
     * Maps a string (display name) to a RideName enum.
     * Returns UNKNOWN if the string doesn't match any known ride.
     */
    public static RideName fromString(String displayName) {
        return BY_DISPLAY_NAME.getOrDefault(displayName, UNKNOWN);
    }

    /**
     * Maps a RideName enum back to its string representation.
     */
    public String toString() {
        return displayName;
    }
    
    /**
     * Checks if this ride is a seasonal ride.
     */
    public boolean isSeasonal() {
        return this == HAUNTED_MANSION_HOLIDAY ||
               this == GUARDIANS_OF_THE_GALAXY_MONSTERS_AFTER_DARK ||
               this == THE_SUGARPINE_EXPRESS ||
               this == THE_SUGARPINE_MERRY_GO_ROUND ||
               this == HYPERSPACE_MOUNTAIN;
    }
    
    /**
     * Matches a truncated ride name (e.g. "Mr Toad's Wild R" or "Mr Toad's Wild R...")
     * to the closest RideName. Handles "..." suffix and prefix like "⏐ ".
     * Returns UNKNOWN if no match.
     */
    public static RideName fromTruncatedString(String s) {
        if (s == null) return UNKNOWN;
        String cleaned = s.trim();
        if (cleaned.isEmpty()) return UNKNOWN;
        // Remove common prefixes
        if (cleaned.startsWith("⏐ ")) cleaned = cleaned.substring(2).trim();
        // Remove leading pipe (handle both ASCII | and Unicode full-width |)
        if (cleaned.startsWith("| ")) cleaned = cleaned.substring(2).trim();
        // Remove "..." suffix (truncation)
        if (cleaned.endsWith("...")) cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        // Exact match first
        cleaned = cleaned.trim();
        RideName exact = fromString(cleaned);
        if (exact != UNKNOWN) return exact;
        // Find displayName that starts with cleaned (truncated match)
        RideName best = UNKNOWN;
        int bestLen = Integer.MAX_VALUE;
        for (RideName r : values()) {
            if (r == UNKNOWN) continue;
            if (r.displayName.startsWith(cleaned) && r.displayName.length() < bestLen) {
                best = r;
                bestLen = r.displayName.length();
            }
        }
        // Special case for Rise of the Resistance (sidebar shows "Rise of the Resistance" without "Star Wars:")
        if (best == UNKNOWN && "Rise of the Resistance".startsWith(cleaned)) {
            return STAR_WARS_RISE_OF_THE_RESISTANCE;
        }
        // Special case for Tower of Terror (sidebar shows "Tower of Terror" without "The Twilight Zone:")
        if (best == UNKNOWN && "Tower of Terror".startsWith(cleaned)) {
            return THE_TWILIGHT_ZONE_TOWER_OF_TERROR;
        }
        return best;
    }

    /**
     * Gets all ride display names for autocomplete.
     */
    public static java.util.List<String> getAllDisplayNames() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (RideName ride : values()) {
            if (ride != UNKNOWN) {
                names.add(ride.displayName);
            }
        }
        return names;
    }
}
