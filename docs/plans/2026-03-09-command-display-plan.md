# Command Display Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement structured command display in the React UI with clique grouping, inline/menu display modes, toolbar and button-bar areas, and icon support on buttons.

**Architecture:** Java-side `ToolbarBuilder` groups `ViewCommandModel`s by clique and placement into a `ReactToolbarControl`. The React `TLToolbar` component renders clique groups with separators (inline) and dropdown popups (menu). `TLPanel` embeds toolbars as child controls for toolbar and button-bar areas. `TLButton` gains icon and display mode support.

**Tech Stack:** Java 17, React (via tl-react-bridge), SSE for state updates, existing ReactControl/TLChild infrastructure.

---

### Task 1: CliqueRegistry — Standard Clique Metadata

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/CliqueRegistry.java`

**Step 1: Create CliqueRegistry**

This class provides ordering and display-mode metadata for cliques. It maps clique names to their order and display mode, and supports inserting local cliques.

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of standard and local cliques with ordering and display mode metadata.
 *
 * <p>
 * Standard cliques are registered at class load time. Local cliques (panel-specific) are added via
 * {@link #withLocalClique(String, String, String, String, String)}.
 * </p>
 */
public class CliqueRegistry {

	/** Display mode: commands shown inline with separators between groups. */
	public static final String DISPLAY_INLINE = "inline";

	/** Display mode: commands collapsed into a dropdown menu. */
	public static final String DISPLAY_MENU = "menu";

	private static final List<CliqueInfo> STANDARD_CLIQUES;

	static {
		List<CliqueInfo> list = new ArrayList<>();
		list.add(new CliqueInfo(CommandCliques.CREATE, 1, DISPLAY_INLINE, null, null));
		list.add(new CliqueInfo(CommandCliques.EDIT, 2, DISPLAY_INLINE, null, null));
		list.add(new CliqueInfo(CommandCliques.DELETE, 3, DISPLAY_INLINE, null, null));
		list.add(new CliqueInfo(CommandCliques.COMMIT, 4, DISPLAY_INLINE, null, null));
		list.add(new CliqueInfo(CommandCliques.NAVIGATE, 5, DISPLAY_INLINE, null, null));
		list.add(new CliqueInfo(CommandCliques.VIEW, 6, DISPLAY_MENU, "View", null));
		list.add(new CliqueInfo(CommandCliques.EXPORT, 7, DISPLAY_MENU, "Export", null));
		list.add(new CliqueInfo(CommandCliques.MORE, 8, DISPLAY_MENU, "More", null));
		STANDARD_CLIQUES = Collections.unmodifiableList(list);
	}

	private final Map<String, CliqueInfo> _cliqueMap;

	private final List<CliqueInfo> _orderedCliques;

	/**
	 * Creates a registry with only standard cliques.
	 */
	public CliqueRegistry() {
		_cliqueMap = new LinkedHashMap<>();
		_orderedCliques = new ArrayList<>(STANDARD_CLIQUES);
		for (CliqueInfo info : STANDARD_CLIQUES) {
			_cliqueMap.put(info.name(), info);
		}
	}

	/**
	 * Creates a new registry with a local clique inserted relative to an existing clique.
	 *
	 * @param name
	 *        The local clique name.
	 * @param afterClique
	 *        Insert after this clique (may be {@code null}).
	 * @param beforeClique
	 *        Insert before this clique (may be {@code null}). {@code afterClique} takes precedence.
	 * @param display
	 *        Display mode ({@link #DISPLAY_INLINE} or {@link #DISPLAY_MENU}).
	 * @param label
	 *        Menu trigger label (only for menu display, may be {@code null}).
	 * @return A new registry with the local clique inserted.
	 */
	public CliqueRegistry withLocalClique(String name, String afterClique, String beforeClique,
			String display, String label) {
		CliqueRegistry copy = new CliqueRegistry();
		copy._orderedCliques.clear();
		copy._orderedCliques.addAll(_orderedCliques);
		copy._cliqueMap.clear();
		copy._cliqueMap.putAll(_cliqueMap);

		int insertIndex = copy._orderedCliques.size(); // Default: append
		if (afterClique != null) {
			for (int i = 0; i < copy._orderedCliques.size(); i++) {
				if (copy._orderedCliques.get(i).name().equals(afterClique)) {
					insertIndex = i + 1;
					break;
				}
			}
		} else if (beforeClique != null) {
			for (int i = 0; i < copy._orderedCliques.size(); i++) {
				if (copy._orderedCliques.get(i).name().equals(beforeClique)) {
					insertIndex = i;
					break;
				}
			}
		}

		// Compute order value between neighbors.
		int order;
		if (insertIndex > 0 && insertIndex < copy._orderedCliques.size()) {
			int prev = copy._orderedCliques.get(insertIndex - 1).order();
			int next = copy._orderedCliques.get(insertIndex).order();
			order = prev + (next - prev) / 2;
		} else if (insertIndex == 0) {
			order = 0;
		} else {
			order = copy._orderedCliques.get(copy._orderedCliques.size() - 1).order() + 1;
		}

		CliqueInfo info = new CliqueInfo(name, order, display != null ? display : DISPLAY_INLINE, label, null);
		copy._orderedCliques.add(insertIndex, info);
		copy._cliqueMap.put(name, info);
		return copy;
	}

	/**
	 * Returns the {@link CliqueInfo} for the given clique name, or a default inline clique.
	 */
	public CliqueInfo getClique(String name) {
		if (name == null) {
			return STANDARD_CLIQUES.get(0); // Default to first (create).
		}
		CliqueInfo info = _cliqueMap.get(name);
		if (info != null) {
			return info;
		}
		// Unknown clique: inline, appended at end.
		return new CliqueInfo(name, Integer.MAX_VALUE, DISPLAY_INLINE, null, null);
	}

	/**
	 * Returns all registered cliques in order.
	 */
	public List<CliqueInfo> getOrderedCliques() {
		return Collections.unmodifiableList(_orderedCliques);
	}

	/**
	 * Metadata for a single clique.
	 *
	 * @param name
	 *        The clique name.
	 * @param order
	 *        Sort order (lower = earlier).
	 * @param display
	 *        Display mode: {@link CliqueRegistry#DISPLAY_INLINE} or
	 *        {@link CliqueRegistry#DISPLAY_MENU}.
	 * @param label
	 *        Menu trigger label (only for menu display, may be {@code null}).
	 * @param icon
	 *        Menu trigger icon (only for menu display, may be {@code null}).
	 */
	public record CliqueInfo(String name, int order, String display, String label, String icon) {
		// Record.
	}
}
```

**Step 2: Commit**

```
Ticket #29108: Add CliqueRegistry for standard and local clique metadata.
```

---

### Task 2: ReactToolbarControl — Java-Side Toolbar Control

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactToolbarControl.java`

**Step 1: Create ReactToolbarControl**

A `ReactControl` that maps to the `TLToolbar` React component. It holds clique groups, each containing child controls.

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that renders a toolbar with clique-grouped child controls.
 *
 * <p>
 * The React component {@code TLToolbar} receives:
 * </p>
 * <ul>
 * <li>{@code groups} - ordered list of clique groups, each with display mode and child
 * controls</li>
 * </ul>
 */
public class ReactToolbarControl extends ReactControl {

	private static final String REACT_MODULE = "TLToolbar";

	private static final String GROUPS = "groups";

	private final List<ReactControl> _allChildren = new ArrayList<>();

	/**
	 * Creates a new empty {@link ReactToolbarControl}.
	 */
	public ReactToolbarControl() {
		super(null, REACT_MODULE);
		getReactState().put(GROUPS, new ArrayList<>());
	}

	/**
	 * Adds a clique group to the toolbar.
	 *
	 * @param name
	 *        The clique name.
	 * @param display
	 *        Display mode: "inline" or "menu".
	 * @param label
	 *        Menu trigger label (only for "menu" display, may be {@code null}).
	 * @param icon
	 *        Menu trigger icon (only for "menu" display, may be {@code null}).
	 * @param items
	 *        The child controls in this group.
	 */
	public void addGroup(String name, String display, String label, String icon,
			List<ReactControl> items) {
		Map<String, Object> group = new HashMap<>();
		group.put("name", name);
		group.put("display", display);
		if (label != null) {
			group.put("label", label);
		}
		if (icon != null) {
			group.put("icon", icon);
		}
		group.put("items", new ArrayList<>(items));
		groupList().add(group);
		_allChildren.addAll(items);
	}

	/**
	 * Whether this toolbar has any groups with items.
	 */
	public boolean isEmpty() {
		return _allChildren.isEmpty();
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl child : _allChildren) {
			child.cleanupTree();
		}
	}

	@SuppressWarnings("unchecked")
	private List<Object> groupList() {
		return (List<Object>) getReactState().get(GROUPS);
	}
}
```

**Step 2: Commit**

```
Ticket #29108: Add ReactToolbarControl for structured toolbar rendering.
```

---

### Task 3: TLToolbar React Component

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLToolbar.tsx`
- Modify: `com.top_logic.layout.react/react-src/controls-entry.ts` (add registration)

**Step 1: Create TLToolbar.tsx**

```tsx
import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useRef, useState, useEffect } = React;

interface CliqueGroup {
  name: string;
  display: 'inline' | 'menu';
  label?: string;
  icon?: string;
  items: unknown[];
  subGroups?: CliqueGroup[];
}

/**
 * Renders a clique group's items inline (side by side).
 */
const InlineGroup: React.FC<{ group: CliqueGroup }> = ({ group }) => {
  const visibleItems = group.items.filter(item => item != null);
  if (visibleItems.length === 0) return null;

  return (
    <div className="tlToolbar__group tlToolbar__group--inline">
      {visibleItems.map((item, i) => (
        <span key={i} className="tlToolbar__item">
          <TLChild control={item} />
        </span>
      ))}
    </div>
  );
};

/**
 * Renders a clique group as a dropdown menu.
 */
const MenuGroup: React.FC<{ group: CliqueGroup }> = ({ group }) => {
  const [open, setOpen] = useState(false);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);

  const visibleItems = group.items.filter(item => item != null);
  if (visibleItems.length === 0) return null;

  // Single item: render directly without dropdown.
  if (visibleItems.length === 1 && !group.subGroups?.length) {
    return (
      <div className="tlToolbar__group tlToolbar__group--inline">
        <span className="tlToolbar__item">
          <TLChild control={visibleItems[0]} />
        </span>
      </div>
    );
  }

  const handleToggle = useCallback(() => {
    setOpen(prev => !prev);
  }, []);

  // Close on outside click.
  useEffect(() => {
    if (!open) return;
    const handleMouseDown = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node) &&
          triggerRef.current && !triggerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleMouseDown);
    return () => document.removeEventListener('mousedown', handleMouseDown);
  }, [open]);

  // Close on Escape.
  useEffect(() => {
    if (!open) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false);
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [open]);

  return (
    <div className="tlToolbar__group tlToolbar__group--menu">
      <button
        ref={triggerRef}
        type="button"
        className="tlToolbar__menuTrigger"
        onClick={handleToggle}
        aria-expanded={open}
        aria-haspopup="true"
      >
        {group.icon && <i className={group.icon} aria-hidden="true" />}
        <span>{group.label ?? group.name}</span>
        <svg className="tlToolbar__chevron" viewBox="0 0 24 24" aria-hidden="true">
          <polyline points="6,9 12,15 18,9" />
        </svg>
      </button>
      {open && (
        <div ref={menuRef} className="tlToolbar__dropdown" role="menu">
          {visibleItems.map((item, i) => (
            <div key={i} className="tlToolbar__dropdownItem" role="menuitem">
              <TLChild control={item} />
            </div>
          ))}
          {group.subGroups?.map((sub, si) => (
            <React.Fragment key={`sub-${si}`}>
              <hr className="tlToolbar__dropdownSeparator" />
              {sub.items.map((item, i) => (
                <div key={i} className="tlToolbar__dropdownItem" role="menuitem">
                  <TLChild control={item} />
                </div>
              ))}
            </React.Fragment>
          ))}
        </div>
      )}
    </div>
  );
};

/**
 * A toolbar that renders clique groups with separators and dropdown menus.
 *
 * State:
 * - groups: CliqueGroup[]
 */
const TLToolbar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const groups = (state.groups as CliqueGroup[]) ?? [];

  // Filter out empty groups.
  const visibleGroups = groups.filter(g => g.items.some(item => item != null));
  if (visibleGroups.length === 0) return null;

  return (
    <div id={controlId} className="tlToolbar" role="toolbar">
      {visibleGroups.map((group, i) => (
        <React.Fragment key={group.name}>
          {i > 0 && <span className="tlToolbar__separator" aria-hidden="true" />}
          {group.display === 'menu'
            ? <MenuGroup group={group} />
            : <InlineGroup group={group} />
          }
        </React.Fragment>
      ))}
    </div>
  );
};

export default TLToolbar;
```

**Step 2: Register TLToolbar in controls-entry.ts**

Add these lines to `com.top_logic.layout.react/react-src/controls-entry.ts`:

```typescript
import TLToolbar from './controls/TLToolbar';
// ... (at registration section)
register('TLToolbar', TLToolbar);
```

**Step 3: Commit**

```
Ticket #29108: Add TLToolbar React component with clique grouping.
```

---

### Task 4: Enhance TLButton with Icon and Display Mode

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLButton.tsx`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/button/ReactButtonControl.java`

**Step 1: Update TLButton.tsx**

Replace the existing TLButton with icon and display mode support:

```tsx
import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * Props accepted when TLButton is used as a sub-component inside a composite control.
 */
export interface TLButtonProps {
  /** The command name to send on click.  Defaults to "click". */
  command?: string;
  /** The button label.  Defaults to state.label. */
  label?: string;
  /** Icon CSS class.  Defaults to state.icon. */
  icon?: string;
  /** Whether the button is disabled.  Defaults to state.disabled. */
  disabled?: boolean;
  /** Display mode.  Defaults to state.displayMode or "label-only". */
  displayMode?: 'icon-only' | 'icon-label' | 'label-only';
}

/**
 * A button rendered via React that sends a command to the server.
 *
 * Supports icon-only, icon+label, and label-only display modes.
 */
const TLButton: React.FC<TLCellProps & TLButtonProps> = ({ controlId, command, label, icon, disabled, displayMode }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const resolvedCommand = command ?? 'click';
  const resolvedLabel = label ?? (state.label as string);
  const resolvedIcon = icon ?? (state.icon as string | undefined);
  const resolvedDisabled = disabled ?? state.disabled === true;
  const resolvedMode = displayMode ?? (state.displayMode as string | undefined) ?? 'label-only';

  const handleClick = useCallback(() => {
    sendCommand(resolvedCommand);
  }, [sendCommand, resolvedCommand]);

  const showIcon = resolvedIcon && (resolvedMode === 'icon-only' || resolvedMode === 'icon-label');
  const showLabel = resolvedMode === 'label-only' || resolvedMode === 'icon-label';

  return (
    <button
      type="button"
      id={controlId}
      onClick={handleClick}
      disabled={resolvedDisabled}
      className={'tlReactButton' + (resolvedMode === 'icon-only' ? ' tlReactButton--iconOnly' : '')}
      aria-label={resolvedMode === 'icon-only' ? resolvedLabel : undefined}
      title={resolvedMode === 'icon-only' ? resolvedLabel : undefined}
    >
      {showIcon && <i className={'tlReactButton__icon ' + resolvedIcon} aria-hidden="true" />}
      {showLabel && <span className="tlReactButton__label">{resolvedLabel}</span>}
    </button>
  );
};

export default TLButton;
```

**Step 2: Update ReactButtonControl.java**

Add `setIcon()` and `setDisplayMode()` methods:

```java
// Add these constants after DISABLED:
/** State key for the button icon CSS class. */
private static final String ICON = "icon";

/** State key for the button display mode. */
private static final String DISPLAY_MODE = "displayMode";

/**
 * Sets the icon CSS class.
 *
 * @param iconCssClass
 *        The CSS class for the icon (e.g., "bi-plus", "bi-pencil"), or {@code null} to clear.
 */
public void setIcon(String iconCssClass) {
    putState(ICON, iconCssClass);
}

/**
 * Sets the display mode.
 *
 * @param displayMode
 *        One of "icon-only", "icon-label", or "label-only".
 */
public void setDisplayMode(String displayMode) {
    putState(DISPLAY_MODE, displayMode);
}
```

**Step 3: Commit**

```
Ticket #29108: Enhance TLButton with icon and display mode support.
```

---

### Task 5: ToolbarBuilder — Groups Commands into Toolbar

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/ToolbarBuilder.java`

**Step 1: Create ToolbarBuilder**

This builds a `ReactToolbarControl` from a `CommandScope`, filtering by placement and grouping by clique.

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.view.command.CliqueRegistry.CliqueInfo;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Builds a {@link ReactToolbarControl} from a {@link CommandScope} for a given placement.
 *
 * <p>
 * Commands are filtered by placement, grouped by clique, and ordered according to the
 * {@link CliqueRegistry}. Each group becomes a toolbar group with the clique's display mode.
 * </p>
 */
public class ToolbarBuilder {

	/**
	 * Builds a toolbar for the given placement from the command scope.
	 *
	 * @param scope
	 *        The command scope containing explicit and implicit commands.
	 * @param placement
	 *        The target placement to filter commands for.
	 * @param registry
	 *        The clique registry (with any local cliques applied).
	 * @return A toolbar control, or {@code null} if no commands match the placement.
	 */
	public static ReactToolbarControl build(CommandScope scope, CommandPlacement placement,
			CliqueRegistry registry) {
		List<ViewCommandModel> allCommands = scope.getAllCommands();

		// Filter by placement.
		List<ViewCommandModel> filtered = new ArrayList<>();
		for (ViewCommandModel model : allCommands) {
			if (model.getPlacement() == placement) {
				filtered.add(model);
			}
		}

		if (filtered.isEmpty()) {
			return null;
		}

		// Group by clique, preserving declaration order within each group.
		Map<String, List<ViewCommandModel>> grouped = new LinkedHashMap<>();
		for (ViewCommandModel model : filtered) {
			String clique = model.getClique();
			if (clique == null) {
				clique = CommandCliques.CREATE; // Default clique.
			}
			grouped.computeIfAbsent(clique, k -> new ArrayList<>()).add(model);
		}

		// Sort groups by clique order.
		List<Map.Entry<String, List<ViewCommandModel>>> sortedGroups = new ArrayList<>(grouped.entrySet());
		sortedGroups.sort((a, b) -> {
			CliqueInfo infoA = registry.getClique(a.getKey());
			CliqueInfo infoB = registry.getClique(b.getKey());
			return Integer.compare(infoA.order(), infoB.order());
		});

		// Build toolbar control.
		ReactToolbarControl toolbar = new ReactToolbarControl();

		for (Map.Entry<String, List<ViewCommandModel>> entry : sortedGroups) {
			String cliqueName = entry.getKey();
			List<ViewCommandModel> models = entry.getValue();
			CliqueInfo info = registry.getClique(cliqueName);

			List<ReactControl> controls = new ArrayList<>();
			for (ViewCommandModel model : models) {
				ReactButtonControl button = createButton(model);
				controls.add(button);
			}

			toolbar.addGroup(cliqueName, info.display(), info.label(), info.icon(), controls);
		}

		return toolbar;
	}

	private static ReactButtonControl createButton(ViewCommandModel model) {
		String label = resolveLabel(model.getLabel());
		ReactButtonControl button = new ReactButtonControl(label,
			ctx -> model.executeCommand(ctx));

		// Set icon if configured.
		ThemeImage image = model.getImage();
		if (image != null) {
			button.setIcon(resolveIcon(image));
			button.setDisplayMode("icon-label");
		}

		// Set disabled state.
		button.setDisabled(!model.getExecutableState().isExecutable());

		// Wire state change listener.
		model.setStateChangeListener(() -> {
			button.setDisabled(!model.getExecutableState().isExecutable());
		});

		return button;
	}

	private static String resolveLabel(ResKey label) {
		if (label == null) {
			return "";
		}
		return ResourcesModule.getInstance()
			.getBundle(Locale.getDefault())
			.getString(label);
	}

	private static String resolveIcon(ThemeImage image) {
		// ThemeImage.toEncodedForm() gives the CSS class or icon reference.
		return image.toEncodedForm();
	}
}
```

**Step 2: Commit**

```
Ticket #29108: Add ToolbarBuilder to group commands by clique into toolbar.
```

---

### Task 6: Modify ReactPanelControl for Structured Toolbar

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactPanelControl.java`

**Step 1: Replace flat toolbar buttons with structured toolbar/button-bar**

Replace `_toolbarButtons`, `addToolbarButton()`, and the `TOOLBAR_BUTTONS` state key with `TOOLBAR` and `BUTTON_BAR` child controls. Keep the existing minimize/maximize/popOut functionality unchanged.

Changes:
- Remove field `_toolbarButtons` and method `addToolbarButton()`
- Remove constant `TOOLBAR_BUTTONS`
- Add constants `TOOLBAR` and `BUTTON_BAR`
- Add field `_toolbar` (`ReactToolbarControl`, may be null) and `_buttonBar` (same)
- Update constructor to accept toolbar and button-bar
- Update `cleanupChildren()` to clean up toolbar and button-bar

New constructor signature:
```java
public ReactPanelControl(String title, ReactControl content,
        ReactToolbarControl toolbar, ReactToolbarControl buttonBar,
        boolean showMinimize, boolean showMaximize, boolean showPopOut)
```

State sent to React:
- `toolbar`: `ChildDescriptor` (or null) — replaces `toolbarButtons`
- `buttonBar`: `ChildDescriptor` (or null) — new

**Step 2: Commit**

```
Ticket #29108: Modify ReactPanelControl for structured toolbar and button-bar.
```

---

### Task 7: Update TLPanel React Component

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLPanel.tsx`

**Step 1: Replace flat toolbar buttons with TLChild toolbar and button-bar**

Replace the `toolbarButtons` rendering with `TLChild` for `state.toolbar` and add a new button-bar area below content.

Key changes:
- Replace `toolbarButtons.map(...)` with `{state.toolbar && <TLChild control={state.toolbar} />}`
- Add button-bar section after content: `{state.buttonBar && <div className="tlPanel__buttonBar"><TLChild control={state.buttonBar} /></div>}`

The panel layout becomes:
```
+-- header: title + [TLChild toolbar] + [min/max/popout buttons] --+
|                                                                    |
|  content (TLChild)                                                 |
|                                                                    |
+-- button-bar: [TLChild buttonBar] -------------------------------+
```

**Step 2: Commit**

```
Ticket #29108: Update TLPanel to use structured toolbar and button-bar.
```

---

### Task 8: Update PanelElement to Use ToolbarBuilder

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/PanelElement.java`

**Step 1: Replace manual button creation with ToolbarBuilder**

Remove the manual loop that creates flat `ReactButtonControl` instances and replace with `ToolbarBuilder.build()` calls. The `PanelElement.Config` already has `getCommands()` — no change needed there.

Key changes to `createControl()`:
1. After creating command models and scope (phases 1-3, unchanged)
2. Create `CliqueRegistry` (standard only for now — local cliques come later)
3. Call `ToolbarBuilder.build(scope, CommandPlacement.TOOLBAR, registry)` for toolbar
4. Call `ToolbarBuilder.build(scope, CommandPlacement.BUTTON_BAR, registry)` for button-bar
5. Pass both to `ReactPanelControl` constructor

Remove imports and code related to the old flat-button approach:
- Remove the `resolveLabel()` method (moved to `ToolbarBuilder`)
- Remove the manual `ReactButtonControl` creation loop in phase 4
- Remove the per-command `setStateChangeListener` wiring (moved to `ToolbarBuilder`)

**Step 2: Commit**

```
Ticket #29108: Update PanelElement to use ToolbarBuilder for command display.
```

---

### Task 9: Build and Test

**Step 1: Build the React bundle**

```bash
cd com.top_logic.layout.react && npm run build
```

**Step 2: Build affected Java modules**

```bash
cd com.top_logic.layout.view && mvn install -DskipTests=true
cd com.top_logic.layout.react && mvn install -DskipTests=true
cd com.top_logic.demo && mvn install -DskipTests=true
```

**Step 3: Fix any compilation errors**

Address any issues found during build.

**Step 4: Commit build fixes if needed**

```
Ticket #29108: Fix build issues in command display implementation.
```

---

### Task 10: Demo View with Commands

**Files:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/dashboard.view.xml`
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/view/command/DemoCommand.java` (add clique/placement defaults)

**Step 1: Add clique/placement to DemoCommand**

Create multiple demo command variants (or add configuration defaults) so the demo shows different cliques:

Create additional demo commands in `com.top_logic.demo/src/main/java/com/top_logic/demo/view/command/`:
- `DemoCreateCommand` (clique: create, placement: toolbar)
- `DemoEditCommand` (clique: edit, placement: toolbar)
- `DemoDeleteCommand` (clique: delete, placement: toolbar)
- `DemoSaveCommand` (clique: commit, placement: button-bar)
- `DemoCancelCommand` (clique: commit, placement: button-bar)
- `DemoExportCommand` (clique: export, placement: toolbar)

Each is minimal — just extends DemoCommand with a Config that sets default clique and placement.

**Step 2: Create a demo view that exercises all features**

Update or create `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/commands-demo.view.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
    <panel title="Command Display Demo" toolbar="true">
        <commands>
            <command class="com.top_logic.demo.view.command.DemoCreateCommand"
                     label="New"/>
            <command class="com.top_logic.demo.view.command.DemoEditCommand"
                     label="Edit"/>
            <command class="com.top_logic.demo.view.command.DemoDeleteCommand"
                     label="Delete"/>
            <command class="com.top_logic.demo.view.command.DemoExportCommand"
                     label="Export Excel"/>
            <command class="com.top_logic.demo.view.command.DemoExportCommand"
                     label="Export PDF"/>
            <command class="com.top_logic.demo.view.command.DemoSaveCommand"
                     label="Save"/>
            <command class="com.top_logic.demo.view.command.DemoCancelCommand"
                     label="Cancel"/>
        </commands>

        <stack direction="column" gap="default">
            <card title="Panel Content" variant="elevated">
                <demo-counter label="Items"/>
            </card>
        </stack>
    </panel>
</view>
```

**Step 3: Register the demo view in navigation**

Add entry in `com.top_logic.demo/src/main/webapp/WEB-INF/views/app.view.xml` sidebar.

**Step 4: Build and verify visually**

```bash
cd com.top_logic.demo && mvn install -DskipTests=true
```

Start the demo app and verify:
- Toolbar shows: `[New] | [Edit] | [Delete] | [Export v]` (with Export as dropdown)
- Button bar shows: `[Save] [Cancel]`
- Clicking commands works (check server log for "Demo command executed")

**Step 5: Commit**

```
Ticket #29108: Add demo view exercising command display features.
```

---

### Task 11: Visual Polish and CSS

**Files:**
- Determine: CSS file location (check existing CSS patterns in the project)

**Step 1: Add CSS for toolbar and button components**

Add styles for:
- `.tlToolbar` — flexbox row, align-items center
- `.tlToolbar__separator` — vertical line between groups
- `.tlToolbar__group--inline` — flexbox row, gap between items
- `.tlToolbar__group--menu` — position relative (for dropdown)
- `.tlToolbar__menuTrigger` — styled button with chevron
- `.tlToolbar__dropdown` — absolutely positioned popup
- `.tlToolbar__dropdownItem` — menu item styling
- `.tlToolbar__dropdownSeparator` — horizontal rule
- `.tlToolbar__chevron` — small SVG chevron
- `.tlReactButton--iconOnly` — square button, no text
- `.tlReactButton__icon` — icon element
- `.tlReactButton__label` — label text
- `.tlPanel__buttonBar` — flex row, justify-content: flex-end, padding, border-top

**Step 2: Commit**

```
Ticket #29108: Add CSS for toolbar and button display.
```
