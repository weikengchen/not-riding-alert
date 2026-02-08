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
- **Hide Health**: Option to hide the health bar (both player and vehicle) for a cleaner interface (default: enabled)
- **Ride Filtering**: Hide specific rides from the strategy display

### ⚙️ Additional Features
- **Autograbbing Detection**: When enabled, entering predefined ride regions automatically releases the cursor and marks you as ready to ride. This allows you to multitask without needing to manually interact with the ride vehicle while waiting for rides to start.
- **Defocus Cursor**: Automatically releases the mouse cursor when you start riding, and grabs it back when you stop riding. 
- **Blindness Effect**: Optional blindness effect when riding (to reduce distractions)
- **Sound Suppression**: Automatically suppresses game sounds when riding
- **Seasonal Ride Support**: Toggle whether seasonal rides appear in recommendations
- **Configurable Sound**: Customize the alert sound to your preference

### 🎯 Autograbbing Detection
Autograbbing detection is a smart feature that automatically detects when you enter a ride's waiting area and prepares the game for your ride session. When you enter a predefined region for a supported ride, the mod will:

1. **Release the mouse cursor** - allowing you to use other applications without Minecraft stealing focus
2. **Mark you as "ready to ride"** - the strategy HUD will display the ride name with "(Autograbbing...)" status
3. **Suppress the alert system** - since the mod knows you're waiting for a ride to start

**Supported Rides with Autograbbing Detection:**
- **Roger Rabbit's Car Toon Spin**
- **Space Mountain**
- **Jungle Cruise**
- **Disneyland Railroad**

**Note:** Autograbbing detection can be toggled on/off using `/nra:autograb`. When disabled, region-based ride detection will not occur.

## Commands

Note: You need to open /scoreboard to enable the mod to capture ride data. You can then use /nra:hidesb to hide the scoreboard later.

All commands use the `nra:` namespace prefix.

### `/nra:alert`
Toggles the alert system on/off.

**Usage:**
```
/nra:alert
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

### `/nra:defocus`
Toggles the defocus cursor feature on/off. When enabled, automatically releases the mouse cursor when you start riding and grabs it back when you stop riding.

**Usage:**
```
/nra:defocus
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

### `/nra:hidehp`
Toggles health bar visibility (both player and vehicle). This is enabled by default to provide a cleaner interface.

**Usage:**
```
/nra:hidehp
```

### `/nra:silent`
Toggles whether ride sounds are suppressed while riding. When enabled, all game sounds are suppressed except for the ride completion sound. Default: enabled.

**Usage:**
```
/nra:silent
```

### `/nra:autograb`
Toggles autograbbing detection for rides. When enabled, entering a predefined region for a ride will automatically release the cursor and mark you as ready to ride. Default: enabled.

**Usage:**
```
/nra:autograb
```

### Ride Goal Commands (/nra:1k, /nra:5k, /nra:10k)
Calculate the total time needed to reach specific ride count goals for all non-seasonal rides, as well as the player's progress so far.

| Command | Goal | Usage | Example Output |
|---------|------|-------|----------------|
| `/nra:1k` | 1000 rides | `/nra:1k` | ` rides to reach 1000: 15`<br>`Total time needed: 3d 5h 12m 34s` |
| `/nra:5k` | 5000 rides | `/nra:5k` | ` rides to reach 5000: 10`<br>`Total time needed: 12d 8h 45m 20s` |
| `/nra:10k` | 10000 rides | `/nra:10k` | ` rides to reach 10000: 8`<br>`Total time needed: 25d 14h 30m 10s` |

### `/nra:minfilter [minutes]`
Filters rides in the strategy display based on minimum ride time (in minutes). If no argument is provided, clears the filter.

**Usage:**
```
/nra:minfilter <minutes>
/nra:minfilter 10
/nra:minfilter
```

**Examples:**
- `/nra:minfilter 5` - Show only rides that take 5+ minutes
- `/nra:minfilter` - Clear the filter and show all rides

## Configuration

The mod automatically creates a configuration file at:
```
.minecraft/config/not-riding-alert.json
```

You can edit this file directly or use the in-game commands. The configuration includes:
- `enabled`: Whether the alert system is enabled
- `soundId`: The sound to play for alerts
- `defocusCursor`: Whether to automatically release/grab the cursor when riding
- `autograb`: Whether autograbbing detection is enabled (default: true)
- `silent`: Whether ride sounds are suppressed while riding (default: true)
- `blindWhenRiding`: Whether to apply blindness when riding
- `seasonalRidesEnabled`: Whether seasonal rides appear in recommendations
- `hiddenRides`: List of rides to hide from the display
- `rideDisplayCount`: Number of rides to show in the HUD
- `hideScoreboard`: Whether to hide the scoreboard
- `hideChat`: Whether to hide the chat
- `hideHealth`: Whether to hide the health bar (default: true)
- `minRideTimeMinutes`: Minimum ride time filter in minutes (null = no filter)

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
5. **Hide UI Elements**: Use `/nra:hidescoreboard`, `/nra:hidechat`, and `/nra:hidehp` for a cleaner interface
6. **Enable Defocus Cursor**: Use `/nra:defocus` to prevent Minecraft from stealing window focus while on rides, allowing you to multitask more easily

## Known Limitations

- Progress tracking is not available for "Davy Crockett's Explorer Canoes" (ride time is player-dependent)
- The mod requires scoreboard data to function properly
- Ride counts are stored locally and don't sync across devices

## Support

- **Issues**: Report bugs or request features on [GitHub](https://github.com/weikengchen/not-riding-alert)
- **Version**: 2.1.8

## License

This mod is licensed under CC0-1.0 (Public Domain).

---

**Note**: This mod is designed specifically for the ImagineFun server and may not work correctly on other servers without similar scoreboard setups.
