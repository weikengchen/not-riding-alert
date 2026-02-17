# Ride Tracking System

## Overview

The Ride Tracking System manages all aspects of ride data including counting completed rides, tracking current progress, and maintaining persistent storage of ride statistics. This data feeds into the strategy calculations and progress display features.

## Key Components

### Ride Identification
- **RideName**: Enum defining all supported rides
  - Contains display names, match strings, and ride duration
  - Provides conversion methods for serialization
  - Handles unknown/unrecognized rides
  - Maps internal ride names to user-friendly display names

### State Holders
- **CurrentRideHolder**: Tracks the ride player is currently on
  - Stores current ride name and progress percentage
  - Updated by mixins based on scoreboard data
  - Provides null checks and state queries
  - Critical for progress tracking and hibernation

- **LastRideHolder**: Tracks the most recently completed ride
  - Maintains history of ride completion
  - Used for post-ride logic and state transitions
  - Helps determine ride completion events

### Count Management
- **RideCountManager**: Singleton managing persistent ride counts
  - Tracks number of times each ride has been completed
  - Automatically saves data every 15 seconds
  - Loads from JSON file on startup
  - Provides thread-safe access to ride statistics

  **Key Operations:**
  - `updateRideCount()`: Increments ride count when completed
  - `getRideCount()`: Retrieves current count for a ride
  - `getAllRideCounts()`: Gets all ride statistics
  - `checkAndSaveIfNeeded()`: Periodic save logic
  - `forceSave()`: Immediate save for testing/shutdown

### Data Flow

1. **Ride Detection**
   - Mixins detect ride start based on scoreboard/vehicle state
   - CurrentRideHolder is updated with ride name
   - Progress tracking begins

2. **Progress Updates**
   - Scoreboard data provides completion percentage
   - CurrentRideHolder.progress is updated
   - Strategy HUD displays real-time progress

3. **Completion Detection**
   - Mixins detect ride end (scoreboard clears, passenger state changes)
   - RideCountManager increments the ride count
   - LastRideHolder stores completed ride
   - CurrentRideHolder is cleared

4. **Persistence**
   - Ride counts saved to JSON file every 15 seconds
   - Only saves when counts increase (optimization)
   - File location: `config/not-riding-alert-rides.json`

## Data Storage

### Ride Count File Format
```json
{
  "space_mountain": 42,
  "pirates_of_the_caribbean": 15,
  "haunted_mansion": 8
}
```

### Goals System
The tracking system supports these milestone goals for each ride:
1, 10, 100, 500, 1000, 5000, 10000 rides

These goals are used by:
- Strategy Calculator to determine next objectives
- Progress tracking to show completion status
- Notifications to inform of milestones reached

## Special Cases

### Unsupported Rides
- **Davy Crockett's Explorer Canoes**: Progress tracking not available (player-dependent ride time)
  - Still counts completions
  - No progress percentage display
  - Still included in strategy calculations

### Unknown Rides
- UNKNOWN enum value for unrecognized rides
- Typically filtered out from displays
- Not persisted in ride counts

## Integration Points

The Ride Tracking System integrates with:
- **Strategy System**: Provides data for goal calculations
- **HUD Renderer**: Supplies progress information for display
- **Mixins**: Receives updates from game events
- **Configuration**: Uses settings for filtering and display options
- **Monkeycraft Integration**: Provides data for notifications

## Performance Considerations

- **ConcurrentHashMap**: Thread-safe access to ride counts
- **Periodic Saving**: Only saves when data changes, with 15-second intervals
- **Lazy Loading**: Counts loaded only when needed
- **Incremental Updates**: Only saves increased counts, not decrements

## Error Handling

- **File Corruption**: Creates new file if corrupted/missing
- **Invalid Ride Names**: Maps to UNKNOWN enum safely
- **Progress Calculation**: Handles missing/invalid scoreboard data
- **Save Failures**: Logs errors but continues normal operation