# Alert System

## Overview

The Alert System is the core functionality of the Not Riding Alert mod. It monitors the player's state and plays configurable sound alerts when the player is idle and not riding a ride on the ImagineFun server.

## Key Components

### Main Controller
- **NotRidingAlertClient**: Central coordinator that orchestrates all alert logic
  - Runs checkNotRidingAlert() every 10 seconds (200 ticks)
  - Coordinates with all trackers and handlers
  - Manages the overall riding state

### State Trackers
- **PlayerMovementTracker**: Tracks recent player movement
  - Determines if player has moved within last 30 seconds
  - Suppresses alerts when player is actively moving
  
- **RideStateTracker**: Tracks riding-related events
  - Monitors recent ride completion (5 second suppression)
  - Tracks vehicle state (5 second suppression after having vehicle)
  - Handles Lincoln-specific suppression logic

- **SuppressionRegionTracker**: Manages location-based suppression
  - Identifies areas where alerts should be suppressed
  - Handles ROTR (Rise of the Resistance) exception areas
  - Links with Lincoln show suppression logic

### Alert Logic Flow

The alert system follows this decision tree every 10 seconds:

1. **Initial Checks**
   - Is player on ImagineFun server?
   - Is the player in-game?
   - Is the alert system enabled in config?

2. **Riding State Check**
   - Is player currently a passenger in a vehicle?
   - Is player in a tracked ride (CurrentRideHolder)?
   - Is player in an autograb region?
   - If any are true: player IS riding → suppress alerts

3. **Suppression Checks** (only when NOT riding)
   - Has player moved recently? → suppress
   - Has player completed a ride recently? → suppress
   - Has player had a vehicle recently? → suppress
   - Is player in a suppression region? → suppress
   - Is Lincoln show suppression active? → suppress

4. **Trigger Alert**
   - If all conditions pass → play configured sound

### Sound Management
- **SoundHelper**: Handles alert sound playback
  - Uses configured sound ID from ModConfig
  - Respects silent mode setting
  - Plays sound through Minecraft's sound system

## Integration Points

The Alert System integrates with:
- Configuration system for settings and toggles
- Mixins for detecting game state changes
- Tracking system for player/ride state
- Sound system for alert playback

## Configuration Options

Key config options affecting alerts:
- `enabled`: Master toggle for alerts
- `silent`: Disables sound alerts completely
- `soundId`: Selects which sound to play

## Special Cases

- **Autograb regions**: Players waiting in ride queues are considered "riding"
- **Lincoln show**: Special suppression during Great Moments with Mr. Lincoln
- **ROTR areas**: Custom suppression zones for Rise of the Resistance ride