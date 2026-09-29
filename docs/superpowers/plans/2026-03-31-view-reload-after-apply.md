# View-Reload nach Apply — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** After clicking "Apply" in the View Designer, the main application window hot-reloads only the affected view subtrees without a full page reload.

**Architecture:** A `ReloadableControl` wrapper is inserted at every view boundary (root view, `<view-ref>`, dialog open). It implements `ViewReloadListener` and registers at the root `ViewContext`. `SaveDesignCommand` fires `viewChanged(paths)` on the app's root ViewContext (passed via a channel from `OpenDesignerCommand`), causing affected `ReloadableControl`s to rebuild their inner control subtrees while preserving the `ViewContext` and its channel values.

**Tech Stack:** Java 17, TopLogic ViewContext/ReactControl framework, JUnit 4

**Spec:** `docs/superpowers/specs/2026-03-31-view-reload-after-apply-design.md`

---

### Task 1: ViewReloadListener Interface

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewReloadListener.java`

- [ ] **Step 1: Create the interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.Set;

/**
 * Listener notified when {@code .view.xml} files have been modified and the corresponding
 * control subtrees should be rebuilt.
 *
 * @see ViewContext#addViewReloadListener(ViewReloadListener)
 * @see ViewContext#fireViewChanged(Set)
 */
public interface ViewReloadListener {

	/**
	 * Called when one or more view files have been written to disk.
	 *
	 * @param changedPaths
	 *        The set of changed view file paths (e.g. {@code "/WEB-INF/views/sidebar.view.xml"}).
	 *        Implementations should check whether their own view path is contained.
	 */
	void viewChanged(Set<String> changedPaths);
}
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewReloadListener.java
git commit -m "Ticket #29108: Add ViewReloadListener interface."
```

---

### Task 2: Add Listener Methods to ViewContext and DefaultViewContext

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/DefaultViewContext.java`

- [ ] **Step 1: Add methods to ViewContext interface**

Add these three methods to `ViewContext.java` (after the `resolveChannel` method, before the closing brace):

```java
/**
 * Registers a listener that is notified when view files change.
 *
 * <p>
 * Listeners are always registered at the root context. Child contexts delegate registration
 * upward so that a single {@link #fireViewChanged(Set)} call reaches all listeners in the
 * hierarchy.
 * </p>
 *
 * @param listener
 *        The listener to register.
 *
 * @see #removeViewReloadListener(ViewReloadListener)
 * @see #fireViewChanged(Set)
 */
void addViewReloadListener(ViewReloadListener listener);

/**
 * Removes a previously registered {@link ViewReloadListener}.
 *
 * @param listener
 *        The listener to remove.
 */
void removeViewReloadListener(ViewReloadListener listener);

/**
 * Notifies all registered {@link ViewReloadListener}s that the given view files have changed.
 *
 * <p>
 * Called by the designer's save command after writing modified {@code .view.xml} files to
 * disk. Child contexts delegate this call to the root context.
 * </p>
 *
 * @param changedPaths
 *        The set of changed view file paths.
 */
void fireViewChanged(java.util.Set<String> changedPaths);
```

Also add the import at the top of `ViewContext.java`:

```java
import java.util.Set;
```

And replace `java.util.Set<String>` in `fireViewChanged` with just `Set<String>`.

- [ ] **Step 2: Implement in DefaultViewContext**

Add a new field to `DefaultViewContext`:

```java
private final List<ViewReloadListener> _reloadListeners;
```

Add import:

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
```

Modify the **public constructor** (`DefaultViewContext(ReactContext reactContext)`) to inherit listeners from a parent ViewContext or create a fresh list:

```java
public DefaultViewContext(ReactContext reactContext) {
	this(reactContext, "view", new HashMap<>(), null, null, null, null,
		reactContext instanceof ViewContext
			? ((ViewContext) reactContext).getReloadListeners()
			: new ArrayList<>());
}
```

Wait — `getReloadListeners()` would expose the internal list. Instead, use a dedicated approach: add `_reloadListeners` to the private constructor and pass it through everywhere.

Update the **private constructor** signature to include the list:

```java
private DefaultViewContext(ReactContext reactContext, String personalizationPath,
		Map<String, ViewChannel> channels, CommandScope commandScope, FormModel formModel,
		ErrorSink errorSink, DirtyChannel dirtyChannel, List<ViewReloadListener> reloadListeners) {
	_reactContext = reactContext;
	_personalizationPath = personalizationPath;
	_channels = channels;
	_commandScope = commandScope;
	_formModel = formModel;
	_errorSink = errorSink;
	_dirtyChannel = dirtyChannel;
	_reloadListeners = reloadListeners;
}
```

Update the **public constructor**:

```java
public DefaultViewContext(ReactContext reactContext) {
	this(reactContext, "view", new HashMap<>(), null, null, null, null,
		reactContext instanceof ViewContext
			? ((ViewContext) reactContext).reloadListeners()
			: new ArrayList<>());
}
```

Add `reloadListeners()` to `ViewContext` interface (package-accessible helper — but since it's an interface, it must be public). Alternative: just keep the delegation pattern without exposing the list. Use this approach instead:

**Public constructor** — check if the `reactContext` is a `DefaultViewContext` and grab listeners directly:

```java
public DefaultViewContext(ReactContext reactContext) {
	this(reactContext, "view", new HashMap<>(), null, null, null, null,
		resolveReloadListeners(reactContext));
}

private static List<ViewReloadListener> resolveReloadListeners(ReactContext reactContext) {
	if (reactContext instanceof DefaultViewContext dvc) {
		return dvc._reloadListeners;
	}
	return new ArrayList<>();
}
```

This works because `ReferenceElement` and `OpenDialogAction` both call `new DefaultViewContext(parentContext)` where `parentContext` is a `ViewContext` (which is a `DefaultViewContext`). The `_reloadListeners` list is shared by reference across the entire hierarchy.

Update all **derived context factory methods** to pass through `_reloadListeners`:

In `childContext(String segment)`:
```java
return new DefaultViewContext(_reactContext, _personalizationPath + "." + segment, _channels, _commandScope,
	_formModel, _errorSink, _dirtyChannel, _reloadListeners);
```

In `withCommandScope(CommandScope scope)`:
```java
return new DefaultViewContext(_reactContext, _personalizationPath, _channels, scope, _formModel, _errorSink,
	_dirtyChannel, _reloadListeners);
```

In `withErrorSink(ErrorSink errorSink)`:
```java
return new DefaultViewContext(_reactContext, _personalizationPath, _channels,
	_commandScope, _formModel, errorSink, _dirtyChannel, _reloadListeners);
```

Implement the three interface methods:

```java
@Override
public void addViewReloadListener(ViewReloadListener listener) {
	_reloadListeners.add(listener);
}

@Override
public void removeViewReloadListener(ViewReloadListener listener) {
	_reloadListeners.remove(listener);
}

@Override
public void fireViewChanged(Set<String> changedPaths) {
	// Copy to avoid ConcurrentModificationException if a listener triggers removal.
	for (ViewReloadListener listener : List.copyOf(_reloadListeners)) {
		listener.viewChanged(changedPaths);
	}
}
```

- [ ] **Step 3: Build to verify compilation**

```bash
mvn -B install -pl com.top_logic.layout.view 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/DefaultViewContext.java
git commit -m "Ticket #29108: Add ViewReloadListener support to ViewContext."
```

---

### Task 3: Test ViewReloadListener Propagation

**Files:**
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/TestViewReloadListener.java`

- [ ] **Step 1: Write the test**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewReloadListener;

/**
 * Tests for {@link ViewReloadListener} registration and propagation through the
 * {@link ViewContext} hierarchy.
 */
public class TestViewReloadListener extends TestCase {

	/**
	 * Tests that a listener registered on a root context receives events.
	 */
	public void testListenerOnRootContext() {
		ViewContext root = new DefaultViewContext(new TestReactContext());

		List<Set<String>> received = new ArrayList<>();
		root.addViewReloadListener(received::add);

		root.fireViewChanged(Set.of("/WEB-INF/views/app.view.xml"));

		assertEquals(1, received.size());
		assertTrue(received.get(0).contains("/WEB-INF/views/app.view.xml"));
	}

	/**
	 * Tests that a listener registered on a child context (via {@code new
	 * DefaultViewContext(parent)}) receives events fired on the root.
	 */
	public void testListenerOnChildContextReceivesRootEvents() {
		ViewContext root = new DefaultViewContext(new TestReactContext());
		// Simulates what ReferenceElement does: new DefaultViewContext(parentContext)
		ViewContext child = new DefaultViewContext(root);

		List<Set<String>> received = new ArrayList<>();
		child.addViewReloadListener(received::add);

		// Fire on root — child listener should still receive it.
		root.fireViewChanged(Set.of("/WEB-INF/views/sidebar.view.xml"));

		assertEquals(1, received.size());
	}

	/**
	 * Tests that a listener registered on a derived context (via
	 * {@link ViewContext#childContext(String)}) shares the same listener list.
	 */
	public void testListenerOnDerivedChildContext() {
		ViewContext root = new DefaultViewContext(new TestReactContext());
		ViewContext derived = root.childContext("panel");

		List<Set<String>> received = new ArrayList<>();
		derived.addViewReloadListener(received::add);

		root.fireViewChanged(Set.of("/WEB-INF/views/panel.view.xml"));

		assertEquals(1, received.size());
	}

	/**
	 * Tests that {@link ViewContext#removeViewReloadListener(ViewReloadListener)} prevents
	 * further notifications.
	 */
	public void testRemoveListener() {
		ViewContext root = new DefaultViewContext(new TestReactContext());

		List<Set<String>> received = new ArrayList<>();
		ViewReloadListener listener = received::add;
		root.addViewReloadListener(listener);
		root.removeViewReloadListener(listener);

		root.fireViewChanged(Set.of("/WEB-INF/views/app.view.xml"));

		assertTrue(received.isEmpty());
	}

	/**
	 * Tests that deeply nested contexts (root → child → grandchild) share the same listener
	 * list.
	 */
	public void testDeeplyNestedContexts() {
		ViewContext root = new DefaultViewContext(new TestReactContext());
		ViewContext child = new DefaultViewContext(root);
		ViewContext grandchild = new DefaultViewContext(child);

		List<Set<String>> received = new ArrayList<>();
		grandchild.addViewReloadListener(received::add);

		root.fireViewChanged(Set.of("/WEB-INF/views/deep.view.xml"));

		assertEquals(1, received.size());
	}
}
```

This test needs a `TestReactContext` — a minimal `ReactContext` stub. Check if one already exists in the test sources.

- [ ] **Step 2: Create or locate TestReactContext**

Search for existing `TestReactContext` or similar mock in the test sources. If none exists, create a minimal stub:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.model.listen.ModelScope;

/**
 * Minimal {@link ReactContext} stub for unit tests.
 */
class TestReactContext implements ReactContext {

	private int _idCounter = 0;

	@Override
	public String allocateId() {
		return "test-" + (_idCounter++);
	}

	@Override
	public String getWindowName() {
		return "test-window";
	}

	@Override
	public String getContextPath() {
		return "/test";
	}

	@Override
	public SSEUpdateQueue getSSEQueue() {
		return null;
	}

	@Override
	public ReactWindowRegistry getWindowRegistry() {
		return null;
	}

	@Override
	public ModelScope getModelScope() {
		return null;
	}

	@Override
	public DialogManager getDialogManager() {
		return null;
	}
}
```

Note: Check the actual `ReactContext` interface for all required methods. The stub must implement all abstract methods — add any missing ones with trivial return values.

- [ ] **Step 3: Run tests**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.layout.view -Dtest=TestViewReloadListener 2>&1 | tee com.top_logic.layout.view/target/mvn-test.log
```

Expected: All 5 tests PASS.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/TestViewReloadListener.java
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/TestReactContext.java
git commit -m "Ticket #29108: Add unit tests for ViewReloadListener propagation."
```

---

### Task 4: ReloadableControl

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ReloadableControl.java`

- [ ] **Step 1: Create ReloadableControl**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.react.control.ReactCompositeControl;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Wrapper control at view file boundaries that supports hot-reloading.
 *
 * <p>
 * Inserted at every point where a {@code .view.xml} file enters the control tree: root view
 * loading, {@code <view-ref>} references, and dialog openings. When the underlying view file
 * changes, this control rebuilds its inner subtree while preserving the {@link ViewContext} and
 * its channel values.
 * </p>
 *
 * <p>
 * The {@code ReloadableControl} itself remains stable in the control tree — only its single child
 * is replaced. This means the parent control never needs to know about the reload.
 * </p>
 *
 * @see ViewReloadListener
 */
public class ReloadableControl extends ReactCompositeControl implements ViewReloadListener {

	private final String _viewPath;

	private final ViewContext _viewContext;

	/**
	 * Creates a new {@link ReloadableControl}.
	 *
	 * @param viewPath
	 *        The full path to the {@code .view.xml} file (e.g.
	 *        {@code "/WEB-INF/views/sidebar.view.xml"}).
	 * @param viewContext
	 *        The {@link ViewContext} used for control creation. Reused on reload so that channel
	 *        values are preserved.
	 * @param innerControl
	 *        The initial control tree built from the view.
	 */
	public ReloadableControl(String viewPath, ViewContext viewContext, ReactControl innerControl) {
		super(viewContext, null, "tl-reloadable", List.of(innerControl));
		_viewPath = viewPath;
		_viewContext = viewContext;

		viewContext.addViewReloadListener(this);
	}

	@Override
	public void viewChanged(Set<String> changedPaths) {
		if (!changedPaths.contains(_viewPath)) {
			return;
		}

		reload();
	}

	/**
	 * Rebuilds the inner control subtree from the current view file on disk.
	 */
	private void reload() {
		// 1. Cleanup old subtree.
		for (ReactControl child : getChildren()) {
			child.cleanupTree();
		}

		// 2. Load fresh UIElements (ViewLoader cache auto-invalidated via timestamp).
		ViewElement newView;
		try {
			newView = ViewLoader.getOrLoadView(_viewPath);
		} catch (ConfigurationException ex) {
			Logger.error("Failed to reload view: " + _viewPath, ex, ReloadableControl.class);
			return;
		}

		// 3. Create new control with the existing ViewContext (channels preserved).
		ReactControl newControl = (ReactControl) newView.createControl(_viewContext);

		// 4. Replace children and push state update to client via SSE.
		replaceChildren(List.of(newControl));
	}

	@Override
	protected void cleanupChildren() {
		_viewContext.removeViewReloadListener(this);
		super.cleanupChildren();
	}
}
```

- [ ] **Step 2: Add `replaceChildren` method to ReactCompositeControl**

In `ReactCompositeControl.java`, add after the `addChild` method:

```java
/**
 * Replaces all children with the given list.
 *
 * <p>
 * Used by {@link com.top_logic.layout.view.ReloadableControl} to swap the inner control
 * subtree on hot-reload. Callers are responsible for cleaning up the old children before
 * calling this method.
 * </p>
 *
 * @param newChildren
 *        The new child controls.
 */
protected void replaceChildren(List<? extends ReactControl> newChildren) {
	_children.clear();
	_children.addAll(newChildren);
	putState(CHILDREN, _children);
}
```

- [ ] **Step 3: Build to verify compilation**

```bash
mvn -B install -pl com.top_logic.layout.react,com.top_logic.layout.view 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ReloadableControl.java
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactCompositeControl.java
git commit -m "Ticket #29108: Add ReloadableControl with hot-reload support."
```

---

### Task 5: Wrap ReferenceElement in ReloadableControl

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ReferenceElement.java:130`

- [ ] **Step 1: Modify createControl()**

In `ReferenceElement.java`, replace the last line of `createControl()` (line 130):

Old:
```java
return referencedView.createControl(childContext);
```

New:
```java
ReactControl innerControl = (ReactControl) referencedView.createControl(childContext);
return new ReloadableControl(fullPath, childContext, innerControl);
```

Add import:
```java
import com.top_logic.layout.react.control.ReactControl;
```

- [ ] **Step 2: Build**

```bash
mvn -B install -pl com.top_logic.layout.view 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ReferenceElement.java
git commit -m "Ticket #29108: Wrap ReferenceElement output in ReloadableControl."
```

---

### Task 6: Wrap Root View in ReloadableControl

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java:128-130`

- [ ] **Step 1: Modify root view loading in ViewServlet.doGet()**

Replace lines 128-130 in `ViewServlet.java`:

Old:
```java
ViewContext viewContext = new DefaultViewContext(displayContext);

IReactControl rootControl = view.createControl(viewContext);
```

New:
```java
ViewContext viewContext = new DefaultViewContext(displayContext);

ReactControl innerControl = (ReactControl) view.createControl(viewContext);
IReactControl rootControl = new ReloadableControl(viewPath, viewContext, innerControl);
```

Add imports:
```java
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ReloadableControl;
```

- [ ] **Step 2: Build**

```bash
mvn -B install -pl com.top_logic.layout.view 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java
git commit -m "Ticket #29108: Wrap root view in ReloadableControl."
```

---

### Task 7: Wrap Dialog View in ReloadableControl

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/OpenDialogAction.java:163`

- [ ] **Step 1: Modify dialog control creation**

In `OpenDialogAction.java`, replace line 163:

Old:
```java
ReactControl dialogControl = (ReactControl) dialogView.createControl(dialogContext);
```

New:
```java
ReactControl innerControl = (ReactControl) dialogView.createControl(dialogContext);
ReactControl dialogControl = new ReloadableControl(_dialogViewPath, dialogContext, innerControl);
```

Add import:
```java
import com.top_logic.layout.view.ReloadableControl;
```

- [ ] **Step 2: Build**

```bash
mvn -B install -pl com.top_logic.layout.view 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/OpenDialogAction.java
git commit -m "Ticket #29108: Wrap dialog view in ReloadableControl."
```

---

### Task 8: Pass App ViewContext to Designer

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/designer/OpenDesignerCommand.java:99`

- [ ] **Step 1: Add appContext channel**

In `OpenDesignerCommand.java`, add after line 99 (`designerContext.registerChannel("designTree", designTreeChannel);`):

```java
// Pass the main window's ViewContext to the designer so that
// SaveDesignCommand can fire view-changed events on it.
DefaultViewChannel appContextChannel = new DefaultViewChannel("appContext");
appContextChannel.set(context);
designerContext.registerChannel("appContext", appContextChannel);
```

Add import (if not already present):
```java
import com.top_logic.layout.view.channel.DefaultViewChannel;
```

- [ ] **Step 2: Build**

```bash
mvn -B install -pl com.top_logic.layout.view 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/designer/OpenDesignerCommand.java
git commit -m "Ticket #29108: Pass app ViewContext to designer via channel."
```

---

### Task 9: Fire viewChanged in SaveDesignCommand

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/designer/SaveDesignCommand.java:81-104`

- [ ] **Step 1: Add reload trigger after file writes**

In `SaveDesignCommand.java`, replace lines 100-104:

Old:
```java
		// TODO: Trigger a reload in the main window. The exact mechanism depends on the
		// ReactWindowRegistry API and needs further investigation before it can be wired up.

		return HandlerResult.DEFAULT_RESULT;
```

New:
```java
		// Trigger hot-reload in the main application window.
		if (viewContext.hasChannel("appContext")) {
			ViewChannel appContextChannel = viewContext.resolveChannel(new ChannelRef("appContext"));
			Object appContextValue = appContextChannel.get();
			if (appContextValue instanceof ViewContext appViewContext) {
				appViewContext.fireViewChanged(fileConfigs.keySet());
			}
		}

		return HandlerResult.DEFAULT_RESULT;
```

Note: `ChannelRef` has a simple `new ChannelRef(String)` constructor — no factory needed.

- [ ] **Step 2: Build**

```bash
mvn -B install -pl com.top_logic.layout.view 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/designer/SaveDesignCommand.java
git commit -m "Ticket #29108: Fire viewChanged after saving view files."
```

---

### Task 10: Client-Side ReloadableControl Passthrough

**Files:**
- Check: Client-side React code for how `tl-reloadable` module needs to be handled

- [ ] **Step 1: Verify client-side behavior**

`ReloadableControl` uses `"tl-reloadable"` as its React module. On the client side, this just needs to render its children — identical to a generic composite control. Check if there's already a generic passthrough React component, or if `ReactCompositeControl` children are rendered automatically by the framework.

If the React bridge automatically renders children for unknown modules, nothing is needed. If a module must be registered, create a minimal passthrough:

```typescript
// In the appropriate React module registry:
// tl-reloadable just renders its children, no extra UI.
```

This step depends on how the React bridge resolves module names. Investigate `ReactControl`'s client-side rendering to determine if explicit registration is needed.

- [ ] **Step 2: Build and verify**

```bash
mvn -B install -pl com.top_logic.layout.view 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log
```

- [ ] **Step 3: Commit (if changes were needed)**

```bash
git add <changed files>
git commit -m "Ticket #29108: Add client-side tl-reloadable passthrough component."
```

---

### Task 11: Manual Verification with Playwright

**Files:** None (verification only)

- [ ] **Step 1: Start the demo app**

Use the `tl-app` skill to start `com.top_logic.demo`.

- [ ] **Step 2: Open the View Designer**

1. Navigate to the demo app
2. Open the View Designer via the toolbar button
3. Verify the designer opens in a new window

- [ ] **Step 3: Make a property change and Apply**

1. Select a visible element in the tree (e.g., sidebar → title or a nav-item label)
2. Change a property in the config form (e.g., a title or CSS class)
3. Click "Apply"
4. Verify the main application window updates without a full page reload
5. Verify tab positions and scroll state are preserved

- [ ] **Step 4: Test dialog reload**

1. Open a dialog in the main app
2. In the designer, find and select the dialog's view
3. Change a dialog property and Apply
4. Verify the dialog updates while staying open

- [ ] **Step 5: Test with ReferenceElement**

1. Find a view that uses `<view-ref>`
2. Change a property in the referenced view
3. Apply and verify only that subtree refreshes
