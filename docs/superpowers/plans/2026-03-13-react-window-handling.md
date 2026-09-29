# React Multi-Window Handling Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add multi-window support to the React UI layer so commands can open new browser windows with server-defined content, and windows can communicate through the server.

**Architecture:** Server-side `ReactWindowRegistry` pre-creates control trees and enqueues structured SSE events. Client-side `window-manager.ts` listens for these events and manages browser windows. `ViewServlet` is extended to serve pre-created windows. All communication is server-only via existing SSE/command infrastructure.

**Tech Stack:** Java 17, msgbuf protocol buffers, TypeScript/React 19, Vite, SSE (Server-Sent Events)

**Spec:** `docs/superpowers/specs/2026-03-13-react-window-handling-design.md`

---

## Chunk 1: SSE Protocol — New Window Event Types

### Task 1: Add window lifecycle events to the msgbuf protocol

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/protocol/sse.proto`

The existing protocol defines events like `StateEvent`, `PatchEvent`, etc. (lines 11-119). Add three new event types following the same pattern.

- [ ] **Step 1: Add WindowOpenEvent to sse.proto**

Append after the last message definition (after `FunctionCall`):

```protobuf
/**
 * Instructs the target window to open a new browser window.
 * Only the window matching targetWindowId should act on this event.
 */
message WindowOpenEvent extends SSEEvent {
	/** The window ID of the opener (for client-side filtering). */
	string targetWindowId;

	/** The window ID for the new window. */
	string windowId;

	/** Window width in pixels. */
	int width;

	/** Window height in pixels. */
	int height;

	/** Window title. */
	string title;

	/** Whether the window is resizable. */
	boolean resizable;
}

/**
 * Instructs the target window to close a previously opened window.
 */
message WindowCloseEvent extends SSEEvent {
	/** The window ID of the window that opened the target. */
	string targetWindowId;

	/** The window ID to close. */
	string windowId;
}

/**
 * Instructs the target window to focus a previously opened window.
 */
message WindowFocusEvent extends SSEEvent {
	/** The window ID of the window that opened the target. */
	string targetWindowId;

	/** The window ID to focus. */
	string windowId;
}
```

- [ ] **Step 2: Generate Java classes from the proto file**

Run:
```bash
cd com.top_logic.layout.react
mvn de.haumacher.msgbuf:msgbuf-generator-maven-plugin:1.1.11:generate
```

Expected: New Java classes generated in `src/main/java/com/top_logic/layout/react/protocol/` — `WindowOpenEvent.java`, `WindowCloseEvent.java`, `WindowFocusEvent.java`. Each extends `SSEEvent`.

- [ ] **Step 3: Verify generated classes compile**

Run:
```bash
cd com.top_logic.layout.react
mvn compile -DskipTests=true
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/protocol/
git commit -m "Ticket #29108: Add window lifecycle SSE event types (WindowOpenEvent, WindowCloseEvent, WindowFocusEvent)."
```

---

## Chunk 2: Server-Side Window Registry

All window registry classes live in `com.top_logic.layout.react` (not `com.top_logic.layout.view`) to avoid a circular dependency. The dependency direction is `view -> react`, so `ReactServlet` (in react) can import the registry without issue, and `ViewServlet` (in view) can also import it since view already depends on react.

### Task 2: Create WindowOptions configuration

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/WindowOptions.java`

- [ ] **Step 1: Write the test**

Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/window/TestWindowOptions.java`

(Tests live in the view module which has the test infrastructure already set up.)

```java
package test.com.top_logic.layout.view.window;

import junit.framework.TestCase;

import com.top_logic.layout.react.window.WindowOptions;

public class TestWindowOptions extends TestCase {

	public void testDefaults() {
		WindowOptions options = new WindowOptions();
		assertEquals(800, options.getWidth());
		assertEquals(600, options.getHeight());
		assertEquals("", options.getTitle());
		assertTrue(options.isResizable());
	}

	public void testBuilder() {
		WindowOptions options = new WindowOptions()
			.setWidth(1024)
			.setHeight(768)
			.setTitle("Help")
			.setResizable(false);

		assertEquals(1024, options.getWidth());
		assertEquals(768, options.getHeight());
		assertEquals("Help", options.getTitle());
		assertFalse(options.isResizable());
	}
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
cd com.top_logic.layout.view
mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.view.window.TestWindowOptions
```

Expected: FAIL — `WindowOptions` class does not exist

- [ ] **Step 3: Implement WindowOptions**

```java
package com.top_logic.layout.react.window;

/**
 * Configuration for a programmatically opened React window.
 */
public class WindowOptions {

	private int _width = 800;

	private int _height = 600;

	private String _title = "";

	private boolean _resizable = true;

	/** Window width in pixels. */
	public int getWidth() {
		return _width;
	}

	/** @see #getWidth() */
	public WindowOptions setWidth(int width) {
		_width = width;
		return this;
	}

	/** Window height in pixels. */
	public int getHeight() {
		return _height;
	}

	/** @see #getHeight() */
	public WindowOptions setHeight(int height) {
		_height = height;
		return this;
	}

	/** Window title. */
	public String getTitle() {
		return _title;
	}

	/** @see #getTitle() */
	public WindowOptions setTitle(String title) {
		_title = title;
		return this;
	}

	/** Whether the window is resizable by the user. */
	public boolean isResizable() {
		return _resizable;
	}

	/** @see #isResizable() */
	public WindowOptions setResizable(boolean resizable) {
		_resizable = resizable;
		return this;
	}
}
```

- [ ] **Step 4: Run test to verify it passes**

First install the react module so the view module picks up the new class:
```bash
cd com.top_logic.layout.react
mvn install -DskipTests=true
```

Then run the test:
```bash
cd com.top_logic.layout.view
mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.view.window.TestWindowOptions
```

Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/WindowOptions.java
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/window/TestWindowOptions.java
git commit -m "Ticket #29108: Add WindowOptions for React window configuration."
```

---

### Task 3: Create WindowEntry

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/WindowEntry.java`

- [ ] **Step 1: Write the test**

Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/window/TestWindowEntry.java`

```java
package test.com.top_logic.layout.view.window;

import junit.framework.TestCase;

import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.layout.react.window.WindowOptions;

public class TestWindowEntry extends TestCase {

	public void testCreation() {
		WindowOptions options = new WindowOptions().setWidth(1024).setTitle("Test");
		WindowEntry entry = new WindowEntry("vNewWindow", "vOpener", null, options);

		assertEquals("vNewWindow", entry.getWindowId());
		assertEquals("vOpener", entry.getOpenerWindowId());
		assertNull(entry.getRootControl());
		assertEquals(1024, entry.getOptions().getWidth());
		assertFalse(entry.isConnected());
	}

	public void testMarkConnected() {
		WindowEntry entry = new WindowEntry("vW1", "vOpener", null, new WindowOptions());
		assertFalse(entry.isConnected());

		entry.markConnected();
		assertTrue(entry.isConnected());
	}
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
cd com.top_logic.layout.view
mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.view.window.TestWindowEntry
```

Expected: FAIL — class does not exist

- [ ] **Step 3: Implement WindowEntry**

```java
package com.top_logic.layout.react.window;

import com.top_logic.layout.react.IReactControl;

/**
 * Tracks a single programmatically opened React window.
 */
public class WindowEntry {

	private final String _windowId;

	private final String _openerWindowId;

	private final IReactControl _rootControl;

	private final WindowOptions _options;

	private boolean _connected;

	/**
	 * Creates a new {@link WindowEntry}.
	 *
	 * @param windowId
	 *        The unique ID for this window.
	 * @param openerWindowId
	 *        The window ID of the opener.
	 * @param rootControl
	 *        The pre-built control tree for this window, or null if not yet created.
	 * @param options
	 *        The window display options.
	 */
	public WindowEntry(String windowId, String openerWindowId, IReactControl rootControl,
			WindowOptions options) {
		_windowId = windowId;
		_openerWindowId = openerWindowId;
		_rootControl = rootControl;
		_options = options;
	}

	/** The unique ID for this window. */
	public String getWindowId() {
		return _windowId;
	}

	/** The window ID of the opener. */
	public String getOpenerWindowId() {
		return _openerWindowId;
	}

	/** The pre-built control tree for this window. */
	public IReactControl getRootControl() {
		return _rootControl;
	}

	/** The window display options. */
	public WindowOptions getOptions() {
		return _options;
	}

	/** Whether the browser window has connected via SSE. */
	public boolean isConnected() {
		return _connected;
	}

	/** Marks this window as connected. */
	public void markConnected() {
		_connected = true;
	}
}
```

- [ ] **Step 4: Run test to verify it passes**

First install the react module:
```bash
cd com.top_logic.layout.react
mvn install -DskipTests=true
```

Then run the test:
```bash
cd com.top_logic.layout.view
mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.view.window.TestWindowEntry
```

Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/WindowEntry.java
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/window/TestWindowEntry.java
git commit -m "Ticket #29108: Add WindowEntry to track programmatically opened React windows."
```

---

### Task 4: Create ReactWindowRegistry

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/ReactWindowRegistry.java`

**Context:** This is the central server-side class. It stores `WindowEntry` instances, generates window IDs, creates sub-sessions, builds control trees, and enqueues SSE events. It uses the existing `SSEUpdateQueue` (session-scoped, stored as session attribute `"tl.react.sseQueue"`) to enqueue `WindowOpenEvent`s. Lives in the react module so `ReactServlet` can import it without circular dependencies.

Window IDs must use the `'v'` prefix because `ViewServlet.extractWindowName()` (line 173 of `ViewServlet.java`) rejects names that don't start with `'v'`.

- [ ] **Step 1: Write the test**

Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/window/TestReactWindowRegistry.java`

```java
package test.com.top_logic.layout.view.window;

import java.util.concurrent.ConcurrentLinkedQueue;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.layout.react.window.WindowOptions;

public class TestReactWindowRegistry extends TestCase {

	public void testOpenWindowCreatesEntry() {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		ReactWindowRegistry registry = new ReactWindowRegistry(queue);

		ReactContext openerCtx = new DefaultReactContext("", "vOpener", queue);
		WindowOptions options = new WindowOptions().setWidth(1024).setTitle("Test");

		String windowId = registry.openWindow(openerCtx, options);

		assertNotNull(windowId);
		assertTrue("Window ID must start with 'v'", windowId.startsWith("v"));

		WindowEntry entry = registry.getWindow(windowId);
		assertNotNull(entry);
		assertEquals(windowId, entry.getWindowId());
		assertEquals("vOpener", entry.getOpenerWindowId());
		assertEquals(1024, entry.getOptions().getWidth());
		assertFalse(entry.isConnected());
	}

	public void testWindowIdsAreUnique() {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		ReactWindowRegistry registry = new ReactWindowRegistry(queue);
		ReactContext ctx = new DefaultReactContext("", "vOpener", queue);

		String id1 = registry.openWindow(ctx, new WindowOptions());
		String id2 = registry.openWindow(ctx, new WindowOptions());

		assertNotSame(id1, id2);
		assertFalse(id1.equals(id2));
	}

	public void testWindowClosed() {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		ReactWindowRegistry registry = new ReactWindowRegistry(queue);
		ReactContext ctx = new DefaultReactContext("", "vOpener", queue);

		String windowId = registry.openWindow(ctx, new WindowOptions());
		assertNotNull(registry.getWindow(windowId));

		registry.windowClosed(windowId);
		assertNull(registry.getWindow(windowId));
	}

	public void testGetNonexistentWindow() {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		ReactWindowRegistry registry = new ReactWindowRegistry(queue);

		assertNull(registry.getWindow("vDoesNotExist"));
	}

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestReactWindowRegistry.class,
			TypeIndex.Module.INSTANCE);
	}
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
cd com.top_logic.layout.react
mvn install -DskipTests=true
cd ../com.top_logic.layout.view
mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.view.window.TestReactWindowRegistry
```

Expected: FAIL — class does not exist

- [ ] **Step 3: Implement ReactWindowRegistry**

```java
package com.top_logic.layout.react.window;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import com.top_logic.layout.react.IReactControl;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.protocol.WindowOpenEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Per-session registry that manages programmatically opened React windows.
 *
 * <p>
 * Stored as an HTTP session attribute alongside {@link SSEUpdateQueue}. Tracks open windows, their
 * control trees, and enqueues lifecycle SSE events.
 * </p>
 */
public class ReactWindowRegistry implements HttpSessionBindingListener {

	private static final String SESSION_ATTRIBUTE_KEY = "tl.react.windowRegistry";

	private static final SecureRandom RANDOM = new SecureRandom();

	private final ConcurrentHashMap<String, WindowEntry> _windows = new ConcurrentHashMap<>();

	private final SSEUpdateQueue _sseQueue;

	/**
	 * Creates a new {@link ReactWindowRegistry}.
	 *
	 * @param sseQueue
	 *        The session's SSE queue for enqueuing window events.
	 */
	public ReactWindowRegistry(SSEUpdateQueue sseQueue) {
		_sseQueue = sseQueue;
	}

	/**
	 * Gets the registry for the given session, creating it if necessary.
	 */
	public static ReactWindowRegistry forSession(HttpSession session) {
		ReactWindowRegistry registry =
			(ReactWindowRegistry) session.getAttribute(SESSION_ATTRIBUTE_KEY);
		if (registry == null) {
			synchronized (session) {
				registry = (ReactWindowRegistry) session.getAttribute(SESSION_ATTRIBUTE_KEY);
				if (registry == null) {
					SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
					registry = new ReactWindowRegistry(queue);
					session.setAttribute(SESSION_ATTRIBUTE_KEY, registry);
				}
			}
		}
		return registry;
	}

	/**
	 * Opens a new window without a pre-built control tree.
	 *
	 * <p>
	 * The control tree will be set later via {@link WindowEntry#setRootControl(IReactControl)}
	 * before the window connects, or built by {@link ViewServlet} from a view path.
	 * </p>
	 *
	 * @param openerContext
	 *        The opener window's {@link ReactContext}.
	 * @param options
	 *        Window display options.
	 * @return The generated window ID.
	 */
	public String openWindow(ReactContext openerContext, WindowOptions options) {
		return openWindow(openerContext, null, options);
	}

	/**
	 * Opens a new window with a pre-built control tree.
	 *
	 * @param openerContext
	 *        The opener window's {@link ReactContext}.
	 * @param rootControl
	 *        The pre-built control tree for the new window, or null.
	 * @param options
	 *        Window display options.
	 * @return The generated window ID.
	 */
	public String openWindow(ReactContext openerContext, IReactControl rootControl,
			WindowOptions options) {
		String windowId = generateWindowId();
		String openerWindowId = openerContext.getWindowName();

		WindowEntry entry = new WindowEntry(windowId, openerWindowId, rootControl, options);
		_windows.put(windowId, entry);

		WindowOpenEvent event = WindowOpenEvent.create()
			.setTargetWindowId(openerWindowId)
			.setWindowId(windowId)
			.setWidth(options.getWidth())
			.setHeight(options.getHeight())
			.setTitle(options.getTitle())
			.setResizable(options.isResizable());
		_sseQueue.enqueue(event);

		return windowId;
	}

	/**
	 * Looks up a window by its ID.
	 *
	 * @return The window entry, or null if not found.
	 */
	public WindowEntry getWindow(String windowId) {
		return _windows.get(windowId);
	}

	/**
	 * Called when a window is closed (either by the user or programmatically).
	 * Cleans up the control tree and removes the entry.
	 */
	public void windowClosed(String windowId) {
		WindowEntry entry = _windows.remove(windowId);
		if (entry != null) {
			IReactControl rootControl = entry.getRootControl();
			if (rootControl instanceof ReactControl) {
				((ReactControl) rootControl).cleanupTree();
			}
		}
	}

	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		// Nothing to do.
	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		for (WindowEntry entry : _windows.values()) {
			IReactControl rootControl = entry.getRootControl();
			if (rootControl instanceof ReactControl) {
				((ReactControl) rootControl).cleanupTree();
			}
		}
		_windows.clear();
	}

	private String generateWindowId() {
		byte[] bytes = new byte[8];
		RANDOM.nextBytes(bytes);
		StringBuilder sb = new StringBuilder("v");
		for (byte b : bytes) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}
}
```

- [ ] **Step 4: Run test to verify it passes**

First install the react module:
```bash
cd com.top_logic.layout.react
mvn install -DskipTests=true
```

Then run the test:
```bash
cd com.top_logic.layout.view
mvn test -DskipTests=false -Dtest=test.com.top_logic.layout.view.window.TestReactWindowRegistry
```

Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/ReactWindowRegistry.java
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/window/TestReactWindowRegistry.java
git commit -m "Ticket #29108: Add ReactWindowRegistry for managing programmatically opened React windows."
```

---

## Chunk 3: ViewServlet Integration

### Task 5: Modify ViewServlet to serve pre-created windows

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java`

**Context:** `ViewServlet.doGet()` (lines 69-111) currently resolves a view path, loads it via `ViewLoader`, creates a `ReactContext` + `ViewContext`, and calls `view.createControl()`. We add a check: after extracting the window name but before loading the view, check `ReactWindowRegistry` for a pre-created `WindowEntry`. If found, use its root control and skip view loading.

- [ ] **Step 1: Add the registry lookup to doGet()**

In `ViewServlet.doGet()`, after `ensureSubSession(request, windowName)` and before `String viewPath = resolveViewPath(pathInfo)`, insert:

```java
// Check if this is a programmatically opened window with a pre-created control tree.
ReactWindowRegistry windowRegistry = ReactWindowRegistry.forSession(session);
WindowEntry windowEntry = windowRegistry.getWindow(windowName);
if (windowEntry != null) {
    windowEntry.markConnected();
    IReactControl rootControl = windowEntry.getRootControl();
    if (rootControl != null) {
        ReactContext displayContext = new DefaultReactContext(
            request.getContextPath(), windowName, SSEUpdateQueue.forSession(session));
        renderPage(request, response, rootControl, displayContext);
        return;
    }
}
```

Add imports at the top:
```java
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.layout.react.IReactControl;
```

- [ ] **Step 2: Verify it compiles**

Run:
```bash
cd com.top_logic.layout.view
mvn compile -DskipTests=true
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java
git commit -m "Ticket #29108: ViewServlet serves pre-created windows from ReactWindowRegistry."
```

---

## Chunk 4: Client-Side Window Manager

### Task 6: Create window-manager.ts

**Files:**
- Create: `com.top_logic.layout.react/react-src/bridge/window-manager.ts`

**Context:** This module manages browser windows in response to SSE events. It reads the current window's name from `document.body.dataset.windowName` (set by `ViewServlet.renderPage()` at line ~295). It provides handler functions called from `sse-client.ts`.

- [ ] **Step 1: Create window-manager.ts**

```typescript
/**
 * Manages browser windows opened via server SSE events.
 *
 * Listens for WindowOpenEvent, WindowCloseEvent, WindowFocusEvent from the SSE stream.
 * Only acts on events where targetWindowId matches the current page's window name.
 */

/** Registry of windows opened from this page. */
const openWindows: Map<string, Window> = new Map();

/** Interval ID for polling closed windows. */
let pollIntervalId: ReturnType<typeof setInterval> | null = null;

/** The current page's window name. */
function getMyWindowId(): string {
  return document.body.dataset.windowName ?? '';
}

/** The context path for URL construction. */
function getContextPath(): string {
  return document.body.dataset.contextPath ?? '';
}

/** Convert WindowOpenEvent options to window.open() features string. */
function buildFeatureString(event: WindowOpenEventData): string {
  const parts: string[] = [];
  if (event.width) parts.push(`width=${event.width}`);
  if (event.height) parts.push(`height=${event.height}`);
  parts.push(`resizable=${event.resizable ? 'yes' : 'no'}`);
  return parts.join(',');
}

/** Start polling for user-closed windows if not already running. */
function ensurePolling(): void {
  if (pollIntervalId !== null) return;
  pollIntervalId = setInterval(() => {
    for (const [windowId, ref] of openWindows) {
      if (ref.closed) {
        openWindows.delete(windowId);
        notifyWindowClosed(windowId);
      }
    }
    if (openWindows.size === 0 && pollIntervalId !== null) {
      clearInterval(pollIntervalId);
      pollIntervalId = null;
    }
  }, 2000);
}

/** Notify server that a window was closed by the user. */
function notifyWindowClosed(windowId: string): void {
  const contextPath = getContextPath();
  const myWindowId = getMyWindowId();
  fetch(`${contextPath}/react-api/command`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      controlId: '',
      command: 'windowClosed',
      windowName: myWindowId,
      arguments: { windowId },
    }),
  }).catch(() => {
    // Best-effort notification. Server will clean up via heartbeat timeout if this fails.
  });
}

// Data types matching msgbuf-generated event shapes.
export interface WindowOpenEventData {
  targetWindowId: string;
  windowId: string;
  width: number;
  height: number;
  title: string;
  resizable: boolean;
}

export interface WindowCloseEventData {
  targetWindowId: string;
  windowId: string;
}

export interface WindowFocusEventData {
  targetWindowId: string;
  windowId: string;
}

/** Handle a WindowOpenEvent from SSE. */
export function handleWindowOpen(event: WindowOpenEventData): void {
  if (event.targetWindowId !== getMyWindowId()) return;

  const contextPath = getContextPath();
  const url = `${contextPath}/view/${event.windowId}/`;
  const ref = window.open(url, event.windowId, buildFeatureString(event));

  if (ref) {
    openWindows.set(event.windowId, ref);
    ensurePolling();
  } else {
    // Popup was blocked.
    notifyWindowBlocked(event.windowId);
  }
}

/** Handle a WindowCloseEvent from SSE. */
export function handleWindowClose(event: WindowCloseEventData): void {
  if (event.targetWindowId !== getMyWindowId()) return;

  const ref = openWindows.get(event.windowId);
  if (ref) {
    ref.close();
    openWindows.delete(event.windowId);
  }
}

/** Handle a WindowFocusEvent from SSE. */
export function handleWindowFocus(event: WindowFocusEventData): void {
  if (event.targetWindowId !== getMyWindowId()) return;

  const ref = openWindows.get(event.windowId);
  if (ref && !ref.closed) {
    ref.focus();
  }
}

/** Notify server that a popup was blocked. */
function notifyWindowBlocked(windowId: string): void {
  const contextPath = getContextPath();
  const myWindowId = getMyWindowId();
  fetch(`${contextPath}/react-api/command`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      controlId: '',
      command: 'windowBlocked',
      windowName: myWindowId,
      arguments: { windowId },
    }),
  }).catch(() => {
    // Best-effort.
  });
}

/**
 * Send a self-close notification on page unload.
 * Handles the case where the opener has closed and nobody is polling this window.
 */
export function initSelfCloseNotification(): void {
  window.addEventListener('beforeunload', () => {
    const contextPath = getContextPath();
    const myWindowId = getMyWindowId();
    if (!myWindowId) return;

    // Use sendBeacon for reliability during unload.
    // Wrap in Blob with application/json Content-Type since sendBeacon with
    // a plain string sends text/plain, which may not be parsed correctly.
    const payload = JSON.stringify({
      controlId: '',
      command: 'windowClosed',
      windowName: myWindowId,
      arguments: { windowId: myWindowId },
    });
    const blob = new Blob([payload], { type: 'application/json' });
    navigator.sendBeacon(`${contextPath}/react-api/command`, blob);
  });
}
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.react/react-src/bridge/window-manager.ts
git commit -m "Ticket #29108: Add client-side window-manager.ts for React multi-window lifecycle."
```

---

### Task 7: Integrate window-manager.ts into sse-client.ts and bridge-entry.ts

**Files:**
- Modify: `com.top_logic.layout.react/react-src/bridge/sse-client.ts`
- Modify: `com.top_logic.layout.react/react-src/bridge-entry.ts`

**Context:** `sse-client.ts` has a `switch(typeCode)` at lines 126-166 that dispatches SSE events. Add three new cases. `bridge-entry.ts` needs to initialize the self-close notification.

- [ ] **Step 1: Add imports to sse-client.ts**

At the top of `sse-client.ts`, add:

```typescript
import {
  handleWindowOpen,
  handleWindowClose,
  handleWindowFocus,
  type WindowOpenEventData,
  type WindowCloseEventData,
  type WindowFocusEventData,
} from './window-manager';
```

- [ ] **Step 2: Add cases to the dispatch switch in sse-client.ts**

In the `switch(typeCode)` block, before the `default:` case, add:

```typescript
    case 'WindowOpenEvent':
      handleWindowOpen(payload as unknown as WindowOpenEventData);
      break;
    case 'WindowCloseEvent':
      handleWindowClose(payload as unknown as WindowCloseEventData);
      break;
    case 'WindowFocusEvent':
      handleWindowFocus(payload as unknown as WindowFocusEventData);
      break;
```

- [ ] **Step 3: Add self-close initialization to bridge-entry.ts**

Add import at the top of `bridge-entry.ts`:

```typescript
export { initSelfCloseNotification } from './bridge/window-manager';
```

Add initialization at the bottom, after the `TLReact` window assignment:

```typescript
import { initSelfCloseNotification } from './bridge/window-manager';
initSelfCloseNotification();
```

- [ ] **Step 4: Verify TypeScript compiles**

Run:
```bash
cd com.top_logic.layout.react
mvn compile -DskipTests=true
```

Expected: BUILD SUCCESS (frontend-maven-plugin runs Vite build)

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.react/react-src/bridge/sse-client.ts
git add com.top_logic.layout.react/react-src/bridge-entry.ts
git commit -m "Ticket #29108: Integrate window lifecycle events into SSE dispatch and bridge initialization."
```

---

## Chunk 5: Server-Side Command Handling for Window Lifecycle

### Task 8: Handle windowClosed and windowBlocked commands in ReactServlet

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/ReactServlet.java`

**Context:** The client sends `windowClosed` and `windowBlocked` commands via POST to `/react-api/command`. These commands have an empty `controlId` because they target the window registry, not a specific control. `ReactServlet` currently rejects empty/null `controlId` at line 235 with a 400 error before reaching the control lookup. We must intercept window lifecycle commands **before** that null check.

- [ ] **Step 1: Read ReactServlet to find the command dispatch code**

Read `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/ReactServlet.java` to find the `handleCommand()` method. Note the exact location of the `controlId`/`commandName` null check (around line 235) — window commands must be handled before that check.

- [ ] **Step 2: Add window command handling**

In the command handling method, after extracting `controlId` and `commandName` from the JSON body but **before** the null/empty check that returns 400, add:

```java
// Handle window lifecycle commands (no control target).
// Must be checked before the controlId null check since these commands have empty controlId.
if ("windowClosed".equals(commandName)) {
    @SuppressWarnings("unchecked")
    Map<String, Object> arguments = (Map<String, Object>) commandData.get("arguments");
    String closedWindowId = (String) arguments.get("windowId");
    ReactWindowRegistry registry = ReactWindowRegistry.forSession(request.getSession());
    registry.windowClosed(closedWindowId);
    sendSuccess(response);
    return;
}
if ("windowBlocked".equals(commandName)) {
    // Popup was blocked by the browser. Could enqueue a snackbar event.
    // For now, just acknowledge.
    sendSuccess(response);
    return;
}
```

Then modify the existing null check to only reject truly invalid requests (where both controlId and commandName are empty/null). The window commands above have already returned, so empty controlId at this point is still an error.

Add import:
```java
import com.top_logic.layout.react.window.ReactWindowRegistry;
```

- [ ] **Step 3: Verify it compiles**

Run:
```bash
cd com.top_logic.layout.react
mvn compile -DskipTests=true
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/ReactServlet.java
git commit -m "Ticket #29108: Handle windowClosed and windowBlocked commands in ReactServlet."
```

---

## Chunk 6: Build and Integration Verification

### Task 9: Build entire module chain and verify

**Files:** None — integration verification only.

- [ ] **Step 1: Install the react module** (library module, safe to clean)

Run:
```bash
cd com.top_logic.layout.react
mvn clean install -DskipTests=true
```

Expected: BUILD SUCCESS

- [ ] **Step 2: Install the view module** (library module, safe to clean)

Run:
```bash
cd com.top_logic.layout.view
mvn clean install -DskipTests=true
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Run view module tests**

Run:
```bash
cd com.top_logic.layout.view
mvn test -DskipTests=false
```

Expected: All tests pass, including the new window tests.

- [ ] **Step 4: Commit any build-generated changes**

Check for regenerated message files or other build artifacts that need committing.

---

### Task 10: Create a demo view that opens a child window

**Files:**
- Create: `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/window/open-window-demo.view.xml` (test resource for manual verification)

This is a manual integration test. The demo app can be started and tested in the browser.

- [ ] **Step 1: Verify demo app starts**

Start the demo app using the `tl-app` skill and verify it loads at `http://localhost:8080`.

- [ ] **Step 2: Test window opening manually**

Navigate to the app, trigger a command that calls `ReactWindowRegistry.openWindow()`, verify:
1. A new browser window opens
2. The child window renders its control tree
3. Closing the child window sends a notification to the server
4. The server cleans up the window entry

- [ ] **Step 3: Final commit with any adjustments**

```bash
git add -A
git commit -m "Ticket #29108: Integration verification and adjustments for React multi-window handling."
```
