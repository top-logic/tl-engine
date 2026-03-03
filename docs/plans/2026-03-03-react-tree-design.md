# React Tree Control Design

## Overview

A React-based tree control (`TLTreeView`) for TopLogic that serves as a context selector
and navigation component. Supports both single-select navigation (sidebar tree) and
multi-select picking (dialog/form tree). Follows the same SSE state + AJAX command
architecture as the existing React table.

## Architecture

Two main artifacts:

| Layer      | Artifact               | Role                                                                 |
|------------|------------------------|----------------------------------------------------------------------|
| **Server** | `ReactTreeControl.java`| Manages tree model, expansion, selection, lazy loading. Sends state via SSE. |
| **Client** | `TLTreeView.tsx`       | Renders flat node list with indentation, toggles, delegated content. Sends commands. |

The server flattens the visible tree (expanded subtrees) into an ordered list with a
`depth` field per node. The client renders this as a flat list with CSS indentation.
This keeps the client simple and avoids recursive React component trees.

No virtual scrolling initially — navigation trees are typically a few dozen to a few
hundred visible nodes, well within DOM limits.

## State Model (Server -> Client via SSE)

```typescript
interface TreeState {
  nodes: NodeState[];
  selectionMode: 'single' | 'multi';
  selectedNodeIds: string[];
  dragEnabled: boolean;
  dropEnabled: boolean;
}

interface NodeState {
  id: string;
  depth: number;          // 0 = root, 1 = child, etc.
  expandable: boolean;    // has children (shows toggle)
  expanded: boolean;      // currently expanded
  leaf: boolean;          // true = no toggle shown
  loading: boolean;       // true while children are being fetched
  selected: boolean;      // current selection state
  content: string;        // child control ID (rendered via TLChild)
}
```

**Incremental updates**: When a node is expanded, the server sends a patch inserting
child nodes at the correct position. When collapsed, a patch removes them. The full
tree is never resent.

## Commands (Client -> Server via AJAX)

| Command       | Parameters                                          | Trigger                                    |
|---------------|-----------------------------------------------------|--------------------------------------------|
| `expand`      | `nodeId`                                            | Click toggle or Right arrow on collapsed   |
| `collapse`    | `nodeId`                                            | Click toggle or Left arrow on expanded     |
| `select`      | `nodeId, ctrlKey, shiftKey`                         | Click node, Enter key                      |
| `contextMenu` | `nodeId, x, y`                                      | Right-click on node                        |
| `dragStart`   | `nodeId`                                            | Drag begins                                |
| `dragOver`    | `nodeId, position: 'above' \| 'within' \| 'below'` | Dragging over a node (debounced)           |
| `drop`        | `nodeId, position: 'above' \| 'within' \| 'below'` | Drop on a node                             |
| `dragEnd`     | _(none)_                                            | Drag cancelled or completed                |

### Drag-and-Drop Protocol

Mirrors legacy `TreeDropEvent` semantics:

- Drop `position` computed from cursor Y relative to node element: top third = `above`,
  middle third = `within`, bottom third = `below`.
- Server responds to `dragOver` with a state patch indicating whether drop is allowed
  (for cursor feedback).
- `drop` triggers server-side `TreeDropTarget.handleDrop()` with equivalent `TreeDropEvent`.
- Drag data URL format (`dnd://<scope>|<controlId>|<dataId>`) constructed server-side,
  passed to client as part of drag state.
- Existing `TreeDropTarget` implementations work without modification.

### Context Menu

Server receives click coordinates and node ID, then opens a `TLMenu` control positioned
at those coordinates.

## Client Rendering (TLTreeView.tsx)

```
TLTreeView
 <ul role="tree" tabIndex={0} onKeyDown={...}>
   {nodes.map(node =>
     <li role="treeitem"
         aria-expanded={node.expandable ? node.expanded : undefined}
         aria-selected={node.selected}
         aria-level={node.depth + 1}
         style={{ paddingLeft: node.depth * indentPx }}
         draggable={dragEnabled}
     >
       [Toggle button]  (if node.expandable)
         Chevron-right (collapsed) | Chevron-down (expanded) | Spinner (loading)

       [Checkbox]        (if selectionMode === 'multi')

       [Content]         <TLChild control={node.content} />
     </li>
   )}
 </ul>
```

- **Flat list with CSS indentation** — `paddingLeft: depth * 20px` (configurable).
- **ARIA tree pattern** — `role="tree"` / `role="treeitem"`, `aria-level`,
  `aria-expanded`, `aria-selected` for accessibility.
- **Delegated content** — Each node's visible content is a nested `ReactControl`
  mounted via `TLChild`. The server's `ReactControlProvider` creates the control.
- **Drop indicators** — CSS classes `tl-tree__drop-above`, `tl-tree__drop-within`,
  `tl-tree__drop-below` for visual feedback during drag.

## Keyboard Navigation

| Key       | Action                                                       |
|-----------|--------------------------------------------------------------|
| **Down**  | Move focus to next visible node                              |
| **Up**    | Move focus to previous visible node                          |
| **Right** | Collapsed+expandable -> expand. Expanded -> first child. Leaf -> no-op. |
| **Left**  | Expanded -> collapse. Collapsed/leaf -> move to parent.      |
| **Enter** | Select focused node                                          |
| **Space** | Toggle checkbox (multi-select mode)                          |
| **Home**  | Move focus to first node                                     |
| **End**   | Move focus to last visible node                              |

Focus is tracked client-side (index into visible nodes). Selection is server-side.
Follows WAI-ARIA TreeView pattern.

## Server-Side Architecture

```
ReactTreeControl extends ReactControl
  Model
    TreeUIModel<Object>          — reuses existing tree model
    SelectionModel               — single or multi selection
    Map<Object, ReactControl>    — content control cache per node

  Configuration (ConfigurationItem)
    selectionMode: 'single' | 'multi'
    dragEnabled: boolean
    dropEnabled: boolean
    contentProvider: ReactControlProvider

  State Building
    buildFullState()             — initial: flatten visible tree
    buildExpansionPatch(nodeId)  — patch: insert/remove children

  Commands
    ExpandCommand    — expand node, fetch children, prefetch grandchildren, send patch
    CollapseCommand  — collapse node, send removal patch
    SelectCommand    — update SelectionModel, notify listeners
    ContextMenuCommand — open TLMenu at coordinates
    DragOverCommand  — evaluate TreeDropTargets, send canDrop feedback
    DropCommand      — execute TreeDropTarget.handleDrop()

  Listeners
    TreeModelListener  — react to external model changes
    SelectionListener  — notify parent components
```

### ReactControlProvider (new generic interface)

```java
@FunctionalInterface
public interface ReactControlProvider {
    ReactControl createControl(Object model);
}
```

Generic provider: given a model object, create a `ReactControl`. Used by the tree
(model = tree node object), reusable for future controls. The table retains its own
`ReactCellControlProvider` which needs additional column context.

### Key Design Choices

- **Reuses `TreeUIModel`** — no new tree model. Existing model handles lazy loading,
  expansion state, and tree structure.
- **Prefetch on expand** — when expanding a node, also triggers `getChildren()` on
  each child (loading grandchildren). Next expand is instant.
- **Control cache** — one `ReactControl` per visible node, keyed by model object.
  Controls for collapsed subtrees are removed from cache on collapse.
- **Drag-and-drop reuses `TreeDropTarget`** — existing drop handlers work unchanged.

## Decisions Log

| Aspect             | Decision                                          |
|--------------------|---------------------------------------------------|
| Architecture       | SSE state + AJAX commands (same as table)         |
| State model        | Flat node list with depth, server owns structure  |
| Selection          | Both single and multi, configurable               |
| Data loading       | Lazy with prefetch (grandchildren on expand)      |
| Node rendering     | Delegated controls via TLChild                    |
| Content provider   | New generic `ReactControlProvider` interface      |
| Drag & drop        | Full (source + target), server-side `TreeDropTarget` |
| Context menu       | Via existing `TLMenu`                             |
| Keyboard           | Full WAI-ARIA tree pattern                        |
| Virtualization     | Not initially (navigation trees are small enough) |
