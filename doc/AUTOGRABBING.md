# Autograbbing System

## Overview

The Autograbbing System automatically detects when players enter ride waiting areas and prepares the game for the ride experience. It allows players to multitask by automatically releasing the mouse cursor and marking them as "ready to ride" when entering predefined ride regions.

## Key Components

### Region Management
- **RegionHolder**: Central registry of ride regions and their locations
  - Maps 3D coordinates to specific rides
  - Determines which ride (if any) the player is currently in
  - Maintains autograb capability for each ride
  - Handles all supported rides across Disneyland Park, California Adventure, and Retro

### Core Handler
- **AutograbFailureHandler**: Manages autograb state and failure detection
  - Tracks when player enters autograb regions
  - Monitors timeout periods for rides not starting
  - Triggers failure alerts when rides don't start within expected time
  - Uses different timeouts for different ride types (longer for Disneyland Railroad)

### Cursor Management
Located in **NotRidingAlertClient.handleCursorManagement()**:
- Automatically releases mouse cursor when entering autograb region
- Tracks previous region to avoid repeated cursor releases
- Provides defocus cursor option for all rides (not just autograb regions)

## Supported Rides

### Disneyland Park
- Alice in Wonderland, Big Thunder Mountain Railroad, Casey Jr. Circus Train
- Chip 'n' Dale's GADGETcoaster, Disneyland Monorail, Disneyland Railroad
- Finding Nemo Submarine Voyage, Haunted Mansion, Indiana Jones™ Adventure
- Jungle Cruise, Matterhorn Bobsleds, Mickey & Friends Parking Tram
- Mr Toad's Wild Ride, Peter Pan's Flight, Pinocchio's Daring Journey
- Pirates of the Caribbean, Roger Rabbit's Car Toon Spin, Snow White's Enchanted Wish
- Space Mountain, Splash Mountain, Star Wars: Rise of the Resistance
- Storybook Land Canal Boats, The Many Adventures of Winnie the Pooh

### Disney California Adventure
- Goofy's Sky School, Grizzly River Run, Guardians of the Galaxy - Mission: BREAKOUT!
- Incredicoaster, Monsters, Inc. Mike & Sulley to the Rescue!
- Radiator Springs Racers, The Little Mermaid - Ariel's Undersea Adventure

### Retro
- The Twilight Zone Tower of Terror

## Autograb Workflow

1. **Region Entry Detection**
   - Player enters a predefined ride region
   - RegionHolder identifies the specific ride
   - System marks player as "in autograb region"

2. **Automatic Actions**
   - Mouse cursor is released (if it was grabbed)
   - System tracks the specific ride being waited for
   - Player status shows "Autograbbing..." in Strategy HUD

3. **State Tracking**
   - Monitors if player becomes a passenger (ride starts)
   - Tracks time spent in region without riding
   - Handles region transitions between different rides

4. **Failure Detection**
   - Triggers if ride doesn't start within timeout period:
     - Standard timeout: 30 seconds (600 ticks)
     - Extended timeout for Disneyland Railroad: 60 seconds (1200 ticks)
   - Plays alert sound if player is idle during failure

5. **Exit Conditions**
   - Player leaves the region
   - Player successfully boards the ride (becomes passenger)
   - Region changes to a different ride

## Configuration Options

- `autograb`: Master toggle for autograbbing feature
- `alertAutograbFailure`: Toggle for failure alerts
- `defocusCursor`: Cursor management for all rides (not just autograb)

## Integration Points

The Autograbbing System integrates with:
- Alert System (for failure notifications)
- Strategy HUD (for status display)
- Configuration System (for feature toggles)
- Mixins (for player location tracking)

## Special Cases

- **Disneyland Railroad**: Uses extended timeout due to longer wait times
- **Region transitions**: Properly handles moving between adjacent ride regions
- **Cursor management**: Distinguishes between autograb cursor release and general defocus setting