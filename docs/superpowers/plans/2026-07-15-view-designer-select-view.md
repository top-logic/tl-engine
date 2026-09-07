# View Designer "Select View" Picker — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a "Select view" button to the View Designer toolbar that lets the user click a view in the main window (eyedropper cursor, hover highlight, ESC to cancel) and selects the corresponding node in the designer.

**Architecture:** Server-mediated via SSE. Each rendered view's root control is stamped with `data-view-source="<path>.view.xml"`. The designer's `SelectViewCommand` finds the main window via the existing `appContext` channel, registers a token-keyed callback, and enqueues a new `ViewPickEvent` to the main window's SSE queue. The main-window client enters a pick mode; on click it POSTs `{token, path}` to a new `react-api/view-pick` endpoint, which runs the callback under the designer window's sub-session to set the `selectedNode` channel (`sourceFile → node` lookup). The designer tree and config editor react to the channel.

**Tech Stack:** Java (TopLogic view/react modules), msgbuf (SSE proto), TypeScript (react-src bridge, Vite), Maven.

## Global Constraints

- Build only through Maven; never `npx vite` / `javac` directly. TS builds via `frontend-maven-plugin` during `mvn compile` of `com.top_logic.layout.react`.
- After changing `com.top_logic.layout.react`, rebuild it before dependents: `mvn -B install -pl com.top_logic.layout.react` then `-pl com.top_logic.layout.view`, then the demo app (`-pl com.top_logic.demo`).
- Member fields use a leading underscore (`_field`); no `this.`.
- The msgbuf-generated files under `protocol/` are checked into git; regeneration (via `mvn compile`) overwrites them — commit the regenerated files.
- Commit messages: `Ticket #29108: <description>`. No AI attribution. Commit with `git commit --no-gpg-sign` (sandbox has read-only `~/.gnupg`).
- Manual end-to-end verification runs in `com.top_logic.demo` (root/root1234); the demo already wires `open-designer` into `app.view.xml`.

---

### Task 1: Stamp `data-view-source` on view root controls

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ReferenceElement.java:143-144`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java:159-160`

**Interfaces:**
- Produces: `ReactControl.setViewSource(String path)` — stamps `data-view-source` on the control's root DOM element in `write(TagWriter)`.

- [ ] **Step 1: Add the `_viewSource` field and setter to `ReactControl`.**

After the existing fields (around `ReactControl.java:81`, after `_sseQueue`), add:

```java
	/**
	 * The source {@code .view.xml} path of the view whose root this control is, or {@code null}.
	 *
	 * @see #setViewSource(String)
	 */
	private String _viewSource;
```

Add a setter (place it near the other simple accessors, e.g. after the constructor block):

```java
	/**
	 * Marks this control as the root of a view loaded from the given source file. The path is
	 * emitted as a {@code data-view-source} DOM attribute so the client "select view" picker can map
	 * a clicked area to its source {@code .view.xml}.
	 *
	 * @param viewSource
	 *        The full view path (e.g. {@code /WEB-INF/views/app.view.xml}), or {@code null}.
	 */
	public void setViewSource(String viewSource) {
		_viewSource = viewSource;
	}
```

- [ ] **Step 2: Emit the attribute in `write(TagWriter)`.**

In `ReactControl.write(...)`, immediately after line 561 (`out.writeAttribute("data-context-path", ...)`) and before `out.endBeginTag()` (line 562), add:

```java
			if (_viewSource != null) {
				out.writeAttribute("data-view-source", _viewSource);
			}
```

- [ ] **Step 3: Stamp the referenced view in `ReferenceElement.createControl`.**

Replace the return statement at `ReferenceElement.java:143-144`:

```java
		return new ReloadableControl(fullPath, childContext,
			(ReactControl) referencedView.createControl(childContext));
```

with:

```java
		ReloadableControl viewControl = new ReloadableControl(fullPath, childContext,
			(ReactControl) referencedView.createControl(childContext));
		viewControl.setViewSource(fullPath);
		return viewControl;
```

- [ ] **Step 4: Stamp the main-window root view in `ViewServlet`.**

Replace `ViewServlet.java:159-160`:

```java
		ReactControl content = new ReloadableControl(viewPath, viewContext,
			(ReactControl) view.createControl(viewContext));
```

with:

```java
		ReloadableControl content = new ReloadableControl(viewPath, viewContext,
			(ReactControl) view.createControl(viewContext));
		content.setViewSource(viewPath);
```

(Keep line 161 `new ReactStackControl(displayContext, List.of(content, snackbar))` unchanged; `content` is now a `ReloadableControl` local — verify the following lines still compile, adjusting the declared type from `ReactControl` to `ReloadableControl` only if needed; `ReactStackControl` accepts `ReactControl`.)

- [ ] **Step 5: Build.**

Run: `mvn -B install -pl com.top_logic.layout.react,com.top_logic.layout.view 2>&1 | tail -5`
Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Verify the attribute renders (manual, deferred to Task 9 end-to-end).**

Note in the task log that DOM verification of `data-view-source` happens in Task 9 (running app). No unit test — `write()` requires a live `TagWriter`/context.

- [ ] **Step 7: Commit.**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java \
        com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ReferenceElement.java \
        com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java
git commit --no-gpg-sign -m "Ticket #29108: Stamp data-view-source on view root controls."
```

---

### Task 2: Add `ViewPickEvent` SSE event type

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/protocol/sse.proto`
- Regenerated (commit): `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/protocol/SSEEvent.java`, `.../protocol/ViewPickEvent.java`, `.../protocol/impl/ViewPickEvent_Impl.java`

**Interfaces:**
- Produces: `com.top_logic.layout.react.protocol.ViewPickEvent` with `create()`, `setToken(String)`, `setTargetWindowId(String)`, and JSON type literal `"ViewPickEvent"`.

- [ ] **Step 1: Add the message to `sse.proto`.**

After the `WindowCloseEvent` message (around `sse.proto:155`), add:

```
/**
 * Instructs the target (main) window to enter "select view" pick mode.
 */
message ViewPickEvent extends SSEEvent {
	/** Correlation token identifying the pending pick registration on the server. */
	string token;

	/** The window ID that should enter pick mode (the designer's opener / main window). */
	string targetWindowId;
}
```

- [ ] **Step 2: Regenerate + compile.**

Run: `mvn -B compile -pl com.top_logic.layout.react 2>&1 | tail -5`
Expected: `BUILD SUCCESS`, and new/updated files:
`git status --short com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/protocol/` should list `ViewPickEvent.java`, `impl/ViewPickEvent_Impl.java`, and a modified `SSEEvent.java`.

- [ ] **Step 3: Verify the generated type discriminator.**

Run: `grep -n "ViewPickEvent" com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/protocol/SSEEvent.java`
Expected: a `VIEW_PICK_EVENT` `TypeKind`, a `visit(ViewPickEvent ...)`, and a `case ViewPickEvent.VIEW_PICK_EVENT__TYPE:` in `readSSEEvent`.

- [ ] **Step 4: Install so downstream modules see the new type.**

Run: `mvn -B install -pl com.top_logic.layout.react 2>&1 | tail -3`
Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit (include regenerated files).**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/protocol/
git commit --no-gpg-sign -m "Ticket #29108: Add ViewPickEvent SSE event type."
```

---

### Task 3: Pick registry on `ReactWindowRegistry`

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/PendingViewPick.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/ReactWindowRegistry.java`

**Interfaces:**
- Produces: `PendingViewPick` record `(String designerWindowId, java.util.function.Consumer<String> onPicked)`.
- Produces: `ReactWindowRegistry.registerPick(String token, PendingViewPick pending)` and `ReactWindowRegistry.consumePick(String token) : PendingViewPick` (returns `null` if unknown; removes the entry).

- [ ] **Step 1: Create `PendingViewPick`.**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import java.util.function.Consumer;

/**
 * A pending "select view" pick registered by the designer while the main window is in pick mode.
 *
 * @param designerWindowId
 *        The designer window whose sub-session the pick result must run in (so channel-driven
 *        control updates flush to the designer's SSE queue).
 * @param onPicked
 *        Callback invoked with the picked view's source path when the user clicks a view.
 */
public record PendingViewPick(String designerWindowId, Consumer<String> onPicked) {
	// Marker record.
}
```

- [ ] **Step 2: Add the registry map + methods to `ReactWindowRegistry`.**

Near the existing `_windowQueues` field (around `ReactWindowRegistry.java:43`), add:

```java
	private final java.util.Map<String, PendingViewPick> _pendingPicks = new java.util.concurrent.ConcurrentHashMap<>();
```

Add these methods (near `getQueue`, around line 92):

```java
	/**
	 * Registers a pending "select view" pick under the given token.
	 */
	public void registerPick(String token, PendingViewPick pending) {
		_pendingPicks.put(token, pending);
	}

	/**
	 * Looks up and removes the pending pick for the given token.
	 *
	 * @return The pending pick, or {@code null} if the token is unknown or already consumed.
	 */
	public PendingViewPick consumePick(String token) {
		return _pendingPicks.remove(token);
	}
```

- [ ] **Step 3: Build.**

Run: `mvn -B install -pl com.top_logic.layout.react 2>&1 | tail -3`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit.**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/PendingViewPick.java \
        com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/window/ReactWindowRegistry.java
git commit --no-gpg-sign -m "Ticket #29108: Add pending view-pick registry to ReactWindowRegistry."
```

---

### Task 4: `SelectViewCommand` + `sourceFile → node` lookup

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/designer/SelectViewCommand.java`
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/designer/TestSelectViewNodeLookup.java`

**Interfaces:**
- Consumes: `ReactWindowRegistry.registerPick`, `PendingViewPick`, `ViewPickEvent` (Tasks 2, 3); `ViewContext.resolveChannel/hasChannel`, `ChannelRef`, `DesignTreeNode.getChildren()/getSourceFile()`.
- Produces: `SelectViewCommand` (`@TagName("select-view")`), and package-visible static `SelectViewCommand.findViewRoot(DesignTreeNode root, String sourceFile) : DesignTreeNode` (BFS; returns the shallowest node whose `getSourceFile()` equals `sourceFile`, or `null`).

- [ ] **Step 1: Write the failing test for `findViewRoot`.**

Create `TestSelectViewNodeLookup.java`. It builds a small tree with a private `DesignTreeNode` subclass (the protected constructor is accessible to subclasses across packages; `getChildren()` is a public mutable list, so no package-private `setParent` is needed — the lookup is BFS-by-depth):

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.designer;

import junit.framework.TestCase;

import com.top_logic.layout.view.designer.DesignTreeNode;
import com.top_logic.layout.view.designer.SelectViewCommand;

/**
 * Tests {@link SelectViewCommand#findViewRoot(DesignTreeNode, String)}.
 */
public class TestSelectViewNodeLookup extends TestCase {

	private static final class Node extends DesignTreeNode {
		Node(String sourceFile) {
			super(sourceFile);
		}

		@Override
		public String getTagName() {
			return "test";
		}
	}

	private static Node child(Node parent, String sourceFile) {
		Node node = new Node(sourceFile);
		parent.getChildren().add(node);
		return node;
	}

	public void testFindsBoundaryNodeForReferencedFile() {
		String rootFile = "/WEB-INF/views/app.view.xml";
		String refFile = "/WEB-INF/views/customers/detail.view.xml";

		Node root = new Node(rootFile);
		Node panel = child(root, rootFile);
		Node refRoot = child(panel, refFile);   // view-ref boundary
		child(refRoot, refFile);                 // deeper node in the referenced file

		assertSame("Root file resolves to the tree root", root,
			SelectViewCommand.findViewRoot(root, rootFile));
		assertSame("Referenced file resolves to its boundary node", refRoot,
			SelectViewCommand.findViewRoot(root, refFile));
		assertNull("Unknown file resolves to null",
			SelectViewCommand.findViewRoot(root, "/WEB-INF/views/missing.view.xml"));
	}
}
```

- [ ] **Step 2: Run the test to verify it fails (compile error: `SelectViewCommand` missing).**

Run: `mvn -B test -DskipTests=false -pl com.top_logic.layout.view -Dtest=test.com.top_logic.layout.view.designer.TestSelectViewNodeLookup 2>&1 | tail -15`
Expected: FAIL — compilation error, `SelectViewCommand` / `findViewRoot` not found.

- [ ] **Step 3: Create `SelectViewCommand`.**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.function.Consumer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.protocol.ViewPickEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.PendingViewPick;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Toolbar command that starts "select view" mode in the main application window. The user then
 * clicks a view there; the matching design-tree node is selected in the designer.
 *
 * @see PendingViewPick
 * @see ViewPickEvent
 */
public class SelectViewCommand implements ViewCommand {

	/** Channel name holding the main application window's {@link ReactContext}. */
	private static final String APP_CONTEXT = "appContext";

	/**
	 * Configuration for {@link SelectViewCommand}.
	 */
	@TagName("select-view")
	public interface Config extends ViewCommand.Config {

		/** Configuration name for {@link #getDesignTree()}. */
		String DESIGN_TREE = "design-tree";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(SelectViewCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Reference to the channel containing the root {@link DesignTreeNode}. */
		@Name(DESIGN_TREE)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getDesignTree();

		/** Reference to the channel receiving the selected {@link DesignTreeNode}. */
		@Name(SELECTION)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final Config _config;

	/**
	 * Creates a new {@link SelectViewCommand}.
	 */
	@CalledByReflection
	public SelectViewCommand(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		if (!(context instanceof ViewContext viewContext)) {
			return HandlerResult.DEFAULT_RESULT;
		}
		if (!viewContext.hasChannel(APP_CONTEXT)) {
			return HandlerResult.DEFAULT_RESULT;
		}
		Object appContextValue = viewContext.resolveChannel(new ChannelRef(APP_CONTEXT)).get();
		if (!(appContextValue instanceof ReactContext mainWindowContext)) {
			return HandlerResult.DEFAULT_RESULT;
		}

		String mainWindowId = mainWindowContext.getWindowName();
		String designerWindowId = context.getWindowName();
		ReactWindowRegistry registry = context.getWindowRegistry();
		if (registry == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		SSEUpdateQueue mainQueue = registry.getQueue(mainWindowId);
		if (mainQueue == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		ViewChannel designTreeChannel = viewContext.resolveChannel(_config.getDesignTree());
		ViewChannel selectionChannel = viewContext.resolveChannel(_config.getSelection());

		String token = UUID.randomUUID().toString();
		Consumer<String> onPicked = path -> {
			Object root = designTreeChannel.get();
			if (root instanceof DesignTreeNode rootNode) {
				DesignTreeNode node = findViewRoot(rootNode, path);
				if (node != null) {
					selectionChannel.set(node);
				} else {
					Logger.info("Select view: no design node for source '" + path + "'.",
						SelectViewCommand.class);
				}
			}
		};
		registry.registerPick(token, new PendingViewPick(designerWindowId, onPicked));

		mainQueue.enqueue(ViewPickEvent.create()
			.setToken(token)
			.setTargetWindowId(mainWindowId));

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Finds the view-root node for the given source file: the shallowest node whose
	 * {@link DesignTreeNode#getSourceFile()} equals {@code sourceFile} (breadth-first), or
	 * {@code null} if none.
	 */
	static DesignTreeNode findViewRoot(DesignTreeNode root, String sourceFile) {
		if (root == null || sourceFile == null) {
			return null;
		}
		Deque<DesignTreeNode> queue = new ArrayDeque<>();
		queue.add(root);
		while (!queue.isEmpty()) {
			DesignTreeNode node = queue.removeFirst();
			if (sourceFile.equals(node.getSourceFile())) {
				return node;
			}
			queue.addAll(node.getChildren());
		}
		return null;
	}
}
```

- [ ] **Step 4: Run the test to verify it passes.**

Run: `mvn -B test -DskipTests=false -pl com.top_logic.layout.view -Dtest=test.com.top_logic.layout.view.designer.TestSelectViewNodeLookup 2>&1 | tail -8`
Expected: `Tests run: 1, Failures: 0, Errors: 0`.

- [ ] **Step 5: Install.**

Run: `mvn -B install -pl com.top_logic.layout.view 2>&1 | tail -3`
Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit.**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/designer/SelectViewCommand.java \
        com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/designer/TestSelectViewNodeLookup.java
git commit --no-gpg-sign -m "Ticket #29108: Add SelectViewCommand with source-file node lookup."
```

---

### Task 5: `react-api/view-pick` servlet endpoint

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/ReactServlet.java`

**Interfaces:**
- Consumes: `ReactWindowRegistry.consumePick`, `PendingViewPick` (Task 3).
- Produces: HTTP `POST /react-api/view-pick` accepting JSON `{token, path}`.

- [ ] **Step 1: Add the `view-pick` case to `doPost`'s switch.**

In `ReactServlet.doPost` (the `switch (pathInfo)` at `ReactServlet.java:296-309`), add before `default:`:

```java
				case "/view-pick":
					handleViewPick(request, response, session);
					break;
```

- [ ] **Step 2: Implement `handleViewPick` (mirror the sub-session + lock structure of `handleCommand`).**

Add this method after `handleNavigateToRoute` (around `ReactServlet.java:521`). It parses `{token, path}`, consumes the pending pick, and runs the callback under the designer window's sub-session so the resulting channel/control updates flush to the designer's SSE queue:

```java
	@SuppressWarnings("unchecked")
	private void handleViewPick(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		String body = new String(request.getInputStream().readAllBytes(), "UTF-8");
		Object parsed;
		try {
			parsed = JSON.fromString(body);
		} catch (JSON.ParseException ex) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + ex.getMessage());
			return;
		}
		if (!(parsed instanceof Map)) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Expected JSON object.");
			return;
		}
		Map<String, Object> data = (Map<String, Object>) parsed;
		String token = (String) data.get("token");
		String path = (String) data.get("path");
		if (token == null || path == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing token or path.");
			return;
		}

		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		PendingViewPick pending = registry.consumePick(token);
		if (pending == null) {
			// Unknown or already-consumed token: acknowledge without action.
			sendSuccess(response);
			return;
		}

		String designerWindowId = pending.designerWindowId();
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		SubsessionHandler rootHandler = installSubSession(displayContext, designerWindowId);

		ReentrantLock requestLock = registry.getRequestLock();
		requestLock.lock();
		try {
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			try {
				pending.onPicked().accept(path);
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}
			registry.synthesizeModelEvents(designerWindowId);
		} finally {
			requestLock.unlock();
		}
		sendSuccess(response);
	}
```

Add the import for `PendingViewPick` if not already present:

```java
import com.top_logic.layout.react.window.PendingViewPick;
```

(`installSubSession`, `getRequestLock`, `synthesizeModelEvents`, `DefaultDisplayContext`, `SubsessionHandler`, `ReentrantLock`, `sendSuccess`, `sendError`, `JSON` are all already used/imported in this class — see `handleCommand`.)

- [ ] **Step 3: Build + install.**

Run: `mvn -B install -pl com.top_logic.layout.react 2>&1 | tail -3`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit.**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/ReactServlet.java
git commit --no-gpg-sign -m "Ticket #29108: Add react-api/view-pick endpoint."
```

---

### Task 6: Client pick mode (eyedropper, highlight, ESC) + SSE wiring

**Files:**
- Create: `com.top_logic.layout.react/react-src/bridge/view-picker.ts`
- Modify: `com.top_logic.layout.react/react-src/bridge/sse-client.ts` (import + `case 'ViewPickEvent'`)
- Modify: `com.top_logic.layout.react/react-src/bridge-entry.ts` (call `initViewPicker()`)
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` (overlay styles)

**Interfaces:**
- Consumes: `ViewPickEvent` payload `{token, targetWindowId}` (Task 2); `react-api/view-pick` (Task 5); `getApiBase()` from `tl-react-bridge`.
- Produces: `initViewPicker()`, `handleViewPick(event: ViewPickEventData)`.

- [ ] **Step 1: Create `view-picker.ts`.**

```ts
import { getApiBase } from './tl-react-bridge';

/** Data shape of the msgbuf-generated ViewPickEvent. */
export interface ViewPickEventData {
  token: string;
  targetWindowId: string;
}

const HIGHLIGHT_ID = 'tl-view-pick-highlight';

let _active = false;
let _token: string | null = null;
let _highlight: HTMLDivElement | null = null;

function nearestViewSource(start: EventTarget | null): HTMLElement | null {
  let el = start as Element | null;
  while (el) {
    if (el instanceof HTMLElement && el.dataset.viewSource) {
      return el;
    }
    el = el.parentElement;
  }
  return null;
}

function moveHighlight(el: HTMLElement): void {
  if (!_highlight) return;
  const r = el.getBoundingClientRect();
  _highlight.style.display = 'block';
  _highlight.style.left = `${r.left}px`;
  _highlight.style.top = `${r.top}px`;
  _highlight.style.width = `${r.width}px`;
  _highlight.style.height = `${r.height}px`;
}

function onPointerMove(e: PointerEvent): void {
  const el = nearestViewSource(e.target);
  if (el) {
    moveHighlight(el);
  } else if (_highlight) {
    _highlight.style.display = 'none';
  }
}

function onClick(e: MouseEvent): void {
  e.preventDefault();
  e.stopPropagation();
  const el = nearestViewSource(e.target);
  const token = _token;
  stop();
  if (el && token) {
    const path = el.dataset.viewSource!;
    void fetch(getApiBase() + 'react-api/view-pick', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, path }),
    }).catch(err => console.error('[TLReact] view-pick failed:', err));
  }
}

function onKeyDown(e: KeyboardEvent): void {
  if (e.key === 'Escape') {
    e.preventDefault();
    e.stopPropagation();
    stop();
  }
}

function start(token: string): void {
  if (_active) stop();
  _active = true;
  _token = token;

  _highlight = document.createElement('div');
  _highlight.id = HIGHLIGHT_ID;
  _highlight.style.display = 'none';
  document.body.appendChild(_highlight);

  document.body.classList.add('tlViewPick--active');
  document.addEventListener('pointermove', onPointerMove, true);
  document.addEventListener('click', onClick, true);
  document.addEventListener('keydown', onKeyDown, true);
}

function stop(): void {
  _active = false;
  _token = null;
  document.body.classList.remove('tlViewPick--active');
  document.removeEventListener('pointermove', onPointerMove, true);
  document.removeEventListener('click', onClick, true);
  document.removeEventListener('keydown', onKeyDown, true);
  if (_highlight) {
    _highlight.remove();
    _highlight = null;
  }
}

/** Handles a ViewPickEvent from SSE: enters pick mode in this window. */
export function handleViewPick(event: ViewPickEventData): void {
  const myWindow = document.body.dataset.windowName ?? '';
  if (event.targetWindowId && event.targetWindowId !== myWindow) {
    return;
  }
  start(event.token);
}

/** Idempotent init hook (no global listeners until a ViewPickEvent arrives). */
export function initViewPicker(): void {
  // Listeners are attached only while picking; nothing to install at startup.
}
```

- [ ] **Step 2: Wire the SSE dispatch in `sse-client.ts`.**

Add to the import block (near `sse-client.ts:14-21`):

```ts
import { handleViewPick, type ViewPickEventData } from './view-picker';
```

Add a `case` in the `dispatch()` switch (near `sse-client.ts:210-218`, alongside the window events):

```ts
    case 'ViewPickEvent':
      handleViewPick(payload as unknown as ViewPickEventData);
      break;
```

- [ ] **Step 3: Init in `bridge-entry.ts`.**

After the tooltip-host init block (`bridge-entry.ts:59-64`), add:

```ts
// Initialize the "select view" picker (cross-window pick mode for the View Designer).
import { initViewPicker } from './bridge/view-picker';
initViewPicker();
```

- [ ] **Step 4: Add CSS (cursor + highlight overlay).**

Append to `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`:

```css
/* --- View Designer "select view" picker ------------------------------- */

.tlViewPick--active,
.tlViewPick--active * {
	cursor: crosshair !important;
}

#tl-view-pick-highlight {
	position: fixed;
	pointer-events: none;
	z-index: 10000;
	border: 2px solid var(--border-interactive, #2680eb);
	background: rgba(38, 128, 235, 0.12);
	box-sizing: border-box;
}
```

- [ ] **Step 5: Build the bundle.**

Run: `mvn -B compile -pl com.top_logic.layout.react 2>&1 | tail -5`
Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Verify the bundle contains the new code.**

Run: `grep -c "ViewPickEvent" com.top_logic.layout.react/src/main/webapp/script/tl-react-bridge.js`
Expected: a non-zero count.

- [ ] **Step 7: Install.**

Run: `mvn -B install -pl com.top_logic.layout.react 2>&1 | tail -3`
Expected: `BUILD SUCCESS`.

- [ ] **Step 8: Commit.**

```bash
git add com.top_logic.layout.react/react-src/bridge/view-picker.ts \
        com.top_logic.layout.react/react-src/bridge/sse-client.ts \
        com.top_logic.layout.react/react-src/bridge-entry.ts \
        com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css \
        com.top_logic.layout.react/src/main/webapp/script/tl-react-bridge.js
git commit --no-gpg-sign -m "Ticket #29108: Add client view-picker (eyedropper, highlight, ESC)."
```

---

### Task 7: Designer tree reflects external selection

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/designer/DesignerTreeElement.java:96-155`

**Interfaces:**
- Consumes: the `selection` channel set by Task 4/5's callback.
- Produces: the designer tree highlights + expands the externally selected node.

**Context:** Today `DesignerTreeElement` wires the selection model → channel (tree→channel) but does not listen channel→tree. Add a listener so an externally set `selectedNode` (from the picker) is reflected in the tree. Guarded against feedback loops: `DefaultViewChannel.set` and `SingleSelectionModel.setSelected` both only fire on an actual change.

- [ ] **Step 1: Add a channel→tree selection listener.**

Inside `createControl`, in the block guarded by `if (selectionRef != null)` (after the existing `selectionModel.addSelectionListener(...)` that ends at line 135, still inside the `if`), append:

```java
			// Reflect an externally set selection (e.g. from the "select view" picker) in the tree.
			selectionChannel.addListener((sender, oldValue, newValue) -> {
				if (newValue instanceof DesignTreeNode target) {
					DefaultTreeUINode uiNode = findUINode(treeModel, treeModel.getRoot(), target);
					if (uiNode != null) {
						selectionModel.setSelected(uiNode, true);
					}
				}
			});
```

- [ ] **Step 2: Add the `findUINode` helper (expands ancestors so the node is materialized).**

Add as a private static method in `DesignerTreeElement`:

```java
	/**
	 * Finds the {@link DefaultTreeUINode} whose business object is {@code target}, expanding nodes
	 * along the way so lazily-built children are materialized. Returns {@code null} if not found.
	 */
	private static DefaultTreeUINode findUINode(DefaultTreeUINodeModel model, DefaultTreeUINode node,
			DesignTreeNode target) {
		if (node.getBusinessObject() == target) {
			return node;
		}
		model.setExpanded(node, true);
		for (DefaultTreeUINode child : node.getChildrenAsList()) {
			DefaultTreeUINode found = findUINode(model, child, target);
			if (found != null) {
				return found;
			}
		}
		return null;
	}
```

Note: confirm the exact child-accessor name on `DefaultTreeUINode`/`DefaultTreeUINodeModel` during implementation (`getChildren()` vs `getChildrenAsList()`), and the expand call (`setExpanded(node, boolean)`); adjust to the actual API. These are `com.top_logic.layout.tree.model` types already imported by this file.

- [ ] **Step 3: Build + install.**

Run: `mvn -B install -pl com.top_logic.layout.view 2>&1 | tail -3`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit.**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/designer/DesignerTreeElement.java
git commit --no-gpg-sign -m "Ticket #29108: Reflect externally set selection in the designer tree."
```

---

### Task 8: Wire `<select-view>` into `designer.view.xml`

**Files:**
- Modify: `com.top_logic.layout.view/src/main/webapp/WEB-INF/views/designer.view.xml`

**Interfaces:**
- Consumes: `SelectViewCommand` (`@TagName("select-view")`, Task 4).

- [ ] **Step 1: Add the toolbar command.**

In the `<commands>` block of `designer.view.xml`, after the `<revert-design ...>` element, add:

```xml
			<select-view design-tree="designTree" selection="selectedNode" placement="TOOLBAR">
				<label>
					<en>Select view</en>
					<de>Sicht auswählen</de>
				</label>
				<tooltip>
					<en>Pick a view in the main window to select it here.</en>
					<de>Eine Sicht im Hauptfenster anklicken, um sie hier auszuwählen.</de>
				</tooltip>
			</select-view>
```

- [ ] **Step 2: Verify well-formedness.**

Run: `xmllint --noout com.top_logic.layout.view/src/main/webapp/WEB-INF/views/designer.view.xml && echo OK`
Expected: `OK`.

- [ ] **Step 3: Install (so the app WAR picks up the view + new command class).**

Run: `mvn -B install -pl com.top_logic.layout.view 2>&1 | tail -3`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit.**

```bash
git add com.top_logic.layout.view/src/main/webapp/WEB-INF/views/designer.view.xml
git commit --no-gpg-sign -m "Ticket #29108: Add 'Select view' button to the designer toolbar."
```

---

### Task 9: End-to-end build + manual verification

**Files:** none (verification only).

- [ ] **Step 1: Full rebuild of the affected modules + demo app.**

Run:
```bash
mvn -B install -pl com.top_logic.layout.react,com.top_logic.layout.view,com.top_logic.demo 2>&1 | tail -8
```
Expected: `BUILD SUCCESS`. If a stale-jar error appears, run `.claude/scripts/rebuild-stale.sh` first.

- [ ] **Step 2: Start `com.top_logic.demo` and log in (root/root1234).**

Use the app-run flow (`tl-app` skill if available, else the project's documented launch). Navigate to a page served under `/view/`.

- [ ] **Step 3: Verify `data-view-source` is present.**

In the browser devtools, confirm the main view's root element has `data-view-source="/WEB-INF/views/app.view.xml"` (and referenced views carry their own paths).

- [ ] **Step 4: Exercise the picker.**

Open the View Designer (Designer button in the app bar). Click **Select view**. In the main window verify: the cursor becomes a crosshair; hovering a view shows the highlight overlay; clicking a view selects the matching node in the designer tree and the config editor shows that view's config.

- [ ] **Step 5: Verify cancel paths.**

Repeat, and confirm: pressing `ESC` ends pick mode with no selection; clicking outside any view (no `data-view-source` ancestor) ends pick mode, discards the click, and makes no selection.

- [ ] **Step 6: Note results in the task log.**

Record what was observed (screenshots if using Playwright). This is the acceptance gate for the feature.

---

## Self-Review Notes

- **Spec coverage:** §1 stamping → Task 1; §2 command → Tasks 4, 8; §3 SSE event → Task 2; §4 client pick mode (cursor/highlight/ESC/click hit+miss) → Task 6; §5 endpoint + registry + node lookup + channel set → Tasks 3, 4, 5; §6 error handling → Tasks 4 (not-found log), 5 (unknown token ack), 6 (click-miss ends mode); §7 data flow → Tasks 4–6; §8 tests → Task 4 (unit), Task 9 (manual). Designer-tree highlight (needed for "selected in the designer") → Task 7.
- **Deviation from spec (confirm with user):** §6's user-facing snackbar for "view not part of the current design" is implemented as a **server log + no selection** (Task 4), not a snackbar, to avoid an unverified InfoService→snackbar API. Can be upgraded later.
- **Open points resolved:** `ReactControl` gets a **specific** `viewSource` field (not a generic attribute map); the endpoint is a **new `/view-pick` `pathInfo` case** in `ReactServlet`.
- **To confirm during implementation (flagged in-task):** exact `DefaultTreeUINode` child-accessor / expand API names in Task 7 Step 2.
