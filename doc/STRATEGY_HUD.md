# Strategy HUD System

## Overview

The Strategy HUD (Heads-Up Display) provides real-time recommendations for which rides to focus on based on the player's current progress toward ride count goals. It displays the top rides that will help reach the next milestones most efficiently.

## Key Components

### Renderer
- **StrategyHudRenderer**: Main rendering class
  - Draws the Strategy HUD overlay
  - Handles layout (single/double column based on ride count)
  - Manages text formatting and positioning
  - Integrates with Minecraft's HUD rendering system

### Calculator
- **StrategyCalculator**: Core logic for ride recommendations
  - Calculates time needed to reach next goals for each ride
  - Sorts rides by time required (easiest first)
  - Applies user filters (ride time, hidden rides, autograbbing only)
  - Provides individual ride goals for current ride display

### Goal Tracking
- **RideGoal**: Data structure representing a ride goal
  - Contains ride name, current count, next goal, rides needed
  - Calculates time estimate based on ride duration
  - Used for sorting and display in the HUD

## Display Features

### Layout
- **Single Column**: 1-7 rides shown in one column
- **Double Column**: 8+ rides split into two columns for better visibility
- **Configurable Count**: User can set 1-16 rides to display

### Visual Elements
- **Current Ride Highlighting**: Shows in green with progress percentage
- **Autograbbing Status**: Displays "(Autograbbing...)" when waiting
- **Goal Information**: Shows rides needed to reach next milestone
- **Progress Indicators**: Current count and next goal displayed

### Text Formatting
- **Short Names Option**: Abbreviated ride names for cleaner display
- **Color Coding**: Green for current ride, normal for others
- **Error Messages**: Display of ride time warnings and other issues

## Calculation Logic

### Goal System
Supported milestones for each ride:
- 1, 10, 100, 500, 1000, 5000, 10000 rides

### Algorithm Flow
1. **Filter Rides** based on user settings
   - Remove hidden rides (configured by user)
   - Apply minimum ride time filter
   - Filter to autograbbing-only rides if enabled
   - Skip unknown rides and invalid ride times

2. **Calculate Goals** for each remaining ride
   - Determine next goal based on current count
   - Calculate rides needed to reach that goal
   - Estimate time using known ride durations

3. **Sort Results** by time needed (easiest first)
   - Prioritize rides requiring less total time
   - Accounts for both ride count needed and ride duration

4. **Select Top N** rides for display
   - Default: 16 rides
   - Configurable 1-16 via settings

### Time Calculations
- **Ride Duration**: Based on predefined ride times
- **Total Time**: (rides needed) × (ride duration in seconds)
- **Invalid Times**: Rides with 99999+ seconds are excluded

## Integration Points

### Registration
- Registered via `HudElementRegistry.attachElementBefore()`
- Renders just before the chat HUD element
- Triggered every client tick

### Data Sources
- **RideCountManager**: Provides current ride statistics
- **CurrentRideHolder**: Supplies current ride and progress
- **RegionHolder**: Identifies autograbbing status
- **ModConfig**: Supplies display preferences and filters

## Configuration Options

### Display Settings
- `rideDisplayCount`: Number of rides to show (1-16)
- `displayShortName`: Toggle for abbreviated ride names
- `onlyAutograbbing`: Filter to only autograbbing-supported rides

### Filtering
- `minRideTimeMinutes`: Exclude rides shorter than X minutes
- `hiddenRides`: User-configured list of rides to hide

## Special Cases

### Current Ride Display
- Always shows the current ride even if filtered out
- Displays progress percentage in green
- Shows "Autograbbing..." status when applicable
- Uses different calculation path (applies no filters)

### Error Handling
- Displays warning for rides without valid time data
- Continues operation with incomplete data
- Shows meaningful messages for missing ride times

### Lincoln Show
- Special handling for Great Moments with Mr. Lincoln
- Integrated with suppression system
- Affects display during show times

## Performance Considerations

- **Recalculation**: Triggered when ride counts change
- **Lazy Evaluation**: Only recalculates when needed
- **Efficient Sorting**: Single pass through all rides
- **Minimal Overhead**: HUD rendering optimized for frequent updates