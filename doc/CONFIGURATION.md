# Configuration System

## Overview

The Configuration System manages all user-configurable settings for the Not Riding Alert mod. It uses Cloth Config for the configuration GUI and integrates with ModMenu for easy access.

## Key Components

### ModConfig
Central configuration class managing all settings:

- **Singleton Pattern**: Single instance accessed via `getInstance()`
- **Persistent Storage**: Saves to `config/not-riding-alert.json`
- **Field Validation**: Includes proper equals/hashCode implementations
- **Default Values**: Sensible defaults for all settings

### ClothConfigScreen
GUI interface for configuration using Cloth Config library:

- **Tabbed Interface**: Organized into logical sections
- **Real-time Updates**: Changes apply immediately
- **Progress Summary**: Shows overall progress toward milestones
- **Input Validation**: Ensures valid values for all settings

### ModMenuApiImpl
ModMenu integration for easy access:

- **Config Screen Provider**: Connects ModMenu to Cloth Config
- **Mod Information**: Supplies metadata for ModMenu display
- **Entry Point**: Provides configuration access from mod list

## Configuration Options

### General Settings
- **globalEnable**: Master toggle for entire mod
- **enabled**: Specific toggle for alert system
- **silent**: Disables all sound alerts
- **soundId**: Selects which Minecraft sound to use
- **blindWhenRiding**: Applies blindness effect during rides
- **fullbrightWhenNotRiding**: Forces full brightness when not riding
- **defocusCursor**: Auto-releases mouse during rides
- **hideScoreboard**: Toggles scoreboard visibility
- **hideChat**: Toggles chat visibility
- **hideHealth**: Toggles health bar visibility

### Tracker Settings
- **autograb**: Enables region-based ride detection
- **rideDisplayCount**: Number of rides to show in Strategy HUD (1-16)
- **minRideTimeMinutes**: Filter out rides shorter than X minutes
- **displayShortName**: Use abbreviated ride names
- **onlyAutograbbing**: Show only autograbbing-supported rides

### Alert Settings
- **alertAutograbFailure**: Alert when autograb times out
- **alertAutograbFailure**: Toggle for autograb failure notifications

### Ride Management
- **hiddenRides**: List of rides to hide from Strategy HUD

## GUI Organization

### Main Tab
Core mod settings:
- Progress summary display
- Master toggles
- Sound configuration
- Visual effects toggles

### Tracker Tab
Strategy HUD and autograbbing:
- Ride display count slider
- Minimum ride time filter
- Short names toggle
- Autograbbing toggle
- Only autograbbing filter

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
  "fullbrightWhenNotRiding": false,
  "defocusCursor": true,
  "silent": true,
  "autograb": true,
  "minRideTimeMinutes": null,
  "rideDisplayCount": 16,
  "hiddenRides": ["davy_crocketts_explorer_canoes"],
  "hideScoreboard": false,
  "hideChat": false,
  "hideHealth": true,
  "onlyAutograbbing": false,
  "alertAutograbFailure": true,
  "displayShortName": false
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

### Ride Display Count
- Minimum of 1, maximum of 16 rides
- Clamping in GUI prevents invalid values
- Affects Strategy HUD layout dynamically

## Integration Points

The Configuration System integrates with:
- **All Mod Components**: Access settings via singleton
- **Cloth Config**: Provides GUI framework
- **ModMenu**: Entry point for configuration
- **Mixins**: Apply visual settings (hide scoreboard/chat/health)
- **HUD Renderer**: Uses display count and filtering options
- **Alert System**: Respects enabled/silent settings
- **Autograbbing**: Checks autograb toggle before region detection