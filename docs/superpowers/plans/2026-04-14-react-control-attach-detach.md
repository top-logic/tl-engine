# Attach/Detach Lifecycle for ReactControls — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Introduce an `attach`/`detach` lifecycle on `ReactControl`, distinct from `cleanupTree`. A control is *attached* while it is part of the currently displayed tree, *detached* while it's alive but not displayed (e.g. the non-active child of a one-of-N container), and *cleaned up* on final disposal. Every one-of-N container (Sidebar, TabBar, DeckPane, Dialog, Drawer, Window) propagates attach/detach correctly, so controls can scope side effects (toolbar-button contributions, timers, subscriptions, …) to the time they are actually visible. Apply the pattern to `DashboardElement` so that its edit/done app-bar commands appear only while the dashboard is the active nav-item content.

**Architecture:**
- `ReactControl` gains `attach()` / `detach()` methods (idempotent), an `isAttached()` getter, and two listener registries (`addAttachListener(Runnable)`, `addDetachListener(Runnable)`).
- Protected hooks `propagateAttach()` / `propagateDetach()` let containers cascade the event to their children. Default implementations are no-ops.
- `ReactCompositeControl` (always-visible composite) overrides both hooks to cascade to *all* children, so Stack/Split/Panel/Card/Grid/AppShell internals etc. — all of which extend it — get cascading for free.
- One-of-N containers (`ReactSidebarControl`, `ReactTabBarControl`, `ReactDeckPaneControl`, `ReactDialogControl`, `ReactDrawerControl`, `ReactWindowControl`) track the currently active child. Their `propagateAttach` attaches only that child; on selection/open change, the outgoing child is detached and the incoming one attached.
- The view system calls `attach()` once on the top-level control after it has been built. We add that call in `AppShellElement.createControl` (the outermost element in a demo view), plus `ReferenceElement.createControl` (defensive — standalone view-refs).
- `DashboardElement` contributes its edit/done commands via attach/detach listeners on the dashboard control, so the commands appear in the app-bar exactly while the dashboard is attached.

**Tech Stack:** Java 17, existing `ReactControl` / `ReactCompositeControl` / `ReactContext` plumbing, no new dependencies.

---

## File Structure

### Modify (core API)
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java` — add API, fields, idempotency, hooks.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactCompositeControl.java` — cascade attach/detach to all children.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ToolbarControl.java` — cascade attach/detach to toolbar buttons (in addition to whatever the concrete subclass adds).

### Modify (one-of-N containers)
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/sidebar/ReactSidebarControl.java` — attach initial active content; on `selectItem`, detach old + attach new; on `cleanupChildren`, detach before cleanup.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tabbar/ReactTabBarControl.java` — same pattern for active tab content.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactDeckPaneControl.java` — same pattern for visible pane.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ReactDialogControl.java` — attach when shown, detach when closed.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ReactDrawerControl.java` — same.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ReactWindowControl.java` — same.

### Modify (integration points)
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/AppShellElement.java` — call `attach()` on the returned root control.
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ReferenceElement.java` — call `attach()` on the referenced view's root control (only when not already attached by a parent container).
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/DashboardElement.java` — contribute edit/done commands via attach/detach listeners.

### Reference (read-only)
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java` — existing `_rendered` flag, `cleanupTree`, `addCleanupAction`.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/sidebar/ReactSidebarControl.java` — existing `_contentCache`, `selectItem`, `getOrCreateContent`.

---

## Task 1: Add attach/detach API to ReactControl

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java`

- [ ] **Step 1: Add imports (if missing)**

Ensure these imports exist near the top:

```java
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
```

- [ ] **Step 2: Add fields and public API**

Add these fields near the existing private fields (e.g. next to `_rendered`):

```java
	/**
	 * Whether this control is currently attached to the displayed UI tree.
	 */
	private boolean _attached;

	private final List<Runnable> _attachListeners = new CopyOnWriteArrayList<>();

	private final List<Runnable> _detachListeners = new CopyOnWriteArrayList<>();
```

Add these public methods somewhere in the class (e.g. next to `cleanupTree`):

```java
	/**
	 * Whether this control is currently attached to the displayed UI tree.
	 *
	 * <p>
	 * A control is attached between calls to {@link #attach()} and {@link #detach()}. It is
	 * detached while it exists in memory but is not currently displayed (e.g. the inactive child
	 * of a one-of-N container like a sidebar, tab-bar or deck-pane). Final disposal is signaled
	 * separately by {@link #cleanupTree()}.
	 * </p>
	 */
	public final boolean isAttached() {
		return _attached;
	}

	/**
	 * Marks this control as attached (part of the displayed tree) and fires
	 * {@link #addAttachListener(Runnable) attach listeners}.
	 *
	 * <p>
	 * Idempotent: if already attached, this call is a no-op.
	 * </p>
	 */
	public final void attach() {
		if (_attached) {
			return;
		}
		_attached = true;
		for (Runnable l : _attachListeners) {
			l.run();
		}
		propagateAttach();
	}

	/**
	 * Marks this control as detached (still in memory but not displayed) and fires
	 * {@link #addDetachListener(Runnable) detach listeners}.
	 *
	 * <p>
	 * Idempotent: if not attached, this call is a no-op.
	 * </p>
	 */
	public final void detach() {
		if (!_attached) {
			return;
		}
		propagateDetach();
		_attached = false;
		for (Runnable l : _detachListeners) {
			l.run();
		}
	}

	/**
	 * Registers a listener that fires whenever this control becomes attached.
	 *
	 * <p>
	 * If this control is already attached at registration time, the listener is invoked
	 * immediately to simplify late registrations.
	 * </p>
	 */
	public final void addAttachListener(Runnable listener) {
		_attachListeners.add(listener);
		if (_attached) {
			listener.run();
		}
	}

	/**
	 * Unregisters a previously added attach listener.
	 */
	public final void removeAttachListener(Runnable listener) {
		_attachListeners.remove(listener);
	}

	/**
	 * Registers a listener that fires whenever this control becomes detached.
	 */
	public final void addDetachListener(Runnable listener) {
		_detachListeners.add(listener);
	}

	/**
	 * Unregisters a previously added detach listener.
	 */
	public final void removeDetachListener(Runnable listener) {
		_detachListeners.remove(listener);
	}

	/**
	 * Hook for subclasses to propagate an {@link #attach()} call to their currently displayed
	 * children. The default does nothing.
	 */
	protected void propagateAttach() {
		// Default: no children to propagate to.
	}

	/**
	 * Hook for subclasses to propagate a {@link #detach()} call to their currently displayed
	 * children. The default does nothing.
	 */
	protected void propagateDetach() {
		// Default: no children to propagate to.
	}
```

- [ ] **Step 3: Ensure cleanupTree detaches first**

Locate the existing `cleanupTree()` method and ensure it calls `detach()` at the very beginning (idempotent; safe if already detached):

Find:

```java
	public final void cleanupTree() {
```

Add as the first statement inside the method body:

```java
		detach();
```

(If `cleanupTree` is not `final`, keep the existing signature. Only add the `detach()` call.)

- [ ] **Step 4: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.react compile 2>&1 | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java
git commit -m "Ticket #29108: Add attach/detach lifecycle to ReactControl."
```

---

## Task 2: Cascade attach/detach in always-visible composites

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactCompositeControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ToolbarControl.java`

- [ ] **Step 1: Override hooks in ReactCompositeControl**

Add (e.g. after `cleanupChildren`):

```java
	@Override
	protected void propagateAttach() {
		for (ReactControl child : _children) {
			child.attach();
		}
	}

	@Override
	protected void propagateDetach() {
		for (ReactControl child : _children) {
			child.detach();
		}
	}
```

- [ ] **Step 2: Override hooks in ToolbarControl**

`ToolbarControl` maintains its own `_toolbarButtons` list in addition to whatever its subclass stores. Cascade to those as well. Open `ToolbarControl.java` and add:

```java
	@Override
	protected void propagateAttach() {
		super.propagateAttach();
		for (ReactControl btn : _toolbarButtons) {
			btn.attach();
		}
	}

	@Override
	protected void propagateDetach() {
		super.propagateDetach();
		for (ReactControl btn : _toolbarButtons) {
			btn.detach();
		}
	}
```

Also update `addToolbarButton` and `removeToolbarButton`:

Find:

```java
	public void addToolbarButton(ReactControl button) {
		_toolbarButtons.add(button);
		putState(TOOLBAR_BUTTONS, _toolbarButtons);
	}
```

Replace with:

```java
	public void addToolbarButton(ReactControl button) {
		_toolbarButtons.add(button);
		putState(TOOLBAR_BUTTONS, _toolbarButtons);
		if (isAttached()) {
			button.attach();
		}
	}
```

Find:

```java
	public boolean removeToolbarButton(ReactControl button) {
```

and in its body, before or after the `_toolbarButtons.remove(button)` line (whichever preserves the return flow), add a `button.detach();` call when the button was actually removed. Concretely, rewrite the method as:

```java
	public boolean removeToolbarButton(ReactControl button) {
		boolean removed = _toolbarButtons.remove(button);
		if (removed) {
			putState(TOOLBAR_BUTTONS, _toolbarButtons);
			button.detach();
		}
		return removed;
	}
```

- [ ] **Step 3: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.react compile 2>&1 | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactCompositeControl.java \
        com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ToolbarControl.java
git commit -m "Ticket #29108: Cascade attach/detach in composite and toolbar controls."
```

---

## Task 3: Sidebar — attach/detach active navigation content

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/sidebar/ReactSidebarControl.java`

- [ ] **Step 1: Attach the initial active content**

Locate the section of `ReactSidebarControl` where the initial active content is ensured. Based on the existing `getState(ACTIVE_CONTENT) == null && _activeItemId != null` block (around the `beforeWriteAction` / render phase — search for `ACTIVE_CONTENT`), change the ensure-active-content code so it also attaches the content:

Before:

```java
		if (getState(ACTIVE_CONTENT) == null && _activeItemId != null) {
			ReactControl activeContent = getOrCreateContent(_activeItemId);
			// (existing code that installs it into state)
		}
```

After:

```java
		if (getState(ACTIVE_CONTENT) == null && _activeItemId != null) {
			ReactControl activeContent = getOrCreateContent(_activeItemId);
			// (existing code that installs it into state)
			if (isAttached()) {
				activeContent.attach();
			}
		}
```

(Preserve whatever state-setting code was there; only add the `if (isAttached()) activeContent.attach();` line.)

- [ ] **Step 2: Detach/attach on selectItem**

Locate `selectItem(String itemId)`. Modify so that the previously active content is detached and the new one attached:

```java
	public void selectItem(String itemId) {
		if (itemId.equals(_activeItemId)) {
			return;
		}
		ReactControl previousContent = _contentCache.get(_activeItemId);
		_activeItemId = itemId;

		if (!isSSEAttached()) {
			putStateSilent(ACTIVE_ITEM_ID, _activeItemId);
			return;
		}

		ReactControl content = getOrCreateContent(itemId);

		Object tx = beginUpdate();
		putState(ACTIVE_ITEM_ID, itemId);
		putState(ACTIVE_CONTENT, content);
		commitUpdate(tx);

		if (previousContent != null) {
			previousContent.detach();
		}
		if (isAttached()) {
			content.attach();
		}
	}
```

- [ ] **Step 3: Propagate attach/detach to currently-active content only**

Override the two hooks at the class level:

```java
	@Override
	protected void propagateAttach() {
		super.propagateAttach();
		if (_activeItemId != null) {
			ReactControl content = _contentCache.get(_activeItemId);
			if (content != null) {
				content.attach();
			}
		}
	}

	@Override
	protected void propagateDetach() {
		super.propagateDetach();
		if (_activeItemId != null) {
			ReactControl content = _contentCache.get(_activeItemId);
			if (content != null) {
				content.detach();
			}
		}
	}
```

(The `super.*` calls handle header / footer / collapsed variants that are covered by the composite cascade.)

- [ ] **Step 4: Detach on cleanupChildren before disposal**

Locate `cleanupChildren()`. Before iterating `_contentCache.values()` and calling `cleanupTree()`, ensure the currently active one is detached. Because `cleanupTree` already calls `detach()` (Task 1 Step 3), this is redundant but clarifies intent:

```java
	@Override
	protected void cleanupChildren() {
		// Detach first so contributed commands / listeners unwind before disposal.
		if (_activeItemId != null) {
			ReactControl active = _contentCache.get(_activeItemId);
			if (active != null) {
				active.detach();
			}
		}
		for (ReactControl cached : _contentCache.values()) {
			cached.cleanupTree();
		}
		// ... rest of existing cleanup code unchanged.
	}
```

- [ ] **Step 5: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.react compile 2>&1 | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/sidebar/ReactSidebarControl.java
git commit -m "Ticket #29108: Sidebar attaches active nav content; detaches on switch."
```

---

## Task 4: TabBar — attach/detach active tab content

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tabbar/ReactTabBarControl.java`

- [ ] **Step 1: Identify the active-tab tracking and switch method**

Open the file. Locate:
- The field or state that holds the currently active tab id (e.g. `_activeTabId`).
- The method that changes the active tab (typically `selectTab(String id)` or similar — grep for `ACTIVE_TAB` / `selectTab` / `switchTab`).
- The map/list of tab content controls (often `_tabContents` or similar).

If the tab bar has the same shape as `ReactSidebarControl` (one active at a time, per-tab content), apply the same pattern:

- [ ] **Step 2: Override propagateAttach / propagateDetach**

```java
	@Override
	protected void propagateAttach() {
		super.propagateAttach();
		ReactControl active = resolveActiveContent();
		if (active != null) {
			active.attach();
		}
	}

	@Override
	protected void propagateDetach() {
		super.propagateDetach();
		ReactControl active = resolveActiveContent();
		if (active != null) {
			active.detach();
		}
	}

	private ReactControl resolveActiveContent() {
		// Return the content control associated with the currently-active tab,
		// or null if no tab is active. Implementation depends on how tab content
		// is stored in this class.
		// PLACEHOLDER: adapt to this class's actual field layout.
		return null;
	}
```

Replace the `PLACEHOLDER` body with the correct lookup based on the class's actual field structure (e.g. `_activeTabId`, `_tabContents.get(_activeTabId)`).

- [ ] **Step 3: Add detach/attach in the tab switch method**

In the method that changes the active tab (e.g. `selectTab`), apply the same pattern as `ReactSidebarControl.selectItem`: capture the previously active content, update state, detach old, attach new (only when `isAttached()`).

- [ ] **Step 4: If tab content is not cached per tab**

If the current `ReactTabBarControl` stores all tab contents at construction time and simply toggles visibility via CSS, the attach/detach semantics still apply conceptually — the *visible* tab is attached, the hidden ones detached. Use `super.propagateAttach()` followed by attaching only the active tab, and override the base composite cascade so that hidden tabs are NOT attached even via the normal children iteration. If the class currently extends `ReactCompositeControl`, you may need to switch to a bare `ReactControl` extension or override `propagateAttach/propagateDetach` without calling `super`. Inspect the file and report BLOCKED if the shape doesn't fit cleanly.

- [ ] **Step 5: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.react compile 2>&1 | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tabbar/ReactTabBarControl.java
git commit -m "Ticket #29108: TabBar attaches active tab content; detaches on switch."
```

---

## Task 5: DeckPane — attach/detach visible pane

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactDeckPaneControl.java`

- [ ] **Step 1: Identify the visible pane**

Open the file. A DeckPane shows one-of-N panes at a time. Identify:
- How the visible pane is selected (e.g. `_activePaneIndex`, `_visiblePaneId`, a command setter).
- The collection of pane content controls.

- [ ] **Step 2: Apply the one-of-N pattern**

Add `propagateAttach` / `propagateDetach` overrides that attach only the visible pane. Add detach/attach on pane switch. Mirror the Sidebar/TabBar pattern.

If the DeckPane uses a different selection API (e.g. by index rather than by id), adapt accordingly.

- [ ] **Step 3: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.react compile 2>&1 | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactDeckPaneControl.java
git commit -m "Ticket #29108: DeckPane attaches visible pane; detaches on switch."
```

---

## Task 6: Dialog / Window / Drawer — attach on show, detach on close

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ReactDialogControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ReactWindowControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ReactDrawerControl.java`

- [ ] **Step 1: For each overlay control, apply this pattern**

Each overlay control has:
- A boolean state (e.g. `_open` / `_visible` / `_shown`) or a state key signaling whether it is currently displayed.
- A content child (or list of children).
- Methods like `show()` / `open()` / `hide()` / `close()`.

Changes per file:

1. Override `propagateAttach` / `propagateDetach`:

```java
	@Override
	protected void propagateAttach() {
		super.propagateAttach();
		if (isCurrentlyShown()) {
			for (ReactControl child : getOverlayContents()) {
				child.attach();
			}
		}
	}

	@Override
	protected void propagateDetach() {
		super.propagateDetach();
		for (ReactControl child : getOverlayContents()) {
			child.detach();
		}
	}

	private boolean isCurrentlyShown() {
		// Return the current visibility state (read from the class's boolean field or state key).
		return /* e.g. _open */;
	}

	private List<ReactControl> getOverlayContents() {
		// Return the overlay's content children.
		return /* e.g. List.of(_content); */;
	}
```

Adapt `isCurrentlyShown()` and `getOverlayContents()` to the actual fields of each class.

**Important:** overlay content is typically also in the composite children list, so `super.propagateAttach` (via `ReactCompositeControl`) would normally attach *all* children unconditionally. Either:
- Don't store overlay content in the composite children list (so super does nothing); or
- Override WITHOUT calling `super.propagateAttach` / `super.propagateDetach` if the composite would incorrectly attach hidden content.

Inspect each class and pick the right approach per class. If neither is clean, report BLOCKED with a description.

2. In `show` / `open` (whatever opens the overlay), after updating the visible state, call `attach()` on the overlay's content — but only if `isAttached()` (i.e. the overlay itself is attached to the outer tree).

3. In `hide` / `close`, call `detach()` on the overlay's content before or after clearing the state.

- [ ] **Step 2: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.react compile 2>&1 | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit (one commit for all three overlay controls)**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ReactDialogControl.java \
        com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ReactWindowControl.java \
        com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ReactDrawerControl.java
git commit -m "Ticket #29108: Dialog, Window and Drawer attach content on show, detach on close."
```

---

## Task 7: Integration — attach root control

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/AppShellElement.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ReferenceElement.java`

- [ ] **Step 1: AppShellElement — attach the shell after construction**

At the end of `AppShellElement.createControl`, just before the return statement, add:

```java
		ReactAppShellControl shellControl = new ReactAppShellControl(context, header, content, footer, snackbar, errorSink);
		shellControl.attach();
		return shellControl;
```

(Refactor the existing single-expression return into a local variable first, if necessary.)

- [ ] **Step 2: ReferenceElement — attach the referenced view when standalone**

In `ReferenceElement.createControl`, after obtaining the `ReactControl` from `referencedView.createControl(childContext)`, wrap the attach call:

```java
		ReactControl viewControl = (ReactControl) referencedView.createControl(childContext);
		ReloadableControl wrapped = new ReloadableControl(fullPath, childContext, viewControl);
		// If the parent context doesn't have a container that will attach us, attach now.
		// In practice, parent containers (sidebar, tab, etc.) drive attach themselves.
		// We only need to self-attach when there is no enclosing shell/composite.
		// Since the common case IS to be nested in an AppShell-driven tree, only
		// attach when the parent context is clearly root (no enclosing composite
		// attach chain). Do NOT attach unconditionally — that could cause double-attach.
		return wrapped;
```

**Do not unconditionally call `attach()` here** — the containing control (AppShell, Sidebar, etc.) will drive it. Add a comment explaining this. Only AppShellElement in Task 7 Step 1 triggers the root attach.

- [ ] **Step 3: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.view compile 2>&1 | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/AppShellElement.java \
        com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ReferenceElement.java
git commit -m "Ticket #29108: AppShell attaches root control after construction."
```

---

## Task 8: DashboardElement — contribute commands via attach/detach

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/DashboardElement.java`

- [ ] **Step 1: Rewrite contributeEditCommands**

Replace the existing method:

```java
	private void contributeEditCommands(ViewContext context, ReactDashboardControl control) {
		CommandScope scope = context.getCommandScope();
		if (scope == null) {
			return;
		}
		DashboardCommandModel edit = DashboardCommandModel.editCommand(control);
		DashboardCommandModel done = DashboardCommandModel.doneCommand(control);
		edit.attach();
		done.attach();
		scope.addCommand(edit);
		scope.addCommand(done);

		control.addCleanupAction(() -> {
			scope.removeCommand(edit);
			scope.removeCommand(done);
			edit.detach();
			done.detach();
		});
	}
```

with:

```java
	private void contributeEditCommands(ViewContext context, ReactDashboardControl control) {
		CommandScope scope = context.getCommandScope();
		if (scope == null) {
			return;
		}
		DashboardCommandModel edit = DashboardCommandModel.editCommand(control);
		DashboardCommandModel done = DashboardCommandModel.doneCommand(control);

		Runnable onAttach = () -> {
			edit.attach();
			done.attach();
			scope.addCommand(edit);
			scope.addCommand(done);
		};
		Runnable onDetach = () -> {
			scope.removeCommand(edit);
			scope.removeCommand(done);
			edit.detach();
			done.detach();
		};

		control.addAttachListener(onAttach);
		control.addDetachListener(onDetach);
		control.addCleanupAction(() -> {
			// Safety net: if the control was still attached at cleanup time
			// (unusual), the attach-listener contract already removed the commands
			// via the earlier detach. No further action needed.
		});
	}
```

Note: `addAttachListener` will invoke `onAttach` immediately if the control is already attached at the time of registration (per Task 1 Step 2 semantics), so commands appear as soon as the dashboard is visible. When the user navigates away, the sidebar detaches the dashboard control, which fires `onDetach`, removing the commands from the scope.

- [ ] **Step 2: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.view compile 2>&1 | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/DashboardElement.java
git commit -m "Ticket #29108: Dashboard contributes commands via attach/detach listeners."
```

---

## Task 9: Full build + Playwright verification

- [ ] **Step 1: Build changed modules only**

Run:

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.react 2>&1 | tail -5
mvn -B install -DskipTests=true -pl com.top_logic.layout.view 2>&1 | tail -5
mvn -B package -DskipTests=true -pl com.top_logic.demo 2>&1 | tail -5
```

Each should end with `BUILD SUCCESS`. Use `dangerouslyDisableSandbox: true` if the DeepL `translate-system-messages` step fails on network.

- [ ] **Step 2: Restart the demo app**

Use the `tl-app` skill: `restart com.top_logic.demo`.

- [ ] **Step 3: Playwright scenarios**

Log in (root / root1234) and verify each case. Navigate to `http://localhost:<PORT>/tl-demo/`.

- **Case A — Dashboard-Layout is active:**
  - Click "Dashboard-Layout" in the sidebar.
  - App bar's trailing actions contain the "Edit Layout" button (visible) plus a hidden "Done" button.
  - Click "Edit Layout" → it becomes hidden, "Done" becomes visible, tiles become draggable.
  - Click "Done" → reverse transition.

- **Case B — Navigate away clears commands:**
  - From Dashboard-Layout, click "Dashboard" (the first nav-item) in the sidebar.
  - App bar contains ONLY "Designer" — neither "Edit Layout" nor "Done" should be in the DOM as part of the app-bar action slot (or both should be `display: none`, but from the app-bar's button count they should be detached and removed).
  - Click "Dashboard-Layout" again → "Edit Layout" reappears.

- **Case C — Edit mode persists across navigation:**
  - On Dashboard-Layout, enter edit mode ("Done" visible).
  - Click "Dashboard". Click "Dashboard-Layout" again.
  - Expected: the dashboard returns to its previous state (still in edit mode OR reset — document whichever is the actual behavior). Edit/Done command visibility should match the actual mode on return.

- **Case D — Drag reorder + persistence (regression check from prior plans):**
  - In edit mode, drag a tile; verify it moves; reload page; verify order persists.

Take screenshots into `com.top_logic.demo/target/dashboard-attach-0{1,2,3,4}-*.png`.

- [ ] **Step 4: Final commit if fixes are needed**

If any verification surfaced fixes, commit them:

```bash
git commit -m "Ticket #29108: Fix <specific issue found during verification>."
```

---

## Notes

- **Idempotency is load-bearing.** `attach()` / `detach()` MUST be no-ops when called on an already-attached / already-detached control. Tasks that "attach new" can always call `attach()` without checking.
- **Order of propagation.** `attach()` fires the listener *before* propagating to children (so a parent's post-attach work runs before child attach side effects). `detach()` fires listeners *after* propagating to children (children detach first, then the parent), mirroring the lifecycle of a tree teardown.
- **Re-attach correctness.** The sidebar cache keeps content alive between navigations. Repeated `attach()` / `detach()` cycles on the same control must leave it in a consistent state each time. Rely on the listener registry (not single-shot hooks).
- **cleanupTree still calls detach first** (Task 1 Step 3), so any code that relies on detach for final cleanup (rare) still runs before `cleanupChildren` disposes resources.
- **Keep commits minimal per module.** Build only the modules you changed; do not use `-am` to avoid rebuilding the world.
- **Commit discipline:** `Ticket #29108:` prefix; never amend; no AI attribution.
