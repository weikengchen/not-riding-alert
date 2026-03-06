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
- **Smart Dynamic Layout**: Automatically calculates optimal column layout (1-8 columns) based on content width and screen size
- **Row Minimization**: Prefers fewer columns when row count is the same to reduce wasted horizontal space
- **Horizontal Centering**: All content is automatically centered on screen for a polished appearance
- **Current Ride Highlighting**: Your current ride is highlighted with progress percentage
- **Customizable Display**: Configure how many rides to show (default: 16)
- **Short Name Option**: Use abbreviated ride names in the tracker for a cleaner display

#### Strategy HUD Versions
The mod offers three different HUD renderer styles:

| Version | Description |
|---------|-------------|
| **V2** (default) | Modern animated layout with smooth transitions, collapsing/expanding animations, and state-based display (full, collapsed, waiting) |
| **V1** | Two-column layout anchored to the top-left corner |
| **V0** | Original classic layout centered on screen |

#### Customizable Tracker Colors
All HUD versions support customizable colors for different states:
- **Normal Color**: Default color for ride entries
- **Autograbbing Color**: Color when waiting to be picked up by a ride
- **Riding Color**: Color for the currently active ride
- **Error Color**: Color for error messages

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
- **Hide Hotbar**: Option to hide the hotbar for a cleaner interface
- **Hide Name Tags**: Option to hide player name tags for a cleaner interface
- **Hide Experience Level**: Option to hide the experience level number for a cleaner interface
- **Hide Love Potion Messages**: Filter out system messages containing love potion effects (optional)
- **Relocate Closed Caption**: Move [CC] messages from chat to a centered overlay with styled text and 8-way shadow outline
- **Ride Filtering**: Hide specific rides from the strategy display

### ⚙️ Additional Features
- **Autograbbing Detection**: When enabled, entering predefined ride regions automatically releases the cursor and marks you as ready to ride. This allows you to multitask without needing to manually interact with the ride vehicle while waiting for rides to start.
- **Defocus Cursor**: Automatically releases the mouse cursor when you start riding, and grabs it back when you stop riding.
- **Window Minimization**: Optionally minimize the game window when riding and automatically restore it when the ride ends.
- **Blindness Effect**: Optional blindness effect when riding (to reduce distractions)
- **Fullbright**: Force full brightness when not riding (client-side only)
- **Sound Suppression**: Automatically suppresses game sounds when riding
- **Seasonal Ride Support**: Toggle whether seasonal rides appear in recommendations
- **Configurable Sound**: Customize the alert sound to your preference
- **Audio Boost Reminder**: Displays "MISSING AUDIO BOOST" in the action bar when you're not connected to the ImagineFun audio client. Configurable to show always, only when riding, or disabled.

### 🐵 Monkeycraft Integration
Optional integration with the Monkeycraft mod for enhanced mobile/remote play experience. When Monkeycraft is installed and connected:

- **Hibernation Control**: Automatically hibernates the remote stream during rides to save bandwidth and battery
- **Progress Updates**: Displays ride progress (percentage and time remaining) in the Monkeycraft app while riding
- **Completion Notifications**: Sends a push notification to your phone when a ride finishes
- **Goal Progress**: Notifications include progress toward your next milestone (e.g., "needs 5 more rides")

This integration is optional - the mod works fully without Monkeycraft installed.

### 🎯 Autograbbing Detection
Autograbbing detection is a smart feature that automatically detects when you enter a ride's waiting area and prepares the game for your ride session. When you enter a predefined region for a supported ride, the mod will:

1. **Release the mouse cursor** - allowing you to use other applications without Minecraft stealing focus
2. **Mark you as "ready to ride"** - the strategy HUD will display the ride name with "(Autograbbing...)" status
3. **Suppress the alert system** - since the mod knows you're waiting for a ride to start

**Supported Rides with Autograbbing Detection:**

**Disneyland Park:**
- **Alice in Wonderland**
- **Big Thunder Mountain Railroad**
- **Casey Jr. Circus Train**
- **Chip 'n' Dale's GADGETcoaster**
- **Disneyland Monorail**
- **Disneyland Railroad**
- **Finding Nemo Submarine Voyage**
- **Haunted Mansion**
- **Indiana Jones™ Adventure**
- **Jungle Cruise**
- **Matterhorn Bobsleds**
- **Mickey & Friends Parking Tram**
- **Mr Toad's Wild Ride**
- **Peter Pan's Flight**
- **Pinocchio's Daring Journey**
- **Pirates of the Caribbean**
- **Roger Rabbit's Car Toon Spin**
- **Snow White's Enchanted Wish**
- **Space Mountain**
- **Splash Mountain**
- **Star Wars: Rise of the Resistance**
- **Storybook Land Canal Boats**
- **The Many Adventures of Winnie the Pooh**

**Disney California Adventure:**
- **Goofy's Sky School**
- **Grizzly River Run**
- **Guardians of the Galaxy - Mission: BREAKOUT!**
- **Incredicoaster**
- **Monsters, Inc. Mike & Sulley to the Rescue!**
- **Radiator Springs Racers**
- **The Little Mermaid - Ariel's Undersea Adventure**

**Retro:**
- **The Twilight Zone Tower of Terror**

**Note:** Autograbbing detection can be toggled on/off in the configuration menu (`/nra`). When disabled, region-based ride detection will not occur.

## Configuration

The mod provides a comprehensive configuration screen accessible via the `/nra` command. This replaces all previous chat commands.

### ⚙️ General Settings (General Tab)
- **Progress Summary**: The configuration screen title displays your overall progress towards 1k, 5k, and 10k ride goals (e.g., "1k (10%, 2d 5h)").
- **Enable Alerts**: Toggle the alert sound when not riding
- **Sound ID**: Select the alert sound from a dropdown menu of available game sounds
- **Defocus Cursor**: Configure when to automatically release the mouse cursor (None, On autograbbing, On ride start)
- **Silent Mode**: Suppress game sounds when riding
- **Alert on Autograb Failure**: Play alert sound if autograbbing fails to detect a vehicle
- **Minimize Window When Riding**: Configure when to automatically minimize the game window

### 📊 Tracker Settings (Tracker Tab)
- **Display Tracker**: Toggle the strategy HUD visibility
- **Strategy HUD Version**: Choose between V0, V1, and V2 renderer styles
- **Auto-grabbing Detection**: Toggle region-based autograbbing feature
- **Ride Display Count**: Set how many rides to show in the HUD (1-60)
- **Minimum Ride Time Filter**: Filter out rides shorter than X minutes
- **Strategy HUD Background Opacity**: Adjust the background opacity (0-100%, default: 80%)
- **Only Show Autograbbing Rides**: Filter to only show rides that support autograbbing
- **Tracker Colors**: Customize colors for Normal, Autograbbing, Riding, and Error states
- **Audio Boost Reminder**: Configure when to show audio boost reminders
- **Max Goal**: Select your target milestone (1K, 5K, or 10K rides) for progress tracking
- **Sorting Rules**: Configure how rides are sorted in the strategy HUD

### 📁 Profiles Tab (New in v2.4.0)
- **Save Current Settings as Profile**: Save your current configuration as a named profile
- **Load Profile**: Quickly switch between saved configuration profiles
- **Edit Profile**: Rename or modify existing profiles
- **Delete Profile**: Remove unwanted profiles
- **Built-in Profiles**: Pre-configured profiles for common use cases

### 🎢 Ride Management (Rides Tab)
- **Toggle Rides**: Individual toggles for every ride to hide/show them in the strategy HUD

### 🎨 Visual Settings (Visual Tab)
- **Dim Screen When Riding**: Toggle screen dimming effect when riding
- **Fullbright**: Force full brightness (None, Only when riding, Only when not riding, Always)
- **Hide Scoreboard**: Toggle scoreboard visibility
- **Hide Chat**: Toggle chat visibility
- **Hide Health**: Toggle health bar visibility
- **Hide Name Tags**: Toggle player name tags visibility
- **Hide Hotbar**: Toggle hotbar visibility
- **Hide Experience Level**: Toggle experience level number visibility
- **Hide Love Potion Messages**: Filter out system messages containing love potion effects
- **Relocate Closed Caption**: Move [CC] messages from chat to a centered overlay with styled text

## Known Limitations

- Progress tracking is not available for "Davy Crockett's Explorer Canoes" (ride time is player-dependent)
- The mod requires scoreboard data to function properly
- Ride counts are stored locally and don't sync across devices

## Changelog

### v2.4.0
- **New Profile System**: Save, load, and manage multiple configuration profiles
- **Closed Caption Enhancements**: Improved [CC] message display with configurable modes and color-coded text
- **Max Goal Selection**: Choose between 1K, 5K, or 10K ride milestones
- **Sorting Rules**: Configure how rides are sorted in the strategy HUD
- **Bug Fixes**: Fixed hiddenRides duplication, V0 HUD bossbar issues, and autograbbing detection
- **UI Improvements**: Enhanced configuration options and better user experience
- **Documentation Consolidation**: Merged separate documentation files into main README

## Support

- **Issues**: Report bugs or request features on [GitHub](https://github.com/weikengchen/not-riding-alert)
- **Version**: 2.4.0

## License

This mod is licensed under CC0-1.0 (Public Domain).

---

**Note**: This mod is designed specifically for the ImagineFun server.