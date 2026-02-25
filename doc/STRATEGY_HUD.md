# Strategy HUD System

## Overview

The Strategy HUD (Heads-Up Display) provides real-time recommendations for which rides to focus on based on the player's current progress toward ride count goals. It displays the top rides that will help reach the next milestones most efficiently.

## Key Components

### Renderer Versions

The mod offers three different HUD renderer styles:

| Version | Description |
|---------|-------------|
| **V2** (default) | Modern animated layout with smooth transitions, collapsing/expanding animations, and state-based display (full, collapsed, waiting) |
| **V1** | Two-column layout anchored to the top-left corner |
| **V0** | Original classic layout centered on screen |

### Dispatcher
- **StrategyHudRendererDispatcher**: Routes rendering to the appropriate version
  - Reads `strategyHudRendererVersion` from ModConfig
  - Delegates to the correct renderer implementation
  - Provides unified interface for HUD rendering

### Renderers
- **StrategyHudRendererV2**: Modern animated renderer (default)
  - Smooth transitions between states
  - Collapsing/expanding animations
  - Three display states: full, collapsed, waiting

- **StrategyHudRendererV1**: Two-column top-left renderer
  - Anchored to top-left corner of screen
  - Two-column layout with left/right split
  - Static positioning

- **StrategyHudRendererV0**: Original centered renderer
  - Centered on screen
  - Classic layout with offset positioning
  - Simple and stable

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
- **Dynamic Columns**: Automatically calculates optimal column layout (1-8 columns) based on content width and screen size
- **Row Minimization**: Prefers fewer columns when row count is the same to reduce wasted horizontal space
- **Horizontal Centering**: All content is automatically centered on screen (V0, V2)
- **Configurable Count**: User can set 1-60 rides to display

### Visual Elements
- **Current Ride Highlighting**: Shows with Riding color with progress percentage
- **Autograbbing Status**: Displays "(Autograbbing...)" when waiting
- **Goal Information**: Shows rides needed to reach next milestone
- **Progress Indicators**: Current count and next goal displayed

### Text Formatting
- **Short Names Option**: Abbreviated ride names for cleaner display
- **Color Coding**: Uses configurable tracker colors
- **Error Messages**: Display of ride time warnings and other issues

### Customizable Tracker Colors
All HUD versions support customizable colors for different states:
- **Normal Color**: Default color for ride entries
- **Autograbbing Color**: Color when waiting to be picked up by a ride
- **Riding Color**: Color for the currently active ride
- **Error Color**: Color for error messages

Colors are stored as ARGB integers in the configuration.

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
   - Configurable 1-60 via settings

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
- **ModConfig**: Supplies display preferences, filters, and renderer version

## Configuration Options

### Display Settings
- `strategyHudRendererVersion`: Choose between V0, V1, and V2 renderer styles
- `rideDisplayCount`: Number of rides to show (1-60)
- `displayShortName`: Toggle for abbreviated ride names
- `onlyAutograbbing`: Filter to only autograbbing-supported rides
- `hudBackgroundOpacity`: Background opacity (0-100%, default: 80%)

### Filtering
- `minRideTimeMinutes`: Exclude rides shorter than X minutes
- `hiddenRides`: User-configured list of rides to hide

### Colors
- `trackerNormalColor`: Default text color (ARGB)
- `trackerAutograbbingColor`: Color for autograbbing state (ARGB)
- `trackerRidingColor`: Color for current ride (ARGB)
- `trackerErrorColor`: Color for error messages (ARGB)

## Special Cases

### Current Ride Display
- Always shows the current ride even if filtered out
- Displays progress percentage in Riding color
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
