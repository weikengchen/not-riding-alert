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
- **Alice in Wonderland**
- **Big Thunder Mountain Railroad**
- **Casey Jr. Circus Train**
- **Disneyland Monorail**
- **Disneyland Railroad**
- **Finding Nemo Submarine Voyage**
- **Haunted Mansion**
- **Indiana Jones Adventure**
- **Jungle Cruise**
- **Mickey and Friends Parking Tram**
- **Roger Rabbit's Car Toon Spin**
- **Space Mountain**

**Note:** Autograbbing detection can be toggled on/off in the configuration menu (`/nra`). When disabled, region-based ride detection will not occur.

## Configuration

The mod provides a comprehensive configuration screen accessible via the `/nra` command. This replaces all previous chat commands.

### ⚙️ General Settings
- **Progress Summary**: The configuration screen title displays your overall progress towards 1k, 5k, and 10k ride goals (e.g., "1k (10%, 2d 5h)").
- **Silent Mode**: Toggle the alert sound on/off.
- **Sound ID**: Select the alert sound from a dropdown menu of all available game sounds.
- **Blindness Effect**: Toggle blindness effect when riding.
- **Defocus Cursor**: Toggle automatic cursor release when riding.
- **Only show known autograbbing rides**: Filter the strategy HUD to only show rides that support autograbbing.

### 📊 Tracker Settings (Tracker Tab)
- **Auto-grabbing detection**: Toggle the region-based autograbbing feature.
- **Ride Display Count**: Slider to set how many rides to show in the HUD (1-16).
- **Minimum Ride Time Filter**: Filter out rides shorter than X minutes.

### 🎢 Ride Management (Rides Tab)
- **Toggle Rides**: Individual toggles for every ride to hide/show them in the strategy HUD. Useful for hiding completed rides.

### 🎨 Display Settings (General Tab)
- **Hide Scoreboard**: Toggle scoreboard visibility.
- **Hide Chat**: Toggle chat visibility.
- **Hide Health**: Toggle health bar visibility.

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
2. **Hide Completed Rides**: Uncheck completed rides in the "Rides" tab of the config (`/nra`) to focus on new goals
3. **Adjust Display Count**: If you want to see more or fewer recommendations, adjust the "Ride Display Count" in the "Tracker" tab
4. **Customize Your Sound**: Find a sound that's noticeable but not annoying for long grinding sessions
5. **Hide UI Elements**: Toggle scoreboard, chat, and health visibility in the "General" tab for a cleaner interface
6. **Enable Defocus Cursor**: Enable this in "General" settings to prevent Minecraft from stealing window focus while on rides, allowing you to multitask more easily

## Known Limitations

- Progress tracking is not available for "Davy Crockett's Explorer Canoes" (ride time is player-dependent)
- The mod requires scoreboard data to function properly
- Ride counts are stored locally and don't sync across devices

## Support

- **Issues**: Report bugs or request features on [GitHub](https://github.com/weikengchen/not-riding-alert)
- **Version**: 2.2.0

## License

This mod is licensed under CC0-1.0 (Public Domain).

---

**Note**: This mod is designed specifically for the ImagineFun server.