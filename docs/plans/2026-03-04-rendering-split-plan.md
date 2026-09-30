# Rendering Split Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Introduce `ViewDisplayContext` and `ViewControl` as the new-world rendering contract, eliminating the old-world adapter classes (`ViewFrameScope`, `ViewLayoutContext`) and the scope-chain hacks in `ViewServlet`.

**Architecture:** New lean interfaces (`ViewDisplayContext`, `ViewControl`) in `tl-layout-react`. `ReactControl` implements `ViewControl` with canonical rendering; its old-world `internalWrite` becomes a thin adapter. View-system elements return `ViewControl` instead of `Control`. `ViewServlet` creates a `DefaultViewDisplayContext` directly.

**Tech Stack:** Java 17, TopLogic TypedConfiguration, React SSE bridge

**Design doc:** `docs/plans/2026-03-04-rendering-split-design.md`

---

### Task 1: ViewDisplayContext interface

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ViewDisplayContext.java`

**Step 1: Create the interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

/**
 * Lean rendering context for the view system.
 *
 * <p>
 * Replaces the combination of {@link com.top_logic.layout.DisplayContext},
 * {@link com.top_logic.layout.ControlScope}, {@link com.top_logic.layout.FrameScope}, and
 * {@link com.top_logic.layout.LayoutContext} for view-system rendering with a minimal contract.
 * </p>
 *
 * @see DefaultViewDisplayContext
 */
public interface ViewDisplayContext {

	/**
	 * Allocates a unique ID for a control's DOM element.
	 */
	String allocateId();

	/**
	 * The window name sent to the client for command routing.
	 */
	String getWindowName();

	/**
	 * The webapp context path for constructing URLs.
	 */
	String getContextPath();

	/**
	 * The SSE queue for pushing state updates and registering controls.
	 */
	SSEUpdateQueue getSSEQueue();

	/**
	 * Creates a {@link ViewDisplayContext} adapter from an old-world
	 * {@link com.top_logic.layout.DisplayContext}.
	 *
	 * <p>
	 * Extracts values from the richer {@link com.top_logic.layout.DisplayContext}: context path
	 * from the request, window name from
	 * {@link com.top_logic.layout.LayoutContext#getWindowId()}, ID allocation from
	 * {@link com.top_logic.layout.FrameScope#createNewID()}, and SSE queue from the HTTP session.
	 * </p>
	 */
	static ViewDisplayContext fromDisplayContext(com.top_logic.layout.DisplayContext context) {
		return new DisplayContextAdapter(context);
	}
}
```

**Step 2: Compile**

Run: `cd com.top_logic.layout.react && mvn compile -q`

**Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ViewDisplayContext.java
git commit -m "Ticket #29108: ViewDisplayContext interface for new-world rendering contract."
```

---

### Task 2: ViewControl interface

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ViewControl.java`

**Step 1: Create the interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * Rendering contract for new-world view controls.
 *
 * <p>
 * Unlike {@link com.top_logic.layout.Control}, this interface does not require a
 * {@link com.top_logic.layout.ControlScope} or {@link com.top_logic.layout.FrameScope}. Controls
 * that need old-world compatibility additionally implement
 * {@link com.top_logic.base.services.simpleajax.HTMLFragment}.
 * </p>
 */
public interface ViewControl {

	/**
	 * The control's unique ID (assigned during {@link #write}).
	 */
	String getID();

	/**
	 * Renders this control into the given writer.
	 *
	 * @param context
	 *        The view rendering context.
	 * @param out
	 *        The writer to render into.
	 */
	void write(ViewDisplayContext context, TagWriter out) throws IOException;
}
```

**Step 2: Compile**

Run: `cd com.top_logic.layout.react && mvn compile -q`

**Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ViewControl.java
git commit -m "Ticket #29108: ViewControl interface for new-world rendering contract."
```

---

### Task 3: DefaultViewDisplayContext and DisplayContextAdapter

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DefaultViewDisplayContext.java`
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DisplayContextAdapter.java`

**Step 1: Create DefaultViewDisplayContext**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple implementation of {@link ViewDisplayContext} for the view system.
 */
public class DefaultViewDisplayContext implements ViewDisplayContext {

	private final AtomicInteger _nextId = new AtomicInteger(1);

	private final String _contextPath;

	private final String _windowName;

	private final SSEUpdateQueue _sseQueue;

	/**
	 * Creates a {@link DefaultViewDisplayContext}.
	 *
	 * @param contextPath
	 *        The webapp context path.
	 * @param windowName
	 *        The window name for command routing.
	 * @param sseQueue
	 *        The SSE queue for the current session.
	 */
	public DefaultViewDisplayContext(String contextPath, String windowName, SSEUpdateQueue sseQueue) {
		_contextPath = contextPath;
		_windowName = windowName;
		_sseQueue = sseQueue;
	}

	@Override
	public String allocateId() {
		return "v" + _nextId.getAndIncrement();
	}

	@Override
	public String getWindowName() {
		return _windowName;
	}

	@Override
	public String getContextPath() {
		return _contextPath;
	}

	@Override
	public SSEUpdateQueue getSSEQueue() {
		return _sseQueue;
	}
}
```

**Step 2: Create DisplayContextAdapter**

This is the bridge from old-world `DisplayContext` to `ViewDisplayContext`, used by `ViewDisplayContext.fromDisplayContext()`.

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;

/**
 * Adapts an old-world {@link DisplayContext} to the lean {@link ViewDisplayContext} contract.
 *
 * <p>
 * Extracts the values that view-system controls need from the richer {@link DisplayContext}.
 * </p>
 */
class DisplayContextAdapter implements ViewDisplayContext {

	private final DisplayContext _context;

	private final FrameScope _frameScope;

	private final SSEUpdateQueue _sseQueue;

	DisplayContextAdapter(DisplayContext context) {
		_context = context;
		_frameScope = context.getExecutionScope().getFrameScope();
		_sseQueue = SSEUpdateQueue.forSession(context.asRequest().getSession());
	}

	@Override
	public String allocateId() {
		return _frameScope.createNewID();
	}

	@Override
	public String getWindowName() {
		return _context.getLayoutContext().getWindowId().getWindowName();
	}

	@Override
	public String getContextPath() {
		return _context.getContextPath();
	}

	@Override
	public SSEUpdateQueue getSSEQueue() {
		return _sseQueue;
	}
}
```

**Step 3: Compile**

Run: `cd com.top_logic.layout.react && mvn compile -q`

**Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DefaultViewDisplayContext.java \
        com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DisplayContextAdapter.java
git commit -m "Ticket #29108: DefaultViewDisplayContext and DisplayContextAdapter implementations."
```

---

### Task 4: Refactor ReactControl JSON serialization to use ViewDisplayContext

Currently, `FrameScope` and `SSEUpdateQueue` are passed as separate parameters through all JSON serialization methods. Replace them with `ViewDisplayContext` (nullable, for already-initialized controls).

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactControl.java`

**Step 1: Change serialization method signatures**

Replace every `(... FrameScope frameScope, SSEUpdateQueue queue)` pair with `(... ViewDisplayContext viewContext)` in the static serialization methods and `writeAsChild`:

| Method | Old signature | New signature |
|---|---|---|
| `toJsonString` | `toJsonString(Map, FrameScope, SSEUpdateQueue)` | `toJsonString(Map, ViewDisplayContext)` |
| `writeJsonLiteral` | `writeJsonLiteral(TagWriter, Map, FrameScope, SSEUpdateQueue)` | `writeJsonLiteral(TagWriter, Map, ViewDisplayContext)` |
| `writeJsonMap` | `writeJsonMap(JsonWriter, Map, FrameScope, SSEUpdateQueue)` | `writeJsonMap(JsonWriter, Map, ViewDisplayContext)` |
| `writeJsonValue` | `writeJsonValue(JsonWriter, Object, FrameScope, SSEUpdateQueue)` | `writeJsonValue(JsonWriter, Object, ViewDisplayContext)` |
| `writeAsChild` | `writeAsChild(JsonWriter, FrameScope, SSEUpdateQueue)` | `writeAsChild(JsonWriter, ViewDisplayContext)` |

Inside `writeAsChild`, replace:
```java
// Old:
if (frameScope != null) {
    _frameScope = frameScope;
    fetchID(frameScope);
}
if (queue != null && _sseQueue == null) {
    _sseQueue = queue;
    queue.registerControl(this);
}
```

With:
```java
// New:
if (viewContext != null) {
    if (id == null) {
        id = viewContext.allocateId();
    }
    SSEUpdateQueue queue = viewContext.getSSEQueue();
    if (_sseQueue == null) {
        _sseQueue = queue;
        queue.registerControl(this);
    }
}
```

Note: `id` is a `protected` field on `AbstractControlBase`. Setting it directly avoids the `fetchID(FrameScope)` dependency.

Inside `writeJsonValue`, the `ReactControl` case becomes:
```java
} else if (value instanceof ReactControl) {
    ((ReactControl) value).writeAsChild(writer, viewContext);
}
```

The existing convenience overloads (`toJsonString(Map)`, `writeJsonLiteral(TagWriter, Map)`, `writeJsonValue(JsonWriter, Object)`) remain and pass `null` as the `ViewDisplayContext`.

Also replace the `_frameScope` field usage. The field is currently used in:
- `internalWrite()` -- will be replaced in Task 5
- `writeAsChild()` -- replaced above
- `patchReactState()` -- uses `_frameScope` to serialize patch JSON: `toJsonString(patch, _frameScope, queue)`. Change to pass a stored `ViewDisplayContext` or pass `null` (controls are already initialized at patch time, so no ID allocation needed).
- `registerChildControl()` / `unregisterChildControl()` -- use `_sseQueue` directly, no change needed for unregister; for register, use stored `_sseQueue`.

Replace the `_frameScope` field with a `_viewContext` field of type `ViewDisplayContext`. Where `_frameScope` was previously stored, store `_viewContext` instead. Where `_frameScope` was used for `fetchID()`, use `_viewContext.allocateId()`.

**Step 2: Compile**

Run: `cd com.top_logic.layout.react && mvn compile -q`

Fix any callers of the old method signatures within `tl-layout-react`. The main callers are in `ReactControl` itself and its subclasses. Search for all usages:

Run: `grep -rn "toJsonString\|writeJsonLiteral\|writeJsonValue\|writeAsChild\|_frameScope" com.top_logic.layout.react/src/main/java/`

Update all call sites.

**Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/
git commit -m "Ticket #29108: Replace FrameScope+SSEUpdateQueue pairs with ViewDisplayContext in ReactControl serialization."
```

---

### Task 5: ReactControl implements ViewControl

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactControl.java`

**Step 1: Add ViewControl implementation**

Add `implements ViewControl` to the class declaration. Move the canonical rendering logic from `internalWrite` into the new `write(ViewDisplayContext, TagWriter)` method. The old `internalWrite` becomes an adapter.

```java
public class ReactControl extends AbstractVisibleControl implements ViewControl {

    // ... existing fields ...
    // Replace: private FrameScope _frameScope;
    // With: private ViewDisplayContext _viewContext;

    // Canonical rendering -- the new-world entry point
    @Override // ViewControl
    public void write(ViewDisplayContext context, TagWriter out) throws IOException {
        // Initialize this control
        id = context.allocateId();
        _viewContext = context;
        SSEUpdateQueue queue = context.getSSEQueue();
        _sseQueue = queue;
        queue.registerControl(this);

        // Write mount-point div
        out.beginBeginTag(HTMLConstants.DIV);
        out.writeAttribute("id", getID());
        writeControlClassesAttribute(out);
        out.writeAttribute("data-react-module", _reactModule);
        out.writeAttribute("data-react-state", toJsonString(_reactState, context));
        out.endBeginTag();
        out.endTag(HTMLConstants.DIV);

        // Bootstrap script
        HTMLUtil.beginScriptAfterRendering(out);
        out.append("TLReact.mount('");
        out.append(getID());
        out.append("', '");
        out.append(_reactModule);
        out.append("', ");
        writeJsonLiteral(out, _reactState, context);
        out.append(", '");
        out.append(context.getWindowName());
        out.append("', '");
        out.append(context.getContextPath());
        out.append("');");
        HTMLUtil.endScriptAfterRendering(out);
    }

    // Old-world adapter
    @Override
    protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
        write(ViewDisplayContext.fromDisplayContext(context), out);
    }
}
```

Note: `writeControlClassesAttribute(out)` writes CSS classes. Check if this is available or if `writeControlAttributes(context, out)` is needed. If the old method needs `DisplayContext`, extract just the class-writing part. If `writeControlAttributes` only writes `id` and `class`, and we already write `id` manually, we may just need the class attribute. Check `AbstractVisibleControl.writeControlAttributes()` to decide.

**Step 2: Compile and fix**

Run: `cd com.top_logic.layout.react && mvn compile -q`

Fix any compilation errors. The `writeControlAttributes` method may need adjusting -- it typically writes `id` (already written), CSS classes, and `style`. Either extract the CSS class writing or skip `writeControlAttributes` and write classes manually.

**Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactControl.java
git commit -m "Ticket #29108: ReactControl implements ViewControl with canonical write(ViewDisplayContext, TagWriter)."
```

---

### Task 6: Update UIElement and ViewContext

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/UIElement.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java`

**Step 1: UIElement returns ViewControl**

```java
package com.top_logic.layout.view;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.react.ViewControl;

public interface UIElement {

	interface Config extends PolymorphicConfiguration<UIElement> {
	}

	/**
	 * Creates a {@link ViewControl} for this element in the given session context.
	 */
	ViewControl createControl(ViewContext context);
}
```

**Step 2: ViewContext drops FrameScope, uses ViewDisplayContext**

```java
package com.top_logic.layout.view;

import com.top_logic.layout.react.ViewDisplayContext;

public class ViewContext {

	private final ViewDisplayContext _displayContext;

	public ViewContext(ViewDisplayContext displayContext) {
		_displayContext = displayContext;
	}

	/**
	 * The view display context for the current session.
	 */
	public ViewDisplayContext getDisplayContext() {
		return _displayContext;
	}
}
```

**Step 3: Compile** (will fail -- expected, dependent files not yet updated)

Run: `cd com.top_logic.layout.view && mvn compile -q 2>&1 | head -30`

Verify errors are only in the expected files: `ContainerElement`, `ViewElement`, element classes, `ViewServlet`, and tests.

**Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/UIElement.java \
        com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java
git commit -m "Ticket #29108: UIElement returns ViewControl, ViewContext uses ViewDisplayContext."
```

---

### Task 7: Update ContainerElement, ViewElement, and concrete elements

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ContainerElement.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/AppShellElement.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/PanelElement.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/StackElement.java`

**Step 1: Update ContainerElement**

Change `createChildControls` return type from `List<Control>` to `List<ViewControl>`:

```java
import com.top_logic.layout.react.ViewControl;

// Remove: import com.top_logic.layout.Control;

protected List<ViewControl> createChildControls(ViewContext context) {
    return _children.stream()
        .map(child -> child.createControl(context))
        .collect(Collectors.toList());
}
```

**Step 2: Update ViewElement**

```java
import com.top_logic.layout.react.ViewControl;

// Remove: import com.top_logic.layout.Control;

@Override
public ViewControl createControl(ViewContext context) {
    return _content.createControl(context);
}
```

**Step 3: Update AppShellElement**

The element casts child controls to `ReactControl` for constructing `ReactAppShellControl`. Since `ReactControl` now implements `ViewControl`, the cast goes from `ViewControl` to `ReactControl`:

```java
import com.top_logic.layout.react.ViewControl;

// Remove: import com.top_logic.layout.Control;

@Override
public ViewControl createControl(ViewContext context) {
    ReactControl header = _header != null ? (ReactControl) _header.createControl(context) : null;
    ReactControl content = (ReactControl) _content.createControl(context);
    ReactControl footer = _footer != null ? (ReactControl) _footer.createControl(context) : null;

    return new ReactAppShellControl(header, content, footer);
}
```

**Step 4: Update PanelElement**

```java
import com.top_logic.layout.react.ViewControl;

// Remove: import com.top_logic.layout.Control;

@Override
public ViewControl createControl(ViewContext context) {
    List<ViewControl> childControls = createChildControls(context);

    ReactControl content;
    if (childControls.size() == 1) {
        content = (ReactControl) childControls.get(0);
    } else {
        List<ReactControl> reactChildren = childControls.stream()
            .map(c -> (ReactControl) c)
            .collect(Collectors.toList());
        content = new ReactStackControl(reactChildren);
    }

    return new ReactPanelControl(_title, content, false, false, false);
}
```

**Step 5: Update StackElement**

```java
import com.top_logic.layout.react.ViewControl;

// Remove: import com.top_logic.layout.Control;

@Override
public ViewControl createControl(ViewContext context) {
    List<ViewControl> childControls = createChildControls(context);

    List<ReactControl> reactChildren = childControls.stream()
        .map(c -> (ReactControl) c)
        .collect(Collectors.toList());

    return new ReactStackControl(_direction, _gap, _align, false, reactChildren);
}
```

**Step 6: Compile**

Run: `cd com.top_logic.layout.view && mvn compile -q`

**Step 7: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ContainerElement.java \
        com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java \
        com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/AppShellElement.java \
        com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/PanelElement.java \
        com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/StackElement.java
git commit -m "Ticket #29108: All UIElement implementations return ViewControl."
```

---

### Task 8: Simplify ViewServlet and delete old adapter classes

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java`
- Delete: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewFrameScope.java`
- Delete: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewLayoutContext.java`

**Step 1: Rewrite ViewServlet**

The doGet method creates a `DefaultViewDisplayContext` directly. No `DisplayContext`, no `TLContext` cast, no `LocalScope`, no `ViewFrameScope`, no `ViewLayoutContext`.

```java
package com.top_logic.layout.view;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultViewDisplayContext;
import com.top_logic.layout.react.SSEUpdateQueue;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.ViewDisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.TopLogicServlet;

public class ViewServlet extends TopLogicServlet {

	private static final String VIEW_BASE_PATH = "/WEB-INF/views/";

	private final ConcurrentHashMap<String, ViewElement> _viewCache = new ConcurrentHashMap<>();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session.");
			return;
		}

		String viewPath = resolveViewPath(request);

		ViewElement view;
		try {
			view = getOrLoadView(viewPath);
		} catch (ConfigurationException ex) {
			Logger.error("Failed to load view: " + viewPath, ex, ViewServlet.class);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"Failed to load view: " + ex.getMessage());
			return;
		}

		ViewDisplayContext viewContext = new DefaultViewDisplayContext(
			request.getContextPath(), "view", SSEUpdateQueue.forSession(session));

		ViewControl rootControl = view.createControl(new ViewContext(viewContext));

		renderPage(viewContext, response, rootControl);
	}

	// resolveViewPath() and getOrLoadView()/loadView() stay unchanged

	private void renderPage(ViewDisplayContext viewContext, HttpServletResponse response,
			ViewControl rootControl) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		TagWriter out = new TagWriter(response.getWriter());

		out.writeContent(HTMLConstants.DOCTYPE_HTML);
		out.beginBeginTag(HTMLConstants.HTML);
		out.writeAttribute("lang", "en");
		out.endBeginTag();

		out.beginBeginTag(HTMLConstants.HEAD);
		out.endBeginTag();
		out.beginBeginTag(HTMLConstants.META);
		out.writeAttribute("charset", "UTF-8");
		out.endEmptyTag();
		out.beginBeginTag(HTMLConstants.META);
		out.writeAttribute("name", "viewport");
		out.writeAttribute("content", "width=device-width, initial-scale=1.0");
		out.endEmptyTag();
		out.beginBeginTag(HTMLConstants.TITLE);
		out.endBeginTag();
		out.writeText("TopLogic View");
		out.endTag(HTMLConstants.TITLE);
		out.beginBeginTag(HTMLConstants.SCRIPT);
		out.writeAttribute("src", viewContext.getContextPath() + "/script/tl-react/tl-react-bridge.js");
		out.endBeginTag();
		out.endTag(HTMLConstants.SCRIPT);
		out.endTag(HTMLConstants.HEAD);

		out.beginBeginTag(HTMLConstants.BODY);
		out.endBeginTag();

		rootControl.write(viewContext, out);

		out.endTag(HTMLConstants.BODY);
		out.endTag(HTMLConstants.HTML);

		out.flushBuffer();
	}
}
```

Note: Keep `resolveViewPath()`, `getOrLoadView()`, and `loadView()` unchanged. Only `doGet()` and `renderPage()` change.

The imports of `DisplayContext`, `ControlScope`, `LocalScope`, `DefaultDisplayContext`, `WindowId`, `TLContext`, and `Control` are removed.

**Step 2: Delete old adapter classes**

```bash
git rm com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewFrameScope.java
git rm com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewLayoutContext.java
```

**Step 3: Compile**

Run: `cd com.top_logic.layout.view && mvn compile -q`

**Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java
git commit -m "Ticket #29108: ViewServlet uses DefaultViewDisplayContext, delete ViewFrameScope and ViewLayoutContext."
```

---

### Task 9: Update tests

**Files:**
- Modify: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/TestViewElement.java`

**Step 1: Update test**

The existing test only tests Phase 1 (XML parsing + UIElement instantiation). It does not call `createControl()`, so it should not be affected by the `ViewControl` return type change. Verify it still compiles and passes.

If the test does reference `Control` anywhere, update those references to `ViewControl`.

**Step 2: Run tests**

Run: `cd com.top_logic.layout.view && mvn test -DskipTests=false -q`

**Step 3: Commit** (if changes were needed)

```bash
git add com.top_logic.layout.view/src/test/java/
git commit -m "Ticket #29108: Update tests for ViewControl return type."
```

---

### Task 10: Full build verification

**Step 1: Build tl-layout-react**

Run: `cd com.top_logic.layout.react && mvn install -q`

**Step 2: Build tl-layout-view**

Run: `cd com.top_logic.layout.view && mvn install -DskipTests=false -q`

**Step 3: Check for downstream breakage**

Search for any other modules that import from `com.top_logic.layout.view`:

Run: `grep -rn "com.top_logic.layout.view" --include="*.java" . | grep -v "com.top_logic.layout.view/" | grep -v "/target/"`

If no downstream consumers exist (this is a new module), the build is complete.

**Step 4: Final commit** (if any fixes were needed)

```bash
git add -A
git commit -m "Ticket #29108: Build fixes for rendering split."
```
