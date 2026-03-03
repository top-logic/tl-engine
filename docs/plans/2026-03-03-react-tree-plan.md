# React Tree Control Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement a React tree control (`TLTreeView`) for context selection and navigation, supporting single/multi-select, lazy loading with prefetch, drag-and-drop, context menus, and full keyboard navigation.

**Architecture:** Server-side `ReactTreeControl` extends `ReactControl`, flattens the visible tree into a flat node list sent via SSE. Client-side `TLTreeView.tsx` renders nodes with CSS indentation, delegated content controls via `TLChild`, and sends commands for expand/collapse, selection, DnD, and context menus. A new generic `ReactControlProvider` interface decouples node content creation.

**Tech Stack:** Java 17, React (via tl-react-bridge), SSE state distribution, Maven

---

## File Overview

### New Files

| File | Purpose |
|------|---------|
| `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactControlProvider.java` | Generic control provider interface |
| `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tree/ReactTreeControl.java` | Server-side tree control |
| `com.top_logic.layout.react/react-src/controls/TLTreeView.tsx` | Client-side React tree component |
| `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactTreeComponent.java` | Demo component |

### Modified Files

| File | Change |
|------|--------|
| `com.top_logic.layout.react/react-src/controls-entry.ts` | Register TLTreeView |
| `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` | Tree styles |

---

### Task 1: ReactControlProvider Interface

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactControlProvider.java`

**Step 1: Create the interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

/**
 * Generic provider that creates {@link ReactControl}s for model objects.
 *
 * <p>
 * Used by composite controls (e.g. tree) to delegate content rendering. Given a model object,
 * creates an appropriate {@link ReactControl} to display it.
 * </p>
 */
@FunctionalInterface
public interface ReactControlProvider {

	/**
	 * Creates a {@link ReactControl} for the given model object.
	 *
	 * @param model
	 *        The application model object.
	 * @return A {@link ReactControl} to render the object. Must not be {@code null}.
	 */
	ReactControl createControl(Object model);
}
```

**Step 2: Build to verify compilation**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

**Step 3: Commit**

```
Ticket #29109: Add generic ReactControlProvider interface.
```

---

### Task 2: ReactTreeControl Server-Side Skeleton

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tree/ReactTreeControl.java`

**Step 1: Create the control class with state building and expand/collapse commands**

The control takes a `TreeUIModel`, a `SelectionModel`, and a `ReactControlProvider`. It flattens the visible tree into a flat node list and sends it as state. Commands handle expand/collapse with lazy loading and grandchild prefetch.

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.ReactControlProvider;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Server-side React control that renders a tree with lazy-loaded children.
 *
 * <p>
 * The tree is flattened into a list of visible nodes, each annotated with its depth. Node content
 * is delegated to child {@link ReactControl}s created by a {@link ReactControlProvider}. Expansion,
 * collapse, and selection are handled server-side via commands.
 * </p>
 */
public class ReactTreeControl extends ReactControl {

	// -- State keys --

	private static final String NODES = "nodes";

	private static final String SELECTION_MODE = "selectionMode";

	private static final String DRAG_ENABLED = "dragEnabled";

	private static final String DROP_ENABLED = "dropEnabled";

	// -- Commands --

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ExpandCommand(),
		new CollapseCommand(),
		new SelectCommand());

	// -- Fields --

	private final TreeUIModel<Object> _treeModel;

	private final SelectionModel _selectionModel;

	private final ReactControlProvider _contentProvider;

	private String _selectionMode = "single";

	private boolean _dragEnabled;

	private boolean _dropEnabled;

	/** Index into the flat visible node list of the last anchor-setting click, or -1. */
	private int _selectionAnchor = -1;

	/** Whether the last anchor-setting action was an add or remove. */
	private boolean _anchorAdded = true;

	/** Cache of content controls for visible nodes. Keyed by node object. */
	private final Map<Object, ReactControl> _nodeControlCache = new LinkedHashMap<>();

	/**
	 * Creates a new {@link ReactTreeControl}.
	 *
	 * @param treeModel
	 *        The tree model providing structure and expansion state.
	 * @param selectionModel
	 *        The selection model.
	 * @param contentProvider
	 *        Provider for creating node content controls.
	 */
	@SuppressWarnings("unchecked")
	public ReactTreeControl(TreeUIModel<?> treeModel, SelectionModel selectionModel,
			ReactControlProvider contentProvider) {
		super(null, "TLTreeView", COMMANDS);
		_treeModel = (TreeUIModel<Object>) treeModel;
		_selectionModel = selectionModel;
		_contentProvider = contentProvider;

		putState(SELECTION_MODE, _selectionMode);
		putState(DRAG_ENABLED, Boolean.FALSE);
		putState(DROP_ENABLED, Boolean.FALSE);
		buildFullState();
	}

	/**
	 * Sets the selection mode.
	 *
	 * @param mode
	 *        One of {@code "single"}, {@code "multi"}.
	 */
	public void setSelectionMode(String mode) {
		_selectionMode = mode;
		putState(SELECTION_MODE, mode);
	}

	/**
	 * Enables or disables drag from tree nodes.
	 */
	public void setDragEnabled(boolean enabled) {
		_dragEnabled = enabled;
		putState(DRAG_ENABLED, Boolean.valueOf(enabled));
	}

	/**
	 * Enables or disables drop onto tree nodes.
	 */
	public void setDropEnabled(boolean enabled) {
		_dropEnabled = enabled;
		putState(DROP_ENABLED, Boolean.valueOf(enabled));
	}

	// -- State building --

	private void buildFullState() {
		cleanupNodeControls();
		List<Map<String, Object>> nodeStates = new ArrayList<>();
		Object root = _treeModel.getRoot();
		if (_treeModel.isRootVisible()) {
			addNodeState(nodeStates, root, 0);
		}
		if (!_treeModel.isRootVisible() || _treeModel.isExpanded(root)) {
			addChildStates(nodeStates, root, _treeModel.isRootVisible() ? 1 : 0);
		}
		putState(NODES, nodeStates);
	}

	private void addChildStates(List<Map<String, Object>> nodeStates, Object parent, int depth) {
		for (Object child : _treeModel.getChildren(parent)) {
			addNodeState(nodeStates, child, depth);
			if (_treeModel.isExpanded(child)) {
				addChildStates(nodeStates, child, depth + 1);
			}
		}
	}

	private void addNodeState(List<Map<String, Object>> nodeStates, Object node, int depth) {
		boolean hasChildren = !_treeModel.isLeaf(node);
		boolean expanded = hasChildren && _treeModel.isExpanded(node);

		ReactControl contentControl = getOrCreateNodeControl(node);

		Map<String, Object> nodeState = new LinkedHashMap<>();
		nodeState.put("id", getNodeId(node));
		nodeState.put("depth", Integer.valueOf(depth));
		nodeState.put("expandable", Boolean.valueOf(hasChildren));
		nodeState.put("expanded", Boolean.valueOf(expanded));
		nodeState.put("leaf", Boolean.valueOf(_treeModel.isLeaf(node)));
		nodeState.put("loading", Boolean.FALSE);
		nodeState.put("selected", Boolean.valueOf(_selectionModel.isSelected(node)));
		nodeState.put("content", contentControl);

		nodeStates.add(nodeState);
	}

	private ReactControl getOrCreateNodeControl(Object node) {
		ReactControl control = _nodeControlCache.get(node);
		if (control == null) {
			control = _contentProvider.createControl(node);
			_nodeControlCache.put(node, control);
			registerChildControl(control);
		}
		return control;
	}

	private String getNodeId(Object node) {
		return String.valueOf(System.identityHashCode(node));
	}

	private Object findNodeById(String nodeId) {
		for (Object node : _nodeControlCache.keySet()) {
			if (getNodeId(node).equals(nodeId)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Returns the index of the given node in the current flat visible node list.
	 */
	private int findNodeIndex(Object node) {
		int index = 0;
		Object root = _treeModel.getRoot();
		if (_treeModel.isRootVisible()) {
			if (root == node) {
				return 0;
			}
			index++;
		}
		if (!_treeModel.isRootVisible() || _treeModel.isExpanded(root)) {
			int result = findNodeIndexRecursive(root, node, index);
			if (result >= 0) {
				return result;
			}
		}
		return -1;
	}

	private int findNodeIndexRecursive(Object parent, Object target, int currentIndex) {
		for (Object child : _treeModel.getChildren(parent)) {
			if (child == target) {
				return currentIndex;
			}
			currentIndex++;
			if (_treeModel.isExpanded(child)) {
				int result = findNodeIndexRecursive(child, target, currentIndex);
				if (result >= 0) {
					return result;
				}
				currentIndex = result == -1 ? currentIndex + countVisibleDescendants(child) : result;
			}
		}
		return -1;
	}

	private int countVisibleDescendants(Object node) {
		int count = 0;
		for (Object child : _treeModel.getChildren(node)) {
			count++;
			if (_treeModel.isExpanded(child)) {
				count += countVisibleDescendants(child);
			}
		}
		return count;
	}

	/**
	 * Collects the flat list of visible nodes in order.
	 */
	private List<Object> collectVisibleNodes() {
		List<Object> result = new ArrayList<>();
		Object root = _treeModel.getRoot();
		if (_treeModel.isRootVisible()) {
			result.add(root);
		}
		if (!_treeModel.isRootVisible() || _treeModel.isExpanded(root)) {
			collectVisibleNodesRecursive(root, result);
		}
		return result;
	}

	private void collectVisibleNodesRecursive(Object parent, List<Object> result) {
		for (Object child : _treeModel.getChildren(parent)) {
			result.add(child);
			if (_treeModel.isExpanded(child)) {
				collectVisibleNodesRecursive(child, result);
			}
		}
	}

	private void cleanupNodeControls() {
		for (ReactControl control : _nodeControlCache.values()) {
			unregisterChildControl(control);
			control.cleanupTree();
		}
		_nodeControlCache.clear();
	}

	@Override
	protected void cleanupChildren() {
		cleanupNodeControls();
	}

	// -- Commands --

	/**
	 * Expands a tree node, loading children and prefetching grandchildren.
	 */
	static class ExpandCommand extends ControlCommand {

		private static final String COMMAND_NAME = "expand";

		ExpandCommand() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactTreeControl tree = (ReactTreeControl) control;
			String nodeId = (String) arguments.get("nodeId");
			Object node = tree.findNodeById(nodeId);
			if (node != null && !tree._treeModel.isLeaf(node) && !tree._treeModel.isExpanded(node)) {
				tree._treeModel.setExpanded(node, true);

				// Prefetch grandchildren: trigger getChildren on each child.
				for (Object child : tree._treeModel.getChildren(node)) {
					if (!tree._treeModel.isLeaf(child)) {
						// Access children to trigger lazy loading.
						tree._treeModel.getChildren(child);
					}
				}

				tree.buildFullState();
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return com.top_logic.layout.I18NConstants.EXPAND;
		}
	}

	/**
	 * Collapses a tree node, removing its children from the visible list.
	 */
	static class CollapseCommand extends ControlCommand {

		private static final String COMMAND_NAME = "collapse";

		CollapseCommand() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactTreeControl tree = (ReactTreeControl) control;
			String nodeId = (String) arguments.get("nodeId");
			Object node = tree.findNodeById(nodeId);
			if (node != null && tree._treeModel.isExpanded(node)) {
				tree._treeModel.setExpanded(node, false);
				tree.buildFullState();
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return com.top_logic.layout.I18NConstants.COLLAPSE;
		}
	}

	/**
	 * Selects a tree node. Supports single, toggle (Ctrl), and range (Shift) selection.
	 */
	static class SelectCommand extends ControlCommand {

		private static final String COMMAND_NAME = "select";

		SelectCommand() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactTreeControl tree = (ReactTreeControl) control;
			String nodeId = (String) arguments.get("nodeId");
			boolean ctrlKey = Boolean.TRUE.equals(arguments.get("ctrlKey"));
			boolean shiftKey = Boolean.TRUE.equals(arguments.get("shiftKey"));

			Object node = tree.findNodeById(nodeId);
			if (node == null || !tree._selectionModel.isSelectable(node)) {
				return HandlerResult.DEFAULT_RESULT;
			}

			if ("multi".equals(tree._selectionMode)) {
				if (shiftKey && tree._selectionAnchor >= 0) {
					// Range selection.
					List<Object> visibleNodes = tree.collectVisibleNodes();
					int clickedIndex = visibleNodes.indexOf(node);
					if (clickedIndex >= 0) {
						int from = Math.min(tree._selectionAnchor, clickedIndex);
						int to = Math.max(tree._selectionAnchor, clickedIndex);
						for (int i = from; i <= to; i++) {
							Object rangeNode = visibleNodes.get(i);
							if (tree._selectionModel.isSelectable(rangeNode)) {
								tree._selectionModel.setSelected(rangeNode, tree._anchorAdded);
							}
						}
					}
				} else if (ctrlKey) {
					// Toggle selection.
					boolean wasSelected = tree._selectionModel.isSelected(node);
					tree._selectionModel.setSelected(node, !wasSelected);
					tree._anchorAdded = !wasSelected;
					List<Object> visibleNodes = tree.collectVisibleNodes();
					tree._selectionAnchor = visibleNodes.indexOf(node);
				} else {
					// Single click in multi mode: replace selection.
					tree._selectionModel.clear();
					tree._selectionModel.setSelected(node, true);
					tree._anchorAdded = true;
					List<Object> visibleNodes = tree.collectVisibleNodes();
					tree._selectionAnchor = visibleNodes.indexOf(node);
				}
			} else {
				// Single select mode.
				tree._selectionModel.clear();
				tree._selectionModel.setSelected(node, true);
			}

			tree.buildFullState();
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return com.top_logic.layout.I18NConstants.SELECT;
		}
	}
}
```

**Note on I18NConstants:** The command `getI18NKey()` methods reference `com.top_logic.layout.I18NConstants`. Check if `EXPAND`, `COLLAPSE`, `SELECT` keys exist there. If not, create tree-specific I18N constants in the tree package. The exact keys will be resolved during implementation — use existing keys where possible, create new ones only if needed.

**Step 2: Build to verify compilation**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS (resolve any missing I18N key references)

**Step 3: Commit**

```
Ticket #29109: Add ReactTreeControl server-side skeleton with expand/collapse/select.
```

---

### Task 3: TLTreeView Client-Side Component

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLTreeView.tsx`
- Modify: `com.top_logic.layout.react/react-src/controls-entry.ts`

**Step 1: Create the React component**

```typescript
import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

interface NodeState {
  id: string;
  depth: number;
  expandable: boolean;
  expanded: boolean;
  leaf: boolean;
  loading: boolean;
  selected: boolean;
  content: unknown;
}

const INDENT_PX = 20;

/**
 * React tree component with lazy-loaded children, selection, and keyboard navigation.
 */
const TLTreeView: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const nodes = (state.nodes as NodeState[]) ?? [];
  const selectionMode = (state.selectionMode as string) ?? 'single';
  const dragEnabled = (state.dragEnabled as boolean) ?? false;
  const dropEnabled = (state.dropEnabled as boolean) ?? false;

  // Focus tracking (client-side only).
  const [focusIndex, setFocusIndex] = React.useState(-1);
  const listRef = React.useRef<HTMLUListElement>(null);

  const handleToggle = React.useCallback((nodeId: string, expanded: boolean) => {
    sendCommand(expanded ? 'collapse' : 'expand', { nodeId });
  }, [sendCommand]);

  const handleSelect = React.useCallback((nodeId: string, e: React.MouseEvent) => {
    sendCommand('select', {
      nodeId,
      ctrlKey: e.ctrlKey || e.metaKey,
      shiftKey: e.shiftKey,
    });
  }, [sendCommand]);

  const handleContextMenu = React.useCallback((nodeId: string, e: React.MouseEvent) => {
    e.preventDefault();
    sendCommand('contextMenu', { nodeId, x: e.clientX, y: e.clientY });
  }, [sendCommand]);

  const handleKeyDown = React.useCallback((e: React.KeyboardEvent) => {
    if (nodes.length === 0) return;

    let newIndex = focusIndex;

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        newIndex = Math.min(focusIndex + 1, nodes.length - 1);
        break;
      case 'ArrowUp':
        e.preventDefault();
        newIndex = Math.max(focusIndex - 1, 0);
        break;
      case 'ArrowRight':
        e.preventDefault();
        if (focusIndex >= 0 && focusIndex < nodes.length) {
          const node = nodes[focusIndex];
          if (node.expandable && !node.expanded) {
            sendCommand('expand', { nodeId: node.id });
            return;
          } else if (node.expanded) {
            // Move to first child.
            newIndex = focusIndex + 1;
          }
        }
        break;
      case 'ArrowLeft':
        e.preventDefault();
        if (focusIndex >= 0 && focusIndex < nodes.length) {
          const node = nodes[focusIndex];
          if (node.expanded) {
            sendCommand('collapse', { nodeId: node.id });
            return;
          } else {
            // Move to parent: find previous node with lower depth.
            const currentDepth = node.depth;
            for (let i = focusIndex - 1; i >= 0; i--) {
              if (nodes[i].depth < currentDepth) {
                newIndex = i;
                break;
              }
            }
          }
        }
        break;
      case 'Enter':
        e.preventDefault();
        if (focusIndex >= 0 && focusIndex < nodes.length) {
          sendCommand('select', {
            nodeId: nodes[focusIndex].id,
            ctrlKey: e.ctrlKey || e.metaKey,
            shiftKey: e.shiftKey,
          });
        }
        return;
      case ' ':
        e.preventDefault();
        if (selectionMode === 'multi' && focusIndex >= 0 && focusIndex < nodes.length) {
          sendCommand('select', {
            nodeId: nodes[focusIndex].id,
            ctrlKey: true,
            shiftKey: false,
          });
        }
        return;
      case 'Home':
        e.preventDefault();
        newIndex = 0;
        break;
      case 'End':
        e.preventDefault();
        newIndex = nodes.length - 1;
        break;
      default:
        return;
    }

    if (newIndex !== focusIndex) {
      setFocusIndex(newIndex);
    }
  }, [focusIndex, nodes, sendCommand, selectionMode]);

  return (
    <ul
      ref={listRef}
      role="tree"
      className="tlTreeView"
      tabIndex={0}
      onKeyDown={handleKeyDown}
    >
      {nodes.map((node, index) => (
        <li
          key={node.id}
          role="treeitem"
          aria-expanded={node.expandable ? node.expanded : undefined}
          aria-selected={node.selected}
          aria-level={node.depth + 1}
          className={[
            'tlTreeView__node',
            node.selected ? 'tlTreeView__node--selected' : '',
            index === focusIndex ? 'tlTreeView__node--focused' : '',
          ].filter(Boolean).join(' ')}
          style={{ paddingLeft: node.depth * INDENT_PX }}
          onClick={(e) => handleSelect(node.id, e)}
          onContextMenu={(e) => handleContextMenu(node.id, e)}
        >
          {node.expandable ? (
            <button
              type="button"
              className="tlTreeView__toggle"
              onClick={(e) => {
                e.stopPropagation();
                handleToggle(node.id, node.expanded);
              }}
              tabIndex={-1}
              aria-label={node.expanded ? 'Collapse' : 'Expand'}
            >
              {node.loading ? (
                <span className="tlTreeView__spinner" />
              ) : (
                <span className={
                  node.expanded ? 'tlTreeView__chevron--down' : 'tlTreeView__chevron--right'
                } />
              )}
            </button>
          ) : (
            <span className="tlTreeView__toggleSpacer" />
          )}
          <span className="tlTreeView__content">
            <TLChild control={node.content} />
          </span>
        </li>
      ))}
    </ul>
  );
};

export default TLTreeView;
```

**Step 2: Register the control in controls-entry.ts**

Add after the existing imports:
```typescript
import TLTreeView from './controls/TLTreeView';
```

Add after the existing registrations:
```typescript
register('TLTreeView', TLTreeView);
```

**Step 3: Build to verify TypeScript compilation**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS (Vite bundles TLTreeView into tl-react-controls.js)

**Step 4: Commit**

```
Ticket #29109: Add TLTreeView React component with keyboard navigation.
```

---

### Task 4: Tree CSS Styles

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

**Step 1: Append tree styles at end of CSS file**

```css
/* -- TLTreeView ----------------------------------------------------------- */

.tlTreeView {
  list-style: none;
  margin: 0;
  padding: 0;
  font-family: var(--font-family);
  font-size: 14px;
  color: var(--text-primary);
  outline: none;
  overflow-y: auto;
  height: 100%;
}

.tlTreeView__node {
  display: flex;
  align-items: center;
  height: 32px;
  padding-right: var(--spacing-03);
  cursor: pointer;
  user-select: none;
  border-left: 3px solid transparent;
}

.tlTreeView__node:hover {
  background: var(--layer-hover);
}

.tlTreeView__node--selected {
  background: var(--layer-selected-01);
  border-left-color: var(--interactive);
}

.tlTreeView__node--selected:hover {
  background: var(--layer-selected-01);
}

.tlTreeView__node--focused {
  outline: 2px solid var(--focus);
  outline-offset: -2px;
}

.tlTreeView__toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  border: none;
  background: none;
  padding: 0;
  cursor: pointer;
  color: var(--text-secondary);
  border-radius: var(--corner-radius);
}

.tlTreeView__toggle:hover {
  background: var(--layer-hover);
  color: var(--text-primary);
}

.tlTreeView__toggleSpacer {
  display: inline-block;
  width: 24px;
  flex-shrink: 0;
}

.tlTreeView__chevron--right::before,
.tlTreeView__chevron--down::before {
  display: inline-block;
  font-size: 12px;
}

.tlTreeView__chevron--right::before {
  content: '\25B6'; /* Right-pointing triangle */
}

.tlTreeView__chevron--down::before {
  content: '\25BC'; /* Down-pointing triangle */
}

.tlTreeView__spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid var(--border-subtle);
  border-top-color: var(--interactive);
  border-radius: 50%;
  animation: tlTreeSpin 0.6s linear infinite;
}

@keyframes tlTreeSpin {
  to { transform: rotate(360deg); }
}

.tlTreeView__content {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* -- Drop indicators -- */

.tlTreeView__node--drop-above {
  box-shadow: inset 0 2px 0 0 var(--interactive);
}

.tlTreeView__node--drop-within {
  outline: 2px solid var(--interactive);
  outline-offset: -2px;
  background: var(--layer-hover);
}

.tlTreeView__node--drop-below {
  box-shadow: inset 0 -2px 0 0 var(--interactive);
}

.tlTreeView__node--no-drop {
  cursor: not-allowed;
}
```

**Step 2: Commit**

```
Ticket #29109: Add TLTreeView CSS styles.
```

---

### Task 5: Context Menu Command

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tree/ReactTreeControl.java`

**Step 1: Add ContextMenuCommand to COMMANDS map and as inner class**

Add to the `createCommandMap(...)` call:
```java
new ContextMenuCommand()
```

Add inner class:
```java
/**
 * Opens a context menu at the given coordinates for a tree node.
 */
static class ContextMenuCommand extends ControlCommand {

    private static final String COMMAND_NAME = "contextMenu";

    ContextMenuCommand() {
        super(COMMAND_NAME);
    }

    @Override
    protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
        ReactTreeControl tree = (ReactTreeControl) control;
        String nodeId = (String) arguments.get("nodeId");
        Object node = tree.findNodeById(nodeId);
        if (node != null && tree._contextMenuProvider != null) {
            int x = ((Number) arguments.get("x")).intValue();
            int y = ((Number) arguments.get("y")).intValue();
            tree._contextMenuProvider.openContextMenu(tree, node, x, y);
        }
        return HandlerResult.DEFAULT_RESULT;
    }

    @Override
    public ResKey getI18NKey() {
        return com.top_logic.layout.I18NConstants.CONTEXT_MENU;
    }
}
```

Add a `ContextMenuProvider` functional interface and field:
```java
/**
 * Provider for opening context menus on tree nodes.
 */
@FunctionalInterface
public interface ContextMenuProvider {
    void openContextMenu(ReactTreeControl tree, Object node, int x, int y);
}

private ContextMenuProvider _contextMenuProvider;

public void setContextMenuProvider(ContextMenuProvider provider) {
    _contextMenuProvider = provider;
}
```

**Step 2: Build**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

**Step 3: Commit**

```
Ticket #29109: Add context menu command to ReactTreeControl.
```

---

### Task 6: Drag-and-Drop Commands

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tree/ReactTreeControl.java`
- Modify: `com.top_logic.layout.react/react-src/controls/TLTreeView.tsx`

**Step 1: Add DnD commands to the server side**

Add to `createCommandMap(...)`:
```java
new DragOverCommand(),
new DropCommand(),
new DragEndCommand()
```

Add fields:
```java
private List<TreeDropTarget> _dropTargets = new ArrayList<>();

/** The node ID currently showing a drop indicator, or null. */
private String _dropIndicatorNodeId;

/** The current drop position indicator. */
private String _dropIndicatorPosition;
```

Add public API:
```java
public void addDropTarget(TreeDropTarget target) {
    _dropTargets.add(target);
}
```

Add inner command classes. `DragOverCommand` evaluates `TreeDropTarget.canDrop()` and sends a state patch with drop indicator info. `DropCommand` calls `TreeDropTarget.handleDrop()` on the first accepting target. `DragEndCommand` clears drop indicators.

The DnD commands construct `TreeDropEvent` using:
- `Position.fromString(position)` — converts `"above"` / `"within"` / `"below"` to enum
- The node resolved from `nodeId`
- `DndData` parsed from the drag data (provided by the framework's `DnD.getDndData()`)

**Step 2: Add DnD event handlers to the React component**

Add drag-and-drop handlers to `TLTreeView.tsx`:
- `onDragStart`: sets drag data, sends `dragStart` command
- `onDragOver`: computes position (top/middle/bottom third of node element), sends `dragOver` command (debounced ~100ms)
- `onDrop`: sends `drop` command with nodeId + position
- `onDragEnd`: sends `dragEnd` command

Add drop indicator CSS classes from state: read `dropIndicatorNodeId` and `dropIndicatorPosition` from state, apply `tlTreeView__node--drop-above/within/below` class to the matching node.

**Step 3: Build**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

**Step 4: Commit**

```
Ticket #29109: Add drag-and-drop support to ReactTreeControl.
```

---

### Task 7: Demo Component

**Files:**
- Create: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactTreeComponent.java`

**Step 1: Create a demo component with a synthetic tree**

Follow the pattern of `DemoReactTableComponent`. Create a `DefaultMutableTLTreeModel` (or similar) with a 3-level synthetic tree:
- Root (hidden)
  - 5 top-level folders ("Folder 1" .. "Folder 5")
    - 5 children each ("Item 1.1" .. "Item 1.5")
      - 3 grandchildren each ("Leaf 1.1.1" .. "Leaf 1.1.3")

Use `ReactTextCellControl` as the content provider (displays node label).

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.control.table.ReactTextCellControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases the React tree control.
 */
public class DemoReactTreeComponent extends LayoutComponent {

    /**
     * Configuration for {@link DemoReactTreeComponent}.
     */
    public interface Config extends LayoutComponent.Config {
        // No additional configuration needed.
    }

    private ReactTreeControl _treeControl;

    /**
     * Creates a new {@link DemoReactTreeComponent}.
     */
    public DemoReactTreeComponent(InstantiationContext context, Config config) throws ConfigurationException {
        super(context, config);
    }

    @Override
    public void writeBody(ServletContext servletContext, HttpServletRequest request,
            HttpServletResponse response, TagWriter out) throws IOException, ServletException {
        DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

        if (_treeControl == null) {
            _treeControl = createDemoTree();
        }

        out.beginTag(HTMLConstants.H2);
        out.writeText("React Tree Demo");
        out.endTag(HTMLConstants.H2);

        out.beginTag(HTMLConstants.PARAGRAPH);
        out.writeText("A tree with 3 levels. Click to select, click arrows to expand/collapse.");
        out.endTag(HTMLConstants.PARAGRAPH);

        out.beginBeginTag(HTMLConstants.DIV);
        out.writeAttribute(HTMLConstants.STYLE_ATTR, "height: 400px; border: 1px solid #ccc;");
        out.endBeginTag();
        _treeControl.write(displayContext, out);
        out.endTag(HTMLConstants.DIV);
    }

    private ReactTreeControl createDemoTree() {
        DefaultMutableTLTreeNode root = new DefaultMutableTLTreeNode(null, "Root");
        for (int i = 1; i <= 5; i++) {
            DefaultMutableTLTreeNode folder = root.createChild("Folder " + i);
            for (int j = 1; j <= 5; j++) {
                DefaultMutableTLTreeNode subfolder = folder.createChild("Item " + i + "." + j);
                for (int k = 1; k <= 3; k++) {
                    subfolder.createChild("Leaf " + i + "." + j + "." + k);
                }
            }
        }

        DefaultMutableTLTreeModel treeModel = new DefaultMutableTLTreeModel(root);
        treeModel.setRootVisible(false);

        SelectionModel selectionModel = new DefaultSingleSelectionModel(/* owner */ null);

        ReactTreeControl tree = new ReactTreeControl(treeModel, selectionModel,
            node -> new ReactTextCellControl(node.toString()));
        return tree;
    }
}
```

**Note:** The exact tree model API (`DefaultMutableTLTreeModel`, `DefaultMutableTLTreeNode`, `DefaultSingleSelectionModel`) must be verified during implementation. The legacy tree control uses these classes — check their constructors and adapt as needed.

**Step 2: Build**

Run: Touch the changed files and build incrementally (do NOT use `mvn clean` on the demo module):
```bash
mvn install -DskipTests=true -pl com.top_logic.layout.react && \
mvn compile -DskipTests=true -pl com.top_logic.demo
```

Expected: BUILD SUCCESS

**Step 3: Commit**

```
Ticket #29109: Add DemoReactTreeComponent.
```

---

### Task 8: Integration Verification

**Step 1: Verify the full build compiles**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

**Step 2: Verify the bundled JS includes TLTreeView**

Run: `grep -l "TLTreeView" com.top_logic.layout.react/target/tl-layout-react-*/script/tl-react-controls.js`
Expected: File found, confirming TLTreeView is bundled.

**Step 3: Manual smoke test (if app is running)**

Navigate to the demo page and verify:
- Tree renders with 5 top-level folders
- Clicking arrows expands/collapses nodes
- Clicking a node selects it (highlight visible)
- Arrow keys navigate, Right expands, Left collapses
- Expanding is smooth (grandchildren are prefetched)

**Step 4: Commit (if any fixes needed)**

```
Ticket #29109: Fix integration issues in React tree control.
```

---

## Summary of Implementation Order

| Task | Description | Depends On |
|------|-------------|------------|
| 1 | ReactControlProvider interface | — |
| 2 | ReactTreeControl server-side (expand/collapse/select) | Task 1 |
| 3 | TLTreeView.tsx client-side + registration | Task 2 |
| 4 | CSS styles | — (parallel with 2-3) |
| 5 | Context menu command | Task 2 |
| 6 | Drag-and-drop (server + client) | Tasks 2, 3 |
| 7 | Demo component | Tasks 2, 3, 4 |
| 8 | Integration verification | All above |
