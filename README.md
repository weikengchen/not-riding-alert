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
- **Advance Ride Completion Notice**: Plays an alert sound a configurable number of seconds before a ride finishes, for rides with continuous departures. Per-ride configurable (0-30 seconds) via the Advance Notice config tab.
- **Blindness Effect**: Optional blindness effect when riding (to reduce distractions)
- **Fullbright**: Force full brightness when not riding (client-side only)
- **Sound Suppression**: Automatically suppresses game sounds when riding
- **Seasonal Ride Support**: Toggle whether seasonal rides appear in recommendations
- **Configurable Sound**: Customize the alert sound to your preference
- **Audio Boost Reminder**: Displays "MISSING AUDIO BOOST" in the action bar when you're not connected to the ImagineFun audio client. Configurable to show always, only when riding, or disabled.

### 📊 Daily Session Stats (New in v2.4.5)
A bottom-right HUD overlay that tracks your daily riding session:

- **Ride Count**: Number of rides completed today
- **Ride Time**: Total time spent on rides today (updates live while on a ride)
- **Ride Time per Hour**: Minutes of ride time per hour of online time — a measure of how efficiently you're riding (e.g., `42m/hr` means 42 out of every 60 online minutes were spent on rides)
- **Daily Streak**: Tracks consecutive days of riding activity, displayed below the stats line
- **Persistent Data**: Stats are saved to disk and persist across reconnects; resets daily at midnight
- **Online Time Only**: The "per hour" metric uses cumulative online time, not wall-clock time — offline gaps don't count against you

### 🏆 Session Milestones (New in v2.4.5)
Celebrates your riding achievements during each session:

- Triggers at ride count thresholds: 10, 25, 50, 100, 150, 200, 250, 500, 1000
- Plays a celebration sound and displays a gold-colored chat message with your total ride time

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
- **Enable OpenAudioMC**: Toggle automatic OpenAudioMC audio session connection (default: off)

### 📊 Tracker Settings (Tracker Tab)
- **Tracker Display Mode**: Control when the strategy HUD is visible — Always, Only When Riding (includes autograb zones), Only When Not Riding, or Never
- **Strategy HUD Version**: Choose between V0, V1, and V2 renderer styles
- **Auto-grabbing Detection**: Toggle region-based autograbbing feature
- **Ride Display Count**: Set how many rides to show in the HUD (1-60)
- **Minimum Ride Time Filter**: Filter out rides shorter than X minutes
- **Strategy HUD Background Opacity**: Adjust the background opacity (0-100%, default: 80%)
- **Only Show Autograbbing Rides**: Filter to only show rides that support autograbbing
- **Closest Ride Mode**: Control closest ride highlighting — Always, Only In-Progress Rides (default), or Never
- **Tracker Colors**: Customize colors for Normal, Autograbbing, Riding, Error, and Closest Ride states
- **Audio Boost Reminder**: Configure when to show audio boost reminders
- **Max Goal**: Select your target milestone (1K, 5K, or 10K rides) for progress tracking
- **Sorting Rules**: Configure how rides are sorted in the strategy HUD

### 📁 Profiles Tab (New in v2.4.0)
- **Save Current Settings as Profile**: Save your current configuration as a named profile
- **Load Profile**: Quickly switch between saved configuration profiles
- **Edit Profile**: Rename or modify existing profiles
- **Delete Profile**: Remove unwanted profiles
- **Built-in Profiles**: Pre-configured profiles for common use cases
- **Profile History**: When you apply a profile, the previous settings are automatically backed up. Browse and restore past configurations from the History screen (entries expire after 1 month)

### ⏱️ Advance Notice Settings (Advance Notice Tab, New in v2.4.5)
- **Per-Ride Advance Notice**: Configure advance notice seconds (0-30) for each ride individually
- Set to 0 to disable advance notice for a specific ride

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
- **Show Daily Session Stats**: Toggle the daily session stats HUD overlay (default: enabled)

### 📍 Closest Ride Detection (New in v2.4.5)
Highlights the ride nearest to the player's current position:

- **Coordinate-Based**: Uses a bundled `ride-coordinates.json` with multiple coordinate points per ride (ride start locations and IFone teleport points)
- **Multi-Point Matching**: Each ride can have multiple coordinate entries; the closest point across all entries is used
- **V0 & V1**: If the closest ride is already in the tracker list, appends "(Closest)" with the closest ride color. If not in the list, shows it on a separate line (like autograbbing)
- **V2**: Highlights matching rides in the list. If the closest ride is not in the list, replaces the last entry with it
- **Filtering**: Configurable to show always, only for in-progress rides (default), or never
- **Smart Suppression**: Closest ride is hidden while riding or autograbbing

### 🔊 OpenAudioMC Integration (New in v2.4.5)
Automatically connects to the ImagineFun audio system without needing a separate browser tab:

- **Auto-Detection**: Detects OpenAudioMC session URLs in chat messages
- **Headless WebView**: Launches a hidden native WebView process to manage the audio session
- **Auto-Connect**: Clicks "Start Audio Session" automatically and notifies you to adjust volume via `/volume`
- **Reconnection**: Retries up to 3 times if the audio session drops mid-connection
- **Cross-Platform**: Native helpers for macOS (WKWebView) and Windows (WebView2)
- **Configurable**: Can be enabled/disabled in the General settings tab (disabled by default, enabled in Grinding and Sightseeing built-in profiles)
- **Chat Commands**: `/oa connect` (sends `/audio` and auto-connects), `/oa disconnect` (terminates connection), `/oa reconnect` (refreshes session or reconnects from scratch)

## Reproducible Build

The mod JAR includes native helper binaries for macOS and Windows. These binaries are **not stored in git** — they are compiled from source during CI using GitHub Actions, so every build is reproducible and auditable.

### How it works

1. **Source files** are in `native/macos/WebViewHelper.swift` and `native/windows/WebViewHelper.cs` — fully readable and reviewable
2. **CI builds** compile them on platform-native runners (macOS for Swift, Windows for .NET)
3. **The JAR** bundles both the compiled binaries and the source files, so anyone can verify what the binaries do by reading the source inside the JAR

### CI workflow (`build.yml`)

| Job | Runner | Compiles |
|-----|--------|----------|
| `build-macos-native` | `macos-latest` | `swiftc` → `webview-helper` (universal arm64+x86_64) |
| `build-windows-native` | `windows-latest` | `dotnet publish` → `webview-helper.exe` (framework-dependent, requires .NET 8 Desktop Runtime) |
| `build` | `ubuntu-latest` | Assembles native artifacts + source into JAR via Gradle |

### Local development

To build the native binaries locally (e.g., before `./gradlew build`):

```bash
# Builds both platforms and copies to src/main/resources/native/
./native/build-all.sh

# macOS only (requires Xcode CLI tools)
cd native/macos && bash build.sh

# Windows cross-compile from macOS (requires .NET SDK: brew install dotnet)
cd native/windows && dotnet publish -c Release -r win-x64 --no-self-contained -p:PublishSingleFile=true -o publish
```

### Runtime behavior

When the mod starts, `WebViewBridge` resolves the helper binary in this order:
1. **Existing binary** at `config/not-riding-alert/native/` (user override)
2. **Compile from source** if `swiftc` (macOS) or `dotnet` (Windows) is available — extracts the `.swift`/`.cs` source from the JAR and compiles it
3. **Pre-compiled binary** from JAR resources (extracted to config directory)
4. **Windows .NET check**: skips if .NET 8 Desktop Runtime is not installed, with a log message pointing to the download page

## Known Limitations

- Progress tracking is not available for "Davy Crockett's Explorer Canoes" (ride time is player-dependent)
- The mod requires scoreboard data to function properly
- Ride counts are stored locally and don't sync across devices

## Support

- **Issues**: Report bugs or request features on [GitHub](https://github.com/weikengchen/not-riding-alert)
- **Version**: 2.4.5

## License

This mod is licensed under CC0-1.0 (Public Domain).

---

**Note**: This mod is designed specifically for the ImagineFun server.