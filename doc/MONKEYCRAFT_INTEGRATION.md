# Monkeycraft Integration

## Overview

The Monkeycraft Integration provides compatibility with the Monkeycraft mod, enabling features like hibernation control, scheduled notifications, and connection lifecycle management. This integration is optional - the mod works fully without Monkeycraft installed.

## Compatibility Pattern

The integration follows the recommended "compat gate" pattern:
- **MonkeycraftCompat**: Safe gatekeeper class
  - Checks if Monkeycraft is installed via `FabricLoader.isModLoaded()`
  - Delegates to implementation only when available
  - Prevents classloading errors when Monkeycraft is missing

- **MonkeycraftCompatImpl**: Implementation class
  - Contains actual Monkeycraft API calls
  - Only loaded when Monkeycraft is present
  - Handles all the integration functionality

## Key Features

### Hibernation Control
Hibernation pauses remote streaming while maintaining the WebSocket connection.

- **startHibernation()**: Begins hibernation with a message
  - Used when a ride starts
  - Shows ride name and progress information
  - Prevents unnecessary data streaming during rides

- **endHibernation()**: Stops hibernation
  - Called when a ride ends
  - Resumes normal streaming behavior

- **setHibernationMessage()**: Updates message while hibernating
  - Updates progress percentage during ride
  - Shows remaining time left
  - Updates every second (20 ticks)

### Notification System
- **setTimedNotification()**: Schedules a notification for future
  - Used to alert when rides complete
  - Calculates exact finish time based on ride duration
  - Includes progress toward next goal in notification

- **cancelTimedNotification()**: Cancels scheduled notifications
  - Used when ride ends unexpectedly
  - Prevents false notifications

- **sendImmediateNotification()**: Sends notification immediately
  - Best-effort delivery
  - Only works when phone app is active/connected

### Connection Management
- **isClientConnected()**: Checks if Monkeycraft client is connected
  - Determines if integration features should be active
  - Prevents unnecessary API calls when disconnected

- **isHibernating()**: Checks current hibernation state
  - Used to coordinate game behavior with hibernation
  - Helps maintain consistent state

## Handler Classes

### HibernationHandler
Manages hibernation lifecycle and notifications:

- **Track Ride Start**:
  - Determines hibernation eligibility (excludes Davy Crockett)
  - Calculates ride start time from current progress
  - Schedules completion notification
  - Starts hibernation with initial message

- **Track Ride Progress**:
  - Updates message every second with current progress
  - Calculates remaining time based on elapsed time
  - Shows percentage complete and time left

- **Track Ride End**:
  - Ends hibernation when ride completes
  - Handles cancellation of pending notifications
  - Resets all state variables

- **Notification Scheduling**:
  - Calculates exact completion time
  - Includes goal progress in notification body
  - Handles rides with unknown duration

## Notification Content

### Ride Start Hibernation Message
```
Riding Space Mountain (0%)
3:45 left
```

### Ride Progress Updates
```
Riding Space Mountain (45%)
2:06 left
```

### Completion Notification
- **Title**: "Ride finished"
- **Body**: 
  - If goal reached: "Space Mountain has finished (needs 5 more rides)"
  - If maxed out: "Space Mountain has finished"

## Integration Flow

1. **Mod Initialization**
   - MonkeycraftCompat.init() called on client start
   - Registers connection/disconnection event handlers
   - Sets up command interception if needed

2. **Ride Start**
   - HibernationHandler detects ride start
   - MonkeycraftCompat.startHibernation() called with ride info
   - MonkeycraftCompat.setTimedNotification() schedules completion alert

3. **Ride Progress**
   - HibernationHandler updates every second
   - MonkeycraftCompat.setHibernationMessage() shows current progress
   - Displays remaining time calculation

4. **Ride End**
   - HibernationHandler detects ride end
   - MonkeycraftCompat.endHibernation() stops hibernation
   - May cancel pending notification if needed

## Configuration

No dedicated config options for Monkeycraft integration. Features are automatically enabled when Monkeycraft is present and connected.

## Error Handling

- **Missing Monkeycraft**: All calls gracefully do nothing
- **Disconnected Client**: Features are paused until reconnection
- **Invalid Ride Data**: Skips hibernation for rides without duration
- **Notification Failures**: Logged but don't affect gameplay

## Performance Considerations

- **Minimal Overhead**: No processing when disconnected
- **Event-Driven**: Only processes during rides
- **Lazy Updates**: Only sends notifications when actually connected
- **Cancels Unused**: Cleans up notifications when rides end early