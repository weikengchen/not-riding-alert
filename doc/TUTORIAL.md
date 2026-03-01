# Tutorial Wizard System

## Overview

The Tutorial Wizard is a client-side onboarding system that guides new users through Not Riding Alert's features. It presents a multi-page "book style" interface with formatted text, persisting progress across sessions.

**Features:**
- Multi-page book-style onboarding UI
- Formatted text with clickable links
- Persistent progress (survives crashes, reconnects)
- No server dependency (client-only)
- Dynamic content based on game state/config

## Architecture

### Package Structure
```
com.chenweikeng.nra.wizard/
├── TutorialManager.java          # State machine + persistence
├── TutorialState.java            # Enum for tutorial stages
├── WizardScreen.java             # Main GUI screen
├── WizardPage.java               # Abstract base class for pages
├── TutorialPages.java            # Static registry (ordered list of pages)
├── WizardActionHandler.java      # Handles link actions (config, navigation)
└── pages/
    ├── Page1Welcome.java         # Welcome page
    ├── Page2Alert.java           # Alert system explanation
    ├── Page3Hud.java             # Strategy HUD explanation
    ├── Page4Autograb.java        # Autograbbing feature
    ├── Page5Config.java          # Visual options / config
    └── Page6Done.java            # Completion page

com.chenweikeng.nra.mixin/
└── MinecraftScreenMixin.java     # Screen stacking for wizard preservation
```

### Asset Structure
```
assets/not-riding-alert/
└── textures/gui/tutorial/
    └── book_background.png       # Book texture (493x295)
```

## Core Components

### TutorialState (Enum)

```java
public enum TutorialState {
    NOT_STARTED,
    PAGE_1,
    PAGE_2,
    PAGE_3,
    PAGE_4,
    PAGE_5,
    FINISHED
}
```

Helper methods: `getNext()`, `getPrevious()`, `getPageIndex()`, `fromPageIndex(int)`, `isActive()`, `isFinished()`

### TutorialManager (Singleton)

**Responsibilities:**
- Track current tutorial stage
- Load/save progress to `config/notridingalert_tutorial.json`
- Determine if tutorial should start on join

**API:**
```java
public class TutorialManager {
    public static TutorialManager getInstance();
    public boolean shouldStartTutorial();    // Returns true if NOT_STARTED and !completed
    public boolean isTutorialActive();       // Returns true if in PAGE_X state
    public boolean isCompleted();
    public TutorialState getState();
    public int getCurrentPageIndex();
    public void advanceToNextPage();
    public void goToPage(int pageIndex);
    public void finishTutorial();
    public void resetTutorial();             // For /setupnra command
    void save();
    void load();
}
```

**Rules:**
- Must NOT reference GUI classes directly
- Pure logic + persistence only
- Save on every state change

### Persistent Storage

**File:** `config/notridingalert_tutorial.json`

```json
{
  "version": 1,
  "state": "PAGE_2",
  "completed": false
}
```

### WizardScreen (extends Screen)

**Responsibilities:**
- Render book layout with background texture
- Display page title and wrapped text
- Handle navigation (PageButton arrows, close button)
- Handle clickable text links directly (no command registration)
- Preserve screen instance across page navigation

**Key Methods:**
```java
public class WizardScreen extends Screen {
    public WizardScreen();                    // Start from page 0
    public WizardScreen(int startIndex);      // Start at specific page
    
    public void goToPage(int pageIndex);      // Navigate without creating new screen
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta);
    
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl);
    
    @Override
    public boolean shouldCloseOnEsc();        // Returns true
}
```

**Click Handling:**
- Uses `ActiveTextCollector.ClickableStyleFinder` to detect styled text under cursor
- Intercepts clicks on links with `wizard_action:` prefix
- Actions handled by `WizardActionHandler`

**Screen Preservation:**
- `goToPage()` changes page in-place without creating new WizardScreen
- Config changes trigger automatic text refresh on next render
- Screen stacking via `MinecraftScreenMixin` preserves wizard when containers open

### WizardPage (Abstract Base Class)

Base class for all tutorial pages. Supports dynamic content generation based on game state.

```java
public abstract class WizardPage {
    protected final int pageIndex;
    
    // Core content - must implement
    public abstract Component getTitle();
    public abstract Component getText(Minecraft client);  // Dynamic!
    public abstract boolean isSkipAllowed();
    
    // Lifecycle hooks
    public void onPageOpen(Minecraft client) {}
    public void onPageClose(Minecraft client) {}
    
    // Text utilities
    protected Component link(String text, String action);
    protected Component bold(String text);
    protected Component italic(String text);
    protected Component colored(String text, ChatFormatting color);
    protected Component text(String text);
    protected Component newline();
}
```

### TutorialPages (Registry)

Central registry holding all tutorial pages in order.

```java
public class TutorialPages {
    private static final List<WizardPage> PAGES = List.of(
        new Page1Welcome(),
        new Page2Alert(),
        new Page3Hud(),
        new Page4Autograb(),
        new Page5Config(),
        new Page6Done()
    );
    
    public static WizardPage getPage(int index);
    public static int getPageCount();
}
```

### WizardActionHandler

Handles link actions from clickable text.

```java
public class WizardActionHandler {
    public static void handle(String action, Minecraft client);
}
```

## Link Actions

Clickable links are created via `link(text, action)` helper method:

| Action | Description | Example |
|--------|-------------|---------|
| `page:N` | Jump to page N (0-indexed) | `link("Go to config", "page:4")` |
| `config:key:value` | Set config option (boolean) | `link("Enable", "config:autograb:true")` |
| `command:cmd` | Run server command | `link("Stats", "command:ridestats")` |
| `factory_reset` | Reset all NRA config to defaults | `link("Reset", "factory_reset")` |
| `finish` | Complete tutorial and close | `link("Done", "finish")` |

### Supported Config Keys

| Key | Description |
|-----|-------------|
| `enabled` | Enable/disable mod |
| `autograb` | Enable autograbbing |
| `hideChat` | Hide chat while riding |
| `hideScoreboard` | Hide scoreboard |
| `hideHealth` | Hide health bar |
| `hideHotbar` | Hide hotbar |

## Text Rendering

### Flow

1. **Page provides text** via `getText(Minecraft client)` returning a `Component`
2. **WizardScreen renders** in `renderBodyText()`:
   - Calls `font.split(textComponent, textWidth)` for word wrapping
   - Iterates lines, drawing each with `graphics.drawString()`
3. **Click detection** via `ActiveTextCollector.ClickableStyleFinder`
4. **Click handling** intercepts `wizard_action:` prefix and routes to `WizardActionHandler`

### Link Implementation

Links embed actions as `ClickEvent.RunCommand` with `wizard_action:` prefix:
```java
Style linkStyle = Style.EMPTY
    .withColor(ChatFormatting.AQUA)
    .withUnderlined(true)
    .withClickEvent(new ClickEvent.RunCommand("wizard_action:" + action));
```

The screen intercepts these before Minecraft tries to execute them as real commands.

## Rendering

### Book Layout
```
+--------------------------------------------------+
|                                        [X]       |  <- Close button
|    +----------------------------------------+    |
|    |                                        |    |
|    |         [CONTENT AREA]                 |    |
|    |                                        |    |
|    +----------------------------------------+    |
|                                                  |
|    [TITLE] (centered)                            |
|                                                  |
|    [Wrapped body text with word wrap]            |
|    [Supports color codes and formatting]         |
|                                                  |
|    [<]                              [>]           |  <- PageButton arrows
+--------------------------------------------------+
```

### Constants
- Book texture: 493x295
- Content area: 335x206 (starting at 80,43)
- Text margin: 10px
- Text line height: 12px

### Render Order
1. Darken background (semi-transparent overlay)
2. Draw book texture
3. Draw title (centered)
4. Draw body text (word-wrapped)
5. Draw navigation buttons (PageButton arrows + close button)

## Navigation

### Buttons

| Button | Type | Behavior |
|--------|------|----------|
| **Close (X)** | Button | Close wizard (top-right corner) |
| **Back** | PageButton | Go to previous page (hidden on page 1) |
| **Next** | PageButton | Advance to next page; on last page, finishes tutorial and closes |

### PageButton Features
- Uses Minecraft's built-in book arrow sprites (`widget/page_forward`, `widget/page_backward`)
- Includes hover states (highlighted versions)
- Plays book page turn sound when clicked

### ESC Behavior

ESC closes the wizard via the X close button.

## Lifecycle Integration

### Join Hook (NotRidingAlertClient)
```java
ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
    if (TutorialManager.getInstance().shouldStartTutorial()) {
        client.execute(() -> {
            if (client.screen == null) {
                client.setScreen(new WizardScreen());
            }
        });
    }
});
```

## Debug Command

`/setupnra` - Resets tutorial state and opens the wizard.

```java
dispatcher.register(
    ClientCommandManager.literal("setupnra")
        .executes(context -> {
            TutorialManager.getInstance().resetTutorial();
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().setScreen(new WizardScreen());
            });
            return 1;
        }));
```

## Design Decisions

1. **Package Location**: `com.chenweikeng.nra.wizard`

2. **Client-Only**: All code is client-side. Never reference Screen/Font/GuiGraphics from server code.

3. **Persistence**: Save after every state change to survive crashes.

4. **Server Check**: Only show tutorial when connected to ImagineFun server.

5. **Java-based Pages**: Pages are Java classes (not JSON) for dynamic content support.

6. **Screen Preservation**: Actions use `goToPage()` instead of creating new screens.

7. **Screen Stacking**: `MinecraftScreenMixin` preserves WizardScreen when server opens containers.

## File Reference

### Source Files
```
src/main/java/com/chenweikeng/nra/wizard/
├── TutorialManager.java
├── TutorialState.java
├── WizardScreen.java
├── WizardPage.java
├── TutorialPages.java
├── WizardActionHandler.java
└── pages/
    ├── Page1Welcome.java
    ├── Page2Alert.java
    ├── Page3Hud.java
    ├── Page4Autograb.java
    ├── Page5Config.java
    └── Page6Done.java

src/main/java/com/chenweikeng/nra/mixin/
└── MinecraftScreenMixin.java
```

### Asset Files
```
src/main/resources/assets/not-riding-alert/textures/gui/tutorial/
└── book_background.png
```

### Config Files
```
config/notridingalert_tutorial.json    # Runtime: tutorial state persistence
```
