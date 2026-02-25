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

## Reminder System

### Canoe Ride Messages
When entering Davy Crockett's Explorer Canoes region and releasing the cursor, the mod displays a helpful reminder message:

- **Message**: "[NRA] Please use LEFT click to ride canoes."
- **Purpose**: Reminds players that canoes require left-click interaction (unlike other rides)
- **Rate Limiting**: Message is shown at most once every 10 seconds to avoid spam
- **Trigger**: Activated when cursor is released in the canoe region

### Audio Boost Reminders
The mod can remind players to connect to the ImagineFun audio client:

- **Message**: "MISSING AUDIO BOOST" (displayed in action bar)
- **Handler**: `ReminderHandler` singleton
- **Detection**: ChatListenerMixin detects connection messages:
  - "You are now connected with the audio client!" → sets connected state
  - "Your audio session has been ended" → clears connected state

**AudioBoostReminderMode Options:**
- `DISABLED`: Never show reminder
- `ONLY_WHEN_RIDING`: Show only during rides (default use case)
- `ALWAYS`: Always show when not connected

### Configuration Reminders
For new users who haven't configured the mod:

- **Handler**: `ConfigReminderHandler`
- **Trigger**: User hasn't opened config (`hasOpenedConfig` is false)
- **Initial Delay**: Shows after 30 seconds (600 ticks) on server
- **Repeat Interval**: Reminds every 10 minutes (12000 ticks)
- **Message**: Guides user to run `/nra` command to open configuration
- **Dismissal**: Setting `hasOpenedConfig` to true stops reminders