# Mixins

## Overview

The Mixins system modifies Minecraft's client code to integrate the Not Riding Alert mod with game events. Mixins are used to intercept game events, modify rendering behavior, and extract data needed for the mod's functionality.

## Mixin Classes

### ChatListenerMixin
**Target**: `ClientPacketListener`  
**Purpose**: Intercepts chat messages to extract ride statistics

- **onGameMessage()**: Captures chat messages containing ride counts
  - Parses messages from `/ridestats` command output
  - Updates ride counts in RideCountManager
  - Filters for ride-related messages only
  - Supports multi-page statistics parsing

### GuiMixin
**Target**: `Gui`  
**Purpose**: Hides UI elements based on configuration

- **render()**: Conditionally renders/hides UI elements
  - Respects `hideScoreboard` setting to hide scoreboard
  - Respects `hideChat` setting to hide chat
  - Respects `hideHealth` setting to hide health bars
  - Applies both player and vehicle health hiding

### ClientLevelMixin
**Target**: `ClientLevel`  
**Purpose**: Tracks time of day for day/night cycle control

- **setTimeOfDay()**: Intercepts time changes
  - Applies fullbright setting when not riding
  - Preserves normal time progression when riding
  - Works with blindness effect for complete visual control

### LivingEntityMixin
**Target**: `LivingEntity`  
**Purpose**: Applies visual effects during rides

- **tick()**: Modifies entity behavior
  - Applies blindness effect when configured
  - Only affects player during rides
  - Preserves normal entity behavior otherwise

### ClientPacketListenerMixin
**Target**: `ClientPacketListener`  
**Purpose**: Extracts scoreboard data for ride tracking

- **onSetScore()**: Captures scoreboard updates
  - Extracts ride progress from scoreboard values
  - Updates CurrentRideHolder with progress percentage
  - Tracks ride start/end based on scoreboard changes
  - Filters for ride-related scoreboard entries

- **onResetScore()**: Handles scoreboard removals
  - Detects ride completion when scores are removed
  - Updates ride state accordingly

### MinecraftMixin
**Target**: `Minecraft`  
**Purpose**: Core game integration and tick management

- **tick()**: Main game tick hook
  - Coordinates with NotRidingAlertClient tick handling
  - Ensures proper initialization order
  - Provides reliable tick timing for all tracking systems

## Mixin Priority and Ordering

Mixins are carefully ordered to ensure:
1. Core game state is established before mod runs
2. Scoreboard data is captured before UI updates
3. Visual effects are applied after ride detection
4. Chat parsing happens after message processing

## Data Extraction

### Scoreboard Parsing
- **Ride Detection**: Current ride identified from scoreboard entries
- **Progress Tracking**: Percentage extracted from score values
- **Completion Detection**: Score removal indicates ride end
- **Stat Updates**: Chat parsing updates ride count data

### Chat Message Processing
- **Pattern Matching**: Identifies ride stat messages
- **Count Extraction**: Parses numerical values from chat
- **Ride Identification**: Maps message content to ride names
- **Multi-page Support**: Handles paginated `/ridestats` output

## Integration Points

The Mixin system integrates with:
- **Core Mod**: Passes extracted data to NotRidingAlertClient
- **Ride Tracking**: Updates CurrentRideHolder and RideCountManager
- **Configuration**: Respects visual setting preferences
- **Alert System**: Provides state for alert logic
- **Strategy HUD**: Supplies data for display

## Performance Considerations

- **Selective Hooking**: Only hooks necessary methods
- **Efficient Filtering**: Early returns for irrelevant events
- **Minimal Processing**: Simple operations in hot paths
- **State Caching**: Avoids redundant calculations

## Error Handling

- **Graceful Degradation**: Continues operation if individual mixins fail
- **Data Validation**: Validates extracted values before use
- **Fallback Behavior**: Uses defaults when data is missing
- **Logging**: Errors logged without breaking gameplay

## Compatibility

- **Version Safety**: Uses Mixin's version-agnostic targeting
- **Mod Compatibility**: Designed to work with common client mods
- **Update Resilience**: Uses stable game APIs unlikely to change
- **Feature Detection**: Handles missing game features gracefully

## Debugging

- **Mixin Debugging**: Uses Mixin's built-in debugging tools
- **Data Logging**: Optional logging of extracted data
- **State Verification**: Tracks mixin state changes
- **Error Reporting**: Detailed error messages for troubleshooting