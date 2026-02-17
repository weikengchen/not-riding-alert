# Not Riding Alert - Documentation

This directory contains technical documentation for the Not Riding Alert mod, organized by functional modules.

## Overview

The Not Riding Alert is a Fabric mod for Minecraft that helps players efficiently grind rides on the ImagineFun server. It provides intelligent alerts when idle, tracks ride progress, and offers strategic recommendations to reach ride count goals.

## Documentation Structure

### Core Systems

| Document | Description |
|----------|-------------|
| [ALERT_SYSTEM.md](ALERT_SYSTEM.md) | Core alert system - sound alerts, suppression logic, movement tracking |
| [RIDE_TRACKING.md](RIDE_TRACKING.md) | Ride count management, persistence, progress tracking |
| [STRATEGY_HUD.md](STRATEGY_HUD.md) | Strategy HUD rendering, goal-based recommendations |

### Feature Modules

| Document | Description |
|----------|-------------|
| [AUTOGRABBING.md](AUTOGRABBING.md) | Region-based ride detection, automatic cursor management |
| [MONKEYCRAFT_INTEGRATION.md](MONKEYCRAFT_INTEGRATION.md) | Optional Monkeycraft API integration for hibernation/notifications |
| [CONFIGURATION.md](CONFIGURATION.md) | Configuration system, Cloth Config GUI, ModMenu integration |

### Technical Details

| Document | Description |
|----------|-------------|
| [MIXINS.md](MIXINS.md) | All mixin classes - game event interception, UI modifications |

## Key Components

### Main Classes
- **NotRidingAlertClient**: Central coordinator and main mod entry point
- **ModConfig**: Configuration management and persistence
- **RideCountManager**: Ride statistics tracking and storage
- **StrategyCalculator**: Goal calculations and recommendations
- **StrategyHudRenderer**: HUD display and rendering

### Tracking Systems
- **PlayerMovementTracker**: Player movement detection
- **RideStateTracker**: Ride state and completion tracking
- **SuppressionRegionTracker**: Location-based alert suppression

### Handlers
- **AutograbFailureHandler**: Autograbbing timeout and failure alerts
- **DayTimeHandler**: Time of day manipulation for visual effects
- **HibernationHandler**: Monkeycraft hibernation and notifications

### Utility Classes
- **RegionHolder**: Ride region mapping and detection
- **SoundHelper**: Alert sound playback management
- **TimeFormatUtil**: Time formatting utilities
- **CurrentRideHolder/LastRideHolder**: Ride state management

## Architecture Patterns

- **Singleton Pattern**: Used for configuration, ride counts, and handlers
- **Observer Pattern**: Mixins observe and report game events
- **Strategy Pattern**: Different alert strategies based on game state
- **Compatibility Gate Pattern**: Optional Monkeycraft integration

## Data Flow

```
Minecraft Events → Mixins → Trackers → NotRidingAlertClient → Various Handlers
                                                        ↓
Configuration ← → ClothConfig GUI ← → ModMenu
                                                        ↓
Strategy Calculator → StrategyHudRenderer → HUD Display
                                                        ↓
RideCountManager ← → JSON Persistence
```

## Getting Started

1. Start with [ALERT_SYSTEM.md](ALERT_SYSTEM.md) to understand the core functionality
2. Read [RIDE_TRACKING.md](RIDE_TRACKING.md) for data management
3. Review [STRATEGY_HUD.md](STRATEGY_HUD.md) for the user-facing display
4. Check [MIXINS.md](MIXINS.md) for how the mod integrates with Minecraft

## Configuration

See [CONFIGURATION.md](CONFIGURATION.md) for all user-configurable options and the Cloth Config GUI structure.

## Optional Features

- **Autograbbing**: Documented in [AUTOGRABBING.md](AUTOGRABBING.md)
- **Monkeycraft Integration**: Documented in [MONKEYCRAFT_INTEGRATION.md](MONKEYCRAFT_INTEGRATION.md)

## Version Information

These documents describe the current implementation of the mod (v2.3.0). The mod is designed specifically for the ImagineFun server and may not function correctly on other servers.