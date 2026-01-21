# Not Riding Alert

A thid-party quality-of-life mod for the ImagineFun server (https://modrinth.com/modpack/imaginefun) designed to help players efficiently grind rides and track their progress.

## Features

### 🎯 Smart Alert System
- **Automatic Alerts**: Plays a customizable sound alert every 10 seconds when you're not riding a ride
- **Smart Suppression**: The alert is automatically suppressed when:
  - You're currently on a ride (detected via scoreboard or vehicle)
  - You've moved recently (within 30 seconds)
  - You've just completed a ride (within 5 seconds)
  - You have a vehicle (within 5 seconds)
  - You're near specific suppression locations

### 📊 Strategy HUD
- **Goal-Based Recommendations**: Displays the top rides you should focus on to reach your next milestone
- **Progress Tracking**: Shows real-time progress percentage for your current ride (e.g., "Space Mountain (45%)")
- **Smart Layout**: Automatically splits into two columns when displaying 8+ rides
- **Current Ride Highlighting**: Your current ride is highlighted in green with progress percentage
- **Customizable Display**: Configure how many rides to show (default: 16)

### 📈 Ride Progress Tracking
- **Real-Time Progress**: Automatically calculates and displays ride completion percentage based on elapsed time
- **Visual Feedback**: Progress percentage appears next to the ride name in green when you're on that ride
- **Accurate Timing**: Uses scoreboard data to track elapsed time and compare against known ride durations

### 🎢 Ride Count Management
- **Automatic Tracking**: Tracks how many times you've ridden each ride
- **Persistent Storage**: Ride counts are saved to disk and persist across sessions
- **Goal Milestones**: Supports goals at 1, 10, 100, 500, 1000, 5000, and 10000 rides per ride

You need to open /ridestats and go through all the pages in all the tabs (including Page 2 of Disneyland Park) to load the initial stats to the mod.

### 🎨 Visual Customization
- **Hide Scoreboard**: Option to hide the scoreboard while still tracking ride data
- **Hide Chat**: Option to hide the chat for a cleaner interface
- **Ride Filtering**: Hide specific rides from the strategy display

### ⚙️ Additional Features
- **Blindness Effect**: Optional blindness effect when riding (to reduce distractions)
- **Sound Suppression**: Automatically suppresses game sounds when riding
- **Seasonal Ride Support**: Toggle whether seasonal rides appear in recommendations
- **Configurable Sound**: Customize the alert sound to your preference

## Commands

Note: You need to open /scoreboard to enable the mod to capture ride data. You can then use /nra:hidesb to hide the scoreboard later.

All commands use the `nra:` namespace prefix.

### `/nra:togglealert`
Toggles the alert system on/off.

**Usage:**
```
/nra:togglealert
```

### `/nra:setsound <soundId>`
Sets the sound that plays when you're not riding.

**Usage:**
```
/nra:setsound <soundId>
/nra:setsound entity.experience_orb.pickup
```

**Examples:**
- `entity.experience_orb.pickup` (default)
- `entity.note_block.note`
- `block.note_block.pling`

### `/nra:blind`
Toggles the blindness effect when riding on/off.

**Usage:**
```
/nra:blind
```

### `/nra:seasonalride`
Toggles whether seasonal rides appear in the strategy recommendations.

**Usage:**
```
/nra:seasonalride
```

### `/nra:hideride <rideName>`
Hides or shows a specific ride in the strategy display. Use Tab to autocomplete ride names.

**Usage:**
```
/nra:hideride <rideName>
/nra:hideride "Space Mountain"
```

### `/nra:display <num>`
Sets the number of rides to display in the strategy HUD (minimum 1).

**Usage:**
```
/nra:display <num>
/nra:display 16
```

**Note:** If you call the command without arguments, it will show the current value.

### `/nra:hidescoreboard` or `/nra:hidesb`
Toggles scoreboard visibility. The scoreboard will be hidden but ride data will still be tracked.

**Usage:**
```
/nra:hidescoreboard
/nra:hidesb
```

### `/nra:hidechat`
Toggles chat visibility.

**Usage:**
```
/nra:hidechat
```

## Configuration

The mod automatically creates a configuration file at:
```
.minecraft/config/not-riding-alert.json
```

You can edit this file directly or use the in-game commands. The configuration includes:
- `enabled`: Whether the alert system is enabled
- `soundId`: The sound to play for alerts
- `blindWhenRiding`: Whether to apply blindness when riding
- `seasonalRidesEnabled`: Whether seasonal rides appear in recommendations
- `hiddenRides`: List of rides to hide from the display
- `rideDisplayCount`: Number of rides to show in the HUD
- `hideScoreboard`: Whether to hide the scoreboard
- `hideChat`: Whether to hide the chat

## Requirements

- **Minecraft**: 1.21.11
- **Fabric Loader**: >= 0.18.4
- **Fabric API**: Required
- **Java**: >= 21

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the latest version of Not Riding Alert from Modrinth
4. Place the `.jar` file in your `mods` folder
5. Launch Minecraft

## How It Works

### Alert System
The mod checks every 10 seconds if you're riding a ride. If you're not riding and haven't moved recently, it plays an alert sound to remind you to get back on a ride.

### Strategy Recommendations
The mod calculates which rides will help you reach your next milestone the fastest. It considers:
- Your current ride count for each ride
- The duration of each ride
- Your next goal milestone (1, 10, 100, 500, 1000, 5000, or 10000 rides)

### Progress Tracking
The mod reads the scoreboard to detect:
- Your current ride (from the "Current Ride" scoreboard entry)
- Elapsed time on the ride (from the time display)
- Calculates percentage based on known ride durations

### Ride Detection
The mod detects when you're on a ride through:
- Scoreboard data (primary method)
- Minecraft vehicle detection (fallback)

## Tips

1. **Use the Strategy HUD**: The green highlighted ride is your current ride - focus on completing it!
2. **Hide Completed Rides**: Use `/nra:hideride` to hide rides you've already completed to focus on new goals
3. **Adjust Display Count**: If you want to see more or fewer recommendations, use `/nra:display`
4. **Customize Your Sound**: Find a sound that's noticeable but not annoying for long grinding sessions
5. **Hide UI Elements**: Use `/nra:hidescoreboard` and `/nra:hidechat` for a cleaner interface

## Known Limitations

- Progress tracking is not available for "Davy Crockett's Explorer Canoes" (ride time is player-dependent)
- The mod requires scoreboard data to function properly
- Ride counts are stored locally and don't sync across devices

## Support

- **Issues**: Report bugs or request features on [GitHub](https://github.com/weikengchen/not-riding-alert)
- **Version**: 2.1.0

## License

This mod is licensed under CC0-1.0 (Public Domain).

---

**Note**: This mod is designed specifically for the ImagineFun server and may not work correctly on other servers without similar scoreboard setups.
