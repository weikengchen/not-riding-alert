package com.chenweikeng.nra.audio;

import com.chenweikeng.nra.handler.ReminderHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages an OpenAudioMC audio session via a headless native webview process. Ports the logic from
 * the MonkeyCraft mobile app's OpenAudioMcService (Flutter).
 *
 * <p>The service:
 *
 * <ol>
 *   <li>Detects OpenAudioMC session URLs in chat messages
 *   <li>Launches a native webview helper process (hidden window with audio)
 *   <li>Polls the DOM every 3 seconds to automate the session
 *   <li>Auto-clicks "Start Audio Session" when the button appears
 *   <li>Detects active audio via presence of a volume slider (input[type="range"])
 *   <li>Reports connection status to {@link ReminderHandler}
 * </ol>
 */
public class OpenAudioMcService {
  private static final Logger LOGGER = LoggerFactory.getLogger("OpenAudioMcService");

  private static final String URL_PREFIX = "https://session.openaudiomc.net/";
  private static final int MAX_RECONNECT_ATTEMPTS = 3;
  private static final int MAX_MID_SESSION_DROP_ATTEMPTS = 3;
  private static final int MONITOR_INTERVAL_MS = 3000;
  private static final int CONNECTION_TIMEOUT_MS = 60000;

  /** JavaScript injected every 3 seconds to check DOM state. */
  private static final String STATUS_CHECK_JS =
      """
      (function() {
        var rangeInput = document.querySelector('input[type="range"]');
        var hasRangeInput = !!rangeInput;

        var buttons = Array.prototype.slice.call(document.querySelectorAll('button, [role="button"]'));
        var hasStartButton = buttons.some(
          function(el) { return (el.outerText || el.textContent || '').trim().toLowerCase() === 'start audio session'; }
        );

        var currentUrl = window.location.href;
        var bodyLen = (document.body && document.body.innerHTML) ? document.body.innerHTML.length : 0;

        return {
          hasRangeInput: hasRangeInput,
          hasStartButton: hasStartButton,
          currentUrl: currentUrl,
          hasSession: currentUrl.indexOf('session=') !== -1 || currentUrl.indexOf('#') !== -1,
          bodyLength: bodyLen
        };
      })();
      """;

  /** One-time JS to dump full page structure for debugging. */
  private static final String PAGE_DUMP_JS =
      """
      (function() {
        var rootEl = document.getElementById('root') || document.getElementById('app');
        var rootHTML = rootEl ? rootEl.innerHTML : (document.body ? document.body.innerHTML : '');
        // Log full HTML to console so it flows through the console channel
        console.log('PAGE_DUMP_HTML_LENGTH:' + rootHTML.length);
        // Split into chunks to avoid truncation
        var chunk = 800;
        for (var i = 0; i < rootHTML.length; i += chunk) {
          console.log('PAGE_DUMP[' + i + ']:' + rootHTML.substring(i, i + chunk));
        }
        return { dumped: true, length: rootHTML.length };
      })();
      """;

  /** JavaScript to auto-click the "Start Audio Session" button using synthetic events. */
  private static final String CLICK_START_JS =
      """
      (function() {
        try {
          var buttons = Array.prototype.slice.call(document.querySelectorAll('button, [role="button"]'));
          var btn = buttons.find(
            function(b) { return (b.outerText || b.textContent || '').trim().toLowerCase() === 'start audio session'; }
          );
          if (!btn) return { clicked: false, error: 'button not found' };

          var rect = btn.getBoundingClientRect();
          var cx = rect.left + rect.width / 2;
          var cy = rect.top + rect.height / 2;
          var common = { bubbles: true, cancelable: true, view: window,
                         clientX: cx, clientY: cy, screenX: cx, screenY: cy,
                         button: 0, buttons: 1 };

          // Dispatch synthetic mouse events (PointerEvent may not exist in WKWebView)
          try {
            btn.dispatchEvent(new PointerEvent('pointerdown', common));
            btn.dispatchEvent(new PointerEvent('pointerup', common));
          } catch(pe) {
            console.warn('[NRA] PointerEvent not available: ' + pe);
          }

          btn.dispatchEvent(new MouseEvent('mousedown', common));
          btn.dispatchEvent(new MouseEvent('mouseup', common));
          btn.dispatchEvent(new MouseEvent('click', common));

          console.log('[NRA] Button clicked, rect=' + JSON.stringify({x:rect.x,y:rect.y,w:rect.width,h:rect.height}));

          // Force-resume all AudioContexts after a short delay
          setTimeout(function() {
            try {
              if (window.__nra_resumeAllAudio) {
                var r = window.__nra_resumeAllAudio();
                console.log('[NRA] Post-click audio resume: ' + JSON.stringify(r));
              } else {
                console.log('[NRA] __nra_resumeAllAudio not available (polyfill not loaded?)');
              }
            } catch(e) {
              console.error('[NRA] Post-click resume error: ' + e);
            }
          }, 500);

          return { clicked: true };
        } catch(e) {
          console.error('[NRA] CLICK_START_JS error: ' + e);
          return { clicked: false, error: String(e) };
        }
      })();
      """;

  private static OpenAudioMcService instance;

  private WebViewBridge bridge;
  private String savedSessionUrl;
  private boolean isConnected;
  private boolean isActive;
  private boolean hasReportedFailure;
  private boolean hasDumpedPage;
  private int reconnectAttempts;
  private int midSessionDropAttempts;
  private volatile boolean serverEndedSession;
  private ScheduledExecutorService scheduler;
  private ScheduledFuture<?> monitorTask;
  private long monitorStartTimeMs;

  private OpenAudioMcService() {}

  public static OpenAudioMcService getInstance() {
    if (instance == null) {
      instance = new OpenAudioMcService();
    }
    return instance;
  }

  /** Returns true if the URL is an OpenAudioMC session URL. */
  public static boolean isOpenAudioMcUrl(String url) {
    return url != null && url.startsWith(URL_PREFIX);
  }

  /**
   * Scans a chat Component tree for OpenAudioMC session URLs in ClickEvents. Returns the first
   * matching URL, or null if none found.
   */
  public static String extractSessionUrl(Component component) {
    String url = extractUrlFromStyle(component.getStyle());
    if (url != null) {
      return url;
    }
    for (Component sibling : component.getSiblings()) {
      url = extractSessionUrl(sibling);
      if (url != null) {
        return url;
      }
    }
    return null;
  }

  private static String extractUrlFromStyle(Style style) {
    if (style == null) {
      return null;
    }
    ClickEvent clickEvent = style.getClickEvent();
    if (clickEvent instanceof ClickEvent.OpenUrl openUrl) {
      String value = openUrl.uri().toString();
      if (isOpenAudioMcUrl(value)) {
        return value;
      }
    }
    return null;
  }

  /**
   * Starts a new audio session. Launches the webview helper if needed, loads the session URL, and
   * begins DOM monitoring.
   */
  public void connect(String sessionUrl) {
    // Deduplicate: if already active with the same URL, ignore
    if (isActive && sessionUrl.equals(savedSessionUrl)) {
      LOGGER.debug("Ignoring duplicate connect for same URL");
      return;
    }
    if (isActive) {
      LOGGER.info("OpenAudioMC already active with different URL, disconnecting first");
      disconnect();
    }

    LOGGER.info("Connecting to OpenAudioMC: {}", sessionUrl);

    if (bridge == null) {
      bridge = new WebViewBridge();
      if (!bridge.start()) {
        LOGGER.error("Failed to start WebView bridge — OpenAudioMC audio will not work");
        return;
      }
    }

    savedSessionUrl = sessionUrl;
    isActive = true;
    isConnected = false;
    hasReportedFailure = false;
    hasDumpedPage = false;
    reconnectAttempts = 0;
    midSessionDropAttempts = 0;
    serverEndedSession = false;
    monitorStartTimeMs = System.currentTimeMillis();

    ReminderHandler.getInstance().setAudioConnected(false);

    bridge.loadUrl(sessionUrl);
    startMonitoring();
  }

  /** Stops the current audio session. Navigates the webview to about:blank (stops audio). */
  public void disconnect() {
    LOGGER.info("Disconnecting OpenAudioMC");
    stopMonitoring();
    isActive = false;
    isConnected = false;
    ReminderHandler.getInstance().setAudioConnected(false);

    if (bridge != null) {
      bridge.loadUrl("about:blank");
    }
  }

  /** Attempts to reconnect using the last known session URL. */
  public void reconnect() {
    if (savedSessionUrl == null) {
      return;
    }
    LOGGER.info("Reconnecting to OpenAudioMC");
    isConnected = false;
    hasReportedFailure = false;
    monitorStartTimeMs = System.currentTimeMillis();

    if (bridge != null) {
      bridge.loadUrl(savedSessionUrl);
    }
    startMonitoring();
  }

  /**
   * Lightweight check called when the app/game returns from background. Verifies the volume slider
   * still exists; if not, triggers a full reconnect.
   */
  public void softRefresh() {
    if (bridge == null || !isActive || !isConnected) {
      return;
    }
    bridge
        .evaluateJs(
            "(function(){ return {value: !!document.querySelector('input[type=\"range\"]')}; })()")
        .thenAccept(
            result -> {
              if (result != null && !result.optBoolean("value", true)) {
                LOGGER.info("Session dropped during suspension, reconnecting");
                reconnect();
              }
            });
  }

  /** Full cleanup: stops monitoring, kills the helper process, nulls all references. */
  public void dispose() {
    stopMonitoring();
    if (scheduler != null) {
      scheduler.shutdownNow();
      scheduler = null;
    }
    if (bridge != null) {
      bridge.stop();
      bridge = null;
    }
    savedSessionUrl = null;
    isActive = false;
    isConnected = false;
  }

  /**
   * Called when the server chat says "You are now connected with the audio client!" — confirms the
   * server recognizes the connection as live.
   */
  public void onServerConfirmedConnection() {
    serverEndedSession = false;
    midSessionDropAttempts = 0;
  }

  /**
   * Called when the server chat says "Your audio session has been ended" — the server has
   * terminated the session, so reconnecting with the same URL won't help.
   */
  public void onServerEndedSession() {
    LOGGER.info("Server ended the audio session");
    serverEndedSession = true;
    if (isConnected) {
      isConnected = false;
      ReminderHandler.getInstance().setAudioConnected(false);
    }
  }

  public boolean isConnected() {
    return isConnected;
  }

  public boolean isActive() {
    return isActive;
  }

  private void startMonitoring() {
    stopMonitoring();
    if (scheduler == null || scheduler.isShutdown()) {
      scheduler =
          Executors.newSingleThreadScheduledExecutor(
              r -> {
                Thread t = new Thread(r, "OpenAudioMC-Monitor");
                t.setDaemon(true);
                return t;
              });
    }
    monitorTask =
        scheduler.scheduleAtFixedRate(
            this::monitorSession, MONITOR_INTERVAL_MS, MONITOR_INTERVAL_MS, TimeUnit.MILLISECONDS);
  }

  private void stopMonitoring() {
    if (monitorTask != null) {
      monitorTask.cancel(false);
      monitorTask = null;
    }
  }

  private void monitorSession() {
    if (bridge == null || !bridge.isRunning() || !isActive) {
      return;
    }

    bridge
        .evaluateJs(STATUS_CHECK_JS)
        .thenAccept(this::handleMonitorResult)
        .exceptionally(
            ex -> {
              LOGGER.error("Monitor eval failed", ex);
              return null;
            });
  }

  private void handleMonitorResult(JSONObject result) {
    if (result == null || !isActive) {
      LOGGER.debug("Monitor result null or inactive: result={}, isActive={}", result, isActive);
      return;
    }

    LOGGER.info(
        "DOM poll: rangeInput={}, startBtn={}, bodyLen={}",
        result.optBoolean("hasRangeInput", false),
        result.optBoolean("hasStartButton", false),
        result.optInt("bodyLength", -1));

    // One-time full HTML dump for debugging
    if (!hasDumpedPage && result.optInt("bodyLength", 0) > 0 && bridge != null) {
      hasDumpedPage = true;
      LOGGER.info("Triggering one-time page HTML dump...");
      bridge.evaluateJs(PAGE_DUMP_JS);
    }

    boolean hasRangeInput = result.optBoolean("hasRangeInput", false);
    boolean hasStartButton = result.optBoolean("hasStartButton", false);
    String currentUrl = result.optString("currentUrl", "");
    boolean hasSession = result.optBoolean("hasSession", false);
    boolean wasConnected = isConnected;

    if (hasRangeInput) {
      // Audio session is active
      if (!isConnected) {
        LOGGER.info("OpenAudioMC audio session connected");
        isConnected = true;
        reconnectAttempts = 0;
        ReminderHandler.getInstance().setAudioConnected(true);
      }
      // Update saved URL if it changed
      if (hasSession && !currentUrl.equals(savedSessionUrl)) {
        savedSessionUrl = currentUrl;
      }
    } else if (hasStartButton) {
      // Page loaded but session not started — auto-click the button
      LOGGER.info("Auto-clicking 'Start Audio Session' button");
      bridge
          .evaluateJs(CLICK_START_JS)
          .thenAccept(r -> LOGGER.info("Click result: {}", r))
          .exceptionally(
              ex -> {
                LOGGER.error("Click eval failed", ex);
                return null;
              });
    } else if (!hasSession
        && !currentUrl.startsWith(URL_PREFIX)
        && savedSessionUrl != null
        && wasConnected) {
      // Session was lost (page navigated away or crashed)
      if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        reconnectAttempts++;
        LOGGER.warn(
            "Session lost, reconnecting (attempt {}/{})",
            reconnectAttempts,
            MAX_RECONNECT_ATTEMPTS);
        isConnected = false;
        ReminderHandler.getInstance().setAudioConnected(false);
        monitorStartTimeMs = System.currentTimeMillis();
        bridge.loadUrl(savedSessionUrl);
      } else {
        handleFailure("max_reconnect");
      }
    } else {
      // Check for connection timeout
      long elapsed = System.currentTimeMillis() - monitorStartTimeMs;
      if (elapsed >= CONNECTION_TIMEOUT_MS && !isConnected && !hasReportedFailure) {
        handleFailure("timeout");
      }
    }

    // Detect mid-session drop (was connected but volume slider disappeared)
    if (wasConnected && !hasRangeInput) {
      isConnected = false;
      ReminderHandler.getInstance().setAudioConnected(false);

      if (serverEndedSession) {
        // Server already told us the session is over — no point reconnecting
        LOGGER.info("Audio session ended by server, closing gracefully");
        handleFailure("server_ended");
      } else if (midSessionDropAttempts < MAX_MID_SESSION_DROP_ATTEMPTS) {
        midSessionDropAttempts++;
        LOGGER.warn(
            "Audio session dropped, reconnecting (attempt {}/{})",
            midSessionDropAttempts,
            MAX_MID_SESSION_DROP_ATTEMPTS);
        monitorStartTimeMs = System.currentTimeMillis();
        bridge.loadUrl(savedSessionUrl);
      } else {
        LOGGER.error("Audio session dropped too many times, giving up");
        handleFailure("mid_session_drop");
        notifyUser(
            "Audio session lost after multiple reconnection attempts. Use /audio to reconnect.");
      }
    }
  }

  private void handleFailure(String reason) {
    LOGGER.error("OpenAudioMC connection failed: {}", reason);
    hasReportedFailure = true;
    isActive = false;
    isConnected = false;
    stopMonitoring();
    ReminderHandler.getInstance().setAudioConnected(false);

    if (bridge != null) {
      bridge.stop();
      bridge = null;
    }
    savedSessionUrl = null;
  }

  private void notifyUser(String message) {
    Minecraft client = Minecraft.getInstance();
    if (client != null) {
      client.execute(
          () ->
              client.gui.getChat().addMessage(Component.literal("\u00A7e[NRA] \u00A7f" + message)));
    }
  }
}
