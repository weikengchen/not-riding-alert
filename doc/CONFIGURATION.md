# Configuration System

## Overview

The Configuration System manages all user-configurable settings for the Not Riding Alert mod. It uses Cloth Config for the configuration GUI and integrates with ModMenu for easy access.

## Key Components

### ModConfig
Central configuration class managing all settings:

- **Singleton Pattern**: Single instance accessed via `getInstance()`
- **Persistent Storage**: Saves to `config/not-riding-alert.json`
- **Field Validation**: Includes proper equals/hashCode implementations
- **Default Values**: Sensible defaults for all settings (managed via `ConfigDefaults`)

### ConfigDefaults
Provides default values for all configuration options:

- Centralizes default value management
- Ensures consistency across configuration loading
- Provides fallback values for missing/invalid config fields

### ClothConfigScreen
GUI interface for configuration using Cloth Config library:

- **Tabbed Interface**: Organized into logical sections
- **Real-time Updates**: Changes apply immediately
- **Progress Summary**: Shows overall progress toward milestones
- **Input Validation**: Ensures valid values for all settings
- **Color Pickers**: Visual color selection for tracker colors

### ModMenuApiImpl
ModMenu integration for easy access:

- **Config Screen Provider**: Connects ModMenu to Cloth Config
- **Mod Information**: Supplies metadata for ModMenu display
- **Entry Point**: Provides configuration access from mod list

## Configuration Options

### General Settings
| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `globalEnable` | boolean | true | Master toggle for entire mod |
| `enabled` | boolean | true | Specific toggle for alert system |
| `silent` | boolean | false | Disables all sound alerts |
| `soundId` | String | "entity.experience_orb.pickup" | Selects which Minecraft sound to use |
| `blindWhenRiding` | boolean | false | Applies blindness effect during rides |

### Fullbright Settings
| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `fullbrightMode` | FullbrightMode | ONLY_WHEN_NOT_RIDING | Controls when fullbright is active |

**FullbrightMode Enum:**
- `NONE`: Fullbright never applied
- `ONLY_WHEN_RIDING`: Fullbright only during rides
- `ONLY_WHEN_NOT_RIDING`: Fullbright only when not riding (default)
- `ALWAYS`: Fullbright always active

### Cursor Settings
| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `cursorReleaseTiming` | CursorReleaseTiming | NONE | Controls when cursor is released |

**CursorReleaseTiming Enum:**
- `NONE`: Never auto-release cursor
- `ON_ZONE_ENTRY`: Release when entering autograb zone
- `ON_VEHICLE_MOUNT`: Release when mounting ride vehicle

### Window Minimize Settings
| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `minimizeWindow` | WindowMinimizeTiming | NONE | Controls when window is minimized |

**WindowMinimizeTiming Enum:**
- `NONE`: Never auto-minimize
- `ON_ZONE_ENTRY`: Minimize when entering autograb zone
- `ON_VEHICLE_MOUNT`: Minimize when mounting ride vehicle

### Display Settings
| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `hideScoreboard` | boolean | false | Toggles scoreboard visibility |
| `hideChat` | boolean | false | Toggles chat visibility |
| `hideHealth` | boolean | true | Toggles health bar visibility |
| `hideNameTag` | boolean | false | Toggles player nametag visibility |
| `hideLovePotionMessages` | boolean | false | Filters love potion effect messages |

### Tracker Settings
| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `autograb` | boolean | true | Enables region-based ride detection |
| `rideDisplayCount` | int | 16 | Number of rides to show in Strategy HUD (1-60) |
| `minRideTimeMinutes` | Integer | null | Filter out rides shorter than X minutes |
| `displayShortName` | boolean | false | Use abbreviated ride names |
| `onlyAutograbbing` | boolean | false | Show only autograbbing-supported rides |
| `strategyHudRendererVersion` | StrategyHudRendererVersion | V2 | HUD renderer style |
| `hudBackgroundOpacity` | int | 80 | Background opacity (0-100%) |

**StrategyHudRendererVersion Enum:**
- `V2`: Modern animated layout (default)
- `V1`: Two-column layout from top-left
- `V0`: Original centered layout

### Tracker Colors
| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `trackerNormalColor` | int (ARGB) | 0xFFFFFFFF | Default text color for ride entries |
| `trackerAutograbbingColor` | int (ARGB) | 0xFFFFFF00 | Color when waiting for autograb |
| `trackerRidingColor` | int (ARGB) | 0xFF00FF00 | Color for currently active ride |
| `trackerErrorColor` | int (ARGB) | 0xFFFF0000 | Color for error messages |

Colors are stored as ARGB integers (Alpha-Red-Green-Blue).

### Alert Settings
| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `alertAutograbFailure` | boolean | true | Alert when autograb times out |

### Reminder Settings
| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `audioBoostReminderMode` | AudioBoostReminderMode | DISABLED | When to show audio boost reminder |

**AudioBoostReminderMode Enum:**
- `DISABLED`: Never show reminder
- `ONLY_WHEN_RIDING`: Show only during rides
- `ALWAYS`: Always show when not connected

### Monkeycraft Settings
| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `hibernationWhenRiding` | boolean | true | Enable hibernation during rides (requires Monkeycraft) |

### Internal Settings
| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `hasOpenedConfig` | boolean | false | Tracks if user has opened config (for reminders) |
| `keepUnchanged` | boolean | false | Internal flag for config handling |

### Ride Management
| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `hiddenRides` | List<String> | seasonal rides | List of rides to hide from Strategy HUD |

## GUI Organization

### Main Tab
Core mod settings:
- Progress summary display
- Master toggles
- Sound configuration
- Visual effects toggles
- Fullbright and cursor settings

### Tracker Tab
Strategy HUD and autograbbing:
- Renderer version selector
- Ride display count slider
- Minimum ride time filter
- Short names toggle
- Autograbbing toggle
- Only autograbbing filter
- Background opacity slider
- Tracker color pickers
- Audio boost reminder mode

### Rides Tab
Individual ride toggles:
- Checkboxes for each supported ride
- Used to hide completed or unwanted rides
- Alphabetical organization

## Configuration Flow

### Loading
1. ModConfig.load() reads from JSON file
2. If file doesn't exist, creates with defaults
3. Singleton instance available throughout mod
4. Components access via ModConfig.getInstance()

### Saving
1. User changes values in Cloth Config GUI
2. ClothConfigScreen applies changes immediately
3. ModConfig.save() writes to JSON file
4. Components detect changes via getInstance()

### Runtime Access
All mod components access configuration through:
```java
ModConfig config = ModConfig.getInstance();
boolean enabled = config.enabled;
String soundId = config.soundId;
FullbrightMode mode = config.fullbrightMode;
```

## Command Integration

The `/nra` command provides access to the configuration screen:
- Opens Cloth Config GUI
- Accessible from chat or keybinding
- Primary method for user configuration

## File Format

### JSON Structure
```json
{
  "globalEnable": true,
  "enabled": true,
  "soundId": "entity.experience_orb.pickup",
  "blindWhenRiding": false,
  "fullbrightMode": "ONLY_WHEN_NOT_RIDING",
  "cursorReleaseTiming": "NONE",
  "silent": false,
  "autograb": true,
  "minRideTimeMinutes": null,
  "rideDisplayCount": 16,
  "hiddenRides": ["davy_crocketts_explorer_canoes"],
  "hideScoreboard": false,
  "hideChat": false,
  "hideHealth": true,
  "hideNameTag": false,
  "onlyAutograbbing": false,
  "alertAutograbFailure": true,
  "displayShortName": false,
  "keepUnchanged": false,
  "hasOpenedConfig": false,
  "hudBackgroundOpacity": 80,
  "minimizeWindow": "NONE",
  "hibernationWhenRiding": true,
  "hideLovePotionMessages": false,
  "strategyHudRendererVersion": "V2",
  "trackerNormalColor": -1,
  "trackerAutograbbingColor": -256,
  "trackerRidingColor": -16711936,
  "trackerErrorColor": -65536,
  "audioBoostReminderMode": "DISABLED"
}
```

## Error Handling

- **File Corruption**: Creates new config with defaults
- **Invalid Values**: Cloth Config provides validation
- **Missing Fields**: Uses default values
- **Save Failures**: Logs errors but continues operation

## Special Cases

### Sound ID Validation
- Uses Minecraft's sound registry
- Dropdown list in GUI prevents invalid IDs
- Falls back to default if invalid

### Hidden Rides
- Uses ride match strings for identification
- Automatically updates when rides are renamed
- Preserves during config updates
- Default hides seasonal rides

### Ride Display Count
- Minimum of 1, maximum of 60 rides
- Clamping in GUI prevents invalid values
- Affects Strategy HUD layout dynamically

### Color Fields
- Stored as ARGB integers
- Cloth Config color pickers provide visual selection
- Alpha channel preserved (0xFF000000 mask applied when saving)

## Integration Points

The Configuration System integrates with:
- **All Mod Components**: Access settings via singleton
- **Cloth Config**: Provides GUI framework
- **ModMenu**: Entry point for configuration
- **Mixins**: Apply visual settings (hide scoreboard/chat/health/nametag)
- **HUD Renderer**: Uses display count, colors, and filtering options
- **Alert System**: Respects enabled/silent settings
- **Autograbbing**: Checks autograb toggle before region detection
- **ReminderHandler**: Uses audioBoostReminderMode setting
- **HibernationHandler**: Uses hibernationWhenRiding setting
