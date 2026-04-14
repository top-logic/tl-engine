# Dashboard Edit Command in AppBar — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the in-dashboard toolbar row with the "Edit Layout" / "Done" button and surface that command instead as a trailing action in the app bar (the existing title row), analogous to how `FormElement` contributes its Edit/Save/Cancel commands to the surrounding panel's toolbar.

**Architecture:**
- `AppShellElement` establishes a shared `CommandScope` and passes it (via `withCommandScope`) to both header and content slots.
- `AppBarElement` consumes the parent scope if present (instead of always creating its own), so the app bar becomes the natural host for any command contributed by descendant elements anywhere in the shell's content area.
- `editMode` moves from client-only React state to server state on `ReactDashboardControl`; the control exposes `enterEditMode()` / `exitEditMode()` methods.
- A new `DashboardCommandModel` (analogous to `FormCommandModel`) provides `editCommand` and `doneCommand` factories with `PLACEMENT_TOOLBAR`. Exactly one is visible at a time based on `editMode`.
- `DashboardElement.createControl()` contributes both commands to `parentContext.getCommandScope()` (silently dropped if there is no parent scope).
- `TLDashboard.tsx` loses its own toolbar and button; `editMode` is read from `state.editMode`.

**Tech Stack:** Java 17, TopLogic config framework, existing `CommandScope` / `CommandModel` / `PLACEMENT_TOOLBAR` plumbing, React via `tl-react-bridge`.

---

## File Structure

### Modify
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/AppShellElement.java` — create shared `CommandScope`, pass to header + content contexts.
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/AppBarElement.java` — use parent `CommandScope` if available; add own configured commands to it.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactDashboardControl.java` — add `editMode` state + `setEditMode(boolean)` method + listener registry.
- `com.top_logic.layout.react/react-src/controls/TLDashboard.tsx` — drop toolbar/button, read `editMode` from state.
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/DashboardElement.java` — contribute edit/done commands to parent scope.

### Create
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/DashboardCommandModel.java` — analogue of `FormCommandModel`, backed by a `ReactDashboardControl`.

### Reference (read-only)
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormCommandModel.java` — pattern to copy.
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/FormElement.java:547-586` — `contributeEditCommands` pattern.
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/CommandScope.java` — `addCommand` / `removeCommand` / `addListener`.

---

## Task 1: AppShell provides shared CommandScope

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/AppShellElement.java`

- [ ] **Step 1: Create a CommandScope in createControl and derive scoped contexts**

Replace the body of `createControl(ViewContext context)` in `AppShellElement.java` so that a fresh `CommandScope` is established (if the parent context has none) and both header and content are built against the scoped context. The footer keeps the existing scoped context as well.

Add the import:

```java
import com.top_logic.layout.view.command.CommandScope;
import java.util.List;
```

Replace:

```java
	@Override
	public IReactControl createControl(ViewContext context) {
		// Create snackbar and error sink first.
		ReactSnackbarControl snackbar = new ReactSnackbarControl(context, "",
			ReactSnackbarControl.Variant.SUCCESS, () -> { /* no-op */ });
		ErrorSink errorSink = createErrorSink(snackbar);

		// Derive context with error sink for children.
		ViewContext scopedContext = context.withErrorSink(errorSink);

		// Create slot controls in the scoped context.
		ReactControl header = createSlotControl(scopedContext, _header);
		ReactControl content = createSlotControl(scopedContext, _content);
		ReactControl footer = createSlotControl(scopedContext, _footer);

		return new ReactAppShellControl(context, header, content, footer, snackbar, errorSink);
	}
```

with:

```java
	@Override
	public IReactControl createControl(ViewContext context) {
		// Create snackbar and error sink first.
		ReactSnackbarControl snackbar = new ReactSnackbarControl(context, "",
			ReactSnackbarControl.Variant.SUCCESS, () -> { /* no-op */ });
		ErrorSink errorSink = createErrorSink(snackbar);

		// Establish a shared command scope so that commands contributed by descendant
		// elements (forms, dashboards, ...) can bubble up to the app bar in the header.
		// If a parent already provides a scope, reuse it.
		CommandScope sharedScope = context.getCommandScope();
		if (sharedScope == null) {
			sharedScope = new CommandScope(List.of());
		}

		ViewContext scopedContext = context
			.withErrorSink(errorSink)
			.withCommandScope(sharedScope);

		// Create slot controls in the scoped context.
		ReactControl header = createSlotControl(scopedContext, _header);
		ReactControl content = createSlotControl(scopedContext, _content);
		ReactControl footer = createSlotControl(scopedContext, _footer);

		return new ReactAppShellControl(context, header, content, footer, snackbar, errorSink);
	}
```

- [ ] **Step 2: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.view compile 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/AppShellElement.java
git commit -m "Ticket #29108: AppShell establishes shared CommandScope for header and content."
```

---

## Task 2: AppBar consumes parent CommandScope if present

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/AppBarElement.java`

- [ ] **Step 1: Use parent scope when available**

Replace the scope-handling in `createControl`:

Locate this block:

```java
		// Build command models.
		List<ViewCommandModel> commandModels = buildCommandModels(context);

		// Create command scope and derived context.
		CommandScope scope = new CommandScope(commandModels);
		ViewContext derivedContext = context.withCommandScope(scope);
```

Replace with:

```java
		// Build command models.
		List<ViewCommandModel> commandModels = buildCommandModels(context);

		// Use parent scope when available so that commands contributed by descendants
		// (e.g. form edit commands, dashboard layout edit) surface in the app bar.
		// Fall back to a private scope if the app bar is used standalone.
		CommandScope parentScope = context.getCommandScope();
		CommandScope scope;
		ViewContext derivedContext;
		if (parentScope != null) {
			for (ViewCommandModel model : commandModels) {
				parentScope.addCommand(model);
			}
			scope = parentScope;
			derivedContext = context;
		} else {
			scope = new CommandScope(commandModels);
			derivedContext = context.withCommandScope(scope);
		}
```

- [ ] **Step 2: Update cleanup to remove contributed commands from shared scope**

Locate the cleanup registration near the end of `createControl`:

```java
		// Register cleanup for command model lifecycle.
		appBar.addCleanupAction(() -> {
			for (ViewCommandModel model : commandModels) {
				model.detach();
			}
		});
```

Replace with:

```java
		// Register cleanup for command model lifecycle. When using a shared scope,
		// also remove our contributed commands.
		final CommandScope cleanupScope = scope;
		final boolean usingSharedScope = (parentScope != null);
		appBar.addCleanupAction(() -> {
			if (usingSharedScope) {
				for (ViewCommandModel model : commandModels) {
					cleanupScope.removeCommand(model);
				}
			}
			for (ViewCommandModel model : commandModels) {
				model.detach();
			}
		});
```

- [ ] **Step 3: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.view compile 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/AppBarElement.java
git commit -m "Ticket #29108: AppBar consumes parent CommandScope so descendants can contribute."
```

---

## Task 3: ReactDashboardControl server-side editMode + methods

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactDashboardControl.java`

- [ ] **Step 1: Add editMode state, setter and listener registry**

Add two new imports near the top:

```java
import java.util.concurrent.CopyOnWriteArrayList;
```

Below the existing `private static final String ORDER_ARG = "order";` line, add:

```java
	private static final String EDIT_MODE = "editMode";
```

Below the `private final Consumer<List<String>> _onReorder;` line, add:

```java
	private boolean _editMode;

	private final List<Runnable> _editModeListeners = new CopyOnWriteArrayList<>();
```

In the constructor, after `putState(CHILDREN, buildDescriptors());`, add:

```java
		putState(EDIT_MODE, Boolean.FALSE);
```

Add these new public methods before `cleanupChildren()`:

```java
	/**
	 * Whether the dashboard is currently in edit mode (drag-to-reorder active).
	 */
	public boolean isEditMode() {
		return _editMode;
	}

	/**
	 * Switches edit mode on. Notifies listeners registered via
	 * {@link #addEditModeListener(Runnable)}.
	 */
	public void enterEditMode() {
		setEditMode(true);
	}

	/**
	 * Switches edit mode off. Notifies listeners.
	 */
	public void exitEditMode() {
		setEditMode(false);
	}

	private void setEditMode(boolean value) {
		if (_editMode == value) {
			return;
		}
		_editMode = value;
		putState(EDIT_MODE, Boolean.valueOf(value));
		for (Runnable l : _editModeListeners) {
			l.run();
		}
	}

	/**
	 * Registers a listener that fires when {@link #isEditMode()} changes.
	 */
	public void addEditModeListener(Runnable listener) {
		_editModeListeners.add(listener);
	}

	/**
	 * Unregisters a previously added edit-mode listener.
	 */
	public void removeEditModeListener(Runnable listener) {
		_editModeListeners.remove(listener);
	}
```

- [ ] **Step 2: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.react compile 2>&1 | tee com.top_logic.layout.react/target/mvn-build.log | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactDashboardControl.java
git commit -m "Ticket #29108: Add server-side editMode to ReactDashboardControl."
```

---

## Task 4: DashboardCommandModel

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/DashboardCommandModel.java`

- [ ] **Step 1: Write the class**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandModel} that delegates to a {@link ReactDashboardControl} for edit-mode
 * lifecycle operations.
 *
 * <p>
 * Factories create an {@code edit} command (visible when the dashboard is not in edit mode)
 * and a {@code done} command (visible when the dashboard is in edit mode). Both carry
 * {@link CommandModel#PLACEMENT_TOOLBAR} so that a surrounding {@code CommandScopeElement}
 * (panel, window, app bar) renders them in its chrome.
 * </p>
 */
public class DashboardCommandModel implements CommandModel {

	private final String _name;

	private final ResKey _labelKey;

	private final ThemeImage _image;

	private final Consumer<ReactContext> _action;

	private final Predicate<ReactDashboardControl> _visibleWhen;

	private final ReactDashboardControl _dashboard;

	private boolean _visible;

	private final List<Runnable> _stateChangeListeners = new ArrayList<>();

	private final Runnable _dashboardListener = this::handleDashboardStateChanged;

	private DashboardCommandModel(String name, ResKey labelKey, ThemeImage image,
			ReactDashboardControl dashboard, Consumer<ReactContext> action,
			Predicate<ReactDashboardControl> visibleWhen) {
		_name = name;
		_labelKey = labelKey;
		_image = image;
		_dashboard = dashboard;
		_action = action;
		_visibleWhen = visibleWhen;
		_visible = visibleWhen.test(dashboard);
	}

	/**
	 * Creates the "Edit Layout" command: visible when the dashboard is not in edit mode.
	 */
	public static DashboardCommandModel editCommand(ReactDashboardControl dashboard) {
		return new DashboardCommandModel("dashboardEdit", I18NConstants.DASHBOARD_EDIT,
			Icons.DASHBOARD_EDIT, dashboard,
			ctx -> dashboard.enterEditMode(),
			d -> !d.isEditMode());
	}

	/**
	 * Creates the "Done" command: visible when the dashboard is in edit mode.
	 */
	public static DashboardCommandModel doneCommand(ReactDashboardControl dashboard) {
		return new DashboardCommandModel("dashboardDone", I18NConstants.DASHBOARD_DONE,
			Icons.DASHBOARD_DONE, dashboard,
			ctx -> dashboard.exitEditMode(),
			d -> d.isEditMode());
	}

	/**
	 * Attaches to the dashboard's state change listener. Must be called before the command
	 * becomes active so that visibility updates propagate.
	 */
	public void attach() {
		_dashboard.addEditModeListener(_dashboardListener);
	}

	/**
	 * Detaches from the dashboard. Must be called during cleanup.
	 */
	public void detach() {
		_dashboard.removeEditModeListener(_dashboardListener);
	}

	private void handleDashboardStateChanged() {
		boolean newVisible = _visibleWhen.test(_dashboard);
		if (newVisible != _visible) {
			_visible = newVisible;
			fireStateChange();
		}
	}

	private void fireStateChange() {
		for (Runnable l : _stateChangeListeners) {
			l.run();
		}
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public ResKey getLabel() {
		return _labelKey;
	}

	@Override
	public ThemeImage getImage() {
		return _image;
	}

	@Override
	public String getPlacement() {
		return PLACEMENT_TOOLBAR;
	}

	@Override
	public boolean isExecutable() {
		return true;
	}

	@Override
	public boolean isVisible() {
		return _visible;
	}

	@Override
	public HandlerResult executeCommand(ReactContext context) {
		_action.accept(context);
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public void addStateChangeListener(Runnable listener) {
		_stateChangeListeners.add(listener);
	}

	@Override
	public void removeStateChangeListener(Runnable listener) {
		_stateChangeListeners.remove(listener);
	}
}
```

- [ ] **Step 2: Add I18N constants and Icons references**

Ensure I18N constants exist. Open `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/I18NConstants.java` and add the following before `initConstants(I18NConstants.class);` if not already present:

```java
	/**
	 * @en Edit Layout
	 */
	public static ResKey DASHBOARD_EDIT;

	/**
	 * @en Done
	 */
	public static ResKey DASHBOARD_DONE;
```

Find the existing `Icons.java` used in the view module (search `import com.top_logic.layout.view.element.Icons;` or inspect `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/Icons.java`). Add two theme image constants:

```java
	public static ThemeImage DASHBOARD_EDIT;

	public static ThemeImage DASHBOARD_DONE;
```

If `Icons` does not exist in `com.top_logic.layout.view.element`, reuse the nearest existing icon facade: inspect how `FormCommandModel` references `Icons.FORM_EDIT`. Mirror the package location exactly (i.e. `com.top_logic.layout.view.form.Icons` or similar). For simplicity, reuse `Icons.FORM_EDIT` as `DASHBOARD_EDIT` icon and a done/check icon for `DASHBOARD_DONE` if a dedicated icon isn't easily discoverable; adjust the import in `DashboardCommandModel.java` accordingly.

**If you cannot locate a suitable Icons class in the `com.top_logic.layout.view.element` package, report BLOCKED — the implementer needs explicit guidance on where themeable icons live.**

- [ ] **Step 3: Build and verify I18N generation**

Run: `mvn -B install -DskipTests=true -pl com.top_logic.layout.view 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log | tail -15`
Expected: `BUILD SUCCESS`. Verify that `messages_en.properties` / `messages_de.properties` under `com.top_logic.layout.view/src/main/java/META-INF/` now contain lines for `...I18NConstants.DASHBOARD_EDIT` and `...I18NConstants.DASHBOARD_DONE`.

If the build fails with a DeepL/network sandbox error during `translate-system-messages`, retry with `dangerouslyDisableSandbox: true`.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/DashboardCommandModel.java \
        com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/I18NConstants.java \
        com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/Icons.java \
        com.top_logic.layout.view/src/main/java/META-INF/messages_en.properties \
        com.top_logic.layout.view/src/main/java/META-INF/messages_de.properties
git commit -m "Ticket #29108: Add DashboardCommandModel with edit/done toolbar commands."
```

(Adjust the list of files to match what actually changed — drop `Icons.java` if you reused an existing one.)

---

## Task 5: DashboardElement contributes commands to parent scope

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/DashboardElement.java`

- [ ] **Step 1: Add contribution + cleanup**

Add imports:

```java
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.view.command.CommandScope;
```

Replace the body of `createControl(ViewContext context)`:

```java
	@Override
	public IReactControl createControl(ViewContext context) {
		List<TileElement> ordered = applyPersonalOrder(_tiles);
		List<Tile> reactTiles = new ArrayList<>(ordered.size());
		for (TileElement t : ordered) {
			reactTiles.add(new Tile(t.getId(), t.getWidth(), t.getRowSpan(), t.createContentControl(context)));
		}
		ReactDashboardControl control =
			new ReactDashboardControl(context, _minColWidth, reactTiles, this::storePersonalOrder);

		contributeEditCommands(context, control);

		return control;
	}

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

Note: `ReactControl.addCleanupAction(Runnable)` is the standard lifecycle hook — verify the method exists on `ReactDashboardControl` (via its `ReactControl` base). If not, use whichever cleanup mechanism `ReactControl` exposes (inspect `ReactControl.java` for `addCleanupAction` / `onDispose` / equivalent).

- [ ] **Step 2: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.view compile 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/DashboardElement.java
git commit -m "Ticket #29108: Dashboard contributes edit/done commands to parent scope."
```

---

## Task 6: TLDashboard.tsx — remove local toolbar, read editMode from state

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLDashboard.tsx`

- [ ] **Step 1: Remove local editMode state, read from server state**

Near the top of the component (`const TLDashboard: React.FC<TLCellProps> = ({ controlId }) => {`), remove:

```tsx
  const [editMode, setEditMode] = useState<boolean>(false);
```

Add below the existing `tiles` line:

```tsx
  const editMode = state.editMode === true;
```

- [ ] **Step 2: Remove the toolbar JSX**

Remove the entire toolbar block:

```tsx
      <div className="tlDashboard__toolbar">
        <button
          type="button"
          className={'tlDashboard__editBtn' + (editMode ? ' tlDashboard__editBtn--active' : '')}
          onClick={() => setEditMode(v => !v)}
        >
          {editMode ? 'Done' : 'Edit Layout'}
        </button>
      </div>
```

- [ ] **Step 3: Rebuild react module**

Run: `mvn -B install -DskipTests=true -pl com.top_logic.layout.react 2>&1 | tee com.top_logic.layout.react/target/mvn-build.log | tail -15`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls/TLDashboard.tsx \
        com.top_logic.layout.react/src/main/webapp/script/tl-react-controls.js
git commit -m "Ticket #29108: Remove dashboard toolbar; editMode driven by server state."
```

---

## Task 7: Remove now-unused CSS rules

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

- [ ] **Step 1: Drop the toolbar and edit-button styles**

Remove (only) these blocks from the dashboard styles section:

```css
.tlDashboard__toolbar {
  display: flex;
  justify-content: flex-end;
}
.tlDashboard__editBtn {
  font: inherit;
  padding: 0.35rem 0.75rem;
  border: 1px solid var(--color-border, #c0c4cc);
  background: var(--color-surface, #fff);
  border-radius: 4px;
  cursor: pointer;
}
.tlDashboard__editBtn--active {
  background: var(--layer-selected, #e6f0ff);
  border-color: var(--layer-selected-hover, #3380ff);
}
```

Keep everything else (`.tlDashboard`, `.tlDashboard__grid`, `.tlDashboard__tile*`, overlay, drop-indicator).

- [ ] **Step 2: Rebuild and commit**

Run: `mvn -B install -DskipTests=true -pl com.top_logic.layout.react 2>&1 | tail -10`
Expected: `BUILD SUCCESS`.

```bash
git add com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css \
        com.top_logic.layout.react/src/main/webapp/script/tl-react-controls.js
git commit -m "Ticket #29108: Remove unused dashboard toolbar CSS."
```

---

## Task 8: Rebuild demo app and verify with Playwright

- [ ] **Step 1: Full rebuild**

Run: `mvn -B install -DskipTests=true -pl com.top_logic.demo -am 2>&1 | tee /tmp/dashboard-appbar-build.log | tail -15`
Expected: `BUILD SUCCESS`. Use `dangerouslyDisableSandbox: true` if network-sandbox fails.

- [ ] **Step 2: Restart the demo app**

Use the `tl-app` skill: `restart com.top_logic.demo`.

- [ ] **Step 3: Verify with Playwright**

Navigate to the demo app URL, log in as `root` / `root1234`, click "Dashboard-Layout" in the sidebar. Expected:

- **No toolbar row above the tiles.** The very first row of the content area is tiles.
- The app bar's trailing action area shows a button labelled "Edit Layout" (alongside any existing "Designer" button).
- Clicking "Edit Layout" in the app bar:
  - Enables dragging (`draggable="true"` on tiles, overlays present).
  - Button text switches to "Done".
- Clicking "Done" exits edit mode. Button switches back to "Edit Layout".
- Dragging a tile while in edit mode still reorders and persists across reload (re-run the reorder test from the first dashboard plan).

Take screenshots into `com.top_logic.demo/target/dashboard-appbar-0{1,2,3,4}-*.png`.

- [ ] **Step 4: Final commit if fixes are needed**

If any fixes were needed during verification, commit them:

```bash
git commit -m "Ticket #29108: Fix <specific issue found during verification>."
```

---

## Notes

- **Scope-null fallback in DashboardElement** mirrors `FormElement.contributeFormCommands` (FormElement.java:402-405). A dashboard placed outside an `<app-shell>` (or any `CommandScopeElement`) simply won't show edit commands — same semantics.
- **Backward compatibility of AppBarElement:** when no parent scope is present, its behavior is identical to before. Apps using `<app-bar>` outside an `<app-shell>` keep working.
- **Icons:** if a dedicated "edit layout" icon does not exist in the theme, reuse `Icons.FORM_EDIT` or a generic "pencil" and a "check" icon. Icon choice is cosmetic and can be refined later.
- **Encoding:** all new/modified Java files must remain ISO-8859-1.
- **Commit discipline:** `Ticket #29108: …` prefix; never amend; no Claude attribution.
